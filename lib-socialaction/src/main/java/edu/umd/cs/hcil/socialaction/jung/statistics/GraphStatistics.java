/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package edu.umd.cs.hcil.socialaction.jung.statistics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.umd.cs.hcil.socialaction.jung.shortestpath.DijkstraDistance;
import edu.umd.cs.hcil.socialaction.jung.shortestpath.Distance;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

/**
 * A set of statistical measures for structural properties of a graph.
 * 
 * Modified from JUNG v1.7.4 (edu.uci.ics.jung.statistics.GraphStatistics) to work with Prefuse.
 * 
 * @author Scott White
 * @author Joshua O'Madadhain
 * @author Adam Perer
 */
public class GraphStatistics {

	/**
	 * Returns a <code>Map</code> of vertices to their clustering coefficients. The clustering coefficient cc(v) of a
	 * vertex v is defined as follows:
	 * <ul>
	 * <li/><code>degree(v) == 0</code>: 0
	 * <li/><code>degree(v) == 1</code>: 1
	 * <li/><code>degree(v) == n, n &gt; 1</code>: given S, the set of neighbors of <code>v</code>: cc(v) = (the sum
	 * over all w in S of the number of other elements of w that are neighbors of w) / ((|S| * (|S| - 1) / 2). Less
	 * formally, the fraction of <code>v</code>'s neighbors that are also neighbors of each other.
	 * <p>
	 * <b>Note</b>: This algorithm treats its argument as an undirected graph; edge direction is ignored.
	 * 
	 * @param graph
	 */
	public static Map<Node, Double> clusteringCoefficients(Graph graph) {
		Map<Node, Double> coefficients = new HashMap<Node, Double>();

		for (Iterator v_iter = graph.nodes(); v_iter.hasNext();) {
			Node v = (Node) v_iter.next();
			int n = v.getDegree();// .numNeighbors();
			if (n == 0)
				coefficients.put(v, new Double(0));
			else if (n == 1)
				coefficients.put(v, new Double(1));
			else {
				// how many of v's neighbors are connected to each other?
				double edge_count = 0;
				for (Iterator i = v.neighbors(); i.hasNext();) {
					Node w = (Node) i.next();

					for (Iterator j = v.neighbors(); j.hasNext();) {
						Node x = (Node) j.next();
						Edge edge = graph.getEdge(w, x);
						if (edge != null)
							edge_count++;

						// edge_count += w.isNeighborOf(x) ? 1 : 0;
					}
				}
				double possible_edges = (n * (n - 1)) / 2.0;
				coefficients.put(v, new Double(edge_count / possible_edges));
			}
		}

		return coefficients;
	}

	/**
	 * For each vertex <code>v</code> in <code>graph</code>, calculates the average shortest path length from
	 * <code>v</code> to all other vertices in <code>graph</code> using the metric specified by <code>d</code>, and
	 * returns the results in a <code>Map</code> from vertices to <code>Double</code> values. If there exists an ordered
	 * pair <code>&lt;u,v&gt;</code> for which <code>d.getDistance(u,v)</code> returns <code>null</code>, then the
	 * average distance value for <code>u</code> will be stored as <code>Double.POSITIVE_INFINITY</code>).
	 * 
	 * <p>
	 * To calculate the average distances, ignoring edge weights if any:
	 * 
	 * <pre>
	 * Map distances = GraphStatistics.averageDistances(g, new UnweightedShortestPath(g));
	 * </pre>
	 * 
	 * To calculate the average distances respecting edge weights:
	 * 
	 * <pre>
	 * DijkstraShortestPath dsp = new DijkstraShortestPath(g, nev);
	 * Map distances = GraphStatistics.averageDistances(g, dsp);
	 * </pre>
	 * 
	 * where <code>nev</code> is an instance of <code>NumberEdgeValue</code> that is used to fetch the weight for each
	 * edge.
	 * 
	 * @see edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath
	 * @see edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance
	 */
	public static Map<Node, Double> averageDistances(Graph graph, Distance d) {
		Map<Node, Double> avg_dist = new HashMap<Node, Double>();

		int n = graph.getNodeCount();
		for (Iterator outer = graph.nodes(); outer.hasNext();) {
			Node v = (Node) outer.next();
			double avgPathLength = 0;
			for (Iterator inner = graph.nodes(); inner.hasNext();) {
				Node w = (Node) inner.next();
				if (v != w) // don't include self-distances
				{
					Number dist = d.getDistance(v, w);
					if (dist == null) {
						avgPathLength = Double.POSITIVE_INFINITY;
						break;
					}
					avgPathLength += dist.doubleValue();
				}
			}
			avgPathLength /= (n - 1);
			avg_dist.put(v, new Double(avgPathLength));
		}
		return avg_dist;
	}

