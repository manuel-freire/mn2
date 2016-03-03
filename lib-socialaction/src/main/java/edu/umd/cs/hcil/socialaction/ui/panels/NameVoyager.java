package edu.umd.cs.hcil.socialaction.ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.action.ActionList;
import prefuse.action.ItemAction;
import prefuse.action.RepaintAction;
import prefuse.action.animate.ArrayAnimator;
import prefuse.action.animate.AxisLabelAnimator;
import prefuse.action.animate.ColorAnimator;
import prefuse.action.animate.QualityControlAnimator;
import prefuse.action.assignment.ColorAction;
import prefuse.action.filter.VisibilityFilter;
import prefuse.action.layout.AxisLabelLayout;
import prefuse.action.layout.CollapsedStackLayout;
import prefuse.action.layout.Layout;
import prefuse.action.layout.StackedAreaChart;
import prefuse.activity.SlowInSlowOutPacer;
import prefuse.controls.ControlAdapter;
import prefuse.controls.HoverActionControl;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.expression.AndPredicate;
import prefuse.data.query.ListQueryBinding;
import prefuse.data.query.ObjectRangeModel;
import prefuse.data.query.SearchQueryBinding;
import prefuse.render.AxisRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.render.PolygonRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.PrefuseLib;
import prefuse.util.UpdateListener;
import prefuse.util.ui.JSearchPanel;
import prefuse.util.ui.JToggleGroup;
import prefuse.util.ui.UILib;
import prefuse.visual.DecoratorItem;
import prefuse.visual.VisualItem;
import prefuse.visual.sort.ItemSorter;
import edu.umd.cs.hcil.socialaction.SocialAction;

/**
 * @version 1.0
 * @author Adam Perer, based off of Jeff Heer's NameVoyager Code
 */
public class NameVoyager extends Display implements Constants {

	/** Never change */
	private static final long serialVersionUID = -1415912205642979558L;

	private static final String POLYGON = PolygonRenderer.POLYGON;

	private Display xd;

	// data groups
	private static final String data = "data";
	private static final String labels = "labels";
	private static final String xaxis = "xaxis";
	private static final String yaxis = "yaxis";

	private ListQueryBinding m_genderQ; // dynamic query filter binding
	private SearchQueryBinding m_searchQ;
	SocialAction m_app;

