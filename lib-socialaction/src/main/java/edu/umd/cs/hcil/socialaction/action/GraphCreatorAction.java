package edu.umd.cs.hcil.socialaction.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.column.Column;
import prefuse.util.collections.IntIterator;
import edu.umd.cs.hcil.socialaction.SocialAction;

/**
 * An Action that imports graph, and then loads them into SocialAction
 * 
 * @author Adam Perer
 */
public class GraphCreatorAction extends Action {

	public static final String SRC = Graph.DEFAULT_SOURCE_KEY;

	public static final String TRG = Graph.DEFAULT_TARGET_KEY;

	public final static int LOAD_STEP_NUMBER = 0;

	public final static String LOAD_NETWORK_STATE = "Load Network";

	public final static String LOAD_NETWORK_TABLE_BIPARTITE = "Table Bipartite";

	// public final static String LOAD_NETWORK_TABLE = "Table Non-Bipartite";
	// public final static String LOAD_NETWORK_GRAPH_BIPARTITE = "Table Bipartite";
	public final static String LOAD_NETWORK_GRAPH = "Graph Non-Bipartite";

	public final static String LOAD_NETWORK_NODETABLE = "NodeTable EdgeTable Non-Bipartite";

	public final static String LOAD_NETWORK_EDGETABLE = "EdgeTable Non-Bipartite";

	public final static String LOAD_NETWORK_MATRIX = "Matrix Non-Bipartite";

	Graph m_importedGraph;

	Graph[] m_graphs;

	Table m_bipartiteTable;

	Table m_nodeTable, m_edgeTable, m_matrixTable, m_sagemanTable, m_senateTest;

	SocialAction m_app;

	String m_partitionA, m_partitionB;

	String m_multiplexField;

	String m_labelField;

	String m_edge1Field, m_edge2Field, m_nodeField;

	String m_edgeWeightField = null;

	boolean m_isBipartite;

	boolean m_isEdgeList = false;

	boolean m_isDirected = false;

	/**
	 * Constructor to set state for importing a table (bi-partite only)
	 * 
	 * @param app
	 *            Reference back to the main class
	 * @param vis
	 *            The Visualization
	 * @param bipartiteTable
	 *            The bipartite table
	 * @param partitionA
	 *            The first partition
	 * @param partitionB
	 *            The second partition
	 * @param multiplexField
	 *            The multiplex field
	 * @param edgeWeightField
	 *            The edge weight field
	 * @param isDirected
	 *            Is the graph directed?
	 */
	public GraphCreatorAction(SocialAction app, Visualization vis, Table bipartiteTable, String partitionA,
			String partitionB, String multiplexField, String edgeWeightField, boolean isDirected) {
		super(vis);

		m_app = app;
		m_bipartiteTable = bipartiteTable;
		m_app = app;
		m_partitionA = partitionA;
		m_partitionB = partitionB;
		m_multiplexField = multiplexField;
		m_edgeWeightField = edgeWeightField;
		m_isDirected = isDirected;

		m_isBipartite = true;

		m_app.addEventToHistory(LOAD_STEP_NUMBER, LOAD_NETWORK_STATE, LOAD_NETWORK_TABLE_BIPARTITE, m_bipartiteTable
				.getRowCount());
	}

	/**
	 * Constructor to set state for importing a graph (non-bipartite)
	 * 
	 * @param app
	 *            Reference back to the main class
	 * @param vis
	 *            The Visualization
	 * @param graph
	 *            The graph
	 * @param labelField
	 *            The label field
	 * @param multiplexField
	 *            The multiplex field
	 */
	public GraphCreatorAction(SocialAction app, Visualization vis, Graph graph, String labelField, String multiplexField) {
		super(vis);

		m_app = app;
		m_importedGraph = graph;
		m_app = app;
		m_multiplexField = multiplexField;
		m_labelField = labelField;

		m_isBipartite = false;

		m_app.addEventToHistory(LOAD_STEP_NUMBER, LOAD_NETWORK_STATE, LOAD_NETWORK_GRAPH, graph.getNodeCount());
	}