	/**
	 * For each vertex <code>v</code> in <code>g</code>, calculates the average shortest path length from <code>v</code>
	 * to all other vertices in <code>g</code>, ignoring edge weights.
	 * 
	 * @see #diameter(ArchetypeGraph, Distance)
	 */
	public static Map<Node, Double> averageDistances(Graph g) {
		return averageDistances(g, new DijkstraDistance((Graph) g));
	}

	/**
	 * Returns the diameter of <code>g</code> using the metric specified by <code>d</code>. The diameter is defined to
	 * be the maximum, over all pairs of vertices <code>u,v</code>, of the length of the shortest path from
	 * <code>u</code> to <code>v</code>. If the graph is disconnected (that is, not all pairs of vertices are reachable
	 * from one another), the value returned will depend on <code>use_max</code>: if <code>use_max == true</code>, the
	 * value returned will be the the maximum shortest path length over all pairs of <b>connected</b> vertices;
	 * otherwise it will be <code>Double.POSITIVE_INFINITY</code>.
	 */
	public static double diameter(Graph g, Distance d, boolean use_max) {
		double diameter = 0;

		for (Iterator outer = g.nodes(); outer.hasNext();) {
			Node v = (Node) outer.next();
			for (Iterator inner = g.nodes(); inner.hasNext();) {
				Node w = (Node) inner.next();
				if (v != w) // don't include self-distances
				{
					Number dist = d.getDistance(v, w);
					if (dist == null) {
						if (!use_max)
							return Double.POSITIVE_INFINITY;
					} else
						diameter = Math.max(diameter, dist.doubleValue());
				}
			}
		}
		return diameter;
	}

	/**
	 * Returns the diameter of <code>g</code> using the metric specified by <code>d</code>. The diameter is defined to
	 * be the maximum, over all pairs of vertices <code>u,v</code>, of the length of the shortest path from
	 * <code>u</code> to <code>v</code>, or <code>Double.POSITIVE_INFINITY</code> if any of these distances do not
	 * exist.
	 * 
	 * @see #diameter(ArchetypeGraph, Distance, boolean)
	 */
	public static double diameter(Graph g, Distance d) {
		return diameter(g, d, false);
	}

	/**
	 * Returns the diameter of <code>g</code>, ignoring edge weights.
	 * 
	 * @see #diameter(ArchetypeGraph, Distance, boolean)
	 */
	public static double diameter(Graph g) {
		return diameter(g, new DijkstraDistance((Graph) g));
	}

	public static double diameter(Graph g, boolean use_max) {
		return diameter(g, new DijkstraDistance((Graph) g), use_max);
	}

	/**
	 * Creates a histogram from a sequence of doubles
	 * 
	 * @param values
	 *            the sequence of doubles
	 * @param min
	 *            the minimum value to bin off of
	 * @param numBins
	 *            the number of bins
	 * @param binWidth
	 *            the width of the bin
	 * @return a histogram
	 */
	// public static Histogram createHistogram(
	// DoubleArrayList values,
	// double min,
	// int numBins,
	// double binWidth) {
	// Histogram histogram = new Histogram(numBins, min, binWidth);
	// for (int idx = 0; idx < values.size(); idx++) {
	// histogram.fill(values.get(idx));
	// }
	// return histogram;
	// }
}
