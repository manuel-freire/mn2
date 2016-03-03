package edu.umd.cs.hcil.socialaction.ui.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import prefuse.data.Graph;
import prefuse.data.Node;
import edu.umd.cs.hcil.socialaction.SocialAction;
import edu.umd.cs.hcil.socialaction.jung.algorithms.cluster.ClusterSet;
import edu.umd.cs.hcil.socialaction.jung.algorithms.cluster.WeakComponentClusterer;
import edu.umd.cs.hcil.socialaction.jung.statistics.GraphStatistics;
import edu.umd.cs.hcil.socialaction.ui.tables.GraphTableModel;

/**
 * Displays a list of stats about the Network
 * 
 * @version 1.0
 * @author Adam Perer
 */
public class NetworkOverviewPanel extends JPanel implements ActionListener {

	/** Never change */
	private static final long serialVersionUID = -7665160492240982740L;

	public final static int OVERVIEW_STEP_NUMBER = 1;

	public final static String OVERVIEW_STATE = "Overview";

	public final static String OVERVIEW_ACTION = "View";

	private GraphTableModel nodeOverviewTableModel, networkOverviewTableModel, edgeOverviewTableModel,
			classOverviewTableModel;

	private String[] columnNames = { "Stat", "Value" };

	private SocialAction m_app;

	private Graph graph;

	private int panelWidth;

	public NetworkOverviewPanel(SocialAction app, Graph graph, int width) {
		this.m_app = app;
		this.graph = graph;
		this.panelWidth = width;

		initUI();
		setContent();
	}

	JXTaskPane classOverview;

	private void initUI() {

		final Box graphStats_panel = Box.createVerticalBox();
		graphStats_panel.setBorder(BorderFactory.createTitledBorder(SocialAction.OVERVIEW_PANEL));

		// a container to put all JXTaskPane together
		JXTaskPaneContainer taskPaneContainer = new JXTaskPaneContainer();

		JXTaskPane networkOverview = new JXTaskPane();
		networkOverview.setTitle("Network Overview");
		taskPaneContainer.add(networkOverview);

		networkOverviewTableModel = new GraphTableModel(columnNames);

		JXTable networkTable = new JXTable(networkOverviewTableModel);
		// networkTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
		networkTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		networkTable.setBackground(null);
		networkTable.setSortable(true);

		networkOverview.add(networkTable);

		JXTaskPane nodeOverview = new JXTaskPane();
		nodeOverview.setTitle("Node Overview");
		taskPaneContainer.add(nodeOverview);

		nodeOverviewTableModel = new GraphTableModel(columnNames);

		JXTable nodeTable = new JXTable(nodeOverviewTableModel);
		nodeTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
		nodeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		nodeTable.setBackground(null);
		nodeTable.setSortable(true);
		nodeTable.packAll();

		nodeOverview.add(nodeTable);

		// create another taskPane, it will show details of the selected file
		JXTaskPane edgeOverview = new JXTaskPane();
		edgeOverview.setTitle("Edge Overview");
		taskPaneContainer.add(edgeOverview);

		edgeOverviewTableModel = new GraphTableModel(columnNames);

		JXTable edgeTable = new JXTable(edgeOverviewTableModel);
		edgeTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
		edgeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		edgeTable.setBackground(null);
		edgeTable.setSortable(true);
		edgeTable.packAll();

		edgeOverview.add(edgeTable);

		classOverview = new JXTaskPane();
		classOverview.setTitle("Class Overview");
		taskPaneContainer.add(classOverview);

		classOverviewTableModel = new GraphTableModel(columnNames);

		JXTable classTable = new JXTable(classOverviewTableModel);
		classTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
		classTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		classTable.setBackground(null);
		classTable.setSortable(true);
		classTable.packAll();

		classOverview.add(classTable);

		classOverview.setVisible(false);

		// put the action list on the left
		graphStats_panel.add(taskPaneContainer, BorderLayout.EAST);

		// and a file browser in the middle
		// graphStats_panel.add(fileBrowser, BorderLayout.CENTER);

		Box titlebox = new Box(BoxLayout.Y_AXIS);// BoxLayout.X_AXIS);
		titlebox.add(Box.createHorizontalStrut(2));

		titlebox.add(Box.createHorizontalGlue());
		titlebox.setMaximumSize(new Dimension(panelWidth, 75));

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		graphStats_panel.add(titlebox);

		// String[] columnNames = { "Stat", "Value" };
		// tableModel = new GraphTableModel(columnNames);
		//
		// TableSorter sorter = new TableSorter(tableModel);
		// graphStatsTable = new JTable(sorter);
		// sorter.setTableHeader(graphStatsTable.getTableHeader());
		// graphStatsTable.setPreferredScrollableViewportSize(new Dimension(panelWidth, 70));
		//
		// // Set up tool tips for column headers.
		// graphStatsTable.getTableHeader().setToolTipText(
		// "Click to specify sorting; Control-Click to specify secondary sorting");
		//
		// // Create the scroll pane and add the table to it.
		// JScrollPane scrollPane = new JScrollPane(graphStatsTable);
		//
		// graphStatsTable.setOpaque(true); // content panes must be opaque
		// graphStats_panel.add(scrollPane);

		this.add(graphStats_panel);

	}

