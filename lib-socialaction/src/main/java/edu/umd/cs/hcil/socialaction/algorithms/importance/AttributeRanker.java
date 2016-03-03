package edu.umd.cs.hcil.socialaction.algorithms.importance;

import edu.umd.cs.hcil.socialaction.jung.algorithms.importance.AbstractRanker;
import prefuse.data.Graph;

/**
 * @author Adam Perer
 */
public class AttributeRanker extends AbstractRanker {
	public final static String KEY = "SocialAction.EDGE_WEIGHT";

	String m_attribute = "";

	public AttributeRanker(Graph graph, String attribute, boolean useNodes) {
		m_attribute = attribute;

		initialize(graph, useNodes, !useNodes);

		// JPrefuseTable.showTableWindow(graph.getEdgeTable());

		/*
		 * Table edgeTable = graph.getEdgeTable(); if (edgeTable.getColumn(KEY) == null) edgeTable.addColumn(KEY,
		 * double.class);
		 */// XXX what does this break?
		// directed = PredicateUtils.enforcesEdgeConstraint(getGraph(), Graph.DIRECTED_EDGE);
	}

	protected double evaluateIteration() {
		// Edge currentEdge = null;
		// int cnt = 0;
		// for (Iterator it = getEdges(); it.hasNext();) {
		// currentEdge = (Edge) it.next();
		// cnt++;
		// if (currentEdge.canGetDouble(m_attribute))
		// setRankScore(currentEdge, currentEdge.getDouble(m_attribute));
		// else
		// setRankScore(currentEdge, 0.0);//currentEdge.getDouble("weight"));
		//            
		// }
		// normalizeRankings();

		return 0;
	}

	public String getRankScoreKey() {
		return m_attribute;
	}
}
