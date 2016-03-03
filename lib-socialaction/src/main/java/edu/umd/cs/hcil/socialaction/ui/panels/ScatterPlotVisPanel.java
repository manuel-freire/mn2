package edu.umd.cs.hcil.socialaction.ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.layout.AxisLabelLayout;
import prefuse.action.layout.AxisLayout;
import prefuse.controls.Control;
import prefuse.controls.ControlAdapter;
import prefuse.controls.ToolTipControl;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.expression.AndPredicate;
import prefuse.data.query.RangeQueryBinding;
import prefuse.data.tuple.TupleSet;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.AxisRenderer;
import prefuse.render.Renderer;
import prefuse.render.RendererFactory;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.util.UpdateListener;
import prefuse.util.ui.JFastLabel;
import prefuse.util.ui.JRangeSlider;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;
import prefuse.visual.expression.VisiblePredicate;
import prefuse.visual.sort.ItemSorter;
import edu.umd.cs.hcil.socialaction.SocialAction;
import edu.umd.cs.hcil.socialaction.action.filter.ScatterPlotVisibilityFilter;
import edu.umd.cs.hcil.socialaction.render.SocialColorMap;

/**
 * @author Adam Perer (influenced by Jeff Heer's Congress App)
 */
public class ScatterPlotVisPanel extends JPanel {

	/** Never change */
	private static final long serialVersionUID = -2395699612719729810L;
	private SocialAction m_app;
	private Visualization m_vis;
	private Display m_display;
	private JRangeSlider yRangeSlider, xRangeSlider;
	private Box infoBox;
	private Rectangle2D m_dataB = new Rectangle2D.Double();
	private Rectangle2D m_xlabB = new Rectangle2D.Double();
	private Rectangle2D m_ylabB = new Rectangle2D.Double();

	private int m_width;
	private int m_height;

	private JFastLabel m_total = new JFastLabel("");

	Graph m_graph;