	public void actionPerformed(ActionEvent e) {

		// if (e.getSource() == layoutButton) {
		// this.sne.toggleAnimation();
		// }

		// setContent();

	}

	public void setContent(Graph graph) {
		this.graph = graph;
		setContent();

	}

	public void setContent() {

		int numColumns = 2;

		String networkOverviewStats[] = { "Density", "Diameter", "# of Components" };
		networkOverviewTableModel.data = new Object[networkOverviewStats.length][numColumns];

		String nodeOverviewStats[] = { "# of Nodes", "Avg. Degree", "Highest Degree", "Avg. In-degree",
				"Avg. Out-degree", "# of Isolates" };
		nodeOverviewTableModel.data = new Object[nodeOverviewStats.length][numColumns];

		String edgeOverviewStats[] = { "# of Edges", "# of Bridges", "Avg. Path Length", "Avg. Weight" };
		edgeOverviewTableModel.data = new Object[edgeOverviewStats.length][numColumns];

		String classOverviewStats[] = { "None", "None" };
		classOverviewTableModel.data = new Object[classOverviewStats.length][numColumns];

		if (graph == null)
			return;

		int numEdges = graph.getEdgeCount();
		int numNodes = graph.getNodeCount();
		double density = (2.0 * numEdges / (numNodes * (1.0 * numNodes - 1)));
		double diameter = 0;// GraphStatistics.diameter(graph, true);

		if (numNodes < 500)
			diameter = GraphStatistics.diameter(graph, true);

		int maxDegree = 0;
		int totalInDegree = 0, totalOutDegree = 0;
		int totalDegree = 0;
		int numIsolates = 0;
		int[][] classTotalInDegree = null;
		int[][] classTotalOutDegree = null;
		double[][] classAvgInDegree = null;
		double[][] classAvgOutDegree = null;
		int numUnique = 0;
		int[] classCount = null;

		boolean useClass = false;

		// if (sne.getClassField() != null) {
		//
		// Column column = graph.getNodeTable().getColumn(sne.getClassField());
		//
		// if (!column.canGetInt()) {
		// System.err.println("Class only works for Int Columns");
		// classOverview.setVisible(false);
		//
		// } else {
		// useClass = true;
		// classOverview.setVisible(true);
		// HashSet classSet = new HashSet();
		//
		// for (int i = 0; i < column.getRowCount(); i++) {
		// classSet.add(column.get(i));
		// }
		//
		//                
		//                
		// numUnique = classSet.size();
		// classOverviewStats = new String[(numUnique*numUnique)*2];
		//                
		classTotalInDegree = new int[numUnique][numUnique];
		classTotalOutDegree = new int[numUnique][numUnique];
		classAvgInDegree = new double[numUnique][numUnique];
		classAvgOutDegree = new double[numUnique][numUnique];
		classCount = new int[numUnique];
		//
		//
		// for (int i = 0; i < numUnique; i++) {
		// classCount[i] = 0;
		// for (int j = 0; j < numUnique; j++) {
		// classTotalInDegree[i][j] = 0;
		// classTotalOutDegree[i][j] = 0;
		// classAvgInDegree[i][j] = 0;
		// classAvgOutDegree[i][j] = 0;
		// classOverviewStats[((i*numUnique)+j)*2] = "Avg. In-degree " + i + "," + j;
		// classOverviewStats[(((i*numUnique)+j)*2)+1] = "Avg. Out-degree " + i + "," + j;
		// }
		// }
		//                
		// classOverviewTableModel.data = new Object[classOverviewStats.length][numColumns];
		//
		// }
		// } else
		// classOverview.setVisible(false);

		for (Iterator it = graph.nodes(); it.hasNext();) {
			Node n = (Node) it.next();
			int degree = graph.getDegree(n);
			int inDegree = graph.getInDegree(n);
			int outDegree = graph.getOutDegree(n);
			if (degree > maxDegree)
				maxDegree = degree;

			totalDegree += degree;
			totalInDegree += inDegree;
			totalOutDegree += outDegree;

			if (useClass) {
				int currentClassType = n.getInt(m_app.getClassField());

				classCount[currentClassType]++;

				for (Iterator i = n.inNeighbors(); i.hasNext();) {
					Node neighbor = (Node) i.next();
					int classType = neighbor.getInt(m_app.getClassField());
					classTotalInDegree[currentClassType][classType]++;
				}

				for (Iterator i = n.outNeighbors(); i.hasNext();) {
					Node neighbor = (Node) i.next();
					int classType = neighbor.getInt(m_app.getClassField());
					classTotalOutDegree[currentClassType][classType]++;
				}

			}
			if (degree == 0)
				numIsolates++;

		}

		if (useClass) {
			for (int i = 0; i < numUnique; i++)
				for (int j = 0; j < numUnique; j++) {
					classAvgInDegree[i][j] = (1.0d * classTotalInDegree[i][j]) / (1.0d * classCount[i]);
					classAvgOutDegree[i][j] = (1.0d * classTotalOutDegree[i][j]) / (1.0d * classCount[i]);
					System.out.println(i + "," + j + ": " + "in : " + classAvgInDegree[i][j]);
					System.out.println(i + "," + j + ": " + "out : " + classAvgOutDegree[i][j]);
				}

		}
		double avgDegree = (1.0d * totalDegree) / (1.0d * graph.getNodeCount());
		double avgInDegree = (1.0d * totalInDegree) / (1.0d * graph.getNodeCount());
		double avgOutDegree = (1.0d * totalOutDegree) / (1.0d * graph.getNodeCount());

		// ClusterSet components = new ClusterSet();

		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);

