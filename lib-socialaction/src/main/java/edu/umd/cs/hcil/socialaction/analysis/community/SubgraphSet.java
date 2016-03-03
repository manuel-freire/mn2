package edu.umd.cs.hcil.socialaction.analysis.community;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;

import prefuse.Visualization;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.tuple.DefaultTupleSet;
import prefusex.community.CommunityStructure;
import prefusex.community.CommunityStructureDirected;
import cern.colt.matrix.impl.PrefuseMatrix;
import edu.umd.cs.hcil.socialaction.SocialAction;

/**
 * SubgraphSet, an extension of Jeff Heer's Prefusex.community's Community Set to handle communities, components, and more!
 * 
 * @author Adam Perer
 */
public class SubgraphSet extends DefaultTupleSet {

	private HashMap<Node, Integer> communityMap = new HashMap<Node, Integer>();

	private HashMap<Integer, Set<Node>> memberSets = new HashMap<Integer, Set<Node>>();

	private int numCommunities = 0;

	private int maxCommunities = 10;

	private PrefuseMatrix mat;

	private List<int[]> mergeList;

	private double[] qvals;

	private BoundedRangeModel range;

	// private boolean removeFoci = true;

	Visualization m_vis;

	public SubgraphSet(Visualization vis) {
		m_vis = vis;
	} //

	public void clear() {
		// super.clear();
		communityMap.clear();
		memberSets.clear();
		numCommunities = 0;
		maxCommunities = 0;
	} //

	public void initCommunity(Visualization vis, Graph g) {
		clear();

		mat = new PrefuseMatrix(g);

		CommunityStructure cstruct = new CommunityStructureDirected();
		cstruct.run(mat);

		mergeList = cstruct.getMergeList();
		qvals = cstruct.getQValues();

		reconstructCommunity(qvals.length - 1);
		reconstructCommunity(getMaxQValueIndex());

	} //

	/*
	 * public void initComponents(ItemRegistry registry) { clear();
	 * 
	 * this.registry = registry; PrefuseConverter.JungGraph g = (PrefuseConverter.JungGraph) registry.getGraph();//
	 * getFilteredGraph();
	 * 
	 * range = new DefaultBoundedRangeModel(0, 0, 0, 0);
	 * 
	 * reconstructComponents(g); }
	 */

	public BoundedRangeModel getRange() {
		return range;
	} //

	private int computeRange(int idx) {
		int max = qvals.length - 1;
		for (int i = 0; i < qvals.length; i++) {
			if (Double.isInfinite(qvals[i]) || Double.isNaN(qvals[i]) || qvals[i] == 0.0) {
				break;
			} else {
				max = i;
			}
		}
		if (idx > max)
			idx = max;
		range = new DefaultBoundedRangeModel(idx, 0, 0, max);
		System.out.println("COMMUNITY: " + idx + " MAX: " + max);
		return idx;
	} //

	private int getMaxQValueIndex() {
		// get index for "optimal" cut
		int idx = -1;
		double max = -1;
		for (int i = 0; i < qvals.length; i++) {
			if (qvals[i] > max) {
				max = qvals[i];
				idx = i;
			}
		}
		return idx;
	} //