	/**
	 * Constructor to set state for importing a graph from an edge table and node table (non-bipartite)
	 * 
	 * @param app
	 *            Reference back to the main class
	 * @param vis
	 *            The Visualization
	 * @param nodeTable
	 *            The node table
	 * @param edgeTable
	 *            The edge table
	 * @param labelField
	 *            The label field
	 * @param nodeField
	 *            The node field
	 * @param edge1Field
	 *            The edge source field
	 * @param edge2Field
	 *            The edge target field
	 * @param multiplexField
	 *            The multiplex field
	 * @param edgeWeightField
	 *            The edge weight field
	 * @param isDirected
	 *            Is the graph directed?
	 */
	public GraphCreatorAction(SocialAction app, Visualization vis, Table nodeTable, Table edgeTable, String labelField,
			String nodeField, String edge1Field, String edge2Field, String multiplexField, String edgeWeightField,
			boolean isDirected) {
		super(vis);

		m_app = app;
		m_importedGraph = null;
		m_app = app;
		m_multiplexField = multiplexField;
		m_labelField = labelField;
		m_nodeTable = nodeTable;
		m_edgeTable = edgeTable;

		m_nodeField = nodeField;
		m_edge1Field = edge1Field;
		m_edge2Field = edge2Field;

		m_edgeWeightField = edgeWeightField;

		m_isBipartite = false;
		m_isDirected = isDirected;

		m_app.addEventToHistory(LOAD_STEP_NUMBER, LOAD_NETWORK_STATE, LOAD_NETWORK_NODETABLE, nodeTable.getRowCount());
	}

	/**
	 * Constructor to set state for importing a graph from an edge table (e.g. UCINET DL format)
	 * 
	 * @param app
	 *            Reference back to the main class
	 * @param vis
	 *            The Visualization
	 * @param edgeListTable
	 *            The edge table
	 * @param labelField
	 *            The label field
	 * @param multiplexField
	 *            The multiplex field
	 * @param edgeWeightField
	 *            The edge weight field
	 * @param isDirected
	 *            Is the graph directed?
	 */
	public GraphCreatorAction(SocialAction app, Visualization vis, Table edgeListTable, String labelField,
			String multiplexField, String edgeWeightField, boolean isDirected) {
		super(vis);

		m_app = app;
		m_importedGraph = null;
		m_app = app;
		m_multiplexField = multiplexField;
		m_labelField = labelField;
		// m_nodeTable = nodeTable;
		m_edgeTable = edgeListTable;

		// m_nodeField = nodeField;
		// m_edge1Field = edge1Field;
		// m_edge2Field = edge2Field;
		//        
		m_edgeWeightField = edgeWeightField;

		m_isBipartite = false;
		m_isEdgeList = true;

		m_app
				.addEventToHistory(LOAD_STEP_NUMBER, LOAD_NETWORK_STATE, LOAD_NETWORK_EDGETABLE, m_edgeTable
						.getRowCount());
	}

	/**
	 * Constructor to set state for importing a graph from a matrix
	 * 
	 * @param app
	 *            Reference back to the main class
	 * @param vis
	 *            The Visualization
	 * @param matrixTable
	 *            The table
	 */
	public GraphCreatorAction(SocialAction app, Visualization vis, Table matrixTable) {
		super(vis);

		m_app = app;
		m_importedGraph = null;
		m_app = app;
		m_matrixTable = matrixTable;
		// m_multiplexField = multiplexField;
		// m_labelField = labelField;
		// m_nodeTable = nodeTable;
		// m_edgeTable = edgeTable;

		m_isBipartite = false;

		m_app.addEventToHistory(LOAD_STEP_NUMBER, LOAD_NETWORK_STATE, LOAD_NETWORK_MATRIX, matrixTable.getRowCount());
	}

	/**
	 * Constructor to set state for importing a graph from Sageman test
	 * 
	 * @param app
	 *            Reference back to the main class
	 * @param vis
	 *            The Visualization
	 * @param sagemanTable
	 *            The table from Sageman
	 * @param isSageman
	 *            Is this the Sageman data set
	 */
	public GraphCreatorAction(SocialAction app, Visualization vis, Table sagemanTable, boolean isSageman) {
		super(vis);

		m_app = app;
		m_importedGraph = null;
		m_app = app;
		m_sagemanTable = sagemanTable;

		// m_multiplexField = multiplexField;
		// m_labelField = labelField;
		// m_nodeTable = nodeTable;
		// m_edgeTable = edgeTable;

		m_isBipartite = false;
		m_isDirected = true;

		m_app.addEventToHistory(LOAD_STEP_NUMBER, LOAD_NETWORK_STATE, "Sageman", sagemanTable.getRowCount());
	}

	/**
	 * Constructor to set state for importing a graph from senate test
	 * 
	 * @param sa
	 *            Reference back to the main class
	 * @param vis
	 *            The Visualization
	 * @param senateTestTable
	 *            The table from the senate data set
	 * @param isSageman
	 *            Is this the Sageman data set
	 * @param isSenate
	 *            is this the Senate data set
	 */
	public GraphCreatorAction(SocialAction sa, Visualization vis, Table senateTestTable, boolean isSageman,
			boolean isSenate) {
		super(vis);

		this.m_app = sa;
		m_importedGraph = null;
		this.m_app = sa;

		m_senateTest = senateTestTable;

		m_labelField = SocialAction.LABEL_COLUMN_NAME;

		// m_multiplexField = multiplexField;
		// m_labelField = labelField;
		// m_nodeTable = nodeTable;
		// m_edgeTable = edgeTable;

		m_isBipartite = false;

		this.m_app
				.addEventToHistory(LOAD_STEP_NUMBER, LOAD_NETWORK_STATE, "Senate Test", senateTestTable.getRowCount());
	}

