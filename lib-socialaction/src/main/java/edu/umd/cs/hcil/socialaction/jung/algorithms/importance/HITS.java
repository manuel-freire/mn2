/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package edu.umd.cs.hcil.socialaction.jung.algorithms.importance;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Tuple;
import edu.umd.cs.hcil.socialaction.ui.panels.Node1DPanel;

/**
 * Calculates the "hubs-and-authorities" importance measures for each node in a graph. These measures are defined
 * recursively as follows:
 * 
 * <ul>
 * <li>The *hubness* of a node is the degree to which a node links to other important authorities</li>
 * <li>The *authoritativeness* of a node is the degree to which a node is pointed to by important hubs</li>
 * <p>
 * Note: This algorithm uses the same key as HITSWithPriors for storing rank sccores.
 * <p>
 * A simple example of usage is:
 * 
 * <pre>
 * HITS ranker = new HITS(someGraph);
 * ranker.evaluate();
 * ranker.printRankings();
 * </pre>
 * 
 * <p>
 * Running time: O(|V|*I) where |V| is the number of vertices and I is the number of iterations until convergence
 * 
 * Modified from JUNG v1.7.4 (edu.uci.ics.jung.algorithms.importance.HITS) to work with Prefuse.
 * 
 * @author Scott White
 * @author Adam Perer
 * @see "Authoritative sources in a hyperlinked environment by Jon Kleinberg, 1997"
 */
public class HITS extends AbstractRanker {
	public static final String AUTHORITY_KEY = Node1DPanel.RANKER_HITS_AUTHORITY;
	public static final String HUB_KEY = Node1DPanel.RANKER_HITS_HUB;
	private String mKeyToUseForRanking;
	private Map<Node, Double> mPreviousAuthorityScores;
	private Map<Node, Double> mPreviousHubScores;

	/**
	 * Constructs an instance of the ranker where the type of importance that is associated with the rank score is the
	 * node's importance as an authority.
	 * 
	 * @param graph
	 *            the graph whose nodes are to be ranked
	 * @param useAuthorityForRanking
	 */
	public HITS(Graph graph, boolean useAuthorityForRanking) {
		mKeyToUseForRanking = AUTHORITY_KEY;
		if (!useAuthorityForRanking) {
			mKeyToUseForRanking = HUB_KEY;
		}
		initialize(graph);
	}

	/**
	 * Constructs an instance of the ranker where the type of importance that is associated with the rank score is the
	 * node's importance as an authority.
	 * 
	 * @param graph
	 *            the graph whose nodes are to be ranked
	 */
	public HITS(Graph graph) {
		mKeyToUseForRanking = AUTHORITY_KEY;
		initialize(graph);
	}

	protected void initialize(Graph g) {

		super.initialize(g, true, false);

		mPreviousAuthorityScores = new HashMap<Node, Double>();
		mPreviousHubScores = new HashMap<Node, Double>();

		if (g.getNodeTable().getColumn(AUTHORITY_KEY) == null)
			g.getNodeTable().addColumn(AUTHORITY_KEY, double.class);
		if (g.getNodeTable().getColumn(HUB_KEY) == null)
			g.getNodeTable().addColumn(HUB_KEY, double.class);

		for (Iterator vIt = g.nodes(); vIt.hasNext();) {
			Node currentNode = (Node) vIt.next();
			setRankScore(currentNode, 1.0, AUTHORITY_KEY);
			setRankScore(currentNode, 1.0, HUB_KEY);

			mPreviousAuthorityScores.put(currentNode, new Double(0));
			mPreviousHubScores.put(currentNode, new Double(0));
		}
	}

	protected void finalizeIterations() {
		super.finalizeIterations();
		for (Iterator it = getVertices(); it.hasNext();) {
			Node currentNode = (Node) it.next();
			if (mKeyToUseForRanking.equals(AUTHORITY_KEY)) {

				// currentNode.removeUserDatum(HUB_KEY);
			} else {
				// currentNode.removeUserDatum(AUTHORITY_KEY);
			}
		}
	}

	/**
	 * the user datum key used to store the rank scores
	 * 
	 * @return the key
	 */
	public String getRankScoreKey() {
		return mKeyToUseForRanking;
	}

	/**
	 * Given a node, returns the corresponding rank score. This implementation of <code>getRankScore</code> assumes the
	 * decoration representing the rank score is of type <code>MutableDouble</code>.
	 * 
	 * @return the rank score for this node
	 */
	public double getRankScore(Tuple v) {
		return getRankScore(v, mKeyToUseForRanking);
	}

