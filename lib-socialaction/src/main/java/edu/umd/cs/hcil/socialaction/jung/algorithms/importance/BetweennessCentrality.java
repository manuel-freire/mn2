/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package edu.umd.cs.hcil.socialaction.jung.algorithms.importance;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.buffer.UnboundedFifoBuffer;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import edu.umd.cs.hcil.socialaction.ui.panels.Node1DPanel;

/**
 * Computes betweenness centrality for each vertex and edge in the graph. The result is that each vertex and edge has a
 * UserData element of type Double whose key is 'centrality.RelativeBetweennessCentrality' Note: Many social network
 * researchers like to normalize the betweenness values by dividing the values by (n-1)(n-2)/2. The values given here
 * are unnormalized.
 * <p>
 * <p>
 * A simple example of usage is: <br>
 * RelativeBetweennessCentrality ranker = new RelativeBetweennessCentrality(someGraph); <br>
 * ranker.evaluate(); <br>
 * ranker.printRankings();
 * <p>
 * Running time is: O(n^2 + nm).
 * 
 * @see "Ulrik Brandes: A Faster Algorithm for Betweenness Centrality. Journal of Mathematical Sociology 25(2):163-177,
 *      2001."
 *      
 * Modified from JUNG v1.7.4 (edu.uci.ics.jung.algorithms.importance.BetweennessCentrality) to work with Prefuse.
 * 
 * @author Scott White
 * @author Adam Perer
 */

public class BetweennessCentrality extends AbstractRanker {

	public static final String CENTRALITY = Node1DPanel.RANKER_BETWEENNESS;
	public static final String CENTRALITY_DIRECTED = Node1DPanel.RANKER_BETWEENNESS + " (Directed)";

	double sigma[];

	double d[];

	double Cb[];
	double Cb_e[];
	double delta[];

	boolean directed = false;

	String rankScoreKey;

	/**
	 * Constructor which initializes the algorithm - rank edges and nodes (undirected)
	 * 
	 * @param g
	 *            the graph whose nodes are to be analyzed
	 */
	public BetweennessCentrality(Graph g) {
		this(g, true);
	}

	/**
	 * Constructor which initializes the algorithm - rank edges and nodes optionally (undirected)
	 * 
	 * @param g
	 *            the graph whose nodes are to be analyzed
	 * @param rankNodes
	 *            whether we rank nodes
	 * @param rankEdges
	 *            whether we rank edges
	 */
	public BetweennessCentrality(Graph g, boolean rankNodes) {
		this(g, rankNodes, true);
	}

	/**
	 * Constructor which initializes the algorithm (undirected)
	 * 
	 * @param g
	 *            the graph whose nodes are to be analyzed
	 * @param rankNodes
	 *            whether we rank nodes
	 * @param rankEdges
	 *            whether we rank edges
	 */
	public BetweennessCentrality(Graph g, boolean rankNodes, boolean rankEdges) {
		this(g, rankNodes, rankEdges, false);
	}

	/**
	 * Constructor which initializes the algorithm
	 * 
	 * @param g
	 *            the graph whose nodes are to be analyzed
	 * @param rankNodes
	 *            whether we rank nodes
	 * @param rankEdges
	 *            whether we rank edges
	 * @param directed
	 *            whether we do a directed betweenness algorithm
	 */
	public BetweennessCentrality(Graph g, boolean rankNodes, boolean rankEdges, boolean directed) {
		initialize(g, rankNodes, rankEdges);
		this.directed = directed;
		rankScoreKey = directed ? CENTRALITY_DIRECTED : CENTRALITY;
		setupTable(g);
	}

	private void setupTable(Graph g) {
		Table nodeTable = g.getNodeTable();
		String colName = directed ? CENTRALITY_DIRECTED : CENTRALITY;
		if (nodeTable.getColumn(colName) == null) {
			nodeTable.addColumn(colName, double.class);
		}

		Table edgeTable = g.getEdgeTable();
		if (edgeTable.getColumn(colName) == null) {
			edgeTable.addColumn(colName, double.class);
		}
	}

