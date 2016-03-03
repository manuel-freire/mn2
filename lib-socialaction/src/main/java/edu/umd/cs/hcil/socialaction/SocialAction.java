package edu.umd.cs.hcil.socialaction;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.taglibs.string.util.StringW;
import org.jdesktop.swingx.JXTitledPanel;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.StrokeAction;
import prefuse.action.filter.GraphDistanceFilter;
import prefuse.action.layout.Layout;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.action.layout.graph.FruchtermanReingoldLayout;
import prefuse.activity.Activity;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.data.io.CSVTableReader;
import prefuse.data.io.CSVTableWriter;
import prefuse.data.io.DataIOException;
import prefuse.data.io.DelimitedTextTableReader;
import prefuse.data.io.DelimitedTextTableWriter;
import prefuse.data.io.GraphMLReader;
import prefuse.data.io.TableWriter;
import prefuse.data.query.RangeQueryBinding;
import prefuse.data.tuple.DefaultTupleSet;
import prefuse.data.tuple.TupleSet;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.render.PolygonRenderer;
import prefuse.render.Renderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.GraphLib;
import prefuse.util.GraphicsLib;
import prefuse.util.collections.IntIterator;
import prefuse.util.display.DisplayLib;
import prefuse.util.display.ItemBoundsListener;
import prefuse.util.force.Force;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.SpringForce;
import prefuse.util.io.IOLib;
import prefuse.util.io.SimpleFileFilter;
import prefuse.util.ui.JPrefuseTable;
import prefuse.util.ui.UILib;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;
import prefuse.visual.tuple.TableNodeItem;
import edu.umd.cs.hcil.socialaction.action.AggregateDataColorAction;
import edu.umd.cs.hcil.socialaction.action.GraphChangerAction;
import edu.umd.cs.hcil.socialaction.action.GraphCreatorAction;
import edu.umd.cs.hcil.socialaction.action.MidichlorianLayout_Comm;
import edu.umd.cs.hcil.socialaction.action.RelationshipConnectorAction;
import edu.umd.cs.hcil.socialaction.action.RepaintNodeAndEdgeTableAction;
import edu.umd.cs.hcil.socialaction.action.SelectedColorDataAction;
import edu.umd.cs.hcil.socialaction.action.SocNetLayout;
import edu.umd.cs.hcil.socialaction.analysis.community.AggregateLayout;
import edu.umd.cs.hcil.socialaction.analysis.community.CommunityConstructor;
import edu.umd.cs.hcil.socialaction.analysis.community.CommunityDragControl;
import edu.umd.cs.hcil.socialaction.analysis.community.CommunityEdgeLabeler;
import edu.umd.cs.hcil.socialaction.analysis.community.CommunityFilter;
import edu.umd.cs.hcil.socialaction.analysis.community.SubgraphSet;
import edu.umd.cs.hcil.socialaction.control.DragReadabilityControl;
import edu.umd.cs.hcil.socialaction.control.SelectionDetailsControl;
import edu.umd.cs.hcil.socialaction.control.SelectionFocusControl;
import edu.umd.cs.hcil.socialaction.control.SocialActionNeighborControl;
import edu.umd.cs.hcil.socialaction.io.SocialActionIOLib;
import edu.umd.cs.hcil.socialaction.jung.statistics.GraphStatistics;
import edu.umd.cs.hcil.socialaction.render.AggregateRenderer;
import edu.umd.cs.hcil.socialaction.render.SelectedEdgeRenderer;
import edu.umd.cs.hcil.socialaction.render.SelectedLabelRenderer;
import edu.umd.cs.hcil.socialaction.render.SocialActionItemSorter;
import edu.umd.cs.hcil.socialaction.render.SocialColorMap;
import edu.umd.cs.hcil.socialaction.ui.panels.CommunityPanel;
import edu.umd.cs.hcil.socialaction.ui.panels.DetailsOnDemandPanel;
import edu.umd.cs.hcil.socialaction.ui.panels.DisplaySettingsPanel;
import edu.umd.cs.hcil.socialaction.ui.panels.Edge1DPanel;
import edu.umd.cs.hcil.socialaction.ui.panels.FilterPanel;
import edu.umd.cs.hcil.socialaction.ui.panels.GraphReadabilityPanel;
import edu.umd.cs.hcil.socialaction.ui.panels.ImportDataPanel;
import edu.umd.cs.hcil.socialaction.ui.panels.ImportPreviewPanel;
import edu.umd.cs.hcil.socialaction.ui.panels.MultiplexPanel;
import edu.umd.cs.hcil.socialaction.ui.panels.NetworkOverviewPanel;
import edu.umd.cs.hcil.socialaction.ui.panels.Node1DPanel;
import edu.umd.cs.hcil.socialaction.ui.panels.NodeAttributes1DPanel;
import edu.umd.cs.hcil.socialaction.ui.panels.Rank2DScatterplotPanel;
import edu.umd.cs.hcil.socialaction.ui.panels.syf.SYFAnnotationPanel;
import edu.umd.cs.hcil.socialaction.ui.panels.syf.SYFHistoryPanel;
import edu.umd.cs.hcil.socialaction.ui.panels.syf.SYFOverviewPanelOld2;

/**
 * @author Adam Perer
 * @author Cody Dunne
 */
public class SocialAction extends JPanel implements ActionListener {

	/** Never change */
	private static final long serialVersionUID = 7719522363410059439L;

        public static final String IMAGE_BASE = "/edu/umd/cs/hcil/socialaction/images";

	public static final int USER_NLM = 0;

	public static final int USER_START = 1;

	public static final int USER_USNEWS = 2;

	public static final int USER_BCBS = 3;

	public static final int USER_ADAM = 4;

	public static final int USER_PHYSICS = 5;

	public static final int USER_WISCONSIN = 6;

	public static final int USER_PLATH = 7;

	public static final int USER_MSU = 8;

	public static final int USER_JIMMY = 9;

	public static final int USER_CONRAD = 10;

	public static final int USER_ELENA = 11;

	public static final int USER_THOMAS = 12;

	public static final int USER_BRADLEY = 13;

	public static final int USER_SHANE = 14;

	public static final int USER_SAKET = 15;

	public static final int USER_ROUNDMARK = 16;

	public static final int USER_KEVIN = 17;

	public static final int USER_SAIC = 18;

	public static final int USER_AHNLEE = 19;

	public static final int USER_UMD = 20;

	public static final int CURRENT_USER = USER_START;

	public static final String VERSION_NUMBER = "052008";

	// public static final String TEMP_WEIGHT_FIELD = "weight";

	public static final String DEFAULT_FONTNAME = "SansSerif";

	public static final String graph = "graph";

	public static final String nodes = "graph.nodes";

	public static final String edges = "graph.edges";

	public static final String community = "graph.community";

	public static final String communityAggr = "aggregates";

	public static final String RANGE_NODE_SLIDER_GROUP = "nodeRangeSliderGroup";

	public static final String RANGE_EDGE_SLIDER_GROUP = "edgeRangeSliderGroup";

	public static final String OVERVIEW_PANEL = "Overview";

	public static final String NODE_1D_PANEL = "Rank Nodes";

	public static final String NODE_ATTRIBUTE_1D_PANEL = "Node Attributes";

	public static final String NODE_2D_PANEL = "Plot Nodes";

	public static final String EDGE_1D_PANEL = "Rank Edges";

	public static final String EDGE_2D_PANEL = "Plot Edges";

	public static final String COMMUNITY_PANEL = "Find Communities";

	public static final String MULTIPLEX_PANEL = "Edge Types";

	public static final String HISTORY_PANEL = "History";

	public static final String ANNOTATION_PANEL = "Annotation";

	public static final String DISPLAY_PANEL = "Settings";

	public static final String DETAILS_PANEL = "Details";

	public static final String IMPORT_DATA_PANEL = "Import Data";

	public static final String READABILITY_PANEL = "Graph Readability";

	public final static String[] STEPS_PANELS = { OVERVIEW_PANEL, NODE_1D_PANEL, EDGE_1D_PANEL, NODE_2D_PANEL,
			EDGE_2D_PANEL, COMMUNITY_PANEL, MULTIPLEX_PANEL, };

	public final static int ANNOTATION_NONE = 0;

	public final static int ANNOTATION_POS = 1;

	public final static int ANNOTATION_NEG = 2;

	public final static String ID_COLUMN_NAME = "ID";

	public final static String LABEL_COLUMN_NAME = "Label";

	public final static String MULTIPLEX_COLUMN_NAME = "Multiplex";

	public final static String PARTITION_COLUMN_NAME = "Partition";

	public final static String IMAGE_COLUMN_NAME = "Image";

	public final static String AGGREGATE_ID_COLUMN_NAME = "id";

	public final static String COMMUNITY_COLUMN_NAME = "Community";

	/** Global spring coefficient for force directed layout */
	public final static float[] LAYOUT_VALUES = { 9E-5f, 9E-6f, 9E-7f, 9E-8f, 9E-9f };
	// TODO How many? (SocialAction layoutSlider)

	/** Between community spring coefficient for force directed layout */
	public final static float[] LAYOUT_VALUES_COMMUNITY = { 9E-6f, 9E-7f, 9E-8f, 9E-9f, 9E-10f };
	// TODO How many? (SocialAction layoutSlider)

	// SocialAction Instance
	private static SocialAction _instance;

	public static SocialAction getInstance() {
		return _instance;
	}

	public Visualization m_vis;

	private AggregateTable aggregateTable;

	public Graph m_graph;

	public Graph[] m_multiplexGraphs;

	public Graph[] m_communityGraphs;

	private String[] m_multiplexGraphsNames;

	private String[] m_communityGraphsNames;

	private Display display;

	private RangeQueryBinding m_rangeQ;

	// private boolean m_isAppletMode = false;

	private boolean m_isSYFMode = false;

	// public JSocialActionTable prefuseTable;

	JScrollPane tableScrollPane;

	JSplitPane topSplit;

	JToolBar mainToolBar, networkVisToolBar;

	JSplitPane mainSplit;

	public JButton enforceBoundsFalseButton, enforceBoundsTrueButton, animateLayoutTrueButton,
			animateLayoutFalseButton, saveData, wordWrapButton;

	public JButton[] layoutButtons, mcNodeAttribButtons, mcEdgeAttribButtons;

	public JButton overviewButton, rankNodesButton, rankEdgesButton, plotNodesButton, communitiesButton,
			multiplexButton, settingsButton, detailsButton, readabilityButton, nodeAttributesButton, importDataButton;

	// LinLogAction m_linlog;
	// ForceDirectedLayout forceDirectedLayout;
	Layout forceDirectedLayout;
	// SocNetLayout forceDirectedLayout;

	public GraphDistanceFilter egoFilter;

	private SelectedColorDataAction nodeFillColorAction, edgeFillColorAction;

	public SelectedLabelRenderer tr;

	private edu.umd.cs.hcil.socialaction.render.SelectedEdgeRenderer edgeRenderer;

	private ActionList draw, animate, communityAction, layoutGraph, setLocation;

	public ActionList getLayoutGraph() {
		return layoutGraph;
	}

	CommunityConstructor commConstruct;

	CommunityPanel commPanel;

	SYFHistoryPanel historyPanel;

	SYFAnnotationPanel annotationPanel;

	FilterPanel filterPanel;

	// private JPanel stepsPanel;

	JPanel tabbedPane;

	Node1DPanel node1DPanel;

	NodeAttributes1DPanel nodeAttributePanel;

	Edge1DPanel edge1DPanel;

	NetworkOverviewPanel graphStatsPanel;

	Rank2DScatterplotPanel rank2DPanel;

	SYFOverviewPanelOld2 syfOverviewPanel;

	MultiplexPanel multiplexPanel;

	DetailsOnDemandPanel detailsPanel;

	ImportDataPanel importDataPanel;

	private GraphReadabilityPanel readabilityPanel;// Show graph quality measurements
	// Need variable so the GraphReadabilityPanel can disable the neighbor highlighting
	private SocialActionNeighborControl neighborControl;

	// Needed to be able to disable it
	private DragReadabilityControl dragReadabilityControl;

	DisplaySettingsPanel displayPanel;

	int[] colorSpectrum, categoryPalette;

	private AggregateDataColorAction aggregateFillDataColorAction;

	int[] aggregateColorPalette;

	public String[] aggregateLabels;

	public HashMap<String, Point2D.Float> locationMap;

	public boolean m_isWordWrap = false;

	private boolean m_isUpdateNodeTable = false;
	private boolean m_isUpdateEdgeTable = false;

	private boolean enforceBounds = true;

	public void setUpdateNodeTable(boolean value) {
		m_isUpdateNodeTable = value;
	}

	public void setUpdateEdgeTable(boolean value) {
		m_isUpdateEdgeTable = value;
	}

	public boolean isUpdateNodeTable() {
		return m_isUpdateNodeTable;
	}

	public boolean isUpdateEdgeTable() {
		return m_isUpdateEdgeTable;
	}

	public void updateNodeTable() {
		node1DPanel.getRankTable().repaint();
	}

	public void updateEdgeTable() {
		edge1DPanel.getRankTable().revalidate();
		edge1DPanel.getRankTable().repaint();
	}

	public JLabel m_appletLabel;

	/*
	 * public void setAppletMode(boolean isApplet) { m_isAppletMode = isApplet; }
	 * 
	 * public boolean isAppletMode() { return m_isAppletMode; }
	 */

	public void setMultiplexGraphs(Graph[] graphs, String[] graphsNames) {

		m_multiplexGraphs = graphs;
		m_multiplexGraphsNames = graphsNames;

	}

	public Graph[] getMultiplexGraphs() {
		return m_multiplexGraphs;
	}

	public String[] getMultiplexGraphsNames() {
		return m_multiplexGraphsNames;
	}

	public void setCommunityGraphs(Graph[] graphs, String[] graphsNames) {
		m_communityGraphs = graphs;
		m_communityGraphsNames = graphsNames;

		// syfOverviewPanel.resetStep(CommunityPanel.COMMUNITY_STEP_NUMBER);
		// syfOverviewPanel.registerCommunitySteps();

	}

	public Graph[] getCommunityGraphs() {
		return m_communityGraphs;
	}

	public String[] getCommunityGraphsNames() {
		return m_communityGraphsNames;
	}

	public Graph getGraph() {
		return m_graph;
	}

	public void createCommunityGraphs() {

		// String SRC = Graph.DEFAULT_SOURCE_KEY;
		// String TRG = Graph.DEFAULT_TARGET_KEY;

		SubgraphSet communities = getSubgraphs();

		m_communityGraphs = new Graph[communities.getCommunityCount() + 1];
		String[] graphsNames = new String[communities.getCommunityCount() + 1];

		for (int i = 0; i < communities.getCommunityCount() + 1; i++) {

			String currentMultiplexValue = "All";
			if (i > 0) {

				Table nodes = new Table();
				for (int j = 0; j < m_graph.getNodeTable().getColumnCount(); j++) {
					nodes.addColumn(m_graph.getNodeTable().getColumnName(j), m_graph.getNodeTable().getColumnType(j));
				}

				Table edges = new Table();
				for (int j = 0; j < m_graph.getEdgeTable().getColumnCount(); j++) {
					edges.addColumn(m_graph.getEdgeTable().getColumnName(j), m_graph.getEdgeTable().getColumnType(j));
				}

				IntIterator rowIt = m_graph.getNodeTable().rows();

				currentMultiplexValue = new Integer(i).toString(); // communities.get.get(i - 1).toString();
				while (rowIt.hasNext()) {
					int row = rowIt.nextInt();

					Node node = m_graph.getNode(row);
					TableNodeItem vNode = (TableNodeItem) getVisualization().getVisualItem(graph, node);

					if (communities.getCommunity(vNode) == i - 1) {
						nodes.addTuple(node);

						// bah .. need to do TWO passes for edges, and keep track of nodeRows. LAME!

						// Iterator edgeIterator = node.edges();
						// while (edgeIterator.hasNext()) {
						//                            
						// Edge edge = (Edge) edgeIterator.next();
						// Node sourceNode = edge.getSourceNode();
						// Node targetNode = edge.getTargetNode();
						// TableNodeItem vSourceNode = (TableNodeItem) getVisualization().getVisualItem(graph,
						// sourceNode);
						// TableNodeItem vTargetNode = (TableNodeItem) getVisualization().getVisualItem(graph,
						// targetNode);
						//
						// if ((communities.getCommunityMembers(i-1).contains(vSourceNode)) &&
						// (communities.getCommunityMembers(i-1).contains(vTargetNode))) {
						//
						// sourceNode.getRow();
						// int newRow = edges.addRow();
						// edges.setInt(newRow, SRC, ((Integer) m_graph.getNodeTable().get);
						// edges.setInt(newRow, TRG, ((Integer) (m_importedGraph.getEdgeTable().get(row,
						// TRG))).intValue());
						//
						//                                
						// edges.addTuple(edge);
						// }
						//                            
						//                            
						//
						// }

					}

				}

				// int newRow = edges.addRow();
				// edges.setInt(newRow, SRC, ((Integer) (m_importedGraph.getEdgeTable().get(row, SRC))).intValue());
				// edges.setInt(newRow, TRG, ((Integer) (m_importedGraph.getEdgeTable().get(row, TRG))).intValue());

				m_communityGraphs[i] = new Graph(nodes, edges, false);
			} else
				m_communityGraphs[i] = m_graph;

			graphsNames[i] = currentMultiplexValue;

		}

		setCommunityGraphs(m_communityGraphs, graphsNames);
	}

