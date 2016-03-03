/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package edu.umd.cs.hcil.socialaction.jung.algorithms.importance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.Tuple;
import cern.colt.list.DoubleArrayList;
import edu.umd.cs.hcil.socialaction.jung.algorithms.IterativeProcess;

/**
 * Abstract class for algorithms that rank nodes or edges by some "importance" metric. Provides a common set of services
 * such as:
 * <ul>
 * <li>storing rank scores</li>
 * <li>getters and setters for rank scores</li>
 * <li>computing default edge weights</li>
 * <li>normalizing default or user-provided edge transition weights</li>
 * <li>normalizing rank scores</li>
 * <li>automatic cleanup of decorations</li>
 * <li>creation of Ranking list</li>
 * <li>print rankings in sorted order by rank</li>
 * </ul>
 * <p>
 * By default, all rank scores are removed from the vertices (or edges) being ranked.
 * 
 * Modified from JUNG v1.7.4 (edu.uci.ics.jung.algorithms.importance.AbstractRanker) to work with Prefuse.
 * 
 * @author Scott White
 * @author Adam Perer
 */
public abstract class AbstractRanker extends IterativeProcess {
	private Graph mGraph;
	private List<Ranking> mRankings;
	public static final String DEFAULT_EDGE_WEIGHT_KEY = "jung.algorithms.importance.AbstractRanker.EdgeWeight";
	private String mUserDefinedEdgeWeightKey;
	private boolean mRemoveRankScoresOnFinalize;
	private boolean mRankNodes;
	private boolean mRankEdges;
	private boolean mNormalizeRankings;

	protected void initialize(Graph graph, boolean isNodeRanker, boolean isEdgeRanker) {
		if (!isNodeRanker && !isEdgeRanker)
			throw new IllegalArgumentException("Must rank edges, vertices, or both");
		mGraph = graph;
		mRemoveRankScoresOnFinalize = true;
		mNormalizeRankings = true;
		mUserDefinedEdgeWeightKey = null;
		mRankNodes = isNodeRanker;
		mRankEdges = isEdgeRanker;
	}

	protected Iterator getVertices() {
		return mGraph.nodes();
		// return mGraph.getNodes();
	}

	protected Iterator getEdges() {
		return mGraph.edges();
		// return mGraph.getNodes();
	}

	protected int getNodeCount() {
		return mGraph.getNodeCount();
	}

	protected Graph getGraph() {
		return mGraph;
	}

	protected void reinitialize() {
	}

	/**
	 * Returns <code>true</code> if this ranker ranks nodes, and <code>false</code> otherwise.
	 */
	public boolean isRankingNodes() {
		return mRankNodes;
	}

	/**
	 * Returns <code>true</code> if this ranker ranks edges, and <code>false</code> otherwise.
	 */
	public boolean isRankingEdges() {
		return mRankEdges;
	}

	/**
	 * Instructs the ranker whether or not it should remove the rank scores from the nodes (or edges) once the ranks
	 * have been computed.
	 * 
	 * @param removeRankScoresOnFinalize
	 *            <code>true</code> if the rank scores are to be removed, <code>false</code> otherwise
	 */
	public void setRemoveRankScoresOnFinalize(boolean removeRankScoresOnFinalize) {
		this.mRemoveRankScoresOnFinalize = removeRankScoresOnFinalize;
	}

	protected void onFinalize(Tuple e) {
	}

	protected void finalizeIterations() {
		ArrayList<Ranking> sortedRankings = new ArrayList<Ranking>();

		int id = 1;
		if (mRankNodes) {
			for (Iterator it = getVertices(); it.hasNext();) {
				Node currentVertex = (Node) it.next();
				NodeRanking ranking = new NodeRanking(id, getRankScore(currentVertex), currentVertex);
				sortedRankings.add(ranking);
				if (mRemoveRankScoresOnFinalize) {
					// currentVertex.removeUserDatum(getRankScoreKey());
				}
				id++;
				onFinalize(currentVertex);
			}
		}
		if (mRankEdges) {
			for (Iterator it = mGraph.edges(); it.hasNext();) {
				Edge currentEdge = (Edge) it.next();
				EdgeRanking ranking = new EdgeRanking(id, getRankScore(currentEdge), currentEdge);
				sortedRankings.add(ranking);
				if (mRemoveRankScoresOnFinalize) {
					// currentEdge.removeUserDatum(getRankScoreKey());
				}
				id++;
				onFinalize(currentEdge);
			}
		}

		mRankings = sortedRankings;
		Collections.sort(mRankings);
	}

