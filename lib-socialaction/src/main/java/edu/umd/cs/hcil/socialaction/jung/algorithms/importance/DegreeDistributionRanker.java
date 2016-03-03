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
import prefuse.data.Table;
import edu.umd.cs.hcil.socialaction.ui.panels.Node1DPanel;

/**
 * A simple node importance ranker based on the degree of the node. The user can specify whether s/he wants to use the
 * indegree or the outdegree as the metric. If the graph is undirected this option is effectively ignored. So for
 * example, if the graph is directed and the user chooses to use in-degree, nodes with the highest in-degree will be
 * ranked highest and similarly nodes with the lowest in-degree will be ranked lowest.
 * <p>
 * A simple example of usage is:
 * 
 * <pre>
 * DegreeDistributionRanker ranker = new DegreeDistributionRanker(someGraph);
 * ranker.evaluate();
 * ranker.printRankings();
 * </pre>
 * 
 * Modified from JUNG v1.7.4 (edu.uci.ics.jung.algorithms.importance.DegreeDistributionRanker) to work with Prefuse.
 * 
 * @author Scott White
 * @author Adam Perer
 */
public class DegreeDistributionRanker extends AbstractRanker {
	public final static String IN_KEY = Node1DPanel.RANKER_INDEGREE;

	public final static String OUT_KEY = Node1DPanel.RANKER_OUTDEGREE;

	private boolean mUseInDegree;

	private boolean directed;

	/**
	 * Default constructor which assumes if the graph is directed the indegree is to be used.
	 * 
	 * @param graph
	 *            the graph whose nodes are to be ranked based on indegree
	 */
	public DegreeDistributionRanker(Graph graph) {
		this(graph, true);
	}

	/**
	 * This constructor allows you to specify whether to use indegree or outdegree.
	 * 
	 * @param graph
	 *            the graph whose nodes are to be ranked based
	 * @param useInDegree
	 *            if <code>true</code>, indicates indegree is to be used, if <code>false</code> outdegree
	 */
	public DegreeDistributionRanker(Graph graph, boolean useInDegree) {
		initialize(graph, true, false);
		mUseInDegree = useInDegree;
		directed = graph.isDirected();

		Table nodeTable = graph.getNodeTable();
		if (useInDegree) {
			if (nodeTable.getColumn(IN_KEY) == null)
				nodeTable.addColumn(IN_KEY, double.class);
		} else {
			if (nodeTable.getColumn(OUT_KEY) == null)
				nodeTable.addColumn(OUT_KEY, double.class);
		}

		// directed = PredicateUtils.enforcesEdgeConstraint(getGraph(), Graph.DIRECTED_EDGE);
	}

	protected double evaluateIteration() {
		Node currentVertex = null;
		for (Iterator it = getVertices(); it.hasNext();) {
			currentVertex = (Node) it.next();
			if (directed) {
				if (mUseInDegree)
					setRankScore(currentVertex, (currentVertex).getInDegree());
				else
					setRankScore(currentVertex, (currentVertex).getOutDegree());
			} else
				setRankScore(currentVertex, currentVertex.getDegree());
		}
		// normalizeRankings();

		return 0;
	}

	public String getRankScoreKey() {
		if (mUseInDegree)
			return IN_KEY;
		else
			return OUT_KEY;
	}
}