	boolean threadAvailable = false;

	public void setupGraphLocation(Graph g) {

		layoutGraph.cancel();

		m_vis.cancel("draw");

		Iterator i = m_graph.nodes();
		while (i.hasNext()) {
			Node node = (Node) i.next();
			float x = (float) getVisualization().getVisualItem(graph, node).getX();
			float y = (float) getVisualization().getVisualItem(graph, node).getY();
			locationMap.put((String) node.getString(getLabelField()), new Point2D.Float(x, y));
		}

		// clear edges

		ActionList changeGraph = new ActionList();
		changeGraph.add(new GraphChangerAction(this, m_vis, g));

		m_vis.putAction("change", changeGraph);
		m_vis.runAfter("change", "setLocation");
		m_vis.run("change");

		threadAvailable = false;
		updateGraph(g, false);
	}

	public void setupGraphLocation(Table t) {

		layoutGraph.cancel();

		m_vis.cancel("draw");

		int cnt = 0;
		Iterator i = m_graph.nodes();
		while (i.hasNext()) {
			Node node = (Node) i.next();
			// String label = node.getString(getLabelField());

			float x = (float) t.getDouble(cnt, "_x");
			float y = (float) t.getDouble(cnt, "_y");

			// float x = (float) getVisualization().getVisualItem(graph, node).getX();
			// float y = (float) getVisualization().getVisualItem(graph, node).getY();
			locationMap.put((String) node.getString(getLabelField()), new Point2D.Float(x, y));
			cnt++;
		}

		// clear edges

		ActionList changeGraph = new ActionList();
		changeGraph.add(new GraphChangerAction(this, m_vis, m_graph));

		m_vis.putAction("change", changeGraph);
		m_vis.runAfter("change", "setLocation");
		m_vis.run("change");

		threadAvailable = false;
		updateGraph(m_graph, false);

	}

	synchronized public void updateGraph(Graph g, boolean resetSYF) {

		while (threadAvailable == false) {
			try {
				wait();
			} catch (InterruptedException e) {
			}

		}
		threadAvailable = false;

		if (g == null)
			g = m_graph;

		updatePanels(/* true */);

		if (!resetSYF) {
			node1DPanel.setContent(g);
			switchToNode1D();
			edge1DPanel.setContent(g);// XXX remove? Add attribute panel?
		}

		m_vis.run("draw");

		if (resetSYF) {
			syfOverviewPanel.reset(g);
		}

		/*
		 * if (m_isAppletMode) { node1DPanel.setSelectedRanking("party"); System.err.println("Applet mode issues");
		 * edge1DPanel.getSlider().setLowValue(edge1DPanel.getSlider().getHighValue() - 1); }
		 */

	}

	synchronized public void setupGraph(Graph g, String label) {
		m_graph = g;

		setLabelField(label);

		if (m_graph.getNodeCount() > 0) {
			setKeyField(m_graph.getNodeTable().getColumnName(0));

			// setNodeColorField(getNodeColorField()); // TODO check later

			// adds graph to visualization and sets renderer label field
			setGraph(m_graph, label);

			setupTupleSets();

			VisualGraph vg = (VisualGraph) m_vis.getVisualGroup(SocialAction.graph);
			vg.addColumn("extraCommunity", boolean.class);

			// setupToolBar();

			rank2DPanel.setContent(m_graph);
			graphStatsPanel.setContent(m_graph);
			multiplexPanel.setContent();

			setNodeColorField(null);

			mcNodeJoinAttrib = null;
			toggleMcJoinAttribButton(mcNodeAttribButtons, mcNodeAttribButtons[1]);
			mcEdgeJoinAttrib = null;
			toggleMcJoinAttribButton(mcEdgeAttribButtons, mcEdgeAttribButtons[1]);

			node1DPanel.setContent(m_graph);
			nodeAttributePanel.setContent(m_graph);
			edge1DPanel.setContent(m_graph);
			displayPanel.setContent(m_graph);

			/*
			 * if (m_isAppletMode) node1DPanel.setSelectedRanking("party");
			 */
		}

		threadAvailable = true;
		notifyAll();
		// CardLayout cl = (CardLayout) tabbedPane.getLayout();
		// cl.show(tabbedPane, SocialAction.STEPS_PANELS[0]);

	}

	public void setupTupleSets() {
		System.err.println("perhaps we are not removing old tupleset listeners...");

		new TupleSetListener() {
			public void tupleSetChanged(TupleSet ts, Tuple[] add, Tuple[] rem) {
				for (int i = 0; i < rem.length; ++i)
					((VisualItem) rem[i]).setFixed(false);
				for (int i = 0; i < add.length; ++i) {
					((VisualItem) add[i]).setFixed(false);
					((VisualItem) add[i]).setFixed(true);
				}
				if (ts.getTupleCount() == 0) {
					ts.addTuple(rem[0]);
					((VisualItem) rem[0]).setFixed(false);
				}
				m_vis.run("draw");
			}
		};

		DefaultTupleSet nodeSliderSet = new DefaultTupleSet();
		DefaultTupleSet edgeSliderSet = new DefaultTupleSet();
		if (m_vis.getFocusGroup(RANGE_NODE_SLIDER_GROUP) != null) {
			m_vis.getFocusGroup(RANGE_NODE_SLIDER_GROUP).clear();
			m_vis.removeGroup(RANGE_NODE_SLIDER_GROUP);
		}

		if (m_vis.getFocusGroup(RANGE_EDGE_SLIDER_GROUP) != null) {
			m_vis.getFocusGroup(RANGE_EDGE_SLIDER_GROUP).clear();
			m_vis.removeGroup(RANGE_EDGE_SLIDER_GROUP);
		}

		m_vis.addFocusGroup(RANGE_NODE_SLIDER_GROUP, nodeSliderSet);
		m_vis.addFocusGroup(RANGE_EDGE_SLIDER_GROUP, edgeSliderSet);

		addToNodeRangeSliderSet(m_graph.getNodes());
		addToEdgeRangeSliderSet(m_graph.getEdges());
	}

	static Logger logger = Logger.getLogger(SocialAction.class);

	public SocialAction(Graph g, String label/* , boolean isAppletMode */) {

		/* setAppletMode(isAppletMode); */
		logger.info("SAInfo\tVersion\t" + VERSION_NUMBER + "\tCurrent User\t" + CURRENT_USER);

		// create a new, empty visualization for our data
		m_vis = new Visualization();

		// --------------------------------------------------------------------
		// set up the renderers

		tr = new SelectedLabelRenderer(this);
		tr.setRoundedCorner(10, 10);
		tr.setHorizontalPadding(4);
		tr.setVerticalPadding(1);
		tr.setRenderType(LabelRenderer.RENDER_TYPE_DRAW_AND_FILL);

		edgeRenderer = new SelectedEdgeRenderer(this);
		// edgeRenderer.setArrowType(Constants.EDGE_ARROW_FORWARD);
		edgeRenderer.setDefaultLineWidth(1);
		// edgeRenderer.setEdgeType(Constants.EDGE_TYPE_CURVE);

		// edgeRenderer.setEdgeType(Constants.EDGE_TYPE_CURVE);

		DefaultRendererFactory drf = new DefaultRendererFactory(tr, edgeRenderer);

		// draw aggregates as polygons with curved edges
		Renderer polyR = new AggregateRenderer(Constants.POLY_TYPE_CURVE);
		((PolygonRenderer) polyR).setCurveSlack(0.15f);
		drf.add("ingroup('" + communityAggr + "')", polyR);
		m_vis.setRendererFactory(drf);

		aggregateTable = m_vis.addAggregates(SocialAction.communityAggr);
		aggregateTable.addColumn(VisualItem.POLYGON, float[].class);
		aggregateTable.addColumn(AGGREGATE_ID_COLUMN_NAME, int.class);
		aggregateTable.addColumn(LABEL_COLUMN_NAME, String.class);

		m_vis.addFocusGroup(community, new SubgraphSet(m_vis));

		// --------------------------------------------------------------------
		// register the data with a visualization

		// --------------------------------------------------------------------
		// create actions to process the visual data

		/*
		 * if (m_isAppletMode) { redGreenSpectrum = SocialColorMap.getInterpolatedMap(50, Color.BLUE, Color.BLACK,
		 * Color.RED); } else {
		 */
		// ColorBrewer diverging 3x#1 purple-white-orange (all ok, bad white middle on text)
		// colorSpectrum = SocialColorMap.getInterpolatedMap(50, new Color(153, 142, 195), new Color(247, 247, 247), new
		// Color(241, 163, 64));
		// ColorBrewer diverging 3x#5 blue-black-red (bad print, bad white middle on text)
		// colorSpectrum = SocialColorMap.getInterpolatedMap(50, new Color(103, 169, 207), new Color(8, 8, 8), new
		// Color(239, 138, 98));
		// ColorBrewer diverging 7x#5 blue-black-red (bad print, bad white middle on text)
		// colorSpectrum = SocialColorMap.getInterpolatedMap(50, new Color(69, 117, 180), new Color(8, 8, 8), new
		// Color(215, 48, 39));
		// SocialAction default green-black-red
		// colorSpectrum = SocialColorMap.getInterpolatedMap(50, new Color(26, 150, 65), Color.BLACK, new Color(215, 25,
		// 28));
		// Old SocialAction default blue-black-red
		colorSpectrum = SocialColorMap.getInterpolatedMap(50, Color.BLUE, Color.BLACK, Color.RED);
		// colorSpectrum = SocialColorMap.getInterpolatedMap(50, Color.BLUE, Color.BLACK, Color.ORANGE);

		// }

		categoryPalette = ColorLib.getCategoryPalette(99);

		nodeFillColorAction = new SelectedColorDataAction(this, nodes, getNodeColorField(), Constants.NUMERICAL,
				VisualItem.FILLCOLOR, colorSpectrum);

		// nodeFillColorAction.add(VisualItem.FIXED, ColorLib.rgb(255, 100, 100));
		// nodeFillColorAction.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 200, 125));
		// // nodeFillColorAction.add("_hover", ColorLib.gray(220,230));
		// nodeFillColorAction.add("ingroup('_focus_')", ColorLib.rgb(255, 200, 125));
		// nodeFillColorAction.add("ingroup('_focus_')", ColorLib.rgb(255, 200, 125));
		// nodeFillColorAction.add(rangeFilter, ColorLib.rgb(255,190,190));

		edgeFillColorAction = new SelectedColorDataAction(this, edges, getEdgeColorField(), Constants.NUMERICAL,
				VisualItem.STROKECOLOR, colorSpectrum);

		aggregateColorPalette = ColorLib.getCategoryPalette(7);

		ColorAction aStroke = new ColorAction(communityAggr, VisualItem.STROKECOLOR);
		aStroke.setDefaultColor(ColorLib.gray(200));
		aggregateFillDataColorAction = new AggregateDataColorAction(communityAggr, AGGREGATE_ID_COLUMN_NAME,
				Constants.NUMERICAL, VisualItem.FILLCOLOR, aggregateColorPalette);

		// aggregateFillDataColorAction.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 200, 125));
		// aggregateFillDataColorAction.add("ingroup('_focus_')", ColorLib.rgb(255, 200, 125));

		// egoFilter = new GraphDistanceFilter(graph, nodes, 2);

		draw = new ActionList();
		// draw.add(egoFilter);
		draw.add(new ColorAction(nodes, VisualItem.STROKECOLOR, 0));
		draw.add(new ColorAction(nodes, VisualItem.TEXTCOLOR, ColorLib.rgb(255, 255, 255)));
		draw.add(new ColorAction(edges, VisualItem.FILLCOLOR, ColorLib.gray(0)));
		draw.add(new ColorAction(edges, VisualItem.STROKECOLOR, ColorLib.gray(0)));

		layoutGraph = new ActionList(Activity.INFINITY);
		forceDirectedLayout = new SocNetLayout(this, graph, false);
		// forceDirectedLayout = new ForceDirectedLayout(graph, true, false);
		if (forceDirectedLayout instanceof ForceDirectedLayout) {
			((ForceDirectedLayout) forceDirectedLayout).setEnforceBounds(enforceBounds);
		}
		// forceDirectedLayout.setMaxTimeStep(40);
		layoutGraph.add(forceDirectedLayout);
		// layoutGraph.setStepTime(50);

		// m_linlog = new LinLogAction( this );

		LocationAction setLocationAction = new LocationAction(this);

		setLocation = new ActionList(Activity.DEFAULT_STEP_TIME);
		setLocation.add(setLocationAction);

		animate = new ActionList(Activity.INFINITY);

		// animate.add(new VisibilityFilter(nodes, rangeFilter));
		animate.add(new ColorAction(communityAggr, VisualItem.STROKECOLOR, ColorLib.gray(200)));
		animate.add(new AggregateLayout(communityAggr));
		animate.add(aggregateFillDataColorAction);

		animate.add(new ColorAction(nodes, VisualItem.STROKECOLOR, ColorLib.rgb(255, 255, 255)));
		animate.add(new ColorAction(nodes, VisualItem.TEXTCOLOR, ColorLib.rgb(255, 255, 255)));

		Predicate p1 = (Predicate) ExpressionParser.parse(VisualItem.HIGHLIGHT);
		Predicate p2 = (Predicate) ExpressionParser.parse("ingroup('_focus_')");
		Predicate p3 = (Predicate) ExpressionParser.parse(VisualItem.FIXED);

		// int highlightColor = ColorLib.rgb(255, 200, 125); // ColorLib.color(Color.YELLOW);
		// int fixedColor = ColorLib.rgb(255, 100, 100);

		int highlightColor = ColorLib.rgb(255, 200, 125); // ColorLib.color(Color.YELLOW);
		int fixedColor = ColorLib.rgb(255, 255, 0);
		// ColorLib.rgb(0, 0, 255);

		animate.add(new ColorAction(nodes, p1, VisualItem.STROKECOLOR, highlightColor));
		animate.add(new ColorAction(nodes, p1, VisualItem.TEXTCOLOR, highlightColor));
		animate.add(new ColorAction(nodes, p2, VisualItem.STROKECOLOR, highlightColor));
		animate.add(new ColorAction(nodes, p2, VisualItem.TEXTCOLOR, highlightColor));
		animate.add(new ColorAction(nodes, p3, VisualItem.STROKECOLOR, fixedColor));
		animate.add(new ColorAction(nodes, p3, VisualItem.TEXTCOLOR, fixedColor));

		StrokeAction strokeAction = new StrokeAction(nodes);
		strokeAction.add(p1, new BasicStroke(3));
		strokeAction.add(p2, new BasicStroke(5));
		strokeAction.add(p3, new BasicStroke(5));
		animate.add(nodeFillColorAction);
		animate.add(edgeFillColorAction);
		animate.add(strokeAction);

		// SizeAction sizeAction = new SizeAction(nodes);
		// sizeAction.add(p1, 2.0);
		// sizeAction.add(p2, 2.0);
		// sizeAction.add(p3, 2.0);
		// animate.add(sizeAction);

		animate.add(new RepaintAction());
		animate.add(new RepaintNodeAndEdgeTableAction(this, m_vis));

		commConstruct = new CommunityConstructor(this, m_vis, community);// , sColor);
		CommunityFilter commFilter = new CommunityFilter(this, m_vis, community, aggregateTable);
		CommunityEdgeLabeler commLabeler = new CommunityEdgeLabeler(this, m_vis, community, community);
		// CommunityTableFiller commTableFiller = new CommunityTableFiller(community, this);

		// initialize list for community filtering

		communityAction = new ActionList();
		communityAction.add(commConstruct);
		// communityAction.add(new AggregateLayout(community));
		communityAction.add(commFilter);
		communityAction.add(commLabeler);
		// community.add(commTableFiller);

		// finally, we register our ActionList with the Visualization.
		// we can later execute our Actions by invoking a method on our
		// Visualization, using the name we've chosen below.
		m_vis.putAction("draw", draw);
		m_vis.putAction("layout", animate);
		m_vis.putAction("graphLayout", layoutGraph);
		m_vis.putAction("setLocation", setLocation);
		// m_vis.putAction("linlog", m_linlog);

		m_vis.runAfter("draw", "layout");
		// m_vis.runAfter("draw", "linlog");

		// --------------------------------------------------------------------
		// set up a display to show the visualization

		display = new Display(m_vis);
		display.setSize(700, 700);
		display.pan(350, 350);
		display.setForeground(Color.GRAY);
		display.setBackground(Color.WHITE);
		// display.setBackground(Color.LIGHT_GRAY);
		display.setHighQuality(true);

		// main display controls
		display.addControlListener(new SelectionFocusControl(1, this));
		display.addControlListener(new SelectionDetailsControl(2, this));
		dragReadabilityControl = new DragReadabilityControl(this);
		display.addControlListener(dragReadabilityControl);
		setRealTimeGraphReadabilityMetrics(false);// Turn off initially
		// display.addControlListener(new DragControl());
		display.addControlListener(new PanControl());
		display.addControlListener(new ZoomControl());
		display.addControlListener(new WheelZoomControl());
		display.addControlListener(new ZoomToFitControl());
		neighborControl = new SocialActionNeighborControl(this);
		display.addControlListener(neighborControl);
		display.addControlListener(new CommunityDragControl());

		display.setItemSorter(new SocialActionItemSorter(this));

		// --------------------------------------------------------------------
		// launch the visualization

		locationMap = new HashMap<String, Point2D.Float>();

		// create a new JSplitPane to present the interface
		mainSplit = new JSplitPane();
		// JXMultiSplitPane mainSplit = new JXMultiSplitPane();

		mainToolBar = addMainToolBarButtons();
		networkVisToolBar = addNetworkVisToolBarButtons();
		// mainTool

		JPanel networkDisplay = new JPanel();
		networkDisplay.setLayout(new BorderLayout());
		// if (!m_isAppletMode) {
		networkDisplay.add(networkVisToolBar, BorderLayout.PAGE_START);
		networkDisplay.setBorder(BorderFactory.createTitledBorder("Network Visualization"));

		// }
		networkDisplay.add(display);

		tabbedPane = getTabbedControls();
		//        
		// mainSplit.setModel(new DefaultSplitPaneModel());
		// mainSplit.add(tabbedPane, DefaultSplitPaneModel.LEFT);
		// mainSplit.add(networkDisplay, DefaultSplitPaneModel.BOTTOM);

		int SYF_WIDTH = 230;

		syfOverviewPanel = new SYFOverviewPanelOld2(this, SYF_WIDTH, tabbedPane);
		syfOverviewPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		// JSplitPane syfSplit = new JSplitPane();
		// if (m_isSYFMode) {
		// syfSplit.setLeftComponent(syfOverviewPanel);
		// syfSplit.setRightComponent(mainSplit);
		// mainSplit.setDividerLocation(SYF_WIDTH);
		//            
		// }

		// if (!m_isAppletMode) {
		mainSplit.setLeftComponent(tabbedPane);
		mainSplit.setDividerLocation(TABSIZE_X);
		// } else {
		// mainSplit.setLeftComponent(new JPanel());
		// mainSplit.setDividerLocation(0);
		// }
		mainSplit.setRightComponent(networkDisplay);
		// split.setRightComponent(fpanel);
		mainSplit.setOneTouchExpandable(true);
		mainSplit.setContinuousLayout(true);

		mainSplit.setBorder(null);
		mainSplit.setMinimumSize(new Dimension(10, 10));

		JXTitledPanel rightSide = new JXTitledPanel();
		rightSide.setTitle("SocialAction");
		rightSide.setBorder(BorderFactory.createLoweredBevelBorder());
		// rightSide.setLayout(new BorderLayout());

		rightSide.add(mainSplit);

		JSplitPane SYFSplit = new JSplitPane();
		if (m_isSYFMode) {

			SYFSplit.setLeftComponent(syfOverviewPanel);
			SYFSplit.setRightComponent(rightSide);
			SYFSplit.setOneTouchExpandable(true);
			SYFSplit.setContinuousLayout(false);
			SYFSplit.setDividerLocation(SYF_WIDTH);
			SYFSplit.setBorder(null);
		}

		// fpanel.add(Box.createVerticalGlue());
		this.setLayout(new BorderLayout());

		if (/* !m_isAppletMode && */!m_isSYFMode) {
			add(mainToolBar, BorderLayout.NORTH);
		}
		if (m_isSYFMode) {
			add(SYFSplit, BorderLayout.CENTER);
		} else {
			add(mainSplit, BorderLayout.CENTER);
		}
		// if (m_isAppletMode)
		// this.setPreferredSize(new Dimension(800, 500));
		// else
		this.setPreferredSize(new Dimension(1000, 700));

		// if (!m_isAppletMode) {
		setupGraph(g, label);
		// } else {
		// matrixAction();
		// JPanel appletPanel = new JPanel();
		// // appletPanel.setLayout(new BorderLayout());
		//
		// appletPanel.setLayout(new BoxLayout(appletPanel, BoxLayout.X_AXIS));
		//
		// m_appletLabel = new JLabel("     ");
		// appletPanel.add(m_appletLabel);
		//
		// JRangeSlider slider = edge1DPanel.getSlider();
		// slider.setPreferredSize(new Dimension(1000, 50));
		// appletPanel.add(slider);
		// ImageIcon icon = new ImageIcon(SocialAction.class.getResource("/hcil-logo-tiny.gif"));
		// JLabel imagePanel = new JLabel(icon);
		//
		// JLabel blankSpace = new JLabel("    ");
		// appletPanel.add(blankSpace);
		// appletPanel.add(imagePanel);
		//
		// icon = new ImageIcon(SocialAction.class.getResource("/umd-logo.gif"));
		// imagePanel = new JLabel(icon);
		//
		// blankSpace = new JLabel("    ");
		// appletPanel.add(blankSpace);
		// appletPanel.add(imagePanel);
		//
		// add(appletPanel, BorderLayout.SOUTH);
		// }

		// now we run our action list

		m_vis.run("draw");

		// m_linlog.run();
		layoutGraph.run();

		_instance = this;
	}