		for (int i = 0; i < networkOverviewStats.length; i++) {
			networkOverviewTableModel.setValueAt(networkOverviewStats[i], i, 0);
			if (i == 0)
				networkOverviewTableModel.setValueAt(Double.parseDouble(df.format(density)), i, 1);
			else if (i == 1)
				networkOverviewTableModel.setValueAt(new Double(diameter), i, 1);
			else if (i == 2) {

				if (numNodes < 500) {
					ClusterSet components = new WeakComponentClusterer().extract(graph);

					networkOverviewTableModel.setValueAt(new Double(components.size()), i, 1);
				} else
					networkOverviewTableModel.setValueAt(new Double(0), i, 1);
			} else
				networkOverviewTableModel.setValueAt(new Integer(0), i, 1);
		}

		for (int i = 0; i < nodeOverviewStats.length; i++) {
			nodeOverviewTableModel.setValueAt(nodeOverviewStats[i], i, 0);
			if (i == 0)
				nodeOverviewTableModel.setValueAt(new Integer(numNodes), i, 1);
			else if (i == 1)
				nodeOverviewTableModel.setValueAt(df.format(avgDegree), i, 1);
			else if (i == 2)
				nodeOverviewTableModel.setValueAt(new Integer(maxDegree), i, 1);
			else if (i == 3)
				nodeOverviewTableModel.setValueAt(df.format(avgInDegree), i, 1);
			else if (i == 4)
				nodeOverviewTableModel.setValueAt(df.format(avgOutDegree), i, 1);
			else if (i == 5)
				nodeOverviewTableModel.setValueAt(new Integer(numIsolates), i, 1);
			else
				nodeOverviewTableModel.setValueAt(new Integer(0), i, 1);
		}

