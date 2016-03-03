/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package edu.umd.cs.hcil.socialaction.jung.algorithms.cluster;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.buffer.UnboundedFifoBuffer;

import prefuse.data.Graph;
import prefuse.data.Node;

/**
 * Finds all weak components in a graph where a weak component is defined as a maximal subgraph in which all pairs of
 * vertices in the subgraph are reachable from one another in the underlying undirected subgraph.
 * <p>
 * Running time: O(|V| + |E|) where |V| is the number of vertices and |E| is the number of edges.
 * 
 * Modified from JUNG v1.7.4 (edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer) to work with Prefuse.
 * 
 * @author Scott White
 * @author Adam Perer
 */
public class WeakComponentClusterer implements GraphClusterer {

	/**
	 * Extracts the weak components from a graph.
	 * 
	 * @param aGraph
	 *            the graph whose weak components are to be extracted
	 * @return the list of weak components
	 */
	public ClusterSet extract(Graph aGraph) {

		VertexClusterSet clusterSet = new VertexClusterSet(aGraph);

		HashSet<Node> unvisitedVertices = new HashSet<Node>();
		for (Iterator vIt = aGraph.nodes(); vIt.hasNext();) {
			unvisitedVertices.add((Node)vIt.next());
		}

		while (!unvisitedVertices.isEmpty()) {
			Set<Node> weakComponentSet = new HashSet<Node>();
			Node root = (Node) unvisitedVertices.iterator().next();
			unvisitedVertices.remove(root);
			weakComponentSet.add(root);

			Buffer queue = new UnboundedFifoBuffer();
			queue.add(root);

			while (!queue.isEmpty()) {
				Node currentVertex = (Node) queue.remove();

				for (Iterator nIt = currentVertex.neighbors(); nIt.hasNext();) {
					Node neighbor = (Node) nIt.next();
					if (unvisitedVertices.contains(neighbor)) {
						queue.add(neighbor);
						unvisitedVertices.remove(neighbor);
						weakComponentSet.add(neighbor);
					}
				}
			}
			clusterSet.addCluster(weakComponentSet);
		}

		return clusterSet;
	}

}