	public NameVoyager(Table t, String[] columns, SocialAction app) {
		super(new Visualization());

		m_app = app;

		// add data to visualization, ensure polygon data is represented
		Schema dataSchema = PrefuseLib.getVisualItemSchema();
		dataSchema.addInterpolatedColumn(POLYGON, float[].class);
		Table vt = m_vis.addTable(data, t, dataSchema);

		// add name labels
		Schema labelSchema = PrefuseLib.getVisualItemSchema();
		labelSchema.setDefault(VisualItem.INTERACTIVE, false);
		labelSchema.setDefault(VisualItem.VISIBLE, false);
		m_vis.addDecorators(labels, data, null, labelSchema);

		// add axis marks and labels
		Schema axisSchema = PrefuseLib.getAxisLabelSchema();
		axisSchema.setDefault(VisualItem.INTERACTIVE, false);
		axisSchema.setInterpolatedDefault(VisualItem.STROKECOLOR, ColorLib.gray(160));
		axisSchema.setInterpolatedDefault(VisualItem.TEXTCOLOR, ColorLib.hex("#666666"));
		m_vis.addTable(xaxis, axisSchema);
		m_vis.addTable(yaxis, axisSchema);

		// set the renderers
		DefaultRendererFactory rf = new DefaultRendererFactory();
		rf.add("INGROUP('data')", new PolygonRenderer(POLY_TYPE_STACK));
		rf.add("INGROUP('labels')", new LabelRenderer("Name"));
		rf.add("INGROUP('xaxis')", new AxisRenderer(LEFT, BOTTOM));
		rf.add("INGROUP('yaxis')", new AxisRenderer(RIGHT, BOTTOM));
		m_vis.setRendererFactory(rf);

		// ------------------------------------------------

		// create dynamic queries for gender and names
		m_genderQ = new ListQueryBinding(vt, "M/F");
		m_searchQ = new SearchQueryBinding(vt, "Name");
		AndPredicate filter = new AndPredicate(m_genderQ.getPredicate(), m_searchQ.getPredicate());

		// action to take upon dynamic query update
		// UpdateListener conveniently implements a number of
		// different listeners, including ExpressionListener
		UpdateListener lstnr = new UpdateListener() {
			public void update(Object src) {
				// before updating, clear any tooltips
				ToolTipManager.sharedInstance().setEnabled(false);
				ToolTipManager.sharedInstance().setEnabled(true);
				// cancel any pending updates to avoid interference
				m_vis.cancel("layout");
				m_vis.cancel("animate");
				// issue after a slight delay in case of interaction
				m_vis.runAfter("layout", 100);
			}
		};
		// changes to the gender and search predicates will percolate up
		// through the encompassing "and" predicate
		filter.addExpressionListener(lstnr);

		// color assignment for name data
		Action color = new NameVoyagerColorFunction(data, columns, m_app);
		m_vis.putAction("color", color);

		// actions to take on mouse over of a data item
		ActionList hover = new ActionList();
		hover.add(color);
		hover.add(new RepaintAction());
		m_vis.putAction("hover", hover);

		// bounds for layout actions
		final Rectangle2D stackBounds = new Rectangle2D.Double(10, 0, 660, 480);
		final Rectangle2D xaxisBounds = new Rectangle2D.Double(0, 0, 660, 15);
		final Rectangle2D yaxisBounds = new Rectangle2D.Double(0, 0, 720, 480);

		// the stacked line chart layout of the name data
		StackedAreaChart stack = new StackedAreaChart(data, POLYGON, columns, 1);
		Layout collapsed = new CollapsedStackLayout(data, POLYGON);
		stack.setLayoutBounds(stackBounds);
		collapsed.setLayoutBounds(stackBounds);

		// x-axis showing the years
		ObjectRangeModel yearModel = new ObjectRangeModel(columns);
		AxisLabelLayout x_axis = new AxisLabelLayout(xaxis, X_AXIS, yearModel, xaxisBounds);
		m_vis.putAction("xaxis", x_axis); // run only on resize

		// y-axis showing name count, uses the range reported by the stack
		AxisLabelLayout y_axis = new AxisLabelLayout(yaxis, Y_AXIS, stack.getRangeModel(), yaxisBounds);

		// action list for recomputing the layout
		ActionList layout = new ActionList();
		layout.add(new VisibilityFilter(data, filter));
		layout.add(collapsed);
		layout.add(y_axis);
		layout.add(stack);

		layout.add(color);
		m_vis.putAction("layout", layout);

		// action list for animating between layouts
		ActionList animate = new ActionList(1400);
		animate.setPacingFunction(new SlowInSlowOutPacer());
		animate.add(new AxisLabelAnimator(yaxis));
		animate.add(new ArrayAnimator(data, POLYGON));
		animate.add(new ColorAnimator(data, VisualItem.FILLCOLOR));
		animate.add(new LabelAction(labels, POLYGON)); // places name labels
		animate.add(new QualityControlAnimator()); // controls anti-aliasing
		animate.add(new RepaintAction());
		m_vis.putAction("animate", animate);
		m_vis.alwaysRunAfter("layout", "animate");

		// ------------------------------------------------

		// initialize the display
		// set to draw all items except the x-axis
		setPredicate("NOT INGROUP('xaxis')");
		setLayout(new BorderLayout());
		setSize(720, 500);
		setItemSorter(new ItemSorter() {
			public int score(VisualItem item) {
				int score = item instanceof DecoratorItem ? (1 << 29) : 0;
				return super.score(item) + score;
			}
		});
		ToolTipManager.sharedInstance().setInitialDelay(400);
		ToolTipManager.sharedInstance().setDismissDelay(60000);
		addControlListener(new TooltipControl());
		addControlListener(new HoverActionControl("hover"));

		// create a separate display to draw x-axis
		xd = new Display(m_vis, "INGROUP('xaxis')");
		xd.setFocusable(false);
		xd.setSize(720, 20);
		// add x-axis as sub-component of the main display
		add(xd, BorderLayout.SOUTH);

		// reset layout bounds on component resize
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				int w = getWidth(), h = getHeight();
				stackBounds.setRect(10, 0, w - 60, h - 20);
				xaxisBounds.setRect(0, 0, w - 60, 15);
				yaxisBounds.setRect(0, 0, w, h - 20);
				// re-run actions to update the layouts
				m_vis.run("layout");
				m_vis.run("xaxis");

			}
		});

		// ------------------------------------------------

		m_vis.run("color"); // initialize color

		m_vis.run("layout"); // do initial layout and animation
		m_vis.run("xaxis"); // layout x-axis values
	}

	public Display getXAxisDisplay() {
		return xd;
	}

	// ------------------------------------------------------------------------

	public ListQueryBinding getGenderQuery() {
		return m_genderQ;
	}

	public SearchQueryBinding getSearchQuery() {
		return m_searchQ;
	}

	public static JFrame demoFrame(Table t, String[] columns, SocialAction app) {

		JFrame frame = new JFrame("SocialAction Timeline");
		frame.setResizable(true);
		frame.setContentPane(demo(t, columns, app));
		frame.pack();
		return frame;
	}

	public static JPanel demo(Table t, String[] columns, SocialAction app) {
		// create a new name voyager component
		final NameVoyager voyager = new NameVoyager(t, columns, app);
		voyager.setFocusable(false);

		// create a search panel
		final JSearchPanel search = voyager.getSearchQuery().createSearchPanel();
		search.setLabelText("name >>");
		search.setShowResultCount(false);
		search.setShowCancel(false);
		search.setShowBorder(false);
		search.setBorder(BorderFactory.createEmptyBorder(5, 0, 4, 0));

		voyager.addControlListener(new ControlAdapter() {
			public void itemClicked(VisualItem item, MouseEvent evt) {
				search.setQuery(item.getString("Name"));
			}
		});

		// get a set of dynamic query radio buttons for filtering on gender
		ListQueryBinding dynQuery = voyager.getGenderQuery();
		JToggleGroup gender = dynQuery.createRadioGroup();
		gender.setLabels(new String[] { "all", "1", "0" });
		gender.setGroupFocusable(false);

		JButton saveButton = new JButton("Save Image");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				try {
					Calendar cal = new GregorianCalendar();
					FileOutputStream stream = new FileOutputStream("timeline " + cal.getTime().toString() + ".png");
					FileOutputStream stream2 = new FileOutputStream("timeline-xaxis " + cal.getTime().toString()
							+ ".png");

					// System.out.println(stream.);
					voyager.saveImage(stream, "PNG", 1.0);
					voyager.getXAxisDisplay().saveImage(stream2, "PNG", 1.0);

				} catch (FileNotFoundException er) {
					System.out.println(er.getMessage());
				}

			}
		});

		// pack up the search box and gender selector into their own panel
		Box box = UILib.getBox(new JComponent[] { search, saveButton, gender }, true, 10, 50, 0);
		box.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(voyager, BorderLayout.CENTER);
		panel.add(box, BorderLayout.SOUTH);
		UILib.setColor(panel, Color.LIGHT_GRAY, Color.BLACK);
		UILib.setFont(box, FontLib.getFont("Tahoma", Font.BOLD, 16));
		return panel;
	}

	// ------------------------------------------------------------------------
	// Custom Actions for the NameVoyager Application

	public static class NameVoyagerColorFunction extends ColorAction {
		private String[] columns;
		private int m_male, m_female;
		SocialAction m_app;

		public NameVoyagerColorFunction(String group, String[] columns, SocialAction app) {
			super(group, VisualItem.FILLCOLOR);
			this.columns = columns;
			this.m_app = app;
		}

		public void run(double frac) {
			m_male = m_female = 0;

			Iterator iter = m_vis.visibleItems(m_group);
			while (iter.hasNext()) {
				VisualItem item = (VisualItem) iter.next();
				int total = 0;
				for (int i = 0; i < columns.length; ++i)
					total += item.getInt(columns[i]);

				String gender = item.getString("M/F");
				if (gender.equals("F") && total > m_female)
					m_female += total;
				else if (gender.equals("M") && total > m_male)
					m_male += total;
			}
			super.run(frac);
		}

		public int getColor(VisualItem item) {

			int[] palette = m_app.getColorPalette();

			int total = 0;
			for (int i = 0; i < columns.length; ++i)
				total += item.getInt(columns[i]);

			// SocialColorMap colorMap = new SocialColorMap(palette, 0, 79);

			// String name = item.getString("Name");
			// double value = m_app.getGraphs()[0].getNodeTable().get()

			// return colorMap.getColor(total);

			palette = ColorLib.getCategoryPalette(99);

			return palette[item.getInt("ID") % 99];

			// boolean male = item.getString("M/F").equals("M");
			// float hue = male ? 0.7f : 0.0f;
			//            
			// if ( item.isHover() ) {
			// // full saturate mouse-over items
			// return ColorLib.hsb(hue, 1.0f, 1.0f);
			// }
			//            
			// // saturate the color based on percentage of occurrence
			// int total = male ? m_male : m_female;
			// int average = 0;
			// for ( int i=0; i<columns.length; ++i )
			// average += item.getInt(columns[i]);
			// float saturation = 0.3f * (1 + ((float)average)/total);
			//            
			// return ColorLib.hsb(hue, saturation, 1.0f);
		}
	} // end of class NameVoyagerColorFunction

	public static class LabelAction extends ItemAction {
		private String m_field;

		public LabelAction(String group, String field) {
			super(group, null);
			m_field = field;
		}

		public void process(VisualItem item, double frac) {
			VisualItem src = ((DecoratorItem) item).getDecoratedItem();
			if (!src.isVisible()) {
				item.setVisible(false);
				return;
			}

			float[] poly = (float[]) src.get(m_field);
			if (poly == null) {
				item.setVisible(false);
				return;
			}
			float x = 0, y = 0, h, height = 0;
			int len = poly.length / 2;
			for (int i = 3; i < len - 2; i += 2) {
				h = poly[i] - poly[poly.length - i];
				if (h > height) {
					height = h;
					x = poly[i - 1];
					y = poly[i] - h / 2;
				}
			}
			if (height > 12) {
				item.setVisible(true);
				item.setX(x);
				item.setY(y);
				item.setFont(FontLib.getFont("Tahoma", Font.BOLD, 6 + Math.sqrt(height)));
				item.setTextColor(ColorLib.rgb(255, 255, 255));
			} else {
				item.setVisible(false);
			}
		}
	}

	public static class TooltipControl extends ControlAdapter {
		private VisualItem m_prevItem = null;
		private int m_prevCol = -1;

		public void itemEntered(VisualItem item, MouseEvent evt) {
			itemMoved(item, evt);
		}

		public void itemMoved(VisualItem item, MouseEvent evt) {
			if (!item.isInGroup(data))
				return;
			// Display d = (Display) evt.getSource();

			// compute column click position
			Rectangle2D b = item.getBounds();
			int c = (int) Math.round(12.0 * (evt.getX() - b.getMinX()) / b.getWidth());
			if (item == m_prevItem && c == m_prevCol)
				return;

			// String column = ranks[c];
			// String hue = item.getString("M/F").equals("M")
			// ? "#5555ff" : "#ff5555";
			// String year = column.substring(0, column.length()-1);
			// d.setToolTipText("<html><body bgcolor=\"" + hue + "\" "+
			// "text=\"#FFFFFF\""+
			// "<font face=\"Tahoma\" size=\"5\">"+
			// item.getString("Name")+"</font><br>"+
			// "Rank in "+year+": " +item.getInt(column)+
			// "</body></html>");

			m_prevItem = item;
			m_prevCol = c;
		}

		public void itemExited(VisualItem item, MouseEvent evt) {
			((Display) evt.getSource()).setToolTipText(null);
			m_prevItem = null;
			m_prevCol = -1;
		}
	}
} // end of class NameVoyager