		for (int i = 0; i < edgeOverviewStats.length; i++) {
			edgeOverviewTableModel.setValueAt(edgeOverviewStats[i], i, 0);
			if (i == 0)
				edgeOverviewTableModel.setValueAt(new Integer(numEdges), i, 1);
			else
				edgeOverviewTableModel.setValueAt("N/A", i, 1);
		}

		if (useClass) {
			for (int i = 0; i < numUnique; i++)
				for (int j = 0; j < numUnique; j++) {
					classOverviewTableModel.setValueAt(classOverviewStats[((i * numUnique) + j) * 2],
							(((i * numUnique) + j) * 2), 0);
					classOverviewTableModel.setValueAt(df.format(classAvgInDegree[i][j]), (((i * numUnique) + j) * 2),
							1);
					classOverviewTableModel.setValueAt(classOverviewStats[((((i * numUnique) + j) * 2)) + 1],
							(((i * numUnique) + j) * 2) + 1, 0);
					classOverviewTableModel.setValueAt(df.format(classAvgOutDegree[i][j]),
							(((i * numUnique) + j) * 2) + 1, 1);
				}
		} else {
			classOverviewTableModel.setValueAt("None", 0, 0);
			classOverviewTableModel.setValueAt(new Integer(0), 0, 1);
		}

		// if (sne.getClassField() != null) {
		//
		// Column column = graph.getNodeTable().getColumn(sne.getClassField());
		//
		// if (!column.canGetInt()) {
		// System.err.println("Class only works for Int Columns");
		//
		// } else {
		// HashSet classSet = new HashSet();
		//
		// for (int i = 0; i < column.getRowCount(); i++)
		// classSet.add(column.get(i));
		//                
		// int numUnique = classSet.size();
		// System.out.println("Unique Class Values: " + numUnique);
		//                
		// // int[][] inDegree = new int[numUnique][numUnique];
		//                
		// Iterator i = classSet.iterator();
		// while (i.hasNext()) {
		// Iterator j = classSet.iterator();
		// int iClass = (Integer) i.next();
		// while (j.hasNext()) {
		// int jClass = (Integer) j.next();
		//                        
		// for (Iterator it = graph.nodes(); it.hasNext();) {
		// Node n = (Node) it.next();
		// if (n.getInt(sne.getClassField()) != iClass) {
		// break;
		// }
		// n.getDegree();
		//                        
		//                        
		// }
		// }
		//                
		//                
		//                
		// }
		// }

		// double diameter = 0;
		//            
		// // GraphStatistics.diameter(this.graph.jungGraph, new UnweightedShortestPath(
		// // (Graph) this.graph.jungGraph), true);
		//
		// nodeOverviewTableModel.setValueAt(new Double(diameter), 2, 1);

		// boolean isConnected = false; //graph.isDirected();//GraphProperties.isConnected(this.graph.jungGraph);
		// int intConnected;
		// if (isConnected)
		// intConnected = 1;
		// else
		// intConnected = 0;
		// nodeOverviewTableModel.setValueAt("Connected", 3, 0);
		// nodeOverviewTableModel.setValueAt(new Integer(intConnected), 3, 1);
		//
		// double numEdges = graph.getEdgeCount();
		// double numNodes = graph.getNodeCount();
		// double density = (2 * numEdges / (numNodes * (numNodes - 1)));
		// nodeOverviewTableModel.setValueAt("Density", 4, 0);
		// nodeOverviewTableModel.setValueAt(new Double(density), 4, 1);

		setEvent();

	}

	public void setEvent() {
		Double[] blah = { 0.0, 1.0 };
		m_app.addEventToHistory(OVERVIEW_STEP_NUMBER, OVERVIEW_STATE, OVERVIEW_ACTION, blah);
	}

}