	/**
	 * Actually import the graph
	 * 
	 * @see prefuse.action.Action#run(double)
	 */
	public void run(double frac) {
		Graph graph = null;

		if (m_isBipartite)
			graph = importBipartiteTable();
		else if (m_matrixTable != null) {
			graph = importMatrix();
		} else if (m_sagemanTable != null) {
			graph = importSageman();
		} else if (m_senateTest != null) {
			graph = importSenateTest();
		} else if (m_importedGraph == null) {
			graph = importGraphFromTables();
			// return;
		} else
			graph = importGraph();

		this.m_app.setupGraph(graph, m_labelField);
	}

	public Graph importGraphFromTables() {

		// JPrefuseTable.showTableWindow(m_nodeTable);
		// JPrefuseTable.showTableWindow(m_edgeTable);

		// Table edges = new Table();
		// edges.addColumn(SRC, int.class);
		// edges.addColumn(TRG, int.class);

		Graph g = new Graph(m_nodeTable, m_edgeTable, m_isDirected, m_nodeField, m_edge1Field, m_edge2Field);

		// this.m_app.getVisualization().run("linlog");

		// System.out.println(g.getNode(0).get(m_labelField));
		//
		// BreadthFirstIterator bfs = new BreadthFirstIterator();
		// Node n = (Node) g.getNode(200);
		//
		// bfs.init(n, 2, Constants.NODE_AND_EDGE_TRAVERSAL);
		//
		// Table egoNodeTable = new Table();
		// egoNodeTable.addColumns(g.getNodeTable().getSchema());
		//        
		// Table egoEdgeTable = new Table();
		// egoEdgeTable.addColumns(g.getEdgeTable().getSchema());
		//        
		// while (bfs.hasNext()) {
		// Object o = bfs.next();
		//
		// if (o instanceof Node) {
		// Node node = (Node) o;
		// egoNodeTable.addTuple(node);
		// if (node.canGet(m_labelField, String.class))
		// System.out.println(node.get(m_labelField));
		// }
		//            
		// if (o instanceof Edge) {
		// Edge edge = (Edge) o;
		// egoEdgeTable.addTuple(edge);
		// }
		// }
		//        
		// // JPrefuseTable.showTableWindow(egoNodeTable);
		// // JPrefuseTable.showTableWindow(g.getNodeTable());
		//
		// Graph g2 = new Graph(egoNodeTable, egoEdgeTable, m_isDirected, m_nodeField, m_edge1Field, m_edge2Field);

		this.m_app.setEdgeWeightField(m_edgeWeightField);

		// m_graphs = new Graph[1];
		// String[] graphsNames = new String[1];
		// m_graphs[0] = g2;
		// graphsNames[0] = "All";

		String[] graphsNames = createMultiplex(g, m_edge1Field, m_edge2Field);

		this.m_app.setMultiplexGraphs(m_graphs, graphsNames);

		return g;
	}

	String[] createMultiplex(Graph origGraph, String edge1Field, String edge2Field) {
		ArrayList<String> list = new ArrayList<String>();
		if (m_multiplexField != null) {
			Column column = origGraph.getEdgeTable().getColumn(m_multiplexField);
			if (column == null)
				return null;

			for (int i = 0; i < origGraph.getEdgeTable().getRowCount(); i++) {
				String row = (String) column.get(i).toString();

				if (!list.contains(row))
					list.add(row);
			}
		}

		m_graphs = new Graph[list.size() + 1];
		String[] graphsNames = new String[list.size() + 1];

		for (int i = 0; i < list.size() + 1; i++) {
			Table edges = new Table();
			edges.addColumns(origGraph.getEdgeTable().getSchema());

			HashMap<String, Integer> edgeMap = new HashMap<String, Integer>();

			IntIterator rowIt = origGraph.getEdgeTable().rows();

			String currentMultiplexValue = "All";
			if (i > 0)
				currentMultiplexValue = list.get(i - 1).toString();
			while (rowIt.hasNext()) {
				int row = rowIt.nextInt();

				// can't figure out what this code was trying to do... so i commented it.

				if (i != 0) {

					// for each row, if graph's edgetable DOESN'T have a

					if (!(origGraph.getEdgeTable().get(row, m_multiplexField).toString().equals(currentMultiplexValue))) {

						continue;
					}
				}

				boolean foundEdge = false;

				if (edgeMap.containsKey((origGraph.getEdgeTable().get(row, edge1Field).toString() + "->" + origGraph
						.getEdgeTable().get(row, edge2Field).toString())))
					foundEdge = true;

				if (!foundEdge) {
					// int newRow = edges.addRow();
					edges.addTuple(origGraph.getEdgeTable().getTuple(row));
					// edges.setInt(newRow, edge1Field, ((Integer) (origGraph.getEdgeTable().get(row,
					// edge1Field))).intValue());
					// edges.setInt(newRow, edge2Field, ((Integer) (origGraph.getEdgeTable().get(row,
					// edge2Field))).intValue());

					edgeMap.put((origGraph.getEdgeTable().get(row, edge1Field).toString() + "->" + origGraph
							.getEdgeTable().get(row, edge2Field).toString()), new Integer(1));

				}
			}

			m_graphs[i] = new Graph(origGraph.getNodeTable(), edges, m_isDirected, m_nodeField, m_edge1Field,
					m_edge2Field);
			graphsNames[i] = currentMultiplexValue;

		}
		return graphsNames;
	}