	protected void computeBetweenness(Graph graph) {

		int numNodes = graph.getNodeCount();
		int numEdges = graph.getEdgeCount();

		Cb = new double[numNodes];

		for (int z = 0; z < numNodes; z++) {
			Cb[z] = 0.0;
		}

		Cb_e = new double[numEdges];

		for (int z = 0; z < numEdges; z++) {
			Cb_e[z] = 0.0;
		}

		for (int i = 0; i < numNodes; i++) {

			Node s = graph.getNode(i);

			Stack<Node> S = new Stack<Node>();
			ArrayList[] P = new ArrayList[numNodes];

			sigma = new double[numNodes];
			d = new double[numNodes];

			for (int j = 0; j < numNodes; j++) {
				sigma[j] = 0;
				d[j] = -1;
				P[j] = new ArrayList<Node>();
			}

			sigma[i] = 1;
			d[i] = 0;

			Buffer Q = new UnboundedFifoBuffer();
			Q.add(s);

			while (!Q.isEmpty()) {
				Node v = (Node) Q.remove();
				S.push(v);
				int vNum = v.getRow();

				int cnt = 0;
				for (Iterator<Node> neighborIterator = directed ? v.outNeighbors() : v.neighbors(); neighborIterator
						.hasNext();) {
					cnt++;

					Node w = neighborIterator.next();
					int wNum = w.getRow();

					// w found for the first time?
					if (d[wNum] < 0) {
						Q.add(w);
						d[wNum] = d[vNum] + 1;

					}

					// shortest path to w via v?
					if (d[wNum] == (d[vNum] + 1)) {
						sigma[wNum] = sigma[wNum] + sigma[vNum];
						P[wNum].add(v);
					}

				}

			}

			delta = new double[numNodes];
			for (int j = 0; j < numNodes; j++) {
				delta[j] = 0;
			}

			// S returns vertices in order of non-increasing distance from s
			while (!S.isEmpty()) {
				Node w = S.pop();

				int wNum = w.getRow();

				int cnt = 0;
				for (Iterator<Node> vIt = P[wNum].iterator(); vIt.hasNext();) {
					Node v = vIt.next();

					int vNum = v.getRow();
					double partialDependency = (1.0 * sigma[vNum] / sigma[wNum]) * (1.0 + delta[wNum]);
					delta[vNum] += partialDependency;
					cnt++;

					Edge currentEdge = graph.getEdge(v, w);
					if (currentEdge == null && !directed) {
						currentEdge = graph.getEdge(w, v);
					}
					if (currentEdge == null) {
						System.err.println(directed ? ("BetweennessCentrality: No edge exists for " + v + "->" + w
								+ " or " + w + "->" + v)
								: ("BetweennessCentrality: No edge exists for " + v + "->" + w));
						continue;
					}
					int eNum = currentEdge.getRow();
					Cb_e[eNum] += partialDependency;
				}

				if (w != s) {

					Cb[wNum] = (Cb[wNum] + delta[wNum]);
				}

			}
		}
	}

	/**
	 * the user datum key used to store the rank scores
	 * 
	 * @return the key
	 */
	public String getRankScoreKey() {
		return rankScoreKey;
	}

	protected double evaluateIteration() {
		computeBetweenness(getGraph());

		Node currentVertex = null;
		for (Iterator<Node> it = getVertices(); it.hasNext();) {
			currentVertex = it.next();
			setRankScore(currentVertex, Cb[currentVertex.getRow()]);// .doubleValue());
		}

		Edge currentEdge = null;
		for (Iterator<Edge> it = getEdges(); it.hasNext();) {
			currentEdge = it.next();
			setRankScore(currentEdge, Cb_e[currentEdge.getRow()]);// .doubleValue());
		}

		return 0;
	}

}
