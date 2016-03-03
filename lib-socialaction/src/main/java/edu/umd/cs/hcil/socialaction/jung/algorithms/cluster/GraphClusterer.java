/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package edu.umd.cs.hcil.socialaction.jung.algorithms.cluster;

import prefuse.data.Graph;

/**
 * Interface for finding clusters (sets of possibly overlapping vertices) in graphs.
 * 
 * Modified from JUNG v1.7.4 (edu.uci.ics.jung.algorithms.cluster.GraphClusterer) to work with Prefuse.
 * 
 * @author Scott White
 * @author Adam Perer
 */
public interface GraphClusterer {

	/**
	 * Extracts the clusters from a graph.
	 * 
	 * @param graph
	 *            the graph
	 * @return the set of clusters
	 */
	public ClusterSet extract(Graph graph);
}