	public Graph importGraph() {
		Graph graph = m_importedGraph;

		// m_graphs = new Graph[1];
		// m_graphs[0] = graph;

		// Make a list of all different multiplex values
		ArrayList<String> list = new ArrayList<String>();
		if (m_multiplexField != null) {
			Column column = m_importedGraph.getEdgeTable().getColumn(m_multiplexField);
			if (column == null)
				return null;

			for (int i = 0; i < m_importedGraph.getEdgeTable().getRowCount(); i++) {
				String row = (String) column.get(i).toString();

				if (!list.contains(row))
					list.add(row);
			}
		}

		// JPrefuseTable.showTableWindow(m_importedGraph.getEdgeTable());

		m_graphs = new Graph[list.size() + 1];
		String[] graphsNames = new String[list.size() + 1];

		for (int i = 0; i < list.size() + 1; i++) {

			Table edges = new Table();
			edges.addColumn(SRC, int.class);
			edges.addColumn(TRG, int.class);

			HashMap<String, Integer> edgeMap = new HashMap<String, Integer>();

			IntIterator rowIt = m_importedGraph.getEdgeTable().rows();

			String currentMultiplexValue = "All";
			if (i > 0)
				currentMultiplexValue = list.get(i - 1).toString();
			while (rowIt.hasNext()) {
				int row = rowIt.nextInt();

				// can't figure out what this code was trying to do... so i commented it.

				if (i != 0) {

					// for each row, if graph's edgetable DOESN'T have a

					if (!(m_importedGraph.getEdgeTable().get(row, m_multiplexField).toString()
							.equals(currentMultiplexValue))) {

						continue;
					}
				}

				boolean foundEdge = false;

				if (edgeMap
						.containsKey((m_importedGraph.getEdgeTable().get(row, SRC).toString() + "->" + m_importedGraph
								.getEdgeTable().get(row, TRG).toString())))
					foundEdge = true;

				if (!foundEdge) {
					int newRow = edges.addRow();
					edges.setInt(newRow, SRC, ((Integer) (m_importedGraph.getEdgeTable().get(row, SRC))).intValue());
					edges.setInt(newRow, TRG, ((Integer) (m_importedGraph.getEdgeTable().get(row, TRG))).intValue());

					edgeMap.put((m_importedGraph.getEdgeTable().get(row, SRC).toString() + "->" + m_importedGraph
							.getEdgeTable().get(row, TRG).toString()), new Integer(1));
				}
			}

			m_graphs[i] = new Graph(m_importedGraph.getNodeTable(), edges, false);
			graphsNames[i] = currentMultiplexValue;

		}

		this.m_app.setMultiplexGraphs(m_graphs, graphsNames);

		return graph;
	}

