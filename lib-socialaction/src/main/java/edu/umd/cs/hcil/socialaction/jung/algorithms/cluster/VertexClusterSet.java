/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package edu.umd.cs.hcil.socialaction.jung.algorithms.cluster;

import prefuse.data.Graph;

/**
 * A ClusterSet where each cluster is a set of vertices
 * 
 * Modified from JUNG v1.7.4 (edu.uci.ics.jung.algorithms.cluster.VertexClusterSet) to work with Prefuse.
 * 
 * @author Scott White
 * @author Adam Perer
 */
public class VertexClusterSet extends ClusterSet {

	/**
	 * Constructs and initializes the set
	 * 
	 * @param underlyingGraph
	 */
	public VertexClusterSet(Graph underlyingGraph) {
		super(underlyingGraph);
	}

	/**
	 * Constructs a new graph from the given cluster
	 * 
	 * @param index
	 *            the position index of the cluster in the collection
	 * @return a new graph representing the cluster
	 */
	// public Graph getClusterAsNewSubGraph(int index) {
	// return GraphUtils.vertexSetToGraph(getCluster(index));
	// }
	/**
	 * Creates a new cluster set where each vertex and cluster in the new cluster set correspond 1-to-1 with those in
	 * the original graph
	 * 
	 * @param anotherGraph
	 *            a new graph whose vertices are equivalent to those in the original graph
	 * @return a new cluster set for the specified graph
	 */
	// public ClusterSet createEquivalentClusterSet(Graph anotherGraph) {
	// ClusterSet newClusterSet = new VertexClusterSet(anotherGraph);
	// for (Iterator cIt=iterator();cIt.hasNext();) {
	// Set cluster = (Set) cIt.next();
	// Set newCluster = new HashSet();
	// for (Iterator vIt=cluster.iterator();vIt.hasNext();) {
	// Node vertex = (Node) vIt.next();
	// Node equivalentVertex = vertex.getEqualVertex(anotherGraph);
	// if (equivalentVertex == null) {
	// throw new
	// IllegalArgumentException("Can not create equivalent cluster set because equivalent vertices could not be found in the other graph.");
	// }
	// newCluster.add(equivalentVertex);
	// }
	// newClusterSet.addCluster(newCluster);
	// }
	// return newClusterSet;
	//
	// }
}