	public void exportVisibleNetwork() {

		Table nodetable = new Table();
		Table edgetable = new Table();

		Iterator i = m_vis.getFocusGroup(RANGE_NODE_SLIDER_GROUP).tuples();
		int cnt = 0;
		while (i.hasNext()) {
			Tuple t = (Tuple) i.next();

			if (cnt == 0) {
				nodetable.addColumns(t.getSchema());
				nodetable.addTuple(t); // add extra row so it gets ignored by import
				nodetable.addTuple(t);
			} else {
				nodetable.addTuple(t);
			}
			cnt++;

			System.out.println(cnt);
		}

		i = m_vis.getFocusGroup(RANGE_EDGE_SLIDER_GROUP).tuples();
		cnt = 0;

		while (i.hasNext()) {
			Tuple t = (Tuple) i.next();

			if (cnt == 0) {
				edgetable.addColumns(t.getSchema());
				edgetable.addTuple(t);
				edgetable.addTuple(t);
			} else {
				edgetable.addTuple(t);
			}
			cnt++;

			System.out.println(cnt);
		}

		saveVisualTables(nodetable, edgetable, null);

	}

	public void addToNodeRangeSliderSet(TupleSet set) {

		Iterator it = set.tuples();
		while (it.hasNext()) {
			Tuple tuple = (Tuple) it.next();
			addToNodeRangeSliderSet(tuple);
		}

	}

	public void addToNodeRangeSliderSet(Tuple t) {
		m_vis.getFocusGroup(RANGE_NODE_SLIDER_GROUP).addTuple(t);
	}

	public void clearNodeRangeSliderSet() {

		m_vis.getFocusGroup(RANGE_NODE_SLIDER_GROUP).clear();
	}

	public int getNodeRangeSliderSetSize() {
		return m_vis.getFocusGroup(RANGE_NODE_SLIDER_GROUP).getTupleCount();
	}

	public void addToEdgeRangeSliderSet(TupleSet set) {

		Iterator it = set.tuples();
		while (it.hasNext()) {
			Tuple tuple = (Tuple) it.next();
			addToEdgeRangeSliderSet(tuple);
		}
	}

	public void addToEdgeRangeSliderSet(Tuple t) {
		m_vis.getFocusGroup(RANGE_EDGE_SLIDER_GROUP).addTuple(t);
	}

	public void clearEdgeRangeSliderSet() {
		m_vis.getFocusGroup(RANGE_EDGE_SLIDER_GROUP).clear();
	}

	public int getEdgeRangeSliderSetSize() {
		return m_vis.getFocusGroup(RANGE_EDGE_SLIDER_GROUP).getTupleCount();
	}

	private int labelSize = 1000, fontSize = 10, edgeSize = 1, layoutValue = 1;

	public int getLabelSize() {
		return labelSize;
	}

	public void setLabelSize(int size) {
		this.labelSize = size;
	}

	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(int fontsize) {
		this.fontSize = fontsize;
	}

	/**
	 * Set the global spring coefficient for force directed layouts
	 * 
	 * @param idx
	 *            the index into LAYOUT_VALUES for the global spring coefficient
	 */
	public void setGlobalSpringCoefficient(int idx) {
		this.layoutValue = idx;
		if (forceDirectedLayout instanceof ForceDirectedLayout) {
			ForceDirectedLayout fdl = (ForceDirectedLayout) forceDirectedLayout;
			ForceSimulator fsim = fdl.getForceSimulator();
			for (int i = 0; i < fsim.getForces().length; i++) {
				Force force = fsim.getForces()[i];
				if (force instanceof SpringForce) {
					SpringForce nbodyForce = (SpringForce) force;
					nbodyForce.setParameter(0, LAYOUT_VALUES[idx]);
					if (fdl instanceof SocNetLayout) {
						((SocNetLayout) fdl).setEXTRACOMM(LAYOUT_VALUES_COMMUNITY[idx]);
					}
				}
			}
		}
	}

	public int getGlobalSpringCoefficient() {
		return layoutValue;
	}

	public int getEdgeSize() {
		return edgeSize;
	}

	public void setEdgeSize(int edgesize) {
		this.edgeSize = edgesize;
		edgeRenderer.setDefaultLineWidth(edgeSize);
	}

	public void setHighQuality(boolean quality) {
		display.setHighQuality(quality);
	}

	public boolean getHighQuality() {
		return display.isHighQuality();
	}

	public Visualization getVisualization() {
		return m_vis;
	}

	private boolean m_isCurvedEdges = false;

	public boolean getCurvedEdges() {
		return m_isCurvedEdges;
	}

	public void setCurvedEdges(boolean isCurve) {

		if (isCurve) {
			edgeRenderer.setEdgeType(Constants.EDGE_TYPE_CURVE);
		} else
			edgeRenderer.setEdgeType(Constants.EDGE_TYPE_LINE);

		m_isCurvedEdges = isCurve;
	}

	public boolean getLayoutFilteredEdges() {
		if (forceDirectedLayout instanceof SocNetLayout) {
			return ((SocNetLayout) forceDirectedLayout).getLayoutFilteredEdges();
		} else {
			return false;
		}
	}

	public boolean getLayoutFilteredNodes() {
		if (forceDirectedLayout instanceof SocNetLayout) {
			return ((SocNetLayout) forceDirectedLayout).getLayoutFilteredNodes();
		} else {
			return false;
		}
	}

	public void setLayoutFilteredNodes(boolean layout) {
		if (forceDirectedLayout instanceof SocNetLayout) {
			((SocNetLayout) forceDirectedLayout).setLayoutFilteredNodes(layout);
		}
	}

	public void setLayoutFilteredEdges(boolean layout) {
		if (forceDirectedLayout instanceof SocNetLayout) {
			((SocNetLayout) forceDirectedLayout).setLayoutFilteredEdges(layout);
		}
	}

	public boolean isArrowRendered() {
		return edgeRenderer.isArrowRendered();
	}

	public void setArrowRendered(boolean value) {
		edgeRenderer.setArrowRendered(value);
	}

	public boolean isConnectorMarkerRendered() {
		return edgeRenderer.isConnectorMarkerRendered();
	}

	public void setConnectorMarkerRendered(boolean value) {
		edgeRenderer.setConnectorMarkerRendered(value);
	}

	public void setNodeRenderProperties(int renderType, boolean value) {
		switch (renderType) {
		case 1:
			tr.setShowText(value);
			break;
		case 2:
			tr.setShowPictures(value);
			break;
		case 3:
			tr.setShowFiltered(value);
			break;
		case 4:
			tr.setShowHighlight(value);
			break;
		default:
			return;

		}
	}

	public boolean getNodeRenderProperties(int renderType) {
		switch (renderType) {
		case 1:
			return tr.getShowText();
		case 2:
			return tr.getShowPictures();
		case 3:
			return tr.getShowFiltered();
		case 4:
			return tr.getShowHighlight();
		default:
			System.err.println("INVALID NUMBER FOR NODE RENDER");
			return false;

		}
	}

	// TODO set to false in final?
	private boolean realTimeGraphReadabilityMetrics = false;

	public boolean getRealTimeGraphReadabilityMetrics() {
		return realTimeGraphReadabilityMetrics;
	}

	public void setRealTimeGraphReadabilityMetrics(boolean realTime) {
		realTimeGraphReadabilityMetrics = realTime;
		dragReadabilityControl.setEnabled(realTime);
	}

	public RangeQueryBinding getRangeBinding() {
		return m_rangeQ;
	}

	/**
	 * The range filter binding for the node table
	 * 
	 * @param columnName
	 *            The column being filtered
	 */
	public void setNodeRangeBinding(String columnName) {
		m_rangeQ = new RangeQueryBinding(m_graph.getNodeTable(), columnName);
	}

	/**
	 * The range filter binding for the edge table
	 * 
	 * @param columnName
	 *            The column being filtered
	 */
	public void setEdgeRangeBinding(String columnName) {
		m_rangeQ = new RangeQueryBinding(m_graph.getEdgeTable(), columnName);
	}

	public void constructCommunities(int idx) {
		commConstruct.setIndex(idx);
		communityAction.run();

		// community.runNow();

	}

	public SubgraphSet getSubgraphs() {
		return (SubgraphSet) m_vis.getFocusGroup(community);

	}

	public int getSubgraphCount() {
		return ((SubgraphSet) m_vis.getFocusGroup(community)).getCommunityCount();

	}

	public void setSelectedCommunity(int community) {

		for (int i = 0; i < aggregateTable.getRowCount(); i++) {

			if (community == aggregateTable.getInt(i, AGGREGATE_ID_COLUMN_NAME)) {

				TupleSet focusGroup = getVisualization().getFocusGroup(Visualization.FOCUS_ITEMS);
				focusGroup.clear();

				Iterator<VisualItem> it = (Iterator<VisualItem>)aggregateTable.aggregatedTuples(i);
				while (it.hasNext()) {
					VisualItem vItem = it.next();
					focusGroup.addTuple(vItem);
				}

			}

		}
		getVisualization().run("draw");

	}

	// UI Methods

