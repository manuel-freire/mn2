/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package edu.umd.cs.hcil.socialaction.jung.algorithms.importance;

import java.util.Iterator;

import prefuse.data.Graph;
import prefuse.data.Node;
import edu.umd.cs.hcil.socialaction.jung.shortestpath.DijkstraDistance;
import edu.umd.cs.hcil.socialaction.ui.panels.Node1DPanel;

/**
 * A simple node importance ranker based on the total shortest path of the node. More central nodes in a connected
 * component will have smaller overall shortest paths, and 'peripheral' nodes on the network will have larger overall
 * shortest paths. Runing this ranker on a graph with more than one connected component will arbitarily mix nodes from
 * both components. For this reason you should probably run this ranker on one component only (but that goes for all
 * rankers).
 * 
 * <p>
 * A simple example of usage is:
 * 
 * <pre>
 * FarnessCentrality ranker = new FarnessCentrality(someGraph);
 * ranker.evaluate();
 * ranker.printRankings();
 * </pre>
 * 
 * Modified from JUNG v1.7.4 (edu.uci.ics.jung.algorithms.importance.BaryCenter) to work with Prefuse.
 * 
 * @author Dan Bolser, Scott White
 * @author Adam Perer
 */
public class FarnessCentrality extends AbstractRanker {

	public final static String KEY = Node1DPanel.RANKER_FARNESS;

	/**
	 * Constructor which initializes the algorithm
	 * 
	 * @param g
	 *            the graph whose nodes are to be analyzed
	 */
	public FarnessCentrality(Graph g) {
		initialize(g, true, false);

		if (g.getNodeTable().getColumn(getRankScoreKey()) == null)
			g.getNodeTable().addColumn(getRankScoreKey(), double.class);

	}

	protected double evaluateIteration() {
		// Use this class to compute shortest path lengths.
		DijkstraDistance p = new DijkstraDistance(getGraph());

		Iterator i = getVertices();

		while (i.hasNext()) {
			Node u = (Node) i.next();

			double baryCenter = 0;

			Iterator<Double> j = p.getDistanceMap(u).values().iterator();

			while (j.hasNext()) {
				baryCenter += ((Number) j.next()).doubleValue();
			}
			setRankScore(u, baryCenter);
		}
		return 0;
	}

	public String getRankScoreKey() {
		return KEY;
	}
}