	public Graph importSenateTest() {
		m_graphs = new Graph[1];
		String[] graphsNames = new String[1];

		HashMap<String, Integer> idMap = new HashMap<String, Integer>();

		HashSet<String> topicList = new HashSet<String>();

		Table nodes = new Table();
		nodes.addColumn(SocialAction.ID_COLUMN_NAME, int.class);
		nodes.addColumn(SocialAction.LABEL_COLUMN_NAME, String.class);
		nodes.addColumn("party", int.class);

		int idCnt = 0;
		for (int i = 2; i < m_senateTest.getRowCount(); i++) {

			String topicLabel = m_senateTest.getString(i, 2);
			topicList.add(topicLabel);

			// if (!idMap.containsKey(nodeLabel)) {
			// if (nodeLabel != "") {
			// int newRow = nodes.addRow();
			// nodes.setInt(newRow, SocialAction.ID_COLUMN_NAME, idCnt);
			// nodes.set(newRow, SocialAction.LABEL_COLUMN_NAME, nodeLabel);
			// nodes.setInt(newRow, "party", 1);
			// idCnt++;
			// idMap.put(nodeLabel, newRow);
			// }
			// }
		}

		for (int i = 3; i < m_senateTest.getColumnCount(); i++) {

			String nodeLabel = m_senateTest.getColumnName(i);
			if (nodeLabel != "") {
				int newRow = nodes.addRow();
				nodes.setInt(newRow, SocialAction.ID_COLUMN_NAME, idCnt);
				nodes.set(newRow, SocialAction.LABEL_COLUMN_NAME, nodeLabel);

				String party = m_senateTest.getString(0, i);
				if (party.equals("R")) {
					nodes.setInt(newRow, "party", 2);
				} else if (party.equals("D")) {
					nodes.setInt(newRow, "party", 0);
				} else
					nodes.setInt(newRow, "party", 1);

				idCnt++;
				idMap.put(nodeLabel, newRow);
			}
		}

		Table edges = new Table();
		edges.addColumn(SRC, int.class);
		edges.addColumn(TRG, int.class);
		edges.addColumn("weight", double.class);

		// int newRow = edges.addRow();

		// edges.setInt(newRow, SRC, 3);
		// edges.setInt(newRow, TRG, 4);
		// edges.setDouble(newRow, "weight", 1.0);

		// go through each Role Call
		for (int i = 2; i < m_senateTest.getRowCount(); i++) {

			String rcLabel = m_senateTest.getString(i, 2);
			if (rcLabel.equals("Congress")) {
				// System.out.println("ROLE CALL: " + rcLabel);

				ArrayList<Integer> yesSenators = new ArrayList<Integer>();
				yesSenators.clear();

				for (int j = 3; j < m_senateTest.getColumnCount(); j++) {
					String senatorLabel = m_senateTest.getColumnName(j);

					if (m_senateTest.getString(i, j).equals("Yea")) {
						if (!yesSenators.contains(idMap.get(senatorLabel)))
							yesSenators.add(idMap.get(senatorLabel));
						// System.out.println(m_senateTest.getString(i, j));
					}
				}

				// System.out.println(yesSenators.size());

				for (int x = 0; x < yesSenators.size(); x++)
					for (int y = x + 1; y < yesSenators.size(); y++) {

						boolean found = false;

						int senator1 = yesSenators.get(x);
						int senator2 = yesSenators.get(y);
						for (int z = 0; z < edges.getRowCount(); z++) {
							if (((edges.getInt(z, SRC) == senator1) && (edges.getInt(z, TRG) == senator2))
									|| ((edges.getInt(z, TRG) == senator1) && (edges.getInt(z, SRC) == senator2))) {
								edges.setDouble(z, "weight", (edges.getDouble(z, "weight") + 1.0));
								found = true;
								break;
							}
						}

						if (!found) {
							int newRow = edges.addRow();
							edges.setInt(newRow, SRC, senator1);
							edges.setInt(newRow, TRG, senator2);
							edges.setDouble(newRow, "weight", 1.0);
						}
					}
			}
		}

		m_graphs[0] = new Graph(nodes, edges, false);

		graphsNames[0] = "All";

		this.m_app.setMultiplexGraphs(m_graphs, graphsNames);

		return m_graphs[0];
	}

