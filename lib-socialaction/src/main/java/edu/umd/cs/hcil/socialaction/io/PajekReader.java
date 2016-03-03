package edu.umd.cs.hcil.socialaction.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.OrPredicate;

import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.io.AbstractGraphReader;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphReader;
import edu.umd.cs.hcil.socialaction.SocialAction;
import edu.umd.cs.hcil.socialaction.action.GraphCreatorAction;

/**
 * @author Adam Perer
 */
public class PajekReader extends AbstractGraphReader implements GraphReader {

	private static final Predicate v_pred = new TagPred("*vertices");
	private static final Predicate a_pred = new TagPred("*arcs");
	private static final Predicate e_pred = new TagPred("*edges");
	private static final Predicate t_pred = new TagPred("*");
	private static final Predicate c_pred = OrPredicate.getInstance(a_pred, e_pred);
	protected static final Predicate l_pred = ListTagPred.getInstance();

	private Table m_nodes;
	private Table m_edges;
	private HashMap<Integer, Integer> idMap;

	@Override
	public Graph readGraph(InputStream is) throws DataIOException {

		Graph g = null;
		try {

			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			// ignore everything until we see '*Vertices'
			String curLine = skip(br, v_pred);

			if (curLine == null) // no vertices in the graph; return empty graph
				return g;

			m_nodes = new Table();
			m_nodes.addColumn(SocialAction.ID_COLUMN_NAME, int.class);
			m_nodes.addColumn(SocialAction.LABEL_COLUMN_NAME, String.class);

			idMap = new HashMap<Integer, Integer>();

			// create appropriate number of vertices
			StringTokenizer st = new StringTokenizer(curLine);
			st.nextToken(); // skip past "*vertices";
			int num_vertices = Integer.parseInt(st.nextToken());

			// int idCnt = 0;
			// read vertices until we see any Pajek format tag ('*...')
			curLine = null;
			while (br.ready()) {
				curLine = br.readLine();
				if (curLine == null || t_pred.evaluate(curLine))
					break;
				if (curLine == "") // skip blank lines
					continue;

				try {
					readVertex(curLine, num_vertices);
				} catch (IllegalArgumentException iae) {
					br.close();
					throw iae;
				}
			}

			m_edges = new Table();
			m_edges.addColumn(GraphCreatorAction.SRC, int.class);
			m_edges.addColumn(GraphCreatorAction.TRG, int.class);
			m_edges.addColumn("weight", double.class);

			// skip over the intermediate stuff (if any)
			// and read the next arcs/edges section that we find
			curLine = readArcsOrEdges(curLine, br, g);

			// ditto
			// readArcsOrEdges(curLine, br, g, nev);

			g = new Graph(m_nodes, m_edges, false);

			br.close();
			// reader.close();

		} catch (IOException e) {
			System.out.println("Trouble reading file?: " + e.getMessage());
		}

		return g;

		// TODO Auto-generated method stub
		// return null;
	}

	private int readVertex(String curLine, int num_vertices) throws IOException {
		String[] parts = null;
		String index;
		String label = null;
		if (curLine.indexOf('"') != -1) {
			String[] initial_split = curLine.trim().split("\"");
			// if there are any quote marks, there should be exactly 2
			if (initial_split.length < 2 || initial_split.length > 3)
				throw new IllegalArgumentException("Unbalanced (or too many) quote marks in " + curLine);
			index = initial_split[0].trim();
			label = initial_split[1].trim();
			if (initial_split.length == 3)
				parts = initial_split[2].trim().split("\\s+", -1);
		} else {
			parts = curLine.trim().split("\\s+", -1);
			index = parts[0];
			if (parts.length > 1 /* && GeneralUtils.isNumeric(parts[1]) */) {
				label = parts[1];
			}
		}
		// String[] parts = curLine.trim().split("\\s+", 2);
		// int v_id = Integer.parseInt(parts[0]) - 1; // go from 1-based to 0-based index
		int v_id = Integer.parseInt(index) - 1; // go from 1-based to 0-based index
		if (v_id >= num_vertices || v_id < 0)
			throw new IllegalArgumentException("Vertex number " + v_id + "is not in the range [1," + num_vertices + "]");

		int newRow = m_nodes.addRow();
		int id = Integer.parseInt(index);
		m_nodes.setInt(newRow, SocialAction.ID_COLUMN_NAME, id);
		// nodes.setInt(newRow, SocialAction.PARTITION_COLUMN_NAME, 1);
		m_nodes.set(newRow, SocialAction.LABEL_COLUMN_NAME, label);

		idMap.put(id, newRow);

		return newRow;
		// parse the rest of the line
		// if (get_locations)
		// {
		// if (parts == null || parts.length < i+2)
		// throw new IllegalArgumentException("Coordinates requested, but" +
		// curLine + " does not include coordinates");
		// double x = Double.parseDouble(parts[i]);
		// double y = Double.parseDouble(parts[i+1]);
		// // if (x < 0 || x > 1 || y < 0 || y > 1)
		// // throw new IllegalArgumentException("Coordinates in line " +
		// // curLine + " are not all in the range [0,1]");
		//                
		// v_locations.setLocation(v, new Point2D.Double(x,y));
		// }

		// if (parts.length == 2)
		// attachLabel(v, parts[1]); // if there is a label, attach it
	}