	/**
	 * Retrieves the list of ranking instances in descending sorted order by rank score If the algorithm is ranking
	 * edges, the instances will be of type <code>EdgeRanking</code>, otherwise if the algorithm is ranking nodes the
	 * instances will be of type <code>NodeRanking</code>
	 * 
	 * @return the list of rankings
	 */
	public List<Ranking> getRankings() {
		return mRankings;
	}

	/**
	 * Return a list of the top k rank scores.
	 * 
	 * @param topKRankings
	 *            the value of k to use
	 * @return list of rank scores
	 */
	public DoubleArrayList getRankScores(int topKRankings) {
		DoubleArrayList scores = new DoubleArrayList();
		int count = 1;
		for (Iterator<Ranking> rIt = getRankings().iterator(); rIt.hasNext();) {
			if (count > topKRankings) {
				return scores;
			}
			NodeRanking currentRanking = (NodeRanking) rIt.next();
			scores.add(currentRanking.rankScore);
			count++;
		}

		return scores;
	}

	/**
	 * The user datum key used to store the rank score.
	 * 
	 * @return the key
	 */
	abstract public String getRankScoreKey();

	/**
	 * Given an edge or node, returns the corresponding rank score. This is a default implementation of getRankScore
	 * which assumes the decorations are of type Double. This method only returns legal values if
	 * <code>setRemoveRankScoresOnFinalize(false)</code> was called prior to <code>evaluate()</code>;
	 * 
	 * @return the rank score value
	 */
	public double getRankScore(Tuple e) {
		return getRankScore(e, getRankScoreKey());

	}

	public double getRankScore(Tuple e, String key) {
		Table table = null;

		if (e instanceof Node)
			table = mGraph.getNodeTable();
		else if (e instanceof Edge)
			table = mGraph.getEdgeTable();

		double rankScore;
		if (table.canGetDouble(key))
			rankScore = table.getDouble(e.getRow(), key);
		else
			rankScore = 0;
		return rankScore;
		// if (rankScore != null) {
		// return rankScore.doubleValue();
		// } else {
		// throw new FatalException("setRemoveRankScoresOnFinalize(false) must be called before evaluate().");
		//            
		// }

	}

	protected void setRankScore(Tuple e, double rankValue, String key) {

		Table table = null;

		if (e instanceof Node)
			table = mGraph.getNodeTable();
		else if (e instanceof Edge)
			table = mGraph.getEdgeTable();

		// double value = table.getDouble(e.getRow(), key);

		// if (value == null) {
		table.setDouble(e.getRow(), key, rankValue);
		// } else {
		// value = rankValue;
		// }

	}

	protected void setRankScore(Tuple e, double rankValue) {
		setRankScore(e, rankValue, getRankScoreKey());

	}

	protected double getEdgeWeight(Edge e) {
		// String edgeWeightKey = getEdgeWeightKeyName();

		System.out.println("Does not handle Edge weights yet..");
		return 1;
		// return ((Double) e.getUserDatum(edgeWeightKey)).doubleValue();
	}

	/**
	 * the user datum key used to store the edge weight, if any
	 * 
	 * @return the key
	 */
	public String getEdgeWeightKeyName() {
		if (mUserDefinedEdgeWeightKey == null) {
			return DEFAULT_EDGE_WEIGHT_KEY;
		} else {
			return mUserDefinedEdgeWeightKey;
		}
	}

	protected void setEdgeWeight(Edge e, double weight) {
		// String edgeWeightKey = getEdgeWeightKeyName();

		System.out.println("Does not handle Edge weights for SETTING yet..");

		// Double value = (Double) e.getUserDatum(edgeWeightKey);
		// if (value == null) {
		// e.setUserDatum(edgeWeightKey,new Double(weight),UserData.SHARED);
		// } else {
		// value.setDoubleValue(weight);
		// }
	}

	protected void assignDefaultEdgeTransitionWeights() {

		for (Iterator vIt = getVertices(); vIt.hasNext();) {
			Node currentVertex = (Node) vIt.next();

			// Set outgoingEdges = null;
			// if (getGraph().isDirected()) {
			// outgoingEdges = currentVertex.getOutEdges();
			// } else {
			// outgoingEdges = currentVertex.getIncidentEdges();
			// }
			// getOutEdges() returns the right thing regardless, so just use this.
			Iterator outgoingEdges = currentVertex.outEdges();

			double numOutEdges = currentVertex.getOutDegree();
			for (Iterator edgeIt = outgoingEdges; edgeIt.hasNext();) {
				Edge currentEdge = (Edge) edgeIt.next();
				setEdgeWeight(currentEdge, 1.0 / numOutEdges);
			}
		}

	}