	public Graph importMatrix() {

		m_labelField = SocialAction.LABEL_COLUMN_NAME;

		ArrayList<String> list = new ArrayList<String>();
		//
		// if (m_multiplexField != null) {
		// Column column = m_bipartiteTable.getColumn(m_multiplexField);
		// if (column == null)
		// return null;
		//

		int senateArray[] = { 180, 290, 1 };

		String currentMultiplexValue = null;
		for (int i = 0; i < senateArray.length; i++) {
			String row = (String) new Integer(senateArray[i]).toString();

			if (!list.contains(row))
				list.add(row);
		}

		m_graphs = new Graph[list.size()];
		String graphs_Names[] = new String[list.size()];

		for (int i = 0; i < list.size(); i++) {

			Table nodes = new Table();
			nodes.addColumn(SocialAction.ID_COLUMN_NAME, int.class);
			nodes.addColumn(SocialAction.LABEL_COLUMN_NAME, String.class);
			nodes.addColumn("party", int.class);
			nodes.addColumn("candidate", int.class);
			nodes.addColumn("Image", String.class);
			// nodes.addColumn(SocialAction.MULTIPLEX_COLUMN_NAME, String.class);

			Table edges = new Table();
			edges.addColumn(SRC, int.class);
			edges.addColumn(TRG, int.class);
			edges.addColumn("weight", double.class);

			int numColumns = m_matrixTable.getColumnCount();

			int idCnt = 0;
			HashMap<String, Integer> idMap = new HashMap<String, Integer>();
			HashMap<String, Integer> edgeMap = new HashMap<String, Integer>();

			for (int j = 0; j < numColumns; j++) {
				// skip first three columns
				if (j > 4) {
					String nodeLabel = m_matrixTable.getColumnName(j);

					int newRow = nodes.addRow();
					nodes.setInt(newRow, SocialAction.ID_COLUMN_NAME, idCnt);
					// nodes.setInt(newRow, SocialAction.PARTITION_COLUMN_NAME, 1);
					nodes.set(newRow, SocialAction.LABEL_COLUMN_NAME, nodeLabel);
					nodes.set(newRow, "party", 0);
					nodes.set(newRow, "Image", "");

					idMap.put(nodeLabel, new Integer(idCnt));
					idCnt++;
				}
			}

			currentMultiplexValue = list.get(i).toString();
			IntIterator rowIt = m_matrixTable.rows();
			while (rowIt.hasNext()) {
				int row = rowIt.nextInt();
				String nodeRow = m_matrixTable.getString(row, 0);
				// System.out.println("Row: " + row + " " + nodeRow);

				String party = m_matrixTable.getString(row, 1);
				String image = m_matrixTable.getString(row, "Image");
				String nodeLabel = m_matrixTable.getString(row, 0);

				nodes.getTuple((Integer) idMap.get(nodeRow)).setString("Image", image);

				// System.out.println(party);
				if (party.equals("R")) {
					nodes.getTuple((Integer) idMap.get(nodeRow)).setInt("party", 2);
				} else if (party.equals("D")) {
					nodes.getTuple((Integer) idMap.get(nodeRow)).setInt("party", 0);
				} else
					nodes.getTuple((Integer) idMap.get(nodeRow)).setInt("party", 1);

				if (nodeLabel.equals("Clinton"))
					nodes.getTuple((Integer) idMap.get(nodeRow)).setInt("candidate", 0);
				else if (nodeLabel.equals("Obama"))
					nodes.getTuple((Integer) idMap.get(nodeRow)).setInt("candidate", 0);
				else if (nodeLabel.equals("McCain"))
					nodes.getTuple((Integer) idMap.get(nodeRow)).setInt("candidate", 1);
				else
					nodes.getTuple((Integer) idMap.get(nodeRow)).setInt("candidate", 2);

				for (int j = 0; j < numColumns; j++) {
					// skip first three columns
					if (j > 2) {

						if (m_matrixTable.canGetInt(m_matrixTable.getColumnName(j))) {
							String nodeColumn = m_matrixTable.getColumnName(j);

							int value = m_matrixTable.getInt(row, j);

							if (value > senateArray[i]) {

								if (!edgeMap.containsKey(nodeColumn + "->" + nodeRow)) {

									int newRow = edges.addRow();

									// System.out.println(nodeColumn + " -> " + nodeRow);

									edges.setInt(newRow, SRC, ((Integer) idMap.get(nodeColumn)).intValue());
									edges.setInt(newRow, TRG, ((Integer) idMap.get(nodeRow)).intValue());
									edges.setDouble(newRow, "weight", value);

									edgeMap.put(nodeColumn + "->" + nodeRow, new Integer(1));
								}
							}
						}
					}
				}
			}

			m_graphs[i] = new Graph(nodes, edges, false);
			graphs_Names[i] = currentMultiplexValue;
			// System.out.println(currentMultiplexValue);
		}

		this.m_app.setMultiplexGraphs(m_graphs, graphs_Names);
		return m_graphs[0];
	}