	protected JToolBar addMainToolBarButtons() {

		mainToolBar = new JToolBar("Main ToolBar");

		// java.net.URL imageURL = ;

		importDataButton = new JButton("Import Data", new ImageIcon(SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/table_save.png")));
		importDataButton.addActionListener(this);
		// importDataButton.setSelected(true);
		// mainToolBar.add(importDataButton);

		// mainToolBar.addSeparator();
		// mainToolBar.addSeparator();

		overviewButton = new JButton("Overview", new ImageIcon(SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/table.png")));
		overviewButton.addActionListener(this);
		overviewButton.setSelected(true);
		mainToolBar.add(overviewButton);

		mainToolBar.addSeparator();

		rankNodesButton = new JButton("Rank Nodes", new ImageIcon(SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/cog.png")));
		rankNodesButton.addActionListener(this);
		mainToolBar.add(rankNodesButton);
		mainToolBar.addSeparator();

		nodeAttributesButton = new JButton("Attribute Nodes", new ImageIcon(SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/cog.png")));
		nodeAttributesButton.addActionListener(this);
		mainToolBar.add(nodeAttributesButton);
		mainToolBar.addSeparator();

		rankEdgesButton = new JButton("Rank Edges", new ImageIcon(SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/link.png")));
		// rankEdgesButton.setEnabled(false);
		rankEdgesButton.addActionListener(this);
		mainToolBar.add(rankEdgesButton);
		mainToolBar.addSeparator();

		plotNodesButton = new JButton("Plot Nodes", new ImageIcon(SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/chart_line.png")));
		plotNodesButton.addActionListener(this);
		mainToolBar.add(plotNodesButton);
		mainToolBar.addSeparator();

		communitiesButton = new JButton(COMMUNITY_PANEL,
				new ImageIcon(SocialAction.class.getResource(
                                SocialAction.IMAGE_BASE + "/chart_pie.png")));
		communitiesButton.addActionListener(this);
		mainToolBar.add(communitiesButton);
		mainToolBar.addSeparator();

		multiplexButton = new JButton("Edge Types", new ImageIcon(SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/link_break.png")));
		multiplexButton.addActionListener(this);
		mainToolBar.add(multiplexButton);
		mainToolBar.addSeparator();

		readabilityButton = new JButton("Graph Readability", new ImageIcon(SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/cog.png")));
		readabilityButton.addActionListener(this);
		mainToolBar.add(readabilityButton);

		mainToolBar.addSeparator();
		mainToolBar.addSeparator();

		detailsButton = new JButton("Details", new ImageIcon(SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/report.png")));
		detailsButton.addActionListener(this);
		mainToolBar.add(detailsButton);
		mainToolBar.addSeparator();
		mainToolBar.addSeparator();

		settingsButton = new JButton("Settings", new ImageIcon(SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/color_wheel.png")));
		settingsButton.addActionListener(this);
		mainToolBar.add(settingsButton);

		mainToolBar.setRollover(false);

		return mainToolBar;
	}

	protected JToolBar addNetworkVisToolBarButtons() {

		networkVisToolBar = new JToolBar("NetworkViz Toolbar");
		// toolBar.setBackground(Color.white);

		JLabel label = new JLabel("Layout:");
		label.setFont(FontLib.getFont("Tahoma", Font.ITALIC, 11));
		networkVisToolBar.add(label);

		// TODO How big do we want? SocNetLayout (2 vals)
		JSlider layoutSlider = new JSlider(0, 4, getGlobalSpringCoefficient());
		layoutSlider.setMaximumSize(new Dimension(40, 30));
		layoutSlider.setPreferredSize(new Dimension(40, 30));
		layoutSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setGlobalSpringCoefficient(((JSlider) e.getSource()).getValue());
			}
		});
		layoutSlider.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				addEventToHistory(DisplaySettingsPanel.DISPLAY_STEP_NUMBER,
						DisplaySettingsPanel.DISPLAY_NETWORKVIS_STATE, DisplaySettingsPanel.DISPLAY_FORCE_SETTING,
						((JSlider) e.getSource()).getValue());
			}
		});

		// Hashtable labelTable = new Hashtable();
		// labelTable.put( new Integer( 0 ), new JLabel("Sparse") );
		// labelTable.put( new Integer( 1 ), new JLabel("") );
		// labelTable.put( new Integer( 2 ), new JLabel("Dense") );
		//        
		// layoutSlider.setLabelTable( labelTable );

		layoutSlider.setMajorTickSpacing(1);
		// layoutSlider.setMinorTickSpacing(1);
		layoutSlider.setPaintTicks(true);
		layoutSlider.setSnapToTicks(true);
		// layoutSlider.setPaintLabels(true);

		networkVisToolBar.add(layoutSlider);

		label = new JLabel("Font Size:");
		label.setFont(FontLib.getFont("Tahoma", Font.ITALIC, 11));
		networkVisToolBar.add(label);

		JSlider fontSlider = new JSlider(4, 150, getFontSize());// Set to min of 4 for zoomed in examples
		fontSlider.setMaximumSize(new Dimension(40, 30));
                fontSlider.setPreferredSize(new Dimension(40, 30));
		fontSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setFontSize(((JSlider) e.getSource()).getValue());

				if (animateLayoutFalseButton.isSelected())
					getVisualization().run("draw");
			}
		});

		fontSlider.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				addEventToHistory(DisplaySettingsPanel.DISPLAY_STEP_NUMBER,
						DisplaySettingsPanel.DISPLAY_NETWORKVIS_STATE, DisplaySettingsPanel.DISPLAY_FONT_SETTING,
						((JSlider) e.getSource()).getValue());
			}
		});

		networkVisToolBar.add(fontSlider);

		label = new JLabel("Edge Size:");
		label.setFont(FontLib.getFont("Tahoma", Font.ITALIC, 11));
		networkVisToolBar.add(label);

		JSlider edgeSlider = new JSlider(1, 50, getEdgeSize());
		edgeSlider.setMaximumSize(new Dimension(40, 30));
                edgeSlider.setPreferredSize(new Dimension(40, 30));
		edgeSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {

				setEdgeSize(((JSlider) e.getSource()).getValue());

				if (animateLayoutFalseButton.isSelected())
					getVisualization().run("draw");
			}
		});
		edgeSlider.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {

				addEventToHistory(DisplaySettingsPanel.DISPLAY_STEP_NUMBER,
						DisplaySettingsPanel.DISPLAY_NETWORKVIS_STATE, DisplaySettingsPanel.DISPLAY_EDGE_SETTING,
						((JSlider) e.getSource()).getValue());
			}
		});

		networkVisToolBar.add(edgeSlider);

		label = new JLabel("Label Size:");
		label.setFont(FontLib.getFont("Tahoma", Font.ITALIC, 11));
		networkVisToolBar.add(label);

		JSlider labelSlider = new JSlider(1, 500, getFontSize());
		labelSlider.setMaximumSize(new Dimension(40, 30));
                labelSlider.setPreferredSize(new Dimension(40, 30));
		labelSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {

				setLabelSize(((JSlider) e.getSource()).getValue());
				if (animateLayoutFalseButton.isSelected())
					getVisualization().run("draw");

			}
		});
		labelSlider.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				addEventToHistory(DisplaySettingsPanel.DISPLAY_STEP_NUMBER,
						DisplaySettingsPanel.DISPLAY_NETWORKVIS_STATE, DisplaySettingsPanel.DISPLAY_LABEL_SETTING,
						((JSlider) e.getSource()).getValue());
			}
		});

		networkVisToolBar.add(labelSlider);

		java.net.URL imageURL = SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/page_white_text_width.png");

		wordWrapButton = new JButton(new ImageIcon(imageURL));
		wordWrapButton.addActionListener(this);
		networkVisToolBar.add(wordWrapButton);

		networkVisToolBar.addSeparator();
		networkVisToolBar.addSeparator();

		imageURL = SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/door.png");
		ImageIcon doorClosedIcon = new ImageIcon(imageURL);
		imageURL = SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/door_open.png");
		ImageIcon doorOpenIcon = new ImageIcon(imageURL);

		enforceBoundsTrueButton = new JButton(doorClosedIcon);
		enforceBoundsTrueButton.addActionListener(this);
		enforceBoundsTrueButton.setSelected(true);
		enforceBoundsFalseButton = new JButton(doorOpenIcon);
		enforceBoundsFalseButton.addActionListener(this);
		enforceBoundsFalseButton.setSelected(false);

		networkVisToolBar.add(enforceBoundsTrueButton);
		networkVisToolBar.add(enforceBoundsFalseButton);

		networkVisToolBar.addSeparator();
		networkVisToolBar.addSeparator();

		imageURL = SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/control_play.png");
		ImageIcon playIcon = new ImageIcon(imageURL);
		imageURL = SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/control_pause.png");
		ImageIcon pauseIcon = new ImageIcon(imageURL);

		animateLayoutTrueButton = new JButton(playIcon);
		animateLayoutTrueButton.addActionListener(this);
		animateLayoutTrueButton.setSelected(true);

		animateLayoutFalseButton = new JButton(pauseIcon);
		animateLayoutFalseButton.addActionListener(this);

		networkVisToolBar.add(animateLayoutTrueButton);
		networkVisToolBar.add(animateLayoutFalseButton);

		networkVisToolBar.addSeparator();
		networkVisToolBar.addSeparator();

		layoutButtons = new JButton[5];
		// layoutButtons[0] = new JButton("Fruchterman-Reingold");
		layoutButtons[1] = new JButton("ForceDir");
		layoutButtons[1].setToolTipText("Force-directed layout");
		layoutButtons[2] = new JButton("SocNet");
		layoutButtons[1].setToolTipText("SocialNetwork layout");
		layoutButtons[3] = new JButton("MidiComm");
		layoutButtons[1].setToolTipText("Midichloarian community layout");
		layoutButtons[4] = new JButton("MidiCommNbr");
		layoutButtons[1].setToolTipText("Midichloarian community with neighbors layout");

		for (JButton jb : layoutButtons) {
			if (jb != null) {
				if (jb.getText().equals("Midichlorian-Comm")) {
					networkVisToolBar.addSeparator();
				}
				jb.addActionListener(this);
				networkVisToolBar.add(jb);
			}
		}
		layoutButtons[2].setSelected(true);