	protected double getPreviousAuthorityScore(Tuple v) {
		return ((Double) mPreviousAuthorityScores.get(v)).doubleValue();
	}

	protected double getPreviousHubScore(Tuple v) {
		return ((Double) mPreviousHubScores.get(v)).doubleValue();
	}

	protected void setRankScore(Tuple v, double rankValue) {
		setRankScore(v, rankValue, mKeyToUseForRanking);
	}

	protected double evaluateIteration() {
		// updatePreviousScores();

		// Perform 2 update steps
		updateAuthorityRankings();
		updateHubRankings();

		double hubMSE = 0;
		double authorityMSE = 0;

		// Normalize rankings and test for convergence
		int numVertices = getNodeCount();
		for (Iterator vIt = getVertices(); vIt.hasNext();) {
			Node currentNode = (Node) vIt.next();

			double currentAuthorityScore = getRankScore(currentNode, AUTHORITY_KEY);
			double currentHubScore = getRankScore(currentNode, HUB_KEY);

			double previousAuthorityScore = getPreviousAuthorityScore(currentNode);
			double previousHubScore = getPreviousHubScore(currentNode);

			hubMSE += Math.pow(currentHubScore - previousHubScore, 2);
			authorityMSE += Math.pow(currentAuthorityScore - previousAuthorityScore, 2);
		}

		hubMSE = Math.pow(hubMSE / numVertices, 0.5);
		authorityMSE = Math.pow(authorityMSE / numVertices, 0.5);

		return hubMSE + authorityMSE;
	}

	/**
	 * If <code>evaluate()</code> has not already been called, the user can override the type of importance. (hub or
	 * authority) that should be associated with the rank score.
	 * 
	 * @param useAuthorityForRanking
	 *            if <code>true</code>, authority is used; if <code>false</code>, hub is used
	 */
	public void setUseAuthorityForRanking(boolean useAuthorityForRanking) {
		if (useAuthorityForRanking) {
			mKeyToUseForRanking = AUTHORITY_KEY;
		} else {
			mKeyToUseForRanking = HUB_KEY;
		}
	}

	private double computeSum(Iterator neighbors, String key) {
		double sum = 0;
		for (Iterator neighborIt = neighbors; neighborIt.hasNext();) {
			Node currentNeighbor = (Node) neighborIt.next();
			sum += getRankScore(currentNeighbor, key);
		}
		return sum;
	}

	private void normalizeRankings(double normConstant, String key) {
		for (Iterator NodeIt = getVertices(); NodeIt.hasNext();) {
			Node v = (Node) NodeIt.next();
			double rankScore = getRankScore(v, key);
			setRankScore(v, rankScore / normConstant, key);
		}
	}

	protected void updateAuthorityRankings() {
		double total = 0;
		// compute authority scores
		for (Iterator NodeIt = getVertices(); NodeIt.hasNext();) {
			Node currentNode = (Node) NodeIt.next();
			double currentHubSum = computeSum(currentNode.inNeighbors(), HUB_KEY);
			double newAuthorityScore = currentHubSum;
			total += newAuthorityScore;
			setRankScore(currentNode, newAuthorityScore, AUTHORITY_KEY);
		}

		normalizeRankings(total, AUTHORITY_KEY);
	}

	protected void updateHubRankings() {
		double total = 0;

		// compute hub scores
		for (Iterator NodeIt = getVertices(); NodeIt.hasNext();) {
			Node currentNode = (Node) NodeIt.next();
			double currentAuthoritySum = computeSum(currentNode.outNeighbors(), AUTHORITY_KEY);
			double newHubScore = currentAuthoritySum;
			total += newHubScore;
			setRankScore(currentNode, newHubScore, HUB_KEY);
		}
		normalizeRankings(total, HUB_KEY);
	}

	/*
	 * protected void updatePreviousScores() { for (Iterator vIt = getVertices(); vIt.hasNext();) { Node currentNode =
	 * (Node) vIt.next(); Double previousAuthorityScore = (Double) mPreviousAuthorityScores.get(currentNode); double
	 * currentAuthorityScore = getRankScore(currentNode, AUTHORITY_KEY); previousAuthorityScore = currentAuthorityScore;
	 * 
	 * Double previousHubScore = (Double) mPreviousHubScores.get(currentNode); double currentHubScore =
	 * getRankScore(currentNode, HUB_KEY); previousHubScore = currentHubScore; } }
	 */

}