	protected void normalizeEdgeTransitionWeights() {

		for (Iterator vIt = getVertices(); vIt.hasNext();) {
			Node currentVertex = (Node) vIt.next();

			// Set outgoingEdges = null;
			// if (getGraph().isDirected()) {
			// outgoingEdges = currentVertex.getOutEdges();
			// } else {
			// outgoingEdges = currentVertex.getIncidentEdges();
			// }
			Iterator outgoingEdges = currentVertex.outEdges();

			double totalEdgeWeight = 0;
			for (Iterator edgeIt = outgoingEdges; edgeIt.hasNext();) {
				Edge currentEdge = (Edge) edgeIt.next();
				totalEdgeWeight += getEdgeWeight(currentEdge);
			}

			// double numOutEdges = outgoingEdges.size();
			for (Iterator edgeIt = outgoingEdges; edgeIt.hasNext();) {
				Edge currentEdge = (Edge) edgeIt.next();
				setEdgeWeight(currentEdge, getEdgeWeight(currentEdge) / totalEdgeWeight);
			}
		}
	}

	protected void normalizeRankings() {
		if (!mNormalizeRankings) {
			return;
		}
		double totalWeight = 0;
		Node currentVertex = null;

		for (Iterator it = getVertices(); it.hasNext();) {
			currentVertex = (Node) it.next();
			totalWeight += getRankScore(currentVertex);
		}

		for (Iterator it = getVertices(); it.hasNext();) {
			currentVertex = (Node) it.next();
			setRankScore(currentVertex, getRankScore(currentVertex) / totalWeight);
		}
	}

	// /**
	// * Print the rankings to standard out in descending order of rank score
	// *
	// * @param verbose
	// * if <code>true</code>, include information about the actual rank order as well as the original position
	// * of the vertex before it was ranked
	// * @param printScore
	// * if <code>true</code>, include the actual value of the rank score
	// */
	// public void printRankings(boolean verbose, boolean printScore) {
	//
	// double total = 0;
	// Format formatter = new Format("%7.6f");
	// int rank = 1;
	// boolean hasLabels = StringLabeller.hasStringLabeller(getGraph());
	// StringLabeller labeller = StringLabeller.getLabeller(getGraph());
	// for (Iterator it = getRankings().iterator(); it.hasNext();) {
	// Ranking currentRanking = (Ranking) it.next();
	// double rankScore = currentRanking.rankScore;
	// if (verbose) {
	// System.out.print("Rank " + rank + ": ");
	// if (printScore) {
	// System.out.print(formatter.format(rankScore));
	// }
	// System.out.print("\tVertex Id: " + currentRanking.originalPos);
	// if (hasLabels && currentRanking instanceof NodeRanking) {
	// Vertex v = ((NodeRanking) currentRanking).vertex;
	// System.out.print(" (" + labeller.getLabel(v) + ")");
	// }
	// System.out.println();
	// } else {
	// System.out.print(rank + "\t");
	// if (printScore) {
	// System.out.print(formatter.format(rankScore));
	// }
	// System.out.println("\t" + currentRanking.originalPos);
	//
	// }
	// total += rankScore;
	// rank++;
	// }
	//
	// if (verbose) {
	// System.out.println("Total: " + formatter.format(total));
	// }
	// }

	/**
	 * Allows the user to specify whether or not s/he wants the rankings to be normalized. In some cases, this will have
	 * no effect since the algorithm doesn't allow normalization as an option
	 * 
	 * @param normalizeRankings
	 */
	public void setNormalizeRankings(boolean normalizeRankings) {
		mNormalizeRankings = normalizeRankings;
	}

	/**
	 * Allows the user to provide his own set of data instances as edge weights by giving the ranker the
	 * <code>UserDatum</code> key where those instances can be found.
	 * 
	 * @param keyName
	 *            the name of the <code>UserDatum</code> key where the data instance representing an edge weight can be
	 *            found
	 */
	public void setUserDefinedEdgeWeightKey(String keyName) {
		mUserDefinedEdgeWeightKey = keyName;
	}
}
