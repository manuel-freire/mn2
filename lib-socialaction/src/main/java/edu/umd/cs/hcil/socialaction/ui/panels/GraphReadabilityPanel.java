package edu.umd.cs.hcil.socialaction.ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import prefuse.Visualization;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.tuple.TupleSet;
import prefuse.util.PrefuseLib;
import prefuse.util.io.IOLib;
import prefuse.visual.VisualItem;
import prefuse.visual.tuple.TableEdgeItem;
import prefuse.visual.tuple.TableNodeItem;
import edu.umd.cs.hcil.socialaction.SocialAction;
import edu.umd.cs.hcil.socialaction.ui.tables.GraphTableModel;

/**
 * Displays graph readability metrics for both the selected node and the graph as a whole. Metrics include node
 * occlusion, edge crossings, edge tunneling...
 * 
 * @author Cody Dunne
 */
public class GraphReadabilityPanel extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final static int GRAPH_READABILITY_STEP_NUMBER = 999;// XXX find a good value
	public final static String GRAPH_READABILITY_ACTION = "Graph Readability Selection";
	private SocialAction m_app;
	private int panelWidth;
	private JLabel label;
	private GraphTableModel nodeDetailsTableModel;
	private GraphTableModel globalDetailsTableModel;
	String nodeDetailsLabels[] = { "Attribute", "Value" };

	private JButton enableButton;
	private JButton computeAllButton;
	private static final String ENABLE = "Enable";

	private static final String DISABLE = "Disable";

	private Visualization m_vis;
	private String graph;
	private static final double NOISE = .0003;

	public static final String NODE_OCCLUSION = "Node occlusion";
	public static final String LOCAL_EDGE_TUNNELS = "Local edge tunnel";
	public static final String TRIGGERED_EDGE_TUNNELS = "Triggered edge tunnel";
	public static final String EDGE_CROSSING = "Edge crossing";
	public static final String[] MEASURES = { NODE_OCCLUSION, LOCAL_EDGE_TUNNELS, TRIGGERED_EDGE_TUNNELS, EDGE_CROSSING };

	private static final String TUNNELS = "Edge tunnels";

	public static final String[] GLOBAL_MEASURES = { NODE_OCCLUSION, TUNNELS, EDGE_CROSSING };

	public int[] globalMeasures = new int[GLOBAL_MEASURES.length];

	private static final boolean presMode = true;

	public GraphReadabilityPanel(SocialAction m_app, int width, String graph) {
		this.m_app = m_app;
		this.panelWidth = width;

		this.m_vis = this.m_app.getVisualization();
		this.graph = graph;

		initUI();

		// setContent();
	}

	private void initUI() {
		final Box graphStats_panel = Box.createVerticalBox();
		graphStats_panel.setBorder(BorderFactory.createTitledBorder(SocialAction.DETAILS_PANEL));

		JXTaskPaneContainer taskPaneContainer = new JXTaskPaneContainer();

		JXTaskPane nodeDetails = new JXTaskPane();
		nodeDetails.setTitle("Node Readability Metrics");

		if (presMode) {
			Font curFont = nodeDetails.getFont();
			nodeDetails.setFont(new Font(curFont.getFontName(), curFont.getStyle(), 18));
		}
		Font curFont = nodeDetails.getFont();
		nodeDetails.setFont(new Font(curFont.getFontName(), curFont.getStyle(), 18));

		taskPaneContainer.add(nodeDetails);

		nodeDetailsTableModel = new GraphTableModel(nodeDetailsLabels);

		JXTable networkTable = new JXTable(nodeDetailsTableModel);
		// networkTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
		networkTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		networkTable.setBackground(null);
		networkTable.setSortable(true);

		if (presMode) {
			curFont = networkTable.getFont();
			networkTable.setFont(new Font(curFont.getFontName(), curFont.getStyle(), 18));
		}
		networkTable.packAll();

		nodeDetails.add(networkTable);

		// ---NEW
		JXTaskPane globalDetails = new JXTaskPane();
		globalDetails.setTitle("Global Readability Metrics");

		if (presMode) {
			curFont = globalDetails.getFont();
			globalDetails.setFont(new Font(curFont.getFontName(), curFont.getStyle(), 18));
		}

		taskPaneContainer.add(globalDetails);

		globalDetailsTableModel = new GraphTableModel(nodeDetailsLabels);

		JXTable globalTable = new JXTable(globalDetailsTableModel);
		// networkTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
		globalTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		globalTable.setBackground(null);
		globalTable.setSortable(true);
		if (presMode) {
			curFont = globalTable.getFont();
			globalTable.setFont(new Font(curFont.getFontName(), curFont.getStyle(), 18));
		}
		globalTable.packAll();

		globalDetails.add(globalTable);
		// ---END NEW

		label = new JLabel();
		label.setBorder(BorderFactory.createLineBorder(Color.black));
		graphStats_panel.add(label);

		// put the action list on the left
		graphStats_panel.add(taskPaneContainer, BorderLayout.EAST);

		// and a file browser in the middle
		// graphStats_panel.add(fileBrowser, BorderLayout.CENTER);

		enableButton = new JButton(ENABLE);
		enableButton.addActionListener(this);

		computeAllButton = new JButton("All");
		computeAllButton.addActionListener(this);

		Box titlebox = new Box(BoxLayout.Y_AXIS);// BoxLayout.X_AXIS);
		titlebox.add(Box.createHorizontalStrut(2));

		titlebox.add(Box.createHorizontalGlue());
		titlebox.setMaximumSize(new Dimension(panelWidth, 75));

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		graphStats_panel.add(titlebox);
		graphStats_panel.add(enableButton);
		graphStats_panel.add(computeAllButton);

		this.add(graphStats_panel);
	}

	public void setContent(VisualItem item) {
		setupTable();

		Image image;

		if (m_app.getImageField() != null) {
			String imageLocation = item.getString(m_app.getImageField());
			URL imageURL = IOLib.urlFromString(imageLocation);
			if (imageURL == null) {
				// System.err.println("Null image: " + imageLocation);
				image = null;
			} else
				image = Toolkit.getDefaultToolkit().createImage(imageURL);

			image = image.getScaledInstance(100, -1, Image.SCALE_SMOOTH);

			ImageIcon icon = null;
			if (image != null) {
				icon = new ImageIcon(image);

				label.setIcon(icon);
			}
		}

		Node node = (Node) item.getSourceTuple();

		int numColumns = 2;

		nodeDetailsTableModel.data = new Object[MEASURES.length + 1][numColumns];
		globalDetailsTableModel.data = new Object[GLOBAL_MEASURES.length][numColumns];

		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);

		// label.setText(item.getSourceTuple().getString(m_app.getLabelField()));

		// Graph m_graph = m_app.getGraph();
		// Table nodeTable = m_graph.getNodeTable();

		int i = 0;
		nodeDetailsTableModel.setValueAt("Label", i, 0);
		nodeDetailsTableModel.setValueAt(node.getString(m_app.getLabelField()), i, 1);
		i++;

		for (String measure : MEASURES) {
			int col;
			if ((col = node.getColumnIndex(measure)) >= 0) {
				// nodeTable.addColumn(measure, int.class);
				nodeDetailsTableModel.setValueAt(measure, i, 0);
				if (node.canGetInt(measure)) {
					nodeDetailsTableModel.setValueAt(node.getInt(col), i, 1);
				} else if (node.canGetDouble(measure)) {
					nodeDetailsTableModel.setValueAt(df.format(node.getInt(col)), i, 1);
				}
				i++;
			}
		}

		/*
		 * for (int i = 0; i < node.getColumnCount(); i++) {
		 * 
		 * nodeDetailsTableModel.setValueAt(node.getColumnName(i), i, 0); if (node.getColumnType(i) == double.class) {
		 * // node.canGetDouble(node.getColumnName(i))) { nodeDetailsTableModel.setValueAt(df.format(node.getDouble(i)),
		 * i, 1);
		 * 
		 * } else {
		 * 
		 * nodeDetailsTableModel.setValueAt(node.get(i), i, 1); }
		 * 
		 * }
		 */

		i = 0;
		for (String measure : GLOBAL_MEASURES) {
			// nodeTable.addColumn(measure, int.class);
			globalDetailsTableModel.setValueAt(measure, i, 0);
			globalDetailsTableModel.setValueAt(globalMeasures[i], i, 1);// XXX int vs double
			i++;
		}

		/*
		 * for (int i = 0; i < node.getColumnCount(); i++) {
		 * 
		 * nodeDetailsTableModel.setValueAt(node.getColumnName(i), i, 0); if (node.getColumnType(i) == double.class) {
		 * // node.canGetDouble(node.getColumnName(i))) { nodeDetailsTableModel.setValueAt(df.format(node.getDouble(i)),
		 * i, 1);
		 * 
		 * } else {
		 * 
		 * nodeDetailsTableModel.setValueAt(node.get(i), i, 1); }
		 * 
		 * }
		 */

		// XXX Find a good event to add
		// m_app.addEventToHistory(GRAPH_READABILITY_STEP_NUMBER, GRAPH_READABILITY_ACTION, GRAPH_READABILITY_ACTION,
		// 1);
		// ------------------------------------
		this.revalidate();
		this.repaint();

	}

	public void setupTable() {
		Graph m_graph = m_app.getGraph();
		Table nodeTable = m_graph.getNodeTable();
		for (String measure : MEASURES) {
			if (nodeTable.getColumn(measure) == null) {
				nodeTable.addColumn(measure, int.class);
			}
		}

		// XXX Edges?
	}

	public boolean setDoubleMeasure(Tuple t, String measure, double value) {
		Graph m_graph = m_app.getGraph();
		Table table = null;
		if (t instanceof Node)
			table = m_graph.getNodeTable();
		else if (t instanceof Edge)
			return false;// table = m_graph.getEdgeTable();
		else
			return false;

		if (!table.canSetDouble(measure)) {
			throw new IllegalArgumentException("Can't set double for measure: " + measure + "("
					+ table.getColumnType(measure) + ")");
		}

		table.getDouble(t.getRow(), measure);
		table.setDouble(t.getRow(), measure, value);
		return true;
	}

	public boolean setIntMeasure(Tuple t, String measure, int value) {
		Graph m_graph = m_app.getGraph();
		Table table = null;
		if (t instanceof Node)
			table = m_graph.getNodeTable();
		else if (t instanceof Edge)
			return false;// table = m_graph.getEdgeTable();
		else
			return false;

		if (!table.canSetInt(measure)) {
			throw new IllegalArgumentException("Can't set int for measure: " + measure + "("
					+ table.getColumnType(measure) + ")");
		}

		table.getInt(t.getRow(), measure);
		table.setInt(t.getRow(), measure, value);
		return true;
	}

	/**
	 * Takes a TableNodeItem and finds all the node overplots it has in the graph.
	 * 
	 * @param n
	 *            the TableNodeItem
	 * @param matches
	 *            a HashSet<VisualItem> to put matching nodes into
	 * @param printTotals
	 *            Do we print totals for the measure?
	 * @param printDetails
	 *            Do we print details of each find?
	 * @return a count of the matches
	 */
	public int getNodeOverplot(TableNodeItem n, HashSet<VisualItem> matches, boolean printTotals, boolean printDetails) {
		if (printTotals)
			System.out.println(NODE_OCCLUSION + ":");
		TupleSet nodes = m_vis.getGroup(PrefuseLib.getGroupName(graph, Graph.NODES));
		if (nodes == null)
			return 0;
		Rectangle2D nodeABounds = n.getBounds();
		int overplots = 0;
		Iterator<?> nodeIter = nodes.tuples();
		while (nodeIter.hasNext()) {
			TableNodeItem nodeB = (TableNodeItem) nodeIter.next();
			if (n == nodeB)
				continue;
			if (nodeABounds.intersects(nodeB.getBounds())) {
				if (printDetails) {
					String label = nodeB.getSourceTuple().getString(m_app.getLabelField());
					if (label == null)
						label = nodeB.toString();
					System.out.printf("OVERPLOT w/" + label + " @ (% 7.2f, % 7.2f)\n", nodeB.getX(), nodeB.getY());
				}
				matches.add(nodeB);
				overplots++;
			}
		}
		if (printTotals)
			System.out.println("Total: " + overplots);
		return overplots;
	}

	// XXX speed up from n^2 to n(n-1)/2
	public int getAllNodeOverplots(HashSet<VisualItem> matches, boolean printTotals, boolean printDetails) {
		if (printTotals)
			System.out.println(NODE_OCCLUSION + "(ALL):");
		TupleSet nodes = m_vis.getGroup(PrefuseLib.getGroupName(graph, Graph.NODES));
		if (nodes == null)
			return 0;
		int overplots = 0;
		Iterator<?> nodeIter = nodes.tuples();
		while (nodeIter.hasNext()) {
			TableNodeItem nodeA = (TableNodeItem) nodeIter.next();
			overplots += getNodeOverplot(nodeA, matches, false, false);
		}
		if (printTotals)
			System.out.println("Total: " + overplots / 2);
		if ((overplots % 2) != 0) {
			System.err.println("need overplot division attention");
		}
		return overplots / 2;
	}

	/**
	 * Takes a TableNodeItem and finds all the edge tunnels it directly causes.
	 * 
	 * @param n
	 *            the TableNodeItem
	 * @param matches
	 *            a HashSet<VisualItem> to put matching edges into
	 * @param printTotals
	 *            Do we print totals for the measure?
	 * @param printDetails
	 *            Do we print details of each find?
	 * @return a count of the matches
	 */
	public int getLocalTunnels(TableNodeItem n, HashSet<VisualItem> matches, boolean printTotals, boolean printDetails) {
		if (printTotals)
			System.out.println(LOCAL_EDGE_TUNNELS + ":");
		TupleSet edges = m_vis.getGroup(PrefuseLib.getGroupName(graph, Graph.EDGES));
		if (edges == null)
			return 0;
		Rectangle2D nodeABounds = n.getBounds();
		int overplots = 0;
		Iterator<?> edgeIter = edges.tuples();
		while (edgeIter.hasNext()) {
			TableEdgeItem edgeB = (TableEdgeItem) edgeIter.next();

			TableNodeItem srcB;
			TableNodeItem tarB;
			try {
				srcB = (TableNodeItem) edgeB.getSourceItem();
				tarB = (TableNodeItem) edgeB.getTargetItem();
			} catch (IllegalArgumentException e) {
				// edgeB.setStrokeColor(prefuse.util.ColorLib.rgb(255,0, 0));
				// edgeB.setFillColor(prefuse.util.ColorLib.rgb(0,255, 0));
				System.out.println("Problem (GLT):" + edgeB);
				continue;
			}

			double srcBx = srcB.getX();
			double srcBy = srcB.getY();
			double tarBx = tarB.getX();
			double tarBy = tarB.getY();
			Line2D.Double edgeBLine = new Line2D.Double(srcBx, srcBy, tarBx, tarBy);
			if (n == srcB || n == tarB)
				continue;
			if (edgeBLine.intersects(nodeABounds)) {
				if (printDetails) {
					System.out.printf("TUNNEL: edge - (% 7.2f, % 7.2f)->(% 7.2f, % 7.2f) \n", srcBx, srcBy, tarBx,
							tarBy);
				}
				matches.add(edgeB);
				overplots++;
			}
		}
		if (printTotals)
			System.out.println("Total: " + overplots);
		return overplots;
	}

	// XXX speed up from n^2 to n(n-1)/2
	public int getAllLocalTunnels(HashSet<VisualItem> matches, boolean printTotals, boolean printDetails) {
		if (printTotals)
			System.out.println(LOCAL_EDGE_TUNNELS + "(ALL):");
		TupleSet nodes = m_vis.getGroup(PrefuseLib.getGroupName(graph, Graph.NODES));
		if (nodes == null)
			return 0;
		int overplots = 0;
		Iterator<?> nodeIter = nodes.tuples();
		while (nodeIter.hasNext()) {
			TableNodeItem nodeA = (TableNodeItem) nodeIter.next();
			overplots += getLocalTunnels(nodeA, matches, false, false);
		}
		if (printTotals)
			System.out.println("Total: " + overplots);
		return overplots;
	}

	/**
	 * Takes a TableNodeItem and finds all the node tunnels its edges have in the graph.
	 * 
	 * @param n
	 *            the TableNodeItem
	 * @param matches
	 *            a HashSet<VisualItem> to put matching nodes into
	 * @param printTotals
	 *            Do we print totals for the measure?
	 * @param printDetails
	 *            Do we print details of each find?
	 * @return a count of the matches
	 */
	public int getTriggeredTunnels(TableNodeItem n, HashSet<VisualItem> matches, boolean printTotals,
			boolean printDetails) {
		if (printTotals)
			System.out.println(TRIGGERED_EDGE_TUNNELS + ":");
		TupleSet nodes = m_vis.getGroup(PrefuseLib.getGroupName(graph, Graph.NODES));
		if (nodes == null)
			return 0;
		Iterator<?> nEdgeIter = n.edges();
		int i = 0;
		int tunnels = 0;
		while (nEdgeIter.hasNext()) {
			TableEdgeItem edgeA = (TableEdgeItem) nEdgeIter.next();

			TableNodeItem srcA;
			TableNodeItem tarA;
			try {
				srcA = (TableNodeItem) edgeA.getSourceItem();
				tarA = (TableNodeItem) edgeA.getTargetItem();
			} catch (IllegalArgumentException e) {
				// edgeA.setStrokeColor(prefuse.util.ColorLib.rgb(255,0, 0));
				// edgeA.setFillColor(prefuse.util.ColorLib.rgb(0,255, 0));
				System.out.println("Problem (GTT):" + edgeA);
				continue;
			}

			double srcAx = srcA.getX();
			double srcAy = srcA.getY();
			double tarAx = tarA.getX();
			double tarAy = tarA.getY();
			Iterator<?> nodeIter = nodes.tuples();
			Line2D.Double edgeALine = new Line2D.Double(srcAx, srcAy, tarAx, tarAy);
			while (nodeIter.hasNext()) {
				TableNodeItem nodeB = (TableNodeItem) nodeIter.next();
				if (n == nodeB || nodeB == srcA || nodeB == tarA)
					continue;
				if (edgeALine.intersects(nodeB.getBounds())) {
					if (printDetails) {
						String label = nodeB.getSourceTuple().getString(m_app.getLabelField());
						if (label == null)
							label = nodeB.toString();
						System.out.printf("TUNNEL: edge %d @ (% 7.2f, % 7.2f) - (% 7.2f, % 7.2f)->(% 7.2f, % 7.2f) & "
								+ label + "\n", i, nodeB.getX(), nodeB.getY(), srcAx, srcAy, tarAx, tarAy);
					}
					matches.add(nodeB);
					tunnels++;
				}
			}
			i++;
		}
		if (printTotals)
			System.out.println("Total: " + tunnels);
		return tunnels;
	}

	/**
	 * Takes a TableNodeItem and finds all the intersections its edges have with other edges in the graph.
	 * 
	 * @param n
	 *            the TableNodeItem
	 * @param matches
	 *            a HashSet<VisualItem> to put matching edges into
	 * @param printTotals
	 *            Do we print totals for the measure?
	 * @param printDetails
	 *            Do we print details of each find?
	 * @return a count of the matches
	 */
	public int getEdgeIntersections(TableNodeItem n, HashSet<VisualItem> matches, boolean printTotals,
			boolean printDetails) {
		if (printTotals)
			System.out.println(EDGE_CROSSING + ":");
		TupleSet edges = m_vis.getGroup(PrefuseLib.getGroupName(graph, Graph.EDGES));

		// System.out.println(edges);

		if (edges == null)
			return 0;
		Iterator<?> nEdgeIter = n.edges();
		int i = 0;

		int intersections = 0;

		while (nEdgeIter.hasNext()) {
			TableEdgeItem edgeA = (TableEdgeItem) nEdgeIter.next();

			TableNodeItem srcA;
			TableNodeItem tarA;
			try {
				srcA = (TableNodeItem) edgeA.getSourceItem();
				tarA = (TableNodeItem) edgeA.getTargetItem();
			} catch (IllegalArgumentException e) {
				// edgeA.setStrokeColor(prefuse.util.ColorLib.rgb(255,0, 0));
				// edgeA.setFillColor(prefuse.util.ColorLib.rgb(0,255, 0));
				System.out.println("Problem (GEI):" + edgeA);
				continue;
			}

			double srcAx = srcA.getX();
			double srcAy = srcA.getY();
			double tarAx = tarA.getX();
			double tarAy = tarA.getY();
			Iterator<?> edgeIter = edges.tuples();
			while (edgeIter.hasNext()) {
				TableEdgeItem edgeB = (TableEdgeItem) edgeIter.next();
				if (edgeA == edgeB)
					continue;

				TableNodeItem srcB;
				TableNodeItem tarB;
				try {
					srcB = (TableNodeItem) edgeB.getSourceItem();
					tarB = (TableNodeItem) edgeB.getTargetItem();
				} catch (IllegalArgumentException e) {
					// edgeB.setStrokeColor(prefuse.util.ColorLib.rgb(255,0, 0));
					// edgeB.setFillColor(prefuse.util.ColorLib.rgb(0,255, 0));
					System.out.println("Problem (GEI2):" + edgeB);
					continue;
				}

				double srcBx = srcB.getX();
				double srcBy = srcB.getY();
				double tarBx = tarB.getX();
				double tarBy = tarB.getY();
				if (Line2D.linesIntersect(srcAx, srcAy, tarAx, tarAy, srcBx, srcBy, tarBx, tarBy)) {
					if (!((Math.abs(srcAx - srcBx) < NOISE && Math.abs(srcAy - srcBy) < NOISE)
							|| // XXX NODE SIZE?
							(Math.abs(srcAx - tarBx) < NOISE && Math.abs(srcAy - tarBy) < NOISE)
							|| (Math.abs(tarAx - srcBx) < NOISE && Math.abs(tarAy - srcBy) < NOISE) || (Math.abs(tarAx
							- tarBx) < NOISE && Math.abs(tarAy - tarBy) < NOISE))) {
						if (printDetails) {
							System.out
									.printf(
											"INTERSECT: edge %d - (% 7.2f, % 7.2f)->(% 7.2f, % 7.2f) & (% 7.2f, % 7.2f)->(% 7.2f, % 7.2f)\n",
											i, srcAx, srcAy, tarAx, tarAy, srcBx, srcBy, tarBx, tarBy);
						}
						matches.add(edgeB);
						intersections++;
					}
				}

				/*
				 * Point2D crossing = new Point2D.Double();
				 * 
				 * int retVal = intersectLineLine(x1a, y1a, x2a, y2a, x1b, y1b, x2b, y2b, crossing); if(retVal ==
				 * NO_INTERSECTION){ //System.out.print(" - No Intersection\n"); } else if(retVal == COINCIDENT ||
				 * retVal == INTERSECT){ if( !( (Math.abs(x1a - x1b) < NOISE && Math.abs(y1a - y1b) < NOISE) ||//XXX
				 * NODE SIZE? (Math.abs(x1a - x2b) < NOISE && Math.abs(y1a - y2b) < NOISE) || (Math.abs(x2a - x1b) <
				 * NOISE && Math.abs(y2a - y1b) < NOISE) || (Math.abs(x2a - x2b) < NOISE && Math.abs(y2a - y2b) <
				 * NOISE))){ System.out.printf( (retVal == COINCIDENT ? "COINCIDENT:" : "INTERSECT:" ) +
				 * " edge %d @ (% 7.2f,% 7.2f) - (% 7.2f, % 7.2f)->(% 7.2f, % 7.2f) & (% 7.2f, % 7.2f)->(% 7.2f, % 7.2f)\n"
				 * , i, crossing.getX(), crossing.getY(), x1a, y1a, x2a, y2a, x1b, y1b, x2b, y2b); intersections++; } }
				 * else if(retVal == PARALLEL){ System.out.printf( "PARALLEL:  " +
				 * " edge %d @ (% 7.2f,% 7.2f) - (% 7.2f, % 7.2f)->(% 7.2f, % 7.2f) & (% 7.2f, % 7.2f)->(% 7.2f, % 7.2f)\n"
				 * , i, crossing.getX(), crossing.getY(), x1a, y1a, x2a, y2a, x1b, y1b, x2b, y2b); intersections++; }
				 * else { System.out.println("WTF"); //throw new
				 * IllegalStateException("Invalid return from GraphicsLib.intersectLineLine"); }
				 */
			}
			i++;
		}
		if (printTotals)
			System.out.println("Total: " + intersections);
		return intersections;
	}

	public int getAllEdgeIntersections(HashSet<VisualItem> matches, boolean printTotals, boolean printDetails) {
		if (printTotals)
			System.out.println(EDGE_CROSSING + "(ALL):");
		TupleSet edges = m_vis.getGroup(PrefuseLib.getGroupName(graph, Graph.EDGES));

		if (edges == null)
			return 0;
		Iterator<?> nEdgeIter = edges.tuples();
		int i = 0;

		int intersections = 0;

		while (nEdgeIter.hasNext()) {
			TableEdgeItem edgeA = (TableEdgeItem) nEdgeIter.next();

			TableNodeItem srcA;
			TableNodeItem tarA;
			try {
				srcA = (TableNodeItem) edgeA.getSourceItem();
				tarA = (TableNodeItem) edgeA.getTargetItem();
			} catch (IllegalArgumentException e) {
				// edgeA.setStrokeColor(prefuse.util.ColorLib.rgb(255,0, 0));
				// edgeA.setFillColor(prefuse.util.ColorLib.rgb(0,255, 0));
				System.out.println("Problem (GAEI):" + edgeA);
				continue;
			}

			double srcAx = srcA.getX();
			double srcAy = srcA.getY();
			double tarAx = tarA.getX();
			double tarAy = tarA.getY();
			Iterator<?> edgeIter = edges.tuples();
			while (edgeIter.hasNext()) {
				TableEdgeItem edgeB = (TableEdgeItem) edgeIter.next();
				if (edgeA == edgeB)
					continue;
				TableNodeItem srcB = (TableNodeItem) edgeB.getSourceItem();
				TableNodeItem tarB = (TableNodeItem) edgeB.getTargetItem();
				double srcBx = srcB.getX();
				double srcBy = srcB.getY();
				double tarBx = tarB.getX();
				double tarBy = tarB.getY();
				if (Line2D.linesIntersect(srcAx, srcAy, tarAx, tarAy, srcBx, srcBy, tarBx, tarBy)) {
					if (!((Math.abs(srcAx - srcBx) < NOISE && Math.abs(srcAy - srcBy) < NOISE)
							|| // XXX NODE SIZE?
							(Math.abs(srcAx - tarBx) < NOISE && Math.abs(srcAy - tarBy) < NOISE)
							|| (Math.abs(tarAx - srcBx) < NOISE && Math.abs(tarAy - srcBy) < NOISE) || (Math.abs(tarAx
							- tarBx) < NOISE && Math.abs(tarAy - tarBy) < NOISE))) {
						if (printDetails) {
							System.out
									.printf(
											"INTERSECT: edge %d - (% 7.2f, % 7.2f)->(% 7.2f, % 7.2f) & (% 7.2f, % 7.2f)->(% 7.2f, % 7.2f)\n",
											i, srcAx, srcAy, tarAx, tarAy, srcBx, srcBy, tarBx, tarBy);
						}
						matches.add(edgeB);
						intersections++;
					}
				}
			}
			i++;
		}

		if (printTotals)
			System.out.println("Total: " + intersections / 2);
		if ((intersections % 2) != 0) {
			System.err.println("need overplot division attention");
		}
		return intersections / 2;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == enableButton) {
			boolean nowEnabled = (enableButton.getText() == ENABLE);
			enableButton.setText(nowEnabled ? DISABLE : ENABLE);
			m_app.setRealTimeGraphReadabilityMetrics(nowEnabled);
			if (nowEnabled) {

				System.out.println("Enabled");

			} else {
				System.out.println("Disabled");
			}
		} else if (e.getSource() == computeAllButton) {// HACK
			setupTable();
			TupleSet nodes = m_vis.getGroup(PrefuseLib.getGroupName(graph, Graph.NODES));
			if (nodes != null) {
				Iterator<?> nodeIter = nodes.tuples();
				while (nodeIter.hasNext()) {
					TableNodeItem n = (TableNodeItem) nodeIter.next();

					int ei = getEdgeIntersections(n, new HashSet<VisualItem>(), false, false);
					setIntMeasure(n, EDGE_CROSSING, ei);

					int it = getTriggeredTunnels(n, new HashSet<VisualItem>(), false, false);
					setIntMeasure(n, TRIGGERED_EDGE_TUNNELS, it);

					int dt = getLocalTunnels(n, new HashSet<VisualItem>(), false, false);
					setIntMeasure(n, LOCAL_EDGE_TUNNELS, dt);

					int no = getNodeOverplot(n, new HashSet<VisualItem>(), false, false);
					setIntMeasure(n, NODE_OCCLUSION, no);

				}
			}
		}
	}
}