	public void reconstructCommunity(int idx) {
		synchronized (m_vis) {
			clear();

			idx = computeRange(idx);

			// merge groups
			int i = 0;
			// use link hashed map to enforce ordering
			// this is crucial for getting stable colors
			HashMap<Integer, List<Node>> merge = new LinkedHashMap<Integer, List<Node>>();
			Iterator<int[]> iter = mergeList.iterator();
			while (iter.hasNext() && i <= idx) {
				int[] edge = (int[]) iter.next();
				Integer k1 = new Integer(edge[0]);
				Integer k2 = new Integer(edge[1]);
				List<Node> l1;
				if ((l1 = (List<Node>) merge.get(k1)) == null) {
					l1 = new ArrayList<Node>();
					l1.add(mat.getNode(k1));
					merge.put(k1, l1);
				}
				List<Node> l2;
				if ((l2 = (List<Node>) merge.get(k2)) == null) {
					l1.add(mat.getNode(k2));
				} else {
					l1.addAll(l2);
					merge.remove(k2);
				}
				i++;
				if (merge.size() > maxCommunities)
					maxCommunities = merge.size();
			}
			// set community count
			this.numCommunities = merge.size();
			// System.out.println("numcomm = "+this.numCommunities);

			// re-label and index community groups
			int id = 0;
			Integer setID;
			List<Node> list;
			Iterator<Node> listIterator;
			Node node;
			Set<Node> nodeSet;

			Iterator<Integer> iter2 = merge.keySet().iterator();

			while (iter2.hasNext()) {
				setID = new Integer(id);
				// get nodes that belong to this community
				list = (List<Node>) merge.get(iter2.next());
				listIterator = list.iterator();

				while (listIterator.hasNext()) {
					node = (Node) listIterator.next();
					addTuple(node);

					if (node.canSetInt(SocialAction.COMMUNITY_COLUMN_NAME))
						node.setInt(SocialAction.COMMUNITY_COLUMN_NAME, setID);

					communityMap.put(node, setID);
					nodeSet = (Set<Node>) memberSets.get(setID);
					if (nodeSet == null) {
						nodeSet = new HashSet<Node>();
						memberSets.put(setID, nodeSet);
					}
					nodeSet.add(node);
				}
				id++;
			}
		}

		// System.out.println("reconstruct communities");
	} //

	// public void reconstructComponents(PrefuseConverter.JungGraph graph) {
	// synchronized (registry) {
	// clear();
	//
	// WeakComponentClusterer wcClusterer = new WeakComponentClusterer();
	// ClusterSet clusterSet = wcClusterer.extract(graph.jungGraph);
	//
	// int numClusters = clusterSet.size();
	//
	// System.out.println("numClusters: " + numClusters);
	//
	// for (int i = 0; i < numClusters; i++) {
	//
	// System.out.println(i + ": " + clusterSet.getCluster(i));
	//
	// }
	//
	// // set community count
	// this.numCommunities = numClusters;
	//
	// // System.out.println("numcomm = "+this.numCommunities);
	//
	// // re-label and index community groups
	// // int id = 0;
	// // iter = merge.keySet().iterator();
	// for (int id = 0; id < numClusters; id++) {
	//
	// Integer setidx = new Integer(id);
	// Set s = clusterSet.getCluster(id);
	// // List l = (List)merge.get(iter.next());
	// Iterator listiter = s.iterator();
	// while (listiter.hasNext()) {
	// Vertex v = (Vertex) listiter.next();
	// Node n = (Node) graph.jungToPrefuseVertices.get(v);
	// System.out.println(v + " --> " + n);
	// if (n instanceof NodeItem) {
	// n = (Node) ((NodeItem) n).getEntity();
	// }
	// this.add(n);
	// communityMap.put(n, setidx);
	// Set set = (Set) memberSets.get(setidx);
	// if (set == null) {
	// set = new HashSet();
	// memberSets.put(setidx, set);
	// }
	// set.add(n);
	// }
	//                
	// }
	//
	// }
	//
	// } //

	public int getMaxCommunityCount() {
		return maxCommunities;
	} //

	public int getCommunityCount() {
		return numCommunities;
	} //

	public Set<Node> getCommunityMembers(int i) {
		return memberSets.get(new Integer(i));
	} //

	public int getCommunity(Node n) {
		Integer comm = (Integer) communityMap.get(n);
		return (comm == null ? -1 : comm.intValue());
	} //

	public boolean areInSameCommunity(Node n1, Node n2) {
		int community1 = getCommunity(n1);
		int community2 = getCommunity(n2);

		return community1 == community2 && community1 != -1 && community2 != -1;
	}

} // end of class CommunitySet