	public Graph importSageman() {

		m_labelField = SocialAction.LABEL_COLUMN_NAME;

		ArrayList<String> list = new ArrayList<String>();

		int[] arrayColumns = { 21, 22, 23, 24, 25, 26, 32 };

		int[] attributeColumns = { 2, 3, 4, 5, 7, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18 };

		for (int i = 0; i < arrayColumns.length; i++) {
			String row = (String) m_sagemanTable.getColumnName(arrayColumns[i]).toString();
			if (!list.contains(row))
				list.add(row);
		}

		m_graphs = new Graph[list.size() + 1];
		String graphs_Names[] = new String[list.size() + 1];

		for (int i = 0; i < list.size() + 1; i++) {

			Table nodes = new Table();
			nodes.addColumn(SocialAction.ID_COLUMN_NAME, int.class);
			nodes.addColumn(SocialAction.LABEL_COLUMN_NAME, String.class);

			for (int j = 0; j < attributeColumns.length; j++) {
				nodes.addColumn(m_sagemanTable.getColumnName(attributeColumns[j]), m_sagemanTable
						.getColumnType(attributeColumns[j]));
			}

			nodes.addColumn(SocialAction.IMAGE_COLUMN_NAME, String.class);

			// nodes.addColumn("party", int.class);
			// nodes.addColumn(SocialAction.MULTIPLEX_COLUMN_NAME, String.class);

			Table edges = new Table();
			edges.addColumn(SRC, int.class);
			edges.addColumn(TRG, int.class);

			int idCnt = 0;
			HashMap<Integer, Integer> idMap = new HashMap<Integer, Integer>();

			IntIterator rowIt2 = m_sagemanTable.rows();
			while (rowIt2.hasNext()) {
				int row = rowIt2.nextInt();
				int nodeID = m_sagemanTable.getInt(row, 0);
				String nodeLabel = m_sagemanTable.getString(row, "Short Name");
				String image = "";
				if (m_sagemanTable.canGetString(SocialAction.IMAGE_COLUMN_NAME))
					image = m_sagemanTable.getString(row, SocialAction.IMAGE_COLUMN_NAME);

				int newRow = nodes.addRow();
				nodes.setInt(newRow, SocialAction.ID_COLUMN_NAME, idCnt);
				// nodes.setInt(newRow, SocialAction.PARTITION_COLUMN_NAME, 1);
				nodes.set(newRow, SocialAction.LABEL_COLUMN_NAME, nodeLabel);
				nodes.set(newRow, SocialAction.IMAGE_COLUMN_NAME, image);
				for (int j = 0; j < attributeColumns.length; j++) {
					nodes.set(newRow, m_sagemanTable.getColumnName(attributeColumns[j]), m_sagemanTable.get(row,
							attributeColumns[j]));
				}

				idMap.put(nodeID, new Integer(idCnt));
				idCnt++;
			}

			String currentMultiplexValue = "All";
			if (i > 0)
				currentMultiplexValue = list.get(i - 1).toString();
			IntIterator rowIt = m_sagemanTable.rows();
			while (rowIt.hasNext()) {
				int row = rowIt.nextInt();
				int nodeID = m_sagemanTable.getInt(row, 0);
				// String nodeRow = m_sagemanTable.getString(row, 1);
				// System.out.println("Row: " + row + " " + nodeRow);

				if (i == 0) {
					for (int y = 0; y < arrayColumns.length; y++) {
						System.out.println(arrayColumns[y]);
						System.out.println(m_sagemanTable.getColumnType(arrayColumns[y]));
						System.out.println(m_sagemanTable.get(row, arrayColumns[y]));
						int[] array = (int[]) m_sagemanTable.get(row, arrayColumns[y]);
						for (int x = 0; x < array.length; x++) {
							int newRow = edges.addRow();
							System.out.println(nodeID + " -> " + array[x]);
							edges.setInt(newRow, SRC, ((Integer) idMap.get(nodeID)).intValue());
							edges.setInt(newRow, TRG, ((Integer) idMap.get(array[x])).intValue());
						}
					}
				} else {
					int[] array = (int[]) m_sagemanTable.get(row, arrayColumns[i - 1]);
					for (int x = 0; x < array.length; x++) {
						int newRow = edges.addRow();
						edges.setInt(newRow, SRC, ((Integer) idMap.get(nodeID)).intValue());
						edges.setInt(newRow, TRG, ((Integer) idMap.get(array[x])).intValue());
					}
				}
			}

			m_graphs[i] = new Graph(nodes, edges, m_isDirected);
			graphs_Names[i] = currentMultiplexValue;
		}

		this.m_app.setMultiplexGraphs(m_graphs, graphs_Names);
		return m_graphs[0];
	}

