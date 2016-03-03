package cern.colt.matrix.impl;

import java.util.HashMap;
import java.util.Iterator;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.visual.NodeItem;

/**
 * A COLT SparseDoubleMatrix2D instance representing the adjacency matrix for a prefuse graph. Useful for performing
 * more advanced mathematical graph theoretic analyses on prefuse graphs.
 * 
 * Nov 25, 2004 - jheer - Created class
 * 
 * Ported to Prefuse Beta and SocialAction
 * 
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 * @author <a href="http://www.perer.org/">Adam Perer</a>
 */
public class PrefuseMatrix extends LargeSparseDoubleMatrix2D {

	/** Never change */
	private static final long serialVersionUID = -1546813919904095948L;
	private HashMap<Node, Integer> nodeIndexMap = new HashMap<Node, Integer>();
	private HashMap<Integer, Node> indexNodeMap = new HashMap<Integer, Node>();

	public PrefuseMatrix(Graph g) {
		super(g.getNodeCount(), g.getNodeCount());

		init(g);

		/*
		 * ItemRegistry registry = getRegistry(g); if ( registry == null ) { init(g); } else { synchronized (registry) {
		 * init(g); } }
		 */
	} //

	protected void init(Graph g) {
		int id = 0;
		Iterator iter = g.nodes();
		while (iter.hasNext()) {
			Node n = (Node) iter.next();
			if (n instanceof NodeItem) {
				// bet this causes problems...
				n = (Node) ((NodeItem) n);
			}
			Integer k = new Integer(id++);
			indexNodeMap.put(k, n);
			nodeIndexMap.put(n, k);
		}

		iter = g.edges();
		while (iter.hasNext()) {
			Edge e = (Edge) iter.next();
			Node nn1 = e.getSourceNode();
			Node nn2 = e.getTargetNode();
			if (nn1 instanceof NodeItem) {
				nn1 = (Node) ((NodeItem) nn1);
			}
			if (nn2 instanceof NodeItem) {
				nn2 = (Node) ((NodeItem) nn2);
			}
			int n1 = nodeIndexMap.get(nn1).intValue();
			int n2 = nodeIndexMap.get(nn2).intValue();
			String wstr = null;// e.getString("weight");
			double w = (wstr == null || wstr.equals("") ? 1.0 : Double.parseDouble(wstr));
			this.setQuick(n1, n2, w);
			if (!g.isDirected()) {
				this.setQuick(n2, n1, w);
			}
		}
	} //

	// protected ItemRegistry getRegistry(Graph g) {
	// Node n = null;
	// ItemRegistry registry = null;
	// while ( g.getNodeCount() > 0 && n == null ) {
	// try {
	// n = (Node)g.getNodes().next();
	// if ( n instanceof NodeItem ) {
	// registry = ((NodeItem)n).getItemRegistry();
	// }
	// } catch ( Exception e ) {}
	// }
	// return registry;
	// } //

	public Node getNode(int idx) {
		return indexNodeMap.get(new Integer(idx));
	} //

	public Node getNode(Integer idx) {
		return indexNodeMap.get(idx);
	} //

	public int getIndex(Node n) {
		Integer idx = nodeIndexMap.get(n);
		return (idx == null ? -1 : idx.intValue());
	} //

	public void remove(Node n) {
		Integer idx = nodeIndexMap.get(n);
		if (idx == null)
			return;

		nodeIndexMap.remove(n);
		indexNodeMap.remove(idx);

		int k = idx.intValue();
		for (int i = 0; i < rows(); i++) {
			this.setQuick(k, i, 0.0);
			this.setQuick(i, k, 0.0);
		}
	} //

} // end of class PrefuseMatrix