	private String readArcsOrEdges(String curLine, BufferedReader br, Graph g) throws IOException {
		String nextLine = curLine;

		// Indexer id = Indexer.getIndexer(g);

		// in case we're not there yet (i.e., format tag isn't arcs or edges)
		if (!c_pred.evaluate(curLine))
			// nextLine = skip(br, e_pred);
			nextLine = skip(br, c_pred);

		// in "*Arcs" and this graph is not strictly undirected
		// boolean reading_arcs = a_pred.evaluate(nextLine) &&
		// !PredicateUtils.enforcesUndirected(g);
		// // in "*Edges" and this graph is not strictly directed
		// boolean reading_edges = e_pred.evaluate(nextLine) &&
		// !PredicateUtils.enforcesDirected(g);

		boolean reading_arcs = false;
		boolean reading_edges = false;

		if (e_pred.evaluate(nextLine)) {
			// if (PredicateUtils.enforcesDirected(g))
			// throw new
			// IllegalArgumentException("Supplied directed-only graph cannot be populated with undirected edges");
			// else
			reading_edges = true;
		}

		if (!(reading_arcs || reading_edges))
			return nextLine;

		boolean is_list = l_pred.evaluate(nextLine);

		while (br.ready()) {
			nextLine = br.readLine();
			if (nextLine == null || t_pred.evaluate(nextLine))
				break;
			if (curLine == "") // skip blank lines
				continue;

			StringTokenizer st = new StringTokenizer(nextLine.trim());

			int vid1 = Integer.parseInt(st.nextToken());

			if (is_list) // one source, multiple destinations
			{
				do {
					int vid2 = Integer.parseInt(st.nextToken());

					int newRow = m_edges.addRow();
					m_edges.setInt(newRow, GraphCreatorAction.SRC, idMap.get(vid1));
					m_edges.setInt(newRow, GraphCreatorAction.TRG, idMap.get(vid2));

				} while (st.hasMoreTokens());
			} else // one source, one destination, at most one weight
			{

				int vid2 = Integer.parseInt(st.nextToken());

				int newRow = m_edges.addRow();
				m_edges.setInt(newRow, GraphCreatorAction.SRC, idMap.get(vid1));
				m_edges.setInt(newRow, GraphCreatorAction.TRG, idMap.get(vid2));

				// Edge e = createAddEdge(st, v1, reading_arcs, g, id, parallel_ok);
				// // get the edge weight if we care
				// if (nev != null)
				// nev.setNumber(e, new Float(st.nextToken()));
			}
		}
		return nextLine;
	}

	// protected Edge createAddEdge(StringTokenizer st, Vertex v1,
	// boolean directed, Graph g, Indexer id, boolean parallel_ok)
	// {
	// int vid2 = Integer.parseInt(st.nextToken()) - 1;
	// Vertex v2 = (Vertex) id.getVertex(vid2);
	// Edge e = null;
	// if (directed)
	// e = new DirectedSparseEdge(v1, v2);
	// else
	// e = new UndirectedSparseEdge(v1, v2);
	//
	// // add this edge if parallel edges are OK,
	// // or if this isn't one; otherwise ignore it
	// if (parallel_ok || !p_pred.evaluate(e))
	// g.addEdge(e);
	//
	// return e;
	// }

	/**
	 * Returns the first line read from <code>br</code> for which <code>p</code> returns <code>true</code>, or
	 * <code>null</code> if there is no such line.
	 * 
	 * @throws IOException
	 */
	protected String skip(BufferedReader br, Predicate p) throws IOException {
		while (br.ready()) {
			String curLine = br.readLine();
			if (curLine == null)
				break;
			curLine = curLine.trim();
			if (p.evaluate(curLine))
				return curLine;
		}
		return null;
	}

	/**
	 * A Predicate which evaluates to <code>true</code> if the argument starts with the constructor-specified String.
	 * 
	 * @author Joshua O'Madadhain
	 */
	protected static class TagPred implements Predicate {
		private String tag;

		public TagPred(String s) {
			this.tag = s;
		}

		public boolean evaluate(Object arg0) {
			String s = (String) arg0;
			return (s != null && s.toLowerCase().startsWith(tag));
		}
	}

	/**
	 * A Predicate which evaluates to <code>true</code> if the argument ends with the string "list".
	 * 
	 * @author Joshua O'Madadhain
	 */
	protected static class ListTagPred implements Predicate {
		protected static ListTagPred instance;

		protected ListTagPred() {
		}

		public static ListTagPred getInstance() {
			if (instance == null)
				instance = new ListTagPred();
			return instance;
		}

		public boolean evaluate(Object arg0) {
			String s = (String) arg0;
			return (s != null && s.toLowerCase().endsWith("list"));
		}
	}

}