	public Graph importBipartiteTable() {

		m_labelField = SocialAction.LABEL_COLUMN_NAME;

		ArrayList<String> list = new ArrayList<String>();

		if (m_multiplexField != null) {
			Column column = m_bipartiteTable.getColumn(m_multiplexField);
			if (column == null)
				return null;

			for (int i = 0; i < m_bipartiteTable.getRowCount(); i++) {
				String row = (String) column.get(i).toString();

				if (!list.contains(row))
					list.add(row);
			}
		}

		m_graphs = new Graph[list.size() + 1];
		String graphs_Names[] = new String[list.size() + 1];

		for (int i = 0; i < list.size() + 1; i++) {

			Table nodes = new Table();
			nodes.addColumn(SocialAction.ID_COLUMN_NAME, int.class);
			nodes.addColumn(SocialAction.LABEL_COLUMN_NAME, String.class);
			nodes.addColumn(SocialAction.PARTITION_COLUMN_NAME, int.class);
			nodes.addColumn(SocialAction.MULTIPLEX_COLUMN_NAME, String.class);
			// nodes.addColumn("Attribute", int.class);

			Table edges = new Table();
			edges.addColumn(SRC, int.class);
			edges.addColumn(TRG, int.class);
			edges.addColumn(m_app.getEdgeWeightField(), double.class);

			HashMap<Integer, Integer> idMap = new HashMap<Integer, Integer>();

			HashMap<String, Integer> edgeMap = new HashMap<String, Integer>();

			int idCnt = 0;

			String currentMultiplexValue = "All";
			if (i > 0)
				currentMultiplexValue = list.get(i - 1).toString();
			IntIterator rowIt = m_bipartiteTable.rows();
			while (rowIt.hasNext()) {
				int row = rowIt.nextInt();

				if (i != 0) {
					if (!(m_bipartiteTable.get(row, m_multiplexField).toString().equals(currentMultiplexValue))) {

						continue;
					}
				}

				boolean foundA = false, foundB = false, foundEdge = false;

				if (idMap.containsKey(m_bipartiteTable.get(row, m_partitionA))) {
					foundA = true;
					// break;
				}
				if (idMap.containsKey(m_bipartiteTable.get(row, m_partitionB))) {
					foundB = true;
					// break;
				}

				try {
					if (edgeMap
							.containsKey((m_bipartiteTable.get(row, m_partitionA).toString() + "->" + m_bipartiteTable
									.get(row, m_partitionB).toString())))
						foundEdge = true;
				} catch (NullPointerException e) {
					System.out.println(m_partitionA + "\t" + m_partitionB);
					System.out.println(m_bipartiteTable.getRowCount() + "\t" + m_bipartiteTable.getColumnCount());
					// System.out.println(m_bipartiteTable.get(row, m_partitionA) | "\t" + m_bipartiteTable.get(row,
					// m_partitionB));
				}
				if (!foundA) {
					// System.out.println("idA " + idCnt);
					int newRow = nodes.addRow();
					nodes.setInt(newRow, SocialAction.ID_COLUMN_NAME, idCnt);
					nodes.setInt(newRow, SocialAction.PARTITION_COLUMN_NAME, 0);

					// nodes.set(newRow, "Attribute", m_bipartiteTable.getInt(row, 4));

					nodes.set(newRow, SocialAction.LABEL_COLUMN_NAME, m_bipartiteTable.get(row, m_partitionA)
							.toString());

					// nodes.set(newRow, SocialAction.LABEL_COLUMN_NAME, "Person " + idCnt);

					if (i == 0)
						nodes.set(newRow, SocialAction.MULTIPLEX_COLUMN_NAME, "All");
					else
						nodes.set(newRow, SocialAction.MULTIPLEX_COLUMN_NAME, m_bipartiteTable.get(row,
								m_multiplexField).toString());

					idMap.put((Integer) m_bipartiteTable.get(row, m_partitionA), new Integer(idCnt));
					idCnt++;
				}

				if (!foundB) {

					int newRow = nodes.addRow();
					nodes.setInt(newRow, SocialAction.ID_COLUMN_NAME, idCnt);
					nodes.setInt(newRow, SocialAction.PARTITION_COLUMN_NAME, 1);

					// nodes.set(newRow, "Attribute", m_bipartiteTable.getInt(row, 4));

					nodes.set(newRow, SocialAction.LABEL_COLUMN_NAME, m_bipartiteTable.get(row, m_partitionB)
							.toString());
					// nodes.set(newRow, SocialAction.LABEL_COLUMN_NAME, "Organization " + idCnt);

					if (i == 0)
						nodes.set(newRow, SocialAction.MULTIPLEX_COLUMN_NAME, "All");
					else
						nodes.set(newRow, SocialAction.MULTIPLEX_COLUMN_NAME, m_bipartiteTable.get(row,
								m_multiplexField).toString());

					idMap.put((Integer) m_bipartiteTable.get(row, m_partitionB), new Integer(idCnt));
					idCnt++;
				}

				if (!foundEdge) {
					int newRow = edges.addRow();
					edges
							.setInt(newRow, SRC, ((Integer) idMap.get(m_bipartiteTable.get(row, m_partitionA)))
									.intValue());
					edges
							.setInt(newRow, TRG, ((Integer) idMap.get(m_bipartiteTable.get(row, m_partitionB)))
									.intValue());
					if (m_edgeWeightField != null) {
						System.out.println("Isn't smartly handling this Edge Weight stuff!!! FIX!");
						edges.setDouble(newRow, m_app.getEdgeWeightField(), ((Integer) m_bipartiteTable.get(row,
								m_edgeWeightField)).doubleValue());
					}

					edgeMap.put((m_bipartiteTable.get(row, m_partitionA).toString() + "->" + m_bipartiteTable.get(row,
							m_partitionB).toString()), new Integer(1));

				}

				// FIX LINKS, THIS IS CLEARLY A BROKEN HASH-aLGORITHM.. yo?

			}

			m_graphs[i] = new Graph(nodes, edges, m_isDirected);
			graphs_Names[i] = currentMultiplexValue;
		}

		this.m_app.setMultiplexGraphs(m_graphs, graphs_Names);
		return m_graphs[0];
	}

} // end of class GroupAction