		mcNodeAttribButtons = new JButton[2];
		mcNodeAttribButtons[0] = new JButton("Node", new ImageIcon(SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/link.png")));
		mcNodeAttribButtons[1] = new JButton("Node", new ImageIcon(SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/link_break.png")));

		for (JButton jb : mcNodeAttribButtons) {
			if (jb != null) {
				jb.addActionListener(this);
				networkVisToolBar.add(jb);
			}
		}
		mcNodeAttribButtons[1].setSelected(true);

		mcEdgeAttribButtons = new JButton[2];
		mcEdgeAttribButtons[0] = new JButton("Edge", new ImageIcon(SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/link.png")));
		mcEdgeAttribButtons[1] = new JButton("Edge", new ImageIcon(SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/link_break.png")));

		for (JButton jb : mcEdgeAttribButtons) {
			if (jb != null) {
				jb.addActionListener(this);
				networkVisToolBar.add(jb);
			}
		}
		mcEdgeAttribButtons[1].setSelected(true);

		// toolBar.add(new SYFProgressPanel());

		// second button
		// button = makeNavigationButton("Up24", UP,
		// "Up to something-or-other",
		// "Up");
		// toolBar.add(button);
		//
		// //third button
		// button = makeNavigationButton("Forward24", NEXT,
		// "Forward to something-or-other",
		// "Next");
		// toolBar.add(button);

		// separator
		networkVisToolBar.addSeparator();
		networkVisToolBar.addSeparator();

		imageURL = SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/table_save.png");
		ImageIcon saveIcon = new ImageIcon(imageURL);

		saveData = new JButton(saveIcon);
		saveData.addActionListener(this);
		saveData.setSelected(false);
		networkVisToolBar.add(saveData);

		// fourth button
		// button = new JButton("Another button");
		// // button.setActionCommand(SOMETHING_ELSE);
		// button.setToolTipText("Something else");
		// // button.addActionListener(this);
		// toolBar.add(button);
		//
		// // fifth component is NOT a button!
		// JTextField textField = new JTextField("A text field");
		// textField.setColumns(10);
		// // textField.addActionListener(this);
		// // textField.setActionCommand(TEXT_ENTERED);
		// toolBar.add(textField);

		networkVisToolBar.setFloatable(true);
		networkVisToolBar.setRollover(true);

		return networkVisToolBar;

	}

	// protected JButton makeNavigationButton(String imageName,
	// String actionCommand,
	// String toolTipText,
	// String altText) {
	// //Look for the image.
	// String imgLocation = "toolbarButtonGraphics/navigation/"
	// + imageName
	// + ".gif";
	// URL imageURL = ToolBarDemo2.class.getResource(imgLocation);
	//
	// //Create and initialize the button.
	// JButton button = new JButton();
	// button.setActionCommand(actionCommand);
	// button.setToolTipText(toolTipText);
	// button.addActionListener(this);
	//
	// if (imageURL != null) { //image found
	// button.setIcon(new ImageIcon(imageURL, altText));
	// } else { //no image found
	// button.setText(altText);
	// System.err.println("Resource not found: "
	// + imgLocation);
	// }
	//
	// return button;
	// }

	int TABSIZE_X = 300;

	int TABSIZE_Y = 500;

	protected JPanel getTabbedControls() {

		final JPanel tabbedControlPanel = new JPanel(new CardLayout()); // new JTabbedPane();

		tabbedControlPanel.setMinimumSize(new Dimension(0, 0));
		tabbedControlPanel.setPreferredSize(new Dimension(TABSIZE_X + 20, TABSIZE_Y + 20));

		commPanel = new CommunityPanel(this, TABSIZE_X);
		commPanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		commPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		node1DPanel = new Node1DPanel(this, m_graph);
		// rank1DPanel.setPreferredSize(new Dimension(TABSIZE_X, TABSIZE_Y));
		node1DPanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		node1DPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		nodeAttributePanel = new NodeAttributes1DPanel(this, m_graph);
		// rank1DPanel.setPreferredSize(new Dimension(TABSIZE_X, TABSIZE_Y));
		nodeAttributePanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		nodeAttributePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		edge1DPanel = new Edge1DPanel(this, m_graph);
		// rank1DPanel.setPreferredSize(new Dimension(TABSIZE_X, TABSIZE_Y));
		edge1DPanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		edge1DPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		/*
		 * rank2DPanel = new Rank2DPanel(this); rank2DPanel.setPreferredSize(new Dimension(TABSIZE_X, TABSIZE_Y));
		 * rank2DPanel.setAlignmentY(Component.BOTTOM_ALIGNMENT); rank2DPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		 */

		graphStatsPanel = new NetworkOverviewPanel(this, m_graph, TABSIZE_X);
		graphStatsPanel.setPreferredSize(new Dimension(TABSIZE_X, 400)); // graphStatsPanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);

		graphStatsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		historyPanel = new SYFHistoryPanel(this, TABSIZE_X);
		historyPanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		historyPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		annotationPanel = new SYFAnnotationPanel(this, TABSIZE_X);
		annotationPanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		annotationPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		multiplexPanel = new MultiplexPanel(this, TABSIZE_X);
		multiplexPanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		multiplexPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		detailsPanel = new DetailsOnDemandPanel(this, TABSIZE_X);
		detailsPanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		detailsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		ImportDataPanel importDataPanel = new ImportDataPanel(TABSIZE_X);
		importDataPanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		importDataPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		readabilityPanel = new GraphReadabilityPanel(this, TABSIZE_X, graph);
		readabilityPanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		readabilityPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		displayPanel = new DisplaySettingsPanel(this, m_graph, TABSIZE_X);
		displayPanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		displayPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		rank2DPanel = new Rank2DScatterplotPanel(this, m_graph, TABSIZE_X);
		rank2DPanel.setPreferredSize(new Dimension(TABSIZE_X, TABSIZE_Y));
		rank2DPanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		rank2DPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		tabbedControlPanel.add(graphStatsPanel, OVERVIEW_PANEL);
		tabbedControlPanel.add(node1DPanel, NODE_1D_PANEL);
		tabbedControlPanel.add(nodeAttributePanel, NODE_ATTRIBUTE_1D_PANEL);
		tabbedControlPanel.add(edge1DPanel, EDGE_1D_PANEL);

		// tabbedControlPanel.add(new JPanel(), EDGE_1D_PANEL);

		tabbedControlPanel.add(rank2DPanel, NODE_2D_PANEL);
		tabbedControlPanel.add(new JPanel(), EDGE_2D_PANEL);
		// tabbedControlPanel.addTab("Filter", filterPanel);

		tabbedControlPanel.add(commPanel, COMMUNITY_PANEL);
		tabbedControlPanel.add(multiplexPanel, MULTIPLEX_PANEL);
		tabbedControlPanel.add(historyPanel, HISTORY_PANEL);
		tabbedControlPanel.add(annotationPanel, ANNOTATION_PANEL);
		tabbedControlPanel.add(displayPanel, DISPLAY_PANEL);
		tabbedControlPanel.add(detailsPanel, DETAILS_PANEL);
		tabbedControlPanel.add(importDataPanel, IMPORT_DATA_PANEL);
		tabbedControlPanel.add(readabilityPanel, READABILITY_PANEL);

		return tabbedControlPanel;
	}

	public void setDetailsPanel(VisualItem item) {
		detailsPanel.setContent(item);
	}

	public void setReadabilityPanel(VisualItem item) {
		readabilityPanel.setContent(item);
	}

	public GraphReadabilityPanel getReadabilityPanel() {
		return readabilityPanel;
	}

	public SocialActionNeighborControl getNeighborControl() {
		return neighborControl;
	}

	public Node1DPanel getNodeRank1D() {
		return node1DPanel;
	}

	public NodeAttributes1DPanel getNodeAttributes1D() {
		return nodeAttributePanel;
	}

	public Edge1DPanel getEdgeRank1D() {
		return edge1DPanel;
	}

	public int[] getColorPalette() {
		return colorSpectrum;
	}

	public int[] getAggregateColorPalette() {
		return aggregateColorPalette;

	}

	SubgraphSet m_currentAggregateSet = null;

	// public void setCurrentAggregateSet(SubgraphSet community) {
	// m_currentAggregateSet = community;
	// commPanel.setListener(community);
	//
	// }
	//
	// public SubgraphSet getCurrentAggregateSet() {
	// return m_currentAggregateSet;
	//
	// }

	public void updateAggregateColorPalette() {
		updateAggregateColorPalette(m_currentAggregateSet);
	}

	public void updateAggregateColorPalette(SubgraphSet community) {

		if (community == null || community.getTupleCount() == 0) {
			// communityColor = null;
			// fadedCommColor = null;
		} else {
			int maxc = community.getCommunityCount();

			// ColorMap cmap = (item.isHighlighted() ? communityColor : fadedCommColor);
			// int idx = communitySet.getCommunity((Node) item.getEntity());

			aggregateColorPalette = new int[maxc];
			aggregateLabels = new String[maxc];

			for (int idx = 0; idx < maxc; idx++) {

				Set memberNodes = community.getCommunityMembers(idx);

				if (memberNodes == null) {
					aggregateColorPalette[idx] = 0;
					continue;
				}

				Iterator memberNodesIterator = memberNodes.iterator();
				// double sumRanks = 0;
				int numberNodes = 0;
				double maxRank = 0;
				double avgRankCount = 0;

				VisualItem maxRankItem = null;

				while (memberNodesIterator.hasNext()) {

					VisualItem vItem = (VisualItem) memberNodesIterator.next();

					double rank = Double.parseDouble(vItem.getString(getNodeColorField()));
					avgRankCount += rank;
					if (rank > maxRank) {
						maxRank = rank;
						maxRankItem = vItem;
					}
					numberNodes++;
				}

				String label = new Integer((int) avgRankCount).toString();

				int color = 0;
				double averageRank = (avgRankCount / (1.0 * numberNodes));
				Table nodes = new Table();
				nodes.addColumn(getNodeColorField(), double.class);

				int newRow = nodes.addRow();
				nodes.setDouble(newRow, getNodeColorField(), averageRank);

				VisualTable tmpTable = new VisualTable(nodes, getVisualization(), "temp");
				VisualItem tmpItem = (VisualItem) tmpTable.getTuple(newRow);

				// AggregateTable m_at = (AggregateTable) m_vis.getGroup(SocialAction.communityAggr);

				color = nodeFillColorAction.getColor(tmpItem);

				if (maxRankItem != null) {
					color = maxRankItem.getFillColor();
					if (maxRankItem.isHighlighted()) {
						maxRankItem.setHighlighted(false);
						getVisualization().run("draw");
						color = maxRankItem.getFillColor();
						// maxRankItem.setHighlighted(true);
						System.err.println("SocialAction: Hack to get around Aggregate Highlighting Problems");
					}
				}
				Color c = ColorLib.getColor(color);
				Color fadedColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), 150);

				aggregateLabels[idx] = label;
				aggregateColorPalette[idx] = ColorLib.color(fadedColor);

			}
		}

		aggregateFillDataColorAction.setPalette(aggregateColorPalette);
	}

	private void setGraph(Graph g, String label) {

		// update labeling
		DefaultRendererFactory drf = (DefaultRendererFactory) m_vis.getRendererFactory();
		// ((LabelRenderer) drf.getDefaultRenderer()).setTextField(label);
		((SelectedLabelRenderer) drf.getDefaultRenderer()).setTextField(label);
		selectedLabelField = label;

		// update graph

		m_vis.removeGroup(graph);

		m_vis.addGraph(graph, g);

		m_vis.setValue(edges, null, VisualItem.INTERACTIVE, Boolean.FALSE);
		// VisualItem f = (VisualItem) vg.getNode(0);
		// m_vis.getGroup(Visualization.FOCUS_ITEMS).setTuple(f);
		// f.setFixed(false);

		// initDataGroups();
	}

	public void importBipartiteTable(Table bipartiteTable, String partitionA, String partitionB, String multiplexField,
			String edgeWeightField, boolean isDirected) {

		layoutGraph.cancel();
		m_vis.cancel("draw");

		// clear edges

		ActionList createBipartiteGraph = new ActionList();

		createBipartiteGraph.add(new GraphCreatorAction(this, m_vis, bipartiteTable, partitionA, partitionB,
				multiplexField, edgeWeightField, isDirected));

		m_vis.putAction("bipartite", createBipartiteGraph);
		m_vis.run("bipartite");// , 5000);

		// updatePanels(true);

		m_vis.runAfter("bipartite", "draw");
		m_vis.runAfter("draw", "layout");
		m_vis.run("draw");
		layoutGraph.run();

		threadAvailable = false;
		updateGraph(null, true);
	}

	public void importMatrix(Table matrixTable) {// , String partitionA, String partitionB, String multiplexField) {

		layoutGraph.cancel();
		m_vis.cancel("draw");

		// clear edges

		ActionList createBipartiteGraph = new ActionList();

		createBipartiteGraph.add(new GraphCreatorAction(this, m_vis, matrixTable));// , partitionA, partitionB,
		// multiplexField));

		m_vis.putAction("bipartite", createBipartiteGraph);
		m_vis.run("bipartite");// , 5000);

		// updatePanels(true);

		m_vis.runAfter("bipartite", "draw");
		m_vis.runAfter("draw", "layout");
		m_vis.run("draw");
		layoutGraph.run();

		threadAvailable = false;
		updateGraph(null, true);
	}

	public void importSenateTest(Table senateTable) {// , String partitionA, String partitionB, String
		// multiplexField) {

		layoutGraph.cancel();
		m_vis.cancel("draw");

		// clear edges

		ActionList createBipartiteGraph = new ActionList();

		createBipartiteGraph.add(new GraphCreatorAction(this, m_vis, senateTable, false, true));// , partitionA,
		// partitionB,
		// multiplexField));

		m_vis.putAction("bipartite", createBipartiteGraph);
		m_vis.run("bipartite");// , 5000);

		// updatePanels(true);

		m_vis.runAfter("bipartite", "draw");
		m_vis.runAfter("draw", "layout");
		m_vis.run("draw");
		layoutGraph.run();

		threadAvailable = false;
		updateGraph(null, true);
	}

	public void importSageman(Table sagemanTable) {// , String partitionA, String partitionB, String multiplexField) {

		layoutGraph.cancel();
		m_vis.cancel("draw");

		// clear edges

		ActionList createBipartiteGraph = new ActionList();

		createBipartiteGraph.add(new GraphCreatorAction(this, m_vis, sagemanTable, true));// , partitionA, partitionB,
		// multiplexField));

		m_vis.putAction("bipartite", createBipartiteGraph);
		m_vis.run("bipartite");// , 5000);

		// updatePanels(true);

		m_vis.runAfter("bipartite", "draw");
		m_vis.runAfter("draw", "layout");
		m_vis.run("draw");
		layoutGraph.run();

		threadAvailable = false;
		updateGraph(null, true);

	}

	public void importGraph(Graph graph, String labelField, String multiplexField) {

		layoutGraph.cancel();
		m_vis.cancel("draw");

		ActionList createGraph = new ActionList();
		createGraph.add(new GraphCreatorAction(this, m_vis, graph, labelField, multiplexField));

		m_vis.putAction("import", createGraph);
		m_vis.run("import");

		// clear edges

		// ActionList createBipartiteGraph = new ActionList();
		// createBipartiteGraph.add(new BipartiteGraphCreatorAction(this, m_vis, m_graph, bipartiteTable, partitionA,
		// partitionB, multiplexField));
		//
		// m_vis.putAction("bipartite", createBipartiteGraph);
		// m_vis.run("bipartite");

		updatePanels(/* true */);

		m_vis.runAfter("draw", "layout");
		m_vis.run("draw");
		layoutGraph.run();

		threadAvailable = false;
		updateGraph(null, true);

	}

	public void importGraphFromHCILFormat(Table nodeTable, Table edgeTable, String labelField, String nodeField,
			String edge1Field, String edge2Field, String multiplexField, String edgeWeightField, boolean isDirected) {

		layoutGraph.cancel();
		m_vis.cancel("draw");

		ActionList createGraph = new ActionList();
		createGraph.add(new GraphCreatorAction(this, m_vis, nodeTable, edgeTable, labelField, nodeField, edge1Field,
				edge2Field, multiplexField, edgeWeightField, isDirected));

		m_vis.putAction("import", createGraph);
		m_vis.run("import");

		// clear edges

		// ActionList createBipartiteGraph = new ActionList();
		// createBipartiteGraph.add(new BipartiteGraphCreatorAction(this, m_vis, m_graph, bipartiteTable, partitionA,
		// partitionB, multiplexField));
		//
		// m_vis.putAction("bipartite", createBipartiteGraph);
		// m_vis.run("bipartite");

		updatePanels(/* true */);

		m_vis.runAfter("draw", "layout");
		m_vis.run("draw");
		layoutGraph.run();
		// m_linlog.run();

		threadAvailable = false;
		updateGraph(null, true);
	}

	private String mcNodeJoinAttrib = null;
	private String mcEdgeJoinAttrib = null;

	private String selectedLabelField = null;

	public void setLabelField(String label) {
		if ((label != null) && (label != selectedLabelField)) {
			selectedLabelField = label;
			DefaultRendererFactory drf = (DefaultRendererFactory) m_vis.getRendererFactory();
			((SelectedLabelRenderer) drf.getDefaultRenderer()).setTextField(selectedLabelField);
		}

		if (m_isWordWrap) {
			Iterator i = m_graph.nodes();
			while (i.hasNext()) {
				Node n = (Node) i.next();
				String nodeLabel = n.getString(selectedLabelField);
				if (nodeLabel != null) {
					// System.out.println(nodeLabel);

					String newLabel = StringW.wordWrap(nodeLabel, 15);// , "X",
					// "Y");
					n.setString(selectedLabelField, newLabel);
				}
			}
		}
	}

	public String getLabelField() {
		return selectedLabelField;
	}

	private String selectedClassField = null;

	public void setClassField(String classField) {
		if ((classField != null) && (classField != selectedClassField)) {
			selectedClassField = classField;
		}
	}

	public String getClassField() {
		return selectedClassField;
	}

	private String selectedEdgeWeightField = null;// TODO used to be "weight"

	public void setEdgeWeightField(String weightField) {
		if ((weightField != null) && (weightField != selectedEdgeWeightField)) {
			selectedEdgeWeightField = weightField;
		}
	}

	public String getEdgeWeightField() {
		return selectedEdgeWeightField;
	}

	private String selectedImageField = null;

	public void setImageField(String label) {
		if ((label != null) && (label != selectedImageField)) {
			selectedImageField = label;
			DefaultRendererFactory drf = (DefaultRendererFactory) m_vis.getRendererFactory();
			((SelectedLabelRenderer) drf.getDefaultRenderer()).setImageField(selectedImageField);
			tr.setMaxImageDimensions(25, 25);
		}
	}

	public String getImageField() {
		return selectedImageField;
	}

	private String selectedKeyField = null;

	public void setKeyField(String label) {
		if ((label != null) && (label != selectedKeyField)) {
			selectedKeyField = label;

			System.err.println("*** Not doing anything for KEY yet!");
			// DefaultRendererFactory drf = (DefaultRendererFactory) m_vis.getRendererFactory();
			// ((LabelRenderer) drf.getDefaultRenderer()).setTextField(selectedKeyField);
		}
	}

	public String getKeyField() {
		return selectedKeyField;
	}

	/** The node field we are coloring by */
	private String selectedNodeColorField = null;

	/** The edge field we are coloring by */
	private String selectedEdgeColorField = null;

	/**
	 * Set node colors using an attribute of the node and choosing the coloring palette based on the data type.
	 * 
	 * @param label
	 *            The attribute of the node.
	 */
	public void setNodeColorField(String label) {
		setNodeColorField(label, Constants.UNKNOWN);
	}

	/**
	 * Set edge colors using an attribute of the edge and choosing the coloring palette based on the data type.
	 * 
	 * @param label
	 *            The attribute of the edge.
	 */
	public void setEdgeColorField(String label) {
		setEdgeColorField(label, Constants.UNKNOWN);
	}

	/**
	 * Set node colors using an attribute of the node and choosing the coloring palette based on the data type.
	 * 
	 * @param label
	 *            The attribute of the node.
	 * @param dataType
	 *            The data type of the variable. Options are prefuse.Constants.[UNKNOWN|NOMINAL|ORDINAL|NUMERICAL], for
	 *            unknown, nominal (categorical) data, ordinal (ordered) data, and numerical (quantitative) data.
	 */
	public void setNodeColorField(String label, int dataType) {
		if ((label != null) && (label != selectedNodeColorField)) {
			selectedNodeColorField = label;

			setNodeColor(dataType);
			System.err.println("Setting node color to: " + selectedNodeColorField);

		} else if (label == null) {
			nodeFillColorAction.setDataField(null);
			selectedNodeColorField = null;
		}
	}

	/**
	 * Set edge colors using an attribute of the edge and choosing the coloring palette based on the data type.
	 * 
	 * @param label
	 *            The attribute of the edge.
	 * @param dataType
	 *            The data type of the variable. Options are prefuse.Constants.[UNKNOWN|NOMINAL|ORDINAL|NUMERICAL], for
	 *            unknown, nominal (categorical) data, ordinal (ordered) data, and numerical (quantitative) data.
	 */
	public void setEdgeColorField(String label, int dataType) {
		if ((label != null) && (label != selectedEdgeColorField)) {
			selectedEdgeColorField = label;

			setEdgeColor(dataType);
			System.err.println("Setting edge color to: " + selectedEdgeColorField);

		} else if (label == null) {
			edgeFillColorAction.setDataField(null);
			selectedEdgeColorField = null;
		}
	}

	/**
	 * Set node colors using the selected color field and choosing the coloring palette based on the data type.
	 * 
	 * @param dataType
	 *            The data type of the variable. Options are prefuse.Constants.[UNKNOWN|NOMINAL|ORDINAL|NUMERICAL], for
	 *            unknown, nominal (categorical) data, ordinal (ordered) data, and numerical (quantitative) data.
	 * 
	 */
	private void setNodeColor(int dataType) {
		if (dataType == Constants.UNKNOWN)
			dataType = getNodeColorDataType();

		nodeFillColorAction.setDataType(dataType);
		nodeFillColorAction.setDataField(selectedNodeColorField);

		if (dataType == Constants.NOMINAL)
			nodeFillColorAction.setPalette(categoryPalette);
		else
			nodeFillColorAction.setPalette(colorSpectrum);

		updateAggregateColorPalette();
	}

	/**
	 * Set edge colors using the selected color field and choosing the coloring palette based on the data type.
	 * 
	 * @param dataType
	 *            The data type of the variable. Options are prefuse.Constants.[UNKNOWN|NOMINAL|ORDINAL|NUMERICAL], for
	 *            unknown, nominal (categorical) data, ordinal (ordered) data, and numerical (quantitative) data.
	 * 
	 */
	private void setEdgeColor(int dataType) {
		if (dataType == Constants.UNKNOWN)
			dataType = getEdgeColorDataType();
		edgeFillColorAction.setDataType(dataType);
		edgeFillColorAction.setDataField(selectedEdgeColorField);

		if (dataType == Constants.NOMINAL)
			edgeFillColorAction.setPalette(categoryPalette);
		else
			edgeFillColorAction.setPalette(colorSpectrum);
	}

	/**
	 * Get the data type of the selected node color attribute.
	 * 
	 * @return The data type of the variable. Options are prefuse.Constants.[UNKNOWN|NOMINAL|ORDINAL|NUMERICAL], for
	 *         unknown, nominal (categorical) data, ordinal (ordered) data, and numerical (quantitative) data.
	 */
	private int getNodeColorDataType() {
		// m_vis.cancel("draw");

		if (m_graph.getNodeTable().getColumnType(selectedNodeColorField) == double.class) {
			return Constants.NUMERICAL;
		} else if (m_graph.getNodeTable().getColumnType(selectedNodeColorField) == int.class) {
			return Constants.NUMERICAL;
		} else
			return Constants.NOMINAL;
	}

	/**
	 * Get the data type of the selected edge color attribute.
	 * 
	 * @return The data type of the variable. Options are prefuse.Constants.[UNKNOWN|NOMINAL|ORDINAL|NUMERICAL], for
	 *         unknown, nominal (categorical) data, ordinal (ordered) data, and numerical (quantitative) data.
	 */
	private int getEdgeColorDataType() {
		// m_vis.cancel("draw");

		if (m_graph.getEdgeTable().getColumnType(selectedEdgeColorField) == double.class) {
			return Constants.NUMERICAL;
		} else if (m_graph.getEdgeTable().getColumnType(selectedEdgeColorField) == int.class) {
			return Constants.NUMERICAL;
		} else
			return Constants.NOMINAL;
	}

	/**
	 * Get the selected node color attribute.
	 * 
	 * @return The attribute.
	 */
	public String getNodeColorField() {
		return selectedNodeColorField;
	}

	public String getNodeJoinAttribute() {
		return this.mcNodeJoinAttrib;
	}

	/**
	 * Get the selected edge color attribute.
	 * 
	 * @return The attribute.
	 */
	public String getEdgeColorField() {
		return selectedEdgeColorField;
	}

	private String selectedRelationshipField = "Friend";

	public void setRelationshipField(String relationship) {
		if ((relationship != null) && (relationship != selectedRelationshipField)) {
			selectedRelationshipField = relationship;

			setGraphRelationship();
		}
	}

	private void setGraphRelationship() {
		m_vis.cancel("draw");

		// clear edges

		ActionList setRelationships = new ActionList();
		setRelationships.add(new RelationshipConnectorAction(this, m_vis, m_graph, selectedRelationshipField));

		m_vis.putAction("relationship", setRelationships);
		m_vis.run("relationship");

		updatePanels(/* true */);

		m_vis.runAfter("draw", "layout");
		m_vis.run("draw");
	}

	/**
	 * Set the stroke color to white for all nodes in the VisualGraph
	 */
	public void updatePanels(/* boolean splitReady */) {
		VisualGraph g = (VisualGraph) m_vis.getVisualGroup(graph);

		for (Iterator i = g.nodes(); i.hasNext();) {
			TableNodeItem node = (TableNodeItem) i.next();
			// node.setStroke(new BasicStroke(2));
			// node.setFont(FontLib.getFont("Arial", 12));
			node.setStrokeColor(ColorLib.color(Color.WHITE));
		}

		// updateNodeTable(splitReady);
		// overviewPanel.updateGraph(m_graph);
	}

	public String getRelationshipField() {
		return selectedRelationshipField;
	}

	public void updateCommunityTable() {
		commPanel.setTableContent();
	}

	// ------------------------------------------------------------------------
	// Main and demo methods

	public static void main(String[] args) {

		Locale.setDefault(Locale.US);
		System.setProperty("user.country", Locale.US.getCountry());
		System.setProperty("user.language", Locale.US.getLanguage());
		System.setProperty("user.variant", Locale.US.getVariant());

		UILib.setPlatformLookAndFeel();

		DOMConfigurator.configure("lib//log4j.xml");

		GraphMLReader graphReader = new GraphMLReader();
		Graph graph = null;
		try {
			graph = graphReader.readGraph("data/intro.graphml");
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}

		String label = "name";

		/* JFrame frame = */demo(graph, label);

		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public static JFrame demo() {
		return demo((String) null, "label");
	}
	
	public static JPanel demo2() {
		return demo2((String) null, "label");
	}

	public static JFrame demo(String datafile, String label) {
		Graph g = null;
		if (datafile == null) {
			g = GraphLib.getGrid(15, 15);
			label = "label";
		} else {
			try {
				g = new GraphMLReader().readGraph(datafile);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		return demo(g, label);
	}
	
	public static JPanel demo2(String datafile, String label) {
		Graph g = null;
		if (datafile == null) {
			g = GraphLib.getGrid(15, 15);
			label = "label";
		} else {
			try {
				g = new GraphMLReader().readGraph(datafile);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		return demo2(g, label);
	}

	public static JFrame demo(Graph g, String label) {
		final SocialAction view = new SocialAction(g, label/* , false */);

		// set up menu
		JMenu dataMenu = new JMenu("Data");
		// dataMenu.add(new OpenGraphAction(view));

		dataMenu.add(new ImportTableAction(view));
		// dataMenu.add(new ImportNIHDemoAction(view));
		dataMenu.add(new ImportGraphAction(view));
		dataMenu.add(new ImportPajekAction(view));
		dataMenu.add(new ExportFlashAction(view));
		dataMenu.add(new ImportPositionsAction(view));
		if ((CURRENT_USER == USER_USNEWS) || (CURRENT_USER == USER_ADAM))
			dataMenu.add(new ImportSenateTestAction(view));
		if ((CURRENT_USER == USER_ADAM) || (CURRENT_USER == USER_ADAM))
			dataMenu.add(new ImportFacebookAction());
		dataMenu.add(new ImportHCILFormatAction(view));
		if ((CURRENT_USER == USER_START) || (CURRENT_USER == USER_ADAM))
			dataMenu.add(new ImportGTDDemoAction(view));
		if ((CURRENT_USER == USER_BCBS) || (CURRENT_USER == USER_ADAM))
			dataMenu.add(new ImportBCBSAction(view));
		if ((CURRENT_USER == USER_USNEWS) || (CURRENT_USER == USER_ADAM))
			dataMenu.add(new ImportMatrixAction(view));
		if ((CURRENT_USER == USER_START) || (CURRENT_USER == USER_ADAM) || (CURRENT_USER == USER_KEVIN))
			dataMenu.add(new ImportSagemanAction(view));

		dataMenu.add(new ExportVisibleNetworkAction(view));

		// dataMenu.add(new GraphMenuAction("Grid", "ctrl 1", view) {
		// protected Graph getGraph() {
		// return GraphLib.getGrid(15, 15);
		// }
		// });
		// dataMenu.add(new GraphMenuAction("Clique", "ctrl 2", view) {
		// protected Graph getGraph() {
		// return GraphLib.getClique(10);
		// }
		// });
		// dataMenu.add(new GraphMenuAction("Honeycomb", "ctrl 3", view) {
		// protected Graph getGraph() {
		// return GraphLib.getHoneycomb(5);
		// }
		// });
		// dataMenu.add(new GraphMenuAction("Balanced Tree", "ctrl 4", view) {
		// protected Graph getGraph() {
		// return GraphLib.getBalancedTree(3, 5);
		// }
		// });
		// dataMenu.add(new GraphMenuAction("Diamond Tree", "ctrl 5", view) {
		// protected Graph getGraph() {
		// return GraphLib.getDiamondTree(3, 3, 3);
		// }
		// });

		JMenuBar menubar = new JMenuBar();
		menubar.add(dataMenu);

		// launch window
		JFrame frame = new JFrame("SocialAction");// v" + VERSION_NUMBER + " (" + CURRENT_USER + ")");
		frame.setJMenuBar(menubar);
		frame.setContentPane(view);
		frame.pack();
		frame.setVisible(true);

		frame.addWindowListener(new WindowAdapter() {
			public void windowActivated(WindowEvent e) {
				view.m_vis.run("layout");
			}

			public void windowDeactivated(WindowEvent e) {
				view.m_vis.cancel("layout");
			}

			public void windowClosing(WindowEvent e) {
				view.saveEventLog();
				System.err.println("Closing...");
				logger.error("Closing");
				System.exit(0);
			}
		});

		return frame;
	}


	public static JPanel demo2(Graph g, String label) {
		final SocialAction view = new SocialAction(g, label/* , false */);

		// set up menu
		JMenu dataMenu = new JMenu("Data");
		// dataMenu.add(new OpenGraphAction(view));

		dataMenu.add(new ImportTableAction(view));
		// dataMenu.add(new ImportNIHDemoAction(view));
		dataMenu.add(new ImportGraphAction(view));
		dataMenu.add(new ImportPajekAction(view));
		dataMenu.add(new ExportFlashAction(view));
		dataMenu.add(new ImportPositionsAction(view));
		if ((CURRENT_USER == USER_USNEWS) || (CURRENT_USER == USER_ADAM))
			dataMenu.add(new ImportSenateTestAction(view));
		if ((CURRENT_USER == USER_ADAM) || (CURRENT_USER == USER_ADAM))
			dataMenu.add(new ImportFacebookAction());
		dataMenu.add(new ImportHCILFormatAction(view));
		if ((CURRENT_USER == USER_START) || (CURRENT_USER == USER_ADAM))
			dataMenu.add(new ImportGTDDemoAction(view));
		if ((CURRENT_USER == USER_BCBS) || (CURRENT_USER == USER_ADAM))
			dataMenu.add(new ImportBCBSAction(view));
		if ((CURRENT_USER == USER_USNEWS) || (CURRENT_USER == USER_ADAM))
			dataMenu.add(new ImportMatrixAction(view));
		if ((CURRENT_USER == USER_START) || (CURRENT_USER == USER_ADAM) || (CURRENT_USER == USER_KEVIN))
			dataMenu.add(new ImportSagemanAction(view));

		dataMenu.add(new ExportVisibleNetworkAction(view));

		final JPopupMenu menubar = new JPopupMenu();
                JButton jb = new JButton("Main menu");
                jb.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        menubar.show((Component)e.getSource(), 0, 0);
                    }
                });
		view.mainToolBar.add(jb, 0);
                menubar.add(dataMenu);

		// launch window
                //view.m_vis.run("layout");

		return view;
	}


	/*
	 * public static javax.swing.JComponent demoApplet(Graph g, String label) { final SocialAction view = new
	 * SocialAction(g, label, true);
	 * 
	 * // launch window JPanel frame = new JPanel();// "SocialAction v" + VERSION_NUMBER); //
	 * frame.setJMenuBar(menubar); frame.add(view); // frame.setContentPane(view); // frame.pack();
	 * frame.setVisible(true);
	 * 
	 * return frame; }
	 */

	// ------------------------------------------------------------------------
	/**
	 * Swing menu action that loads a graph into the graph viewer.
	 */
	public abstract static class GraphMenuAction extends AbstractAction {

		/** Never change */
		private static final long serialVersionUID = -2858503806105957290L;
		private SocialAction m_view;

		public GraphMenuAction(String name, String accel, SocialAction view) {
			m_view = view;
			this.putValue(AbstractAction.NAME, name);
			this.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(accel));
		}

		public void actionPerformed(ActionEvent e) {
			m_view.setGraph(getGraph(), "label");
		}

		protected abstract Graph getGraph();
	}

	public static class ImportGraphAction extends AbstractAction {

		/** Never change */
		private static final long serialVersionUID = 1982459194161755027L;
		private SocialAction m_view;

		public ImportGraphAction(SocialAction view) {
			m_view = view;
			this.putValue(AbstractAction.NAME, "Import Network from Graph...");
			this.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl 1"));
		}

		public void actionPerformed(ActionEvent e) {

			Graph g = IOLib.getGraphFile(m_view);

			// Graph g = ExampleClient.getFacebookGraph();

			JFrame newFrame = new JFrame();
			newFrame.setSize(new Dimension(600, 500));

			ImportPreviewPanel panel = new ImportPreviewPanel(m_view, g);
			newFrame.setContentPane(panel);
			newFrame.setVisible(true);
		}
	}

	public static class ImportGTDDemoAction extends AbstractAction {

		/** Never change */
		private static final long serialVersionUID = -2552585598144799944L;
		private SocialAction m_view;

		public ImportGTDDemoAction(SocialAction view) {
			m_view = view;
			this.putValue(AbstractAction.NAME, "Import GTD Demo...");
			this.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl G"));
		}

		public void actionPerformed(ActionEvent e) {

			// m_view.getVisualization().getDisplay(0).setHighQuality(false);
			//          
			// Table t = SocialActionIOLib.getTableFilePreview(m_view);

			Table tmpTable = null;
			CSVTableReader csvReader = new CSVTableReader();
			try {
				tmpTable = csvReader.readTable("data/gtdforadam-nogenerics-noblanks-moreThan5.csv");
			} catch (DataIOException er) {
				System.err.println(er.getMessage());
			}

			// JPrefuseTable.showTableWindow(tmpTable);

			m_view.setNodeColorField(null);
			m_view.importBipartiteTable(tmpTable, "uscCountry", "gname", "IYEAR", null, false);
		}
	}

	public static class ImportBCBSAction extends AbstractAction {

		/** Never change */
		private static final long serialVersionUID = 3983386142524931473L;
		private SocialAction m_view;

		public ImportBCBSAction(SocialAction view) {
			m_view = view;
			this.putValue(AbstractAction.NAME, "Import BCBS Demo...");
			this.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl B"));
		}

		public void actionPerformed(ActionEvent e) {

			// m_view.getVisualization().getDisplay(0).setHighQuality(false);
			//          
			// Table t = SocialActionIOLib.getTableFilePreview(m_view);

			Table tmpTable = null;
			CSVTableReader csvReader = new CSVTableReader();
			try {
				tmpTable = csvReader.readTable("/board.csv");
			} catch (DataIOException er) {
				System.err.println(er.getMessage());
			}

			// JPrefuseTable.showTableWindow(tmpTable);

			m_view.setNodeColorField(null);
			m_view.importBipartiteTable(tmpTable, "Name", "Organization", "Role", null, true);
		}
	}

	public static class ImportMatrixAction extends AbstractAction {

		/** Never change */
		private static final long serialVersionUID = 8602051992796571248L;
		private SocialAction m_view;

		public ImportMatrixAction(SocialAction view) {
			m_view = view;
			this.putValue(AbstractAction.NAME, "Import Senate Matrix...");
			this.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl M"));
		}

		public void actionPerformed(ActionEvent e) {

			// m_view.getVisualization().getDisplay(0).setHighQuality(false);
			//          
			// Table t = SocialActionIOLib.getTableFilePreview(m_view);

			// m_view.redGreenSpectrum = SocialColorMap.getInterpolatedMap(50, Color.BLUE, Color.BLACK, Color.RED);
			// m_view.

			m_view.matrixAction();
		}
	}

	public void matrixAction() {
		Table tmpTable = null;
		CSVTableReader csvReader = new CSVTableReader();
		try {
			// tmpTable = csvReader.readTable("/Voting_Coincidence.csv");
			tmpTable = csvReader.readTable("/Co-votes_updated_w_URLs.csv");
		} catch (DataIOException er) {
			System.err.println(er.getMessage());
		}

		// JPrefuseTable.showTableWindow(tmpTable);

		this.setNodeColorField(null);
		this.setImageField(SocialAction.IMAGE_COLUMN_NAME);

		this.importMatrix(tmpTable);

	}

	public static class ImportSagemanAction extends AbstractAction {

		/** Never change */
		private static final long serialVersionUID = -2178764254599332631L;
		private SocialAction m_view;

		public ImportSagemanAction(SocialAction view) {
			m_view = view;
			this.putValue(AbstractAction.NAME, "Import Sageman...");
			this.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl S"));
		}

		public void actionPerformed(ActionEvent e) {

			// m_view.getVisualization().getDisplay(0).setHighQuality(false);
			//          
			// Table t = SocialActionIOLib.getTableFilePreview(m_view);

			Table tmpTable = null;
			CSVTableReader csvReader = new CSVTableReader();
			try {
				tmpTable = csvReader.readTable("data/sageman3.csv");
			} catch (DataIOException er) {
				System.err.println(er.getMessage());
			}

			// JPrefuseTable.showTableWindow(tmpTable);

			m_view.setNodeColorField(null);

			m_view.importSageman(tmpTable);
			m_view.setImageField(IMAGE_COLUMN_NAME);
		}
	}

	public static class ImportSenateTestAction extends AbstractAction {

		/** Never change */
		private static final long serialVersionUID = -4458922618428611784L;
		private SocialAction m_view;

		public ImportSenateTestAction(SocialAction view) {
			m_view = view;
			this.putValue(AbstractAction.NAME, "Import Senate Test...");
			this.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl T"));
		}

		public void actionPerformed(ActionEvent e) {

			// m_view.getVisualization().getDisplay(0).setHighQuality(false);
			//          
			// Table t = SocialActionIOLib.getTableFilePreview(m_view);

			Table tmpTable = null;
			CSVTableReader csvReader = new CSVTableReader();
			try {
				tmpTable = csvReader.readTable("/Raw Data_310_w_subject.csv");
			} catch (DataIOException er) {
				System.err.println(er.getMessage());
			}

			// JPrefuseTable.showTableWindow(tmpTable);

			m_view.setNodeColorField(null);

			m_view.importSenateTest(tmpTable);
			// m_view.setImageField(IMAGE_COLUMN_NAME);
		}
	}

	public static class ImportFacebookAction extends AbstractAction {
		/** Never change */
		private static final long serialVersionUID = -8456218361262546941L;

		public ImportFacebookAction() {
			this.putValue(AbstractAction.NAME, "Import Network from Facebook...");
			this.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl 2"));
		}

		public void actionPerformed(ActionEvent e) {

			// Graph g = IOLib.getGraphFile(m_view);

			/*
			 * Graph g = SocialActionFacebookClient.getFacebookGraph();
			 * m_view.setImageField(SocialAction.IMAGE_COLUMN_NAME); // m_view.tr.setRoundedCorner(50,50);
			 * 
			 * JFrame newFrame = new JFrame(); newFrame.setSize(new Dimension(600, 500));
			 * 
			 * ImportPreviewPanel panel = new ImportPreviewPanel(m_view, g); newFrame.setContentPane(panel);
			 * newFrame.setVisible(true);
			 */
		}
	}

	public static class ExportFlashAction extends AbstractAction {

		/** Never change */
		private static final long serialVersionUID = -1299819062639992682L;
		private SocialAction m_view;

		public ExportFlashAction(SocialAction view) {
			m_view = view;
			this.putValue(AbstractAction.NAME, "Export Network to Flash...");
			this.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl F"));
		}

		public void actionPerformed(ActionEvent e) {
			m_view.saveToFlashTables();
		}
	}

	public static class ExportVisibleNetworkAction extends AbstractAction {

		/** Never change */
		private static final long serialVersionUID = -8610371822266697240L;
		private SocialAction m_view;

		public ExportVisibleNetworkAction(SocialAction view) {
			m_view = view;
			this.putValue(AbstractAction.NAME, "Export Visible Network...");
			this.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl V"));
		}

		public void actionPerformed(ActionEvent e) {

			m_view.exportVisibleNetwork();

		}
	}

	public static class ImportPositionsAction extends AbstractAction {

		/** Never change */
		private static final long serialVersionUID = 1799941718633976524L;
		private SocialAction m_view;

		public ImportPositionsAction(SocialAction view) {
			m_view = view;
			this.putValue(AbstractAction.NAME, "Import Node Positions from Table...");
			this.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl N"));
		}

		public void actionPerformed(ActionEvent e) {

			Table t = SocialActionIOLib.getTableFilePreview(m_view);

			if (t == null)
				return;

			m_view.setupGraphLocation(t);

			// JFrame newFrame = new JFrame();
			// newFrame.setSize(new Dimension(600, 500));

			// ImportPreviewPanel panel = new ImportPreviewPanel(m_view, t);
			// newFrame.setContentPane(panel);
			// newFrame.setVisible(true);

			/*
			 * String label = getLabel(m_view, g); if (label != null) { m_view.setGraph(g, label); }
			 */
		}
	}

	public static class ImportTableAction extends AbstractAction {

		/** Never change */
		private static final long serialVersionUID = -6308537123414346951L;
		private SocialAction m_view;

		public ImportTableAction(SocialAction view) {
			m_view = view;
			this.putValue(AbstractAction.NAME, "Import Network from Table...");
			this.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl O"));
		}

		public void actionPerformed(ActionEvent e) {

			Table t = SocialActionIOLib.getTableFilePreview(m_view);

			if (t == null)
				return;

			JFrame newFrame = new JFrame();
			newFrame.setSize(new Dimension(600, 500));

			ImportPreviewPanel panel = new ImportPreviewPanel(m_view, t);
			newFrame.setContentPane(panel);
			newFrame.setVisible(true);

			/*
			 * String label = getLabel(m_view, g); if (label != null) { m_view.setGraph(g, label); }
			 */
		}

	}

	public static class ImportPajekAction extends AbstractAction {

		/** Never change */
		private static final long serialVersionUID = -7645706431483733058L;
		private SocialAction m_view;

		public ImportPajekAction(SocialAction view) {
			m_view = view;
			this.putValue(AbstractAction.NAME, "Import Pajek Network...");
			this.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl P"));
		}

		public void actionPerformed(ActionEvent e) {

			Graph g = SocialActionIOLib.getPajekGraphFile(m_view);

			if (g == null)
				return;

			// JPrefuseTable.showTableWindow(g.getNodeTable());

			JFrame newFrame = new JFrame();
			newFrame.setSize(new Dimension(600, 500));

			ImportPreviewPanel panel = new ImportPreviewPanel(m_view, g);
			newFrame.setContentPane(panel);
			newFrame.setVisible(true);

			/*
			 * String label = getLabel(m_view, g); if (label != null) { m_view.setGraph(g, label); }
			 */
		}
	}

	public static class ImportNIHDemoAction extends AbstractAction {

		/** Never change */
		private static final long serialVersionUID = 6042702633498167233L;
		private SocialAction m_view;

		public ImportNIHDemoAction(SocialAction view) {
			m_view = view;
			this.putValue(AbstractAction.NAME, "Import NIH Demo...");
			this.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl 3"));
		}

		public void actionPerformed(ActionEvent e) {

			Table nodeTable = null;
			DelimitedTextTableReader csvReader = new DelimitedTextTableReader();
			try {
				nodeTable = csvReader.readTable("/topic119-nodes.txt");
			} catch (DataIOException er) {
				System.err.println(er.getMessage());
			}

			if (nodeTable == null)
				return;

			Table edgeTable = null;

			try {
				edgeTable = csvReader.readTable("/topic119-links.txt");
			} catch (DataIOException er) {
				System.err.println(er.getMessage());
			}

			if (edgeTable == null)
				return;

			JFrame newFrame = new JFrame();
			newFrame.setSize(new Dimension(600, 500));

			m_view.setNodeColorField(null);
			if (nodeTable.canGetInt("REL"))
				m_view.setClassField("REL");
			m_view.importGraphFromHCILFormat(nodeTable, edgeTable, "ID", "ID", "ID1", "ID2", null, null, false);
		}
	}

	/**
	 * Imports an HCIL formated table file (http://www.cs.umd.edu/hcil/nvss/netFormat.shtml)
	 * 
	 * @author Adam Perer
	 * @author Cody Dunne
	 */
	public static class ImportHCILFormatAction extends AbstractAction {

		/** Never change */
		private static final long serialVersionUID = -3432719809689459851L;
		private SocialAction m_view;

		public ImportHCILFormatAction(SocialAction view) {
			m_view = view;
			this.putValue(AbstractAction.NAME, "Import Network from HCIL Tables...");
			this.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl 3"));
		}

		public void actionPerformed(ActionEvent e) {
			JFileChooser jfc = new JFileChooser();
			jfc.setDialogType(JFileChooser.OPEN_DIALOG);
			jfc.setAcceptAllFileFilterUsed(false);

			jfc.setDialogTitle("Open HCIL Node File");
			Table nodeTable = SocialActionIOLib.getHCILFormatTableFilePreview(m_view, jfc);

			if (nodeTable == null)
				return;

			jfc.setDialogTitle("Open HCIL Edge File");

			Table edgeTable = SocialActionIOLib.getHCILFormatTableFilePreview(m_view, jfc);

			if (edgeTable == null)
				return;

			// JPrefuseTable.showTableWindow(edgeTable);

			JFrame newFrame = new JFrame();
			newFrame.setSize(new Dimension(600, 500));

			m_view.setNodeColorField(null);
			// if (nodeTable.canGetInt("REL"))
			// m_view.setClassField("REL");

			ImportPreviewPanel panel = new ImportPreviewPanel(m_view, nodeTable, edgeTable);
			newFrame.setContentPane(panel);
			newFrame.setVisible(true);

			// m_view.importGraphFromHCILFormat(nodeTable, edgeTable);
		}
	}

	public HashMap<String, Point2D.Float> getLocationMap() {
		return locationMap;
	}

	public static class LocationAction extends Action {
		private SocialAction m_view;

		public LocationAction(SocialAction view) {
			m_view = view;
		}

		public void run(double frac) {

			System.out.println("Starting Location Changing " + m_view.getGraph().getNodeCount());

			Iterator i = m_view.getGraph().nodes();

			// m_view.getGraphs()[0].nodes();
			while (i.hasNext()) {
				Node node = (Node) i.next();
				Point2D.Float location = null;
				try {
					location = (Point2D.Float) m_view.getLocationMap().get(node.getString(m_view.getLabelField()));
				} catch (NullPointerException e) {
					System.out.println("Couldn't find Location Info for Node: " + node);
				}
				if (location != null) {
					VisualItem item = m_view.getVisualization().getVisualItem(graph, node);
					if (item != null) {
						item.setX(location.x);
						item.setY(location.y);
					}
					// System.out.println(m_view.getVisualization().getVisualItem(graph, node) + " " + location);

					// getItemRegistry().getNodeItem(node).setLocation(location);// location);
				} else
					System.out.println("UH OH... no location found");
			}

			System.out.println("Ending Location Changing");
		}
	}

	public static class FitOverviewListener implements ItemBoundsListener {
		private Rectangle2D m_bounds = new Rectangle2D.Double();

		private Rectangle2D m_temp = new Rectangle2D.Double();

		private double m_d = 15;

		public void itemBoundsChanged(Display d) {
			d.getItemBounds(m_temp);
			GraphicsLib.expand(m_temp, 25 / d.getScale());

			double dd = m_d / d.getScale();
			double xd = Math.abs(m_temp.getMinX() - m_bounds.getMinX());
			double yd = Math.abs(m_temp.getMinY() - m_bounds.getMinY());
			double wd = Math.abs(m_temp.getWidth() - m_bounds.getWidth());
			double hd = Math.abs(m_temp.getHeight() - m_bounds.getHeight());
			if (xd > dd || yd > dd || wd > dd || hd > dd) {
				m_bounds.setFrame(m_temp);
				DisplayLib.fitViewToBounds(d, m_bounds, 0);
			}
		}
	}

	public long endTime;
	public long startTime;

	/*
	 * All events defined in SocialAction class are handled here (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == enforceBoundsFalseButton) {
			enforceBoundsFalseButton.setSelected(true);
			enforceBoundsTrueButton.setSelected(false);
			if (forceDirectedLayout instanceof ForceDirectedLayout) {
				((ForceDirectedLayout) forceDirectedLayout).setEnforceBounds(false);
			}
			enforceBounds = false;
			addEventToHistory(DisplaySettingsPanel.DISPLAY_STEP_NUMBER, DisplaySettingsPanel.DISPLAY_NETWORKVIS_STATE,
					DisplaySettingsPanel.DISPLAY_LAYOUT_SETTING, 0);

		} else if (e.getSource() == enforceBoundsTrueButton) {
			enforceBoundsTrueButton.setSelected(true);
			enforceBoundsFalseButton.setSelected(false);
			if (forceDirectedLayout instanceof ForceDirectedLayout) {
				((ForceDirectedLayout) forceDirectedLayout).setEnforceBounds(true);
			}
			enforceBounds = true;
			addEventToHistory(DisplaySettingsPanel.DISPLAY_STEP_NUMBER, DisplaySettingsPanel.DISPLAY_NETWORKVIS_STATE,
					DisplaySettingsPanel.DISPLAY_LAYOUT_SETTING, 1);
		} else if (e.getSource() == animateLayoutTrueButton) {
			animateLayoutTrueButton.setSelected(true);
			animateLayoutFalseButton.setSelected(false);

			startTime = (int) System.currentTimeMillis();
			layoutGraph.run();
			addEventToHistory(DisplaySettingsPanel.DISPLAY_STEP_NUMBER, DisplaySettingsPanel.DISPLAY_NETWORKVIS_STATE,
					DisplaySettingsPanel.DISPLAY_ANIMATE_SETTING, 1);

		} else if (e.getSource() == animateLayoutFalseButton) {
			animateLayoutTrueButton.setSelected(false);
			animateLayoutFalseButton.setSelected(true);

			endTime = (int) System.currentTimeMillis();

			System.out.println("Duration " + (endTime - startTime));
			layoutGraph.cancel();

			addEventToHistory(DisplaySettingsPanel.DISPLAY_STEP_NUMBER, DisplaySettingsPanel.DISPLAY_NETWORKVIS_STATE,
					DisplaySettingsPanel.DISPLAY_ANIMATE_SETTING, 0);

			// Buttons for selecting alternative layouts
		} else if (e.getSource() == layoutButtons[0]) {
			setLayout(new FruchtermanReingoldLayout(graph, 1), layoutButtons[0]);

		} else if (e.getSource() == layoutButtons[1]) {
			setLayout(new ForceDirectedLayout(graph, true, false), layoutButtons[1]);

		} else if (e.getSource() == layoutButtons[2]) {
			setLayout(new SocNetLayout(this, graph, false), layoutButtons[2]);

		} else if (e.getSource() == layoutButtons[3]) {
			setLayout(new MidichlorianLayout_Comm(this, graph, true, false), layoutButtons[3]);

		} else if (e.getSource() == layoutButtons[4]) {
			setLayout(new MidichlorianLayout_Comm(this, graph, true, true), layoutButtons[4]);

		} else if (e.getSource() == mcNodeAttribButtons[0]) {
			mcNodeJoinAttrib = selectedNodeColorField;
			toggleMcJoinAttribButton(mcNodeAttribButtons, mcNodeAttribButtons[0]);

		} else if (e.getSource() == mcNodeAttribButtons[1]) {
			mcNodeJoinAttrib = null;
			toggleMcJoinAttribButton(mcNodeAttribButtons, mcNodeAttribButtons[1]);

		} else if (e.getSource() == mcEdgeAttribButtons[0]) {
			mcEdgeJoinAttrib = selectedEdgeColorField;
			toggleMcJoinAttribButton(mcEdgeAttribButtons, mcEdgeAttribButtons[0]);

		} else if (e.getSource() == mcEdgeAttribButtons[1]) {
			mcEdgeJoinAttrib = null;
			toggleMcJoinAttribButton(mcEdgeAttribButtons, mcEdgeAttribButtons[1]);

		} else if (e.getSource() == saveData) {
			saveTables();
			addEventToHistory(DisplaySettingsPanel.DISPLAY_STEP_NUMBER, DisplaySettingsPanel.DISPLAY_EXPORT_STATE,
					DisplaySettingsPanel.DISPLAY_EXPORT, 1);

		} else if (e.getSource() == wordWrapButton) {
			m_isWordWrap = true;
			setLabelField(selectedLabelField);
			addEventToHistory(DisplaySettingsPanel.DISPLAY_STEP_NUMBER, DisplaySettingsPanel.DISPLAY_NETWORKVIS_STATE,
					DisplaySettingsPanel.DISPLAY_WORDWRAP_SETTING, 1);

		} else {
			unselectMainToolBarButtons();
			CardLayout cl = (CardLayout) tabbedPane.getLayout();

			if (mainSplit.getDividerLocation() != TABSIZE_X)
				mainSplit.setDividerLocation(TABSIZE_X);

			if (e.getSource() == importDataButton) {
				// switchToOverview();
				importDataButton.setSelected(true);
				cl.show(tabbedPane, SocialAction.IMPORT_DATA_PANEL);
				mainSplit.setDividerLocation(1.0);

			} else if (e.getSource() == overviewButton) {
				switchToOverview();
				overviewButton.setSelected(true);
				cl.show(tabbedPane, SocialAction.STEPS_PANELS[0]);

			} else if (e.getSource() == rankNodesButton) {
				switchToNode1D();
				rankNodesButton.setSelected(true);
				cl.show(tabbedPane, SocialAction.STEPS_PANELS[1]);

			} else if (e.getSource() == nodeAttributesButton) {
				switchToNodeAttributes1D();
				nodeAttributesButton.setSelected(true);
				cl.show(tabbedPane, SocialAction.NODE_ATTRIBUTE_1D_PANEL);
			}

			else if (e.getSource() == rankEdgesButton) {
				switchToEdge1D();
				rankEdgesButton.setSelected(true);
				cl.show(tabbedPane, SocialAction.STEPS_PANELS[2]);
			} else if (e.getSource() == plotNodesButton) {
				switchToNode2D();
				plotNodesButton.setSelected(true);
				cl.show(tabbedPane, SocialAction.STEPS_PANELS[3]);
			} else if (e.getSource() == communitiesButton) {
				switchToCommunity(); // does nothing
				communitiesButton.setSelected(true);
				cl.show(tabbedPane, SocialAction.STEPS_PANELS[5]);
			} else if (e.getSource() == multiplexButton) {
				multiplexButton.setSelected(true);
				cl.show(tabbedPane, SocialAction.STEPS_PANELS[6]);
			} else if (e.getSource() == detailsButton) {
				detailsButton.setSelected(true);
				cl.show(tabbedPane, SocialAction.DETAILS_PANEL);
			} else if (e.getSource() == readabilityButton) {
				readabilityButton.setSelected(true);
				cl.show(tabbedPane, SocialAction.READABILITY_PANEL);
			} else if (e.getSource() == settingsButton) {
				settingsButton.setSelected(true);
				cl.show(tabbedPane, SocialAction.DISPLAY_PANEL);
			}
		}
	}

	/**
	 * Used by the layout selection buttons to clean up and get the new layout up and running
	 * 
	 * @param l
	 *            an already created layout
	 * @param layoutButton
	 *            the button that was hit
	 */
	public void setLayout(Layout l, JButton layoutButton) {
		layoutGraph.remove(forceDirectedLayout);
		forceDirectedLayout = l;
		layoutGraph.add(forceDirectedLayout);
		m_vis.putAction("graphLayout", layoutGraph);

		if (forceDirectedLayout instanceof ForceDirectedLayout) {
			((ForceDirectedLayout) forceDirectedLayout).setEnforceBounds(enforceBounds);
		}

		// Make sure the global coefficient slider is retained
		setGlobalSpringCoefficient(getGlobalSpringCoefficient());

		for (JButton jb : layoutButtons) {
			if (jb != null) {
				jb.setSelected(false);
			}
		}
		layoutButton.setSelected(true);
		if (layoutGraph.get(0) instanceof ForceDirectedLayout) {
			displayPanel.setForceSimulator(((ForceDirectedLayout) layoutGraph.get(0)).getForceSimulator());
			displayPanel.revalidate();
		}
	}

	/**
	 * Toggles the correct attribute button
	 * 
	 * @param mcAttribButtons
	 *            the button group
	 * @param attribButton
	 *            the button that was hit
	 */
	public void toggleMcJoinAttribButton(JButton[] mcAttribButtons, JButton attribButton) {
		for (JButton jb : mcAttribButtons) {
			if (jb != null) {
				jb.setSelected(false);
				jb.setToolTipText(null);
			}
		}

		if (mcAttribButtons.equals(mcNodeAttribButtons)) {
			attribButton.setToolTipText(mcNodeJoinAttrib);
			System.out.println("MCNJA: " + mcNodeJoinAttrib);
		} else if (mcAttribButtons.equals(mcEdgeAttribButtons)) {
			System.out.println("MCEJA: " + mcEdgeJoinAttrib);
			attribButton.setToolTipText(mcEdgeJoinAttrib);
		}

		if (mcAttribButtons.equals(mcNodeAttribButtons) && selectedNodeColorField == null) {
			mcAttribButtons[1].setSelected(true);
		} else if (mcAttribButtons.equals(mcEdgeAttribButtons) && selectedEdgeColorField == null) {
			mcAttribButtons[1].setSelected(true);
		} else {
			attribButton.setSelected(true);
		}
	}

	/**
	 * Deselects all toolbar buttons
	 */
	public void unselectMainToolBarButtons() {
		overviewButton.setSelected(false);
		rankNodesButton.setSelected(false);
		rankEdgesButton.setSelected(false);
		plotNodesButton.setSelected(false);
		communitiesButton.setSelected(false);
		multiplexButton.setSelected(false);
		settingsButton.setSelected(false);
		detailsButton.setSelected(false);
		readabilityButton.setSelected(false);
		nodeAttributesButton.setSelected(false);
		importDataButton.setSelected(false);
	}

	public void saveTables() {

		batchRank();

		// JPrefuseTable.showTableWindow(m_graph.getNodeTable());

		// CSVTableWriter csv = new CSVTableWriter();
		//
		// JFileChooser jfc = new JFileChooser();
		// jfc.setDialogType(JFileChooser.SAVE_DIALOG);
		// jfc.setDialogTitle("Save Table File");
		// jfc.setAcceptAllFileFilterUsed(false);
		//
		// SimpleFileFilter ff;
		//
		// // TODO: have this generate automatically
		// // tie into PrefuseConfig??
		//
		// // CSV
		// ff = new SimpleFileFilter("csv", "Comma Separated Values (CSV) File (*.csv)", new CSVTableReader());
		// ff.addExtension("gz");
		//        
		// jfc.setFileFilter(ff);
		//
		// int retval = jfc.showSaveDialog(this);
		//
		// File f = jfc.getSelectedFile();
		//
		// try {
		// csv.writeTable(m_graph.getNodeTable(), f);
		//
		// } catch (DataIOException e) {
		// System.err.println("Can't write file!");
		// }

		// JPrefuseTable.showTableWindow(m_graph.getEdgeTable());
	}

	public void saveToFlashTables() {
		// JPrefuseTable.showTableWindow(aggregateTable);
		//        
		VisualGraph vg = (VisualGraph) m_vis.getVisualGroup(SocialAction.graph);
		saveVisualTables(vg.getNodeTable(), vg.getEdgeTable(), aggregateTable);
	}

	public void saveVisualTables(Table nodeVisualTable, Table edgeVisualTable, Table aggregateTable) {

		// DelimitedTextTableWriter txt = new DelimitedTextTableWriter();

		JFileChooser jfc = new JFileChooser();
		jfc.setDialogType(JFileChooser.SAVE_DIALOG);
		jfc.setDialogTitle("Save Table File");
		jfc.setAcceptAllFileFilterUsed(false);

		SimpleFileFilter ff;

		// TODO: have this generate automatically
		// tie into PrefuseConfig??

		// CSV

		ff = new SimpleFileFilter("csv", "Comma-seperated (CSV) File (*.csv)", new CSVTableWriter());
		jfc.setFileFilter(ff);
		ff = new SimpleFileFilter("txt", "Tab-delimited (TXT) File (*.txt)", new DelimitedTextTableWriter());
		jfc.setFileFilter(ff);

		int retval = jfc.showSaveDialog(this);

		if (retval != JFileChooser.APPROVE_OPTION) {
			return;
		}

		File nodeFile = jfc.getSelectedFile();

		retval = jfc.showSaveDialog(this);

		if (retval != JFileChooser.APPROVE_OPTION) {
			return;
		}

		File edgeFile = jfc.getSelectedFile();

		File aggregateFile = null;

		if (aggregateTable != null) {
			retval = jfc.showSaveDialog(this);
			if (retval != JFileChooser.APPROVE_OPTION) {
				return;
			}

			aggregateFile = jfc.getSelectedFile();
		}

		ff = (SimpleFileFilter) jfc.getFileFilter();
		TableWriter tw = (TableWriter) ff.getUserData();

		try {
			tw.writeTable(nodeVisualTable, nodeFile);
			tw.writeTable(edgeVisualTable, edgeFile);
			if (aggregateTable != null)
				tw.writeTable(aggregateTable, aggregateFile);

		} catch (DataIOException e) {
			System.err.println("Can't write file!");
		}

		// JPrefuseTable.showTableWindow(m_graph.getEdgeTable());
	}

	public void batchRank() {
		Table timeTable = new Table();
		timeTable.addColumn("Algorithm", String.class);
		timeTable.addColumn("Time", int.class);

		for (int i = 0; i < Node1DPanel.RANKER_TYPES_1D.length; i++) {

			int beforetime = (int) System.currentTimeMillis();

			node1DPanel.setRankType(Node1DPanel.RANKER_TYPES_1D[i]);

			int aftertime = (int) System.currentTimeMillis();

			int time = aftertime - beforetime;

			int newRow = timeTable.addRow();
			timeTable.set(newRow, "Algorithm", Node1DPanel.RANKER_TYPES_1D[i]);
			timeTable.setInt(newRow, "Time", time);

			// System.out.println("" + Node1DPanel.RANKER_TYPES_1D[i] + " " + (System.currentTimeMillis()-time));
		}

		// calculate Density
		int beforetime = (int) System.currentTimeMillis();

		int numEdges = m_graph.getEdgeCount();
		int numNodes = m_graph.getNodeCount();
		double density = (2.0 * numEdges / (numNodes * (1.0 * numNodes - 1)));

		int aftertime = (int) System.currentTimeMillis();
		int time = aftertime - beforetime;

		int newRow = timeTable.addRow();
		timeTable.set(newRow, "Algorithm", "Density");
		timeTable.setInt(newRow, "Time", time);

		// calculate Diameter
		beforetime = (int) System.currentTimeMillis();

		double diameter = GraphStatistics.diameter(m_graph, true);

		aftertime = (int) System.currentTimeMillis();
		time = aftertime - beforetime;

		newRow = timeTable.addRow();
		timeTable.set(newRow, "Algorithm", "Diameter");
		timeTable.setInt(newRow, "Time", time);

		JPrefuseTable.showTableWindow(timeTable);

		// calculate Community Time
		beforetime = (int) System.currentTimeMillis();
		SubgraphSet comm = (SubgraphSet) m_vis.getFocusGroup(SocialAction.community);
		comm.initCommunity(m_vis, m_graph);

		aftertime = (int) System.currentTimeMillis();
		time = aftertime - beforetime;

		newRow = timeTable.addRow();
		timeTable.set(newRow, "Algorithm", "Community");
		timeTable.setInt(newRow, "Time", time);

		// then insert stats about graph
		newRow = timeTable.addRow();
		timeTable.set(newRow, "Algorithm", "NodeCount");
		timeTable.setInt(newRow, "Time", m_graph.getNodeCount());

		newRow = timeTable.addRow();
		timeTable.set(newRow, "Algorithm", "EdgeCount");
		timeTable.setInt(newRow, "Time", m_graph.getEdgeCount());

		newRow = timeTable.addRow();
		timeTable.set(newRow, "Algorithm", "DensityValue");
		timeTable.set(newRow, "Time", density);

		newRow = timeTable.addRow();
		timeTable.set(newRow, "Algorithm", "DiameterValue");
		timeTable.set(newRow, "Time", diameter);

		try {
			DelimitedTextTableWriter txt = new DelimitedTextTableWriter();
			Calendar cal = new GregorianCalendar();
			File f = new File("TimeStat_" + SocialAction.getFilenameFromDate(cal) + ".txt");
			FileOutputStream stream = new FileOutputStream(f);
			try {
				txt.writeTable(timeTable, stream);
			} catch (DataIOException e) {
				System.err.println("Can't write file!");
			}
			try {
				stream.close();
			} catch (IOException er) {
				System.out.println(er.getMessage());
			}
		} catch (FileNotFoundException er) {
			System.out.println(er.getMessage());
		}
	}

	public void addEventToHistory(int step, String state, String action, double parameter) {
		Double parameters[] = new Double[1];
		parameters[0] = parameter;

		addEventToHistory(step, state, action, parameters);
	}

	public void addEventToHistory(int step, String state, String action, double lowValue, double highValue) {
		Double parameters[] = new Double[2];
		parameters[0] = lowValue;
		parameters[1] = highValue;

		addEventToHistory(step, state, action, parameters);
	}

	public void addEventToHistory(int step, String state, String action, Double[] parameters) {
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		int randomAnnotation = 0; // ((int) (Math.random() * 100.0) % 5);
		historyPanel.addEventToHistory(cal.getTime(), step, state, action, 0, randomAnnotation);
		// annotationPanel.addEventToHistory(cal.getTime(), step, state, action, 0, randomAnnotation);
		syfOverviewPanel.updateProgressSteps();

		logger.info("Event:\t" + step + "\t" + state + "\t" + action + "\t" + printDoubleArray(parameters));
	}

	public String printDoubleArray(Double[] parameters) {
		String array = "[";
		for (int i = 0; i < parameters.length; i++) {
			if (i != 0)
				array += ",";
			array += ("" + parameters[i]);
		}
		array += "]";
		return array;
	}

	public void addNode1DRankEvent(String state) {
		syfOverviewPanel.addActionToSYF(Node1DPanel.NODE1D_STEP_NUMBER, state, Node1DPanel.NODE1D_RANK_ACTION);
	}

	public void addNode2DRankEvent(String state) {
		// syfOverviewPanel.addActionToSYF(Rank2DScatterplotPanel.NODE2D_STEP_NUMBER, state,
		// Node1DPanel.NODE1D_RANK_ACTION);

		System.err.println("Need to add " + state + "to Node2D SYF");
	}

	public SYFHistoryPanel getHistoryPanel() {
		return historyPanel;
	}

	public SYFAnnotationPanel getAnnotationPanel() {
		return annotationPanel;
	}

	public void doSYFComment() {
		syfOverviewPanel.setAnnotation(SYFAnnotationPanel.FAKE_COMMENT, true, "insight africa");
	}

	public void doSYFActionsOnNewGraph(int[] steps, String[] states, String[] actions) {
		// load graph (instead of using this graph...)
		Graph g = this.m_graph;
		// do SYF actions

		syfOverviewPanel.reset(g);

		double fakeParameter[] = { 0.0, 1.0 };
		for (int i = 0; i < steps.length; i++) {
			doSYFAction(steps[i], states[i], actions[i], fakeParameter);
		}
	}

	public void doSYFAction(int step, String state, String action, double[] parameters) {
		CardLayout cl = (CardLayout) tabbedPane.getLayout();
		cl.show(tabbedPane, SocialAction.STEPS_PANELS[step - 1]);

		// panel.revalidate();
		if (action.equals(Node1DPanel.NODE1D_RANK_ACTION)) {
			node1DPanel.setRankType(state);
			switchToNode1D();
			syfOverviewPanel.setSelectedButton(step - 1);
			System.out.println("action: " + action);
			System.out.println("state: " + state);
		} else if (action.equals(Edge1DPanel.EDGE1D_RANK_ACTION)) {
			edge1DPanel.setRankType(state);
			switchToEdge1D();
			syfOverviewPanel.setSelectedButton(step - 1);
			System.out.println("action: " + action);
			System.out.println("state: " + state);
		} else if (action.equals(Rank2DScatterplotPanel.NODE2D_SCATTERPLOT_ACTION)) {

			rank2DPanel.setRankType(state);
			switchToNode2D();
			syfOverviewPanel.setSelectedButton(step - 1);
			System.out.println("action: " + action);
			System.out.println("state: " + state);
		} else if (action.equals(NetworkOverviewPanel.OVERVIEW_ACTION)) {
			switchToOverview();
		} else if (action.equals(CommunityPanel.COMMUNITY_ACTION)) {
			switchToCommunity();
		}
	}

	public void switchToNode1D() {
		node1DPanel.setContent();
		setNodeColorField(Node1DPanel.getFieldName((String) getNodeRank1D().getRankType().getSelectedItem()));
		updateAggregateColorPalette();
		getVisualization().run("draw");

		// m_app.getNodeRank1D().setContent();
	}

	public void switchToNodeAttributes1D() {
		nodeAttributePanel.setContent();
		setNodeColorField(NodeAttributes1DPanel.getFieldName((String) getNodeAttributes1D().getRankType()
				.getSelectedItem()));
		updateAggregateColorPalette();
		getVisualization().run("draw");

		// m_app.getNodeAttributes1D().setContent();
	}

	public void switchToEdge1D() {
		edge1DPanel.setContent();
		if (getEdgeRank1D().getRankType().getSelectedItem() == null) {
			if (getEdgeRank1D().getRankType().getItemCount() > 0) {
				getEdgeRank1D().getRankType().setSelectedIndex(0);
			} else {
				return;
			}
		}
		setEdgeColorField(edge1DPanel.getFieldName((String) getEdgeRank1D().getRankType().getSelectedItem()));
		updateAggregateColorPalette();
		getVisualization().run("draw");

		// m_app.getEdgeRank1D()().setContent();
	}

	public void switchToOverview() {
		graphStatsPanel.setEvent();
	}

	public void switchToCommunity() {
		// commPanel.createCommunities();
	}

	public void switchToNode2D() {
		rank2DPanel.plotRankings();
		setNodeColorField(Rank2DScatterplotPanel.SCATTERPLOT_COLOR);
		updateAggregateColorPalette();
		getVisualization().run("draw");
	}

	public static String getFilenameFromDate(Calendar cal) {
		String name = "" + cal.get(Calendar.DATE) + "-" + cal.get(Calendar.MONTH) + "-" + cal.get(Calendar.YEAR) + "_"
				+ cal.get(Calendar.HOUR_OF_DAY) + "-" + cal.get(Calendar.MINUTE) + "-" + cal.get(Calendar.SECOND) + "-"
				+ cal.get(Calendar.MILLISECOND);

		return name;
	}

	public void saveEventLog() {
		// CSVTableWriter writer = new CSVTableWriter();
		// Calendar cal = new GregorianCalendar();

		// String newFolderPath = "C:\\Program Files\\SocialAction";
		// File f = new File(newFolderPath);
		// f.mkdir();

		// try {
		// writer.writeTable(historyPanel.getHistoryTable(), "log_" + getFilenameFromDate(cal) + ".csv");
		// } catch (DataIOException e) {
		// System.err.println("Couldn't save... ack!");
		// }
	}
}