	public void initialize(Table t, String xColumn, String yColumn, String colorColumn, Graph graph) {

		if (m_display != null)
			this.remove(m_display);
		if (xRangeSlider != null)
			this.remove(xRangeSlider);
		if (yRangeSlider != null)
			this.remove(yRangeSlider);
		if (infoBox != null)
			this.remove(infoBox);

		m_graph = graph;

		// STEP 1: setup the visualized data

		final String group = "two_d_ranking";

		m_vis = new Visualization();

		VisualTable vt = m_vis.addTable(group, t);

		m_vis.setRendererFactory(new RendererFactory() {
			AbstractShapeRenderer sr = new ShapeRenderer();
			Renderer arY = new AxisRenderer(Constants.RIGHT, Constants.TOP);
			Renderer arX = new AxisRenderer(Constants.CENTER, Constants.FAR_BOTTOM);

			public Renderer getRenderer(VisualItem item) {
				return item.isInGroup("ylab") ? arY : item.isInGroup("xlab") ? arX : sr;
			}
		});

		// --------------------------------------------------------------------
		// STEP 2: create actions to process the visual data

		// set up dynamic queries, search set
		RangeQueryBinding yAxisQ = new RangeQueryBinding(vt, yColumn);
		RangeQueryBinding xAxisQ = new RangeQueryBinding(vt, xColumn);

		// construct the filtering predicate
		AndPredicate filter = new AndPredicate(yAxisQ.getPredicate());
		filter.add(xAxisQ.getPredicate());

		// set up the actions
		AxisLayout xaxis = new AxisLayout(group, xColumn, Constants.X_AXIS, VisiblePredicate.TRUE);
		AxisLayout yaxis = new AxisLayout(group, yColumn, Constants.Y_AXIS, VisiblePredicate.TRUE);

		xaxis.setRangeModel(xAxisQ.getModel());
		yaxis.setRangeModel(yAxisQ.getModel());

		xaxis.setLayoutBounds(m_dataB);
		yaxis.setLayoutBounds(m_dataB);

		NumberFormat nf = NumberFormat.getNumberInstance();// .getCurrencyInstance();
		// nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(4);

		AxisLabelLayout ylabels = new AxisLabelLayout("ylab", yaxis, m_ylabB);

		ylabels.setNumberFormat(nf);

		AxisLabelLayout xlabels = new AxisLabelLayout("xlab", xaxis, m_xlabB);
		m_vis.putAction("xlabels", xlabels);
		// nf = NumberFormat.getNumberInstance();
		// nf.setMaximumFractionDigits(4);
		xlabels.setNumberFormat(nf);

		int[] palette = SocialColorMap.getInterpolatedMap(40, new Color(26, 150, 65), Color.BLACK, new Color(215, 25,
				28));

		DataColorAction color = new DataColorAction(group, colorColumn, Constants.ORDINAL, VisualItem.FILLCOLOR,
				palette);

		m_app.setNodeColorField(colorColumn);

		ActionList draw = new ActionList();
		draw.add(color);
		draw.add(xaxis);
		draw.add(yaxis);
		draw.add(ylabels);
		draw.add(new ColorAction(group, VisualItem.STROKECOLOR, 0));
		draw.add(new RepaintAction());
		m_vis.putAction("draw", draw);

		ActionList update = new ActionList();
		update.add(new ScatterPlotVisibilityFilter(group, filter, m_app, null, SocialAction.nodes));
		update.add(xaxis);
		update.add(yaxis);
		update.add(ylabels);
		update.add(new RepaintAction());
		m_vis.putAction("update", update);

		UpdateListener lstnr = new UpdateListener() {
			public void update(Object src) {
				m_vis.run("update");
			}
		};
		filter.addExpressionListener(lstnr);

		// STEP 4: set up a display and ui components to show the visualization

		m_display = new Display(m_vis);
		m_display.setItemSorter(new ItemSorter() {
			public int score(VisualItem item) {
				int score = super.score(item);
				if (item.isInGroup(group))
					score += 5;// item.getInt(xColumn);
				return score;
			}
		});
		m_display.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		// m_display.setSize(this.getParent().getWidth()-100, this.getParent().getHeight()-200);
		resizePrefuseDisplay(this.getParent().getWidth(), this.getParent().getHeight());
		m_display.setHighQuality(true);
		m_display.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				displayLayout();
			}
		});
		displayLayout();

		m_total.setPreferredSize(new Dimension(500, 20));
		m_total.setHorizontalAlignment(SwingConstants.RIGHT);
		m_total.setVerticalAlignment(SwingConstants.BOTTOM);

		ToolTipControl ttc = new ToolTipControl("label");
		Control hoverc = new ControlAdapter() {
			public void itemEntered(VisualItem item, MouseEvent evt) {
				if (item.isInGroup(group)) {
					m_total.setText(item.getString(m_app.getLabelField()));
					item.setFillColor(item.getStrokeColor());
					item.setStrokeColor(ColorLib.rgb(0, 0, 0));
					item.getVisualization().repaint();
					VisualItem graphItem = m_app.getVisualization().getVisualItem(SocialAction.nodes,
							item.getSourceTuple());
					if (graphItem != null)
						graphItem.setFixed(true);
					// graphItem.setHighlighted(true);

				}
			}

			public void itemExited(VisualItem item, MouseEvent evt) {
				if (item.isInGroup(group)) {
					m_total.setText("");
					item.setFillColor(item.getEndFillColor());
					item.setStrokeColor(item.getEndStrokeColor());
					item.getVisualization().repaint();

					VisualItem graphItem = m_app.getVisualization().getVisualItem(SocialAction.nodes,
							item.getSourceTuple());
					if (graphItem != null)
						graphItem.setFixed(false);
					// graphItem.setHighlighted(false);
				}
			}

			public void itemReleased(VisualItem item, MouseEvent evt) {
				if (item.isInGroup(group)) {
					m_total.setText(item.getString(m_app.getLabelField()));
					item.setFillColor(item.getStrokeColor());
					item.setStrokeColor(ColorLib.rgb(0, 0, 0));
					item.getVisualization().repaint();
					VisualItem graphItem = m_app.getVisualization().getVisualItem(SocialAction.nodes,
							item.getSourceTuple());
					if (graphItem != null) {
						TupleSet focusGroup = m_app.getVisualization().getFocusGroup(Visualization.FOCUS_ITEMS);
						focusGroup.setTuple(graphItem);
						// graphItem.setHighlighted(true);
					}
					// System.out.println();
				}
			}

		};
		m_display.addControlListener(ttc);
		m_display.addControlListener(hoverc);

		// --------------------------------------------------------------------
		// STEP 5: launching the visualization

		this.addComponentListener(lstnr);

		infoBox = new Box(BoxLayout.X_AXIS);
		// infoBox.add(Box.createHorizontalStrut(5));
		// infoBox.add(m_details);
		// infoBox.add(Box.createHorizontalGlue());
		infoBox.add(Box.createHorizontalStrut(5));
		infoBox.add(m_total);
		infoBox.add(Box.createHorizontalStrut(5));

		// set up search box
		// JSearchPanel searcher = searchQ.createSearchPanel();
		// searcher.setLabelText("Candidate: ");
		// searcher.setBorder(BorderFactory.createEmptyBorder(5,5,5,0));

		// create dynamic queries
		Box radioBox = new Box(BoxLayout.X_AXIS);
		radioBox.add(Box.createHorizontalStrut(5));
		// radioBox.add(searcher);
		radioBox.add(Box.createHorizontalGlue());
		radioBox.add(Box.createHorizontalStrut(5));
		// radioBox.add(yearsQ.createRadioGroup());
		radioBox.add(Box.createHorizontalStrut(16));

		yRangeSlider = yAxisQ.createVerticalRangeSlider();
		// yRangeSlider.setThumbColor(null);
		// yRangeSlider.setMinExtent(150000);
		yRangeSlider.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				m_display.setHighQuality(false);
				if (m_app.animateLayoutFalseButton.isSelected())
					m_app.getVisualization().run("draw");
			}

			public void mouseReleased(MouseEvent e) {
				m_display.setHighQuality(true);
				m_display.repaint();
				if (m_app.animateLayoutFalseButton.isSelected())
					m_app.getVisualization().run("draw");

				double lowValue = ((JRangeSlider) e.getSource()).getLowValue();
				double highValue = ((JRangeSlider) e.getSource()).getHighValue();

				m_app.addEventToHistory(Rank2DScatterplotPanel.NODE2D_STEP_NUMBER,
						Rank2DScatterplotPanel.NODE2D_YFILTER_STATE, Rank2DScatterplotPanel.NODE2D_FILTER_ACTION,
						lowValue, highValue);

			}
		});

		xRangeSlider = xAxisQ.createHorizontalRangeSlider();
		// xRangeSlider.setThumbColor(null);
		// xRangeSlider.setMinExtent(150000);
		xRangeSlider.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				m_display.setHighQuality(false);
				if (m_app.animateLayoutFalseButton.isSelected())
					m_app.getVisualization().run("draw");
			}

			public void mouseReleased(MouseEvent e) {
				m_display.setHighQuality(true);
				m_display.repaint();
				if (m_app.animateLayoutFalseButton.isSelected())
					m_app.getVisualization().run("draw");

				double lowValue = ((JRangeSlider) e.getSource()).getLowValue();
				double highValue = ((JRangeSlider) e.getSource()).getHighValue();

				m_app.addEventToHistory(Rank2DScatterplotPanel.NODE2D_STEP_NUMBER,
						Rank2DScatterplotPanel.NODE2D_XFILTER_STATE, Rank2DScatterplotPanel.NODE2D_FILTER_ACTION,
						lowValue, highValue);
			}
		});

		m_vis.run("draw");
		m_vis.run("xlabels");

		add(infoBox, BorderLayout.NORTH);
		add(m_display, BorderLayout.CENTER);
		add(yRangeSlider, BorderLayout.EAST);
		add(xRangeSlider, BorderLayout.SOUTH);
		// add(radioBox, BorderLayout.SOUTH);
		// UILib.setColor(this, ColorLib.getColor(255,255,255), Color.GRAY);
		// yRangeSlider.setForeground(Color.LIGHT_GRAY);
		// UILib.setFont(radioBox, FontLib.getFont("Tahoma", 15));

	}

	public void resizePrefuseDisplay(int width, int height) {

		m_width = width;
		m_height = height;

		if (m_width > m_height)
			setSize(new Dimension(m_height, m_height));
		else
			setSize(new Dimension(m_width, m_width));
		// if (m_display != null) {
		// m_display.setSize(width-100, height-150);
		//            
		// }
		//        
		//        
		this.doLayout();
		this.revalidate();
	}

	public Display getDisplay() {
		return m_display;
	}

	public ScatterPlotVisPanel(SocialAction app, Graph graph, int width, int height) {
		super();
		m_app = app;
		m_graph = graph;
		m_width = width;
		m_height = height;

		this.setLayout(new BorderLayout());

		if (m_width > m_height)
			setSize(new Dimension(m_height, m_height));
		else
			setSize(new Dimension(m_width, m_width));
		// setPreferredSize(new Dimension(m_width, m_height));

	}

	public void displayLayout() {
		Insets i = m_display.getInsets();

		// int w = this.getWidth();
		// int h = this.getHeight();

		int w = m_display.getWidth();
		int h = m_display.getHeight();

		int iw = i.left + i.right;
		int ih = i.top + i.bottom;
		int aw = 85;
		int ah = 15;

		m_dataB.setRect(i.left, i.top, w - iw - aw, h - ih - ah);
		m_xlabB.setRect(i.left, h - ah - i.bottom, w - iw - aw, ah - 10);
		m_ylabB.setRect(i.left, i.top, w - iw, h - ih - ah);

		m_vis.run("update");
		m_vis.run("xlabels");
	}

}