package edu.umd.cs.hcil.socialaction.algorithms.importance;

import java.util.Iterator;

import prefuse.data.Graph;
import prefuse.data.Node;
import edu.umd.cs.hcil.socialaction.jung.algorithms.importance.AbstractRanker;
import edu.umd.cs.hcil.socialaction.jung.shortestpath.DijkstraDistance;
import edu.umd.cs.hcil.socialaction.ui.panels.Node1DPanel;

/**
 * Closeness Centrality
 * 
 * @author Adam Perer
 */
public class ClosenessCentrality extends AbstractRanker {

	public final static String KEY = Node1DPanel.RANKER_CLOSENESS;

	/**
	 * Constructor which initializes the algorithm
	 * 
	 * @param g
	 *            the graph whose nodes are to be analyzed
	 */
	public ClosenessCentrality(Graph g) {
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

			double sumDistance = 0;

			Iterator<Double> j = p.getDistanceMap(u).values().iterator();

			while (j.hasNext()) {
				sumDistance += j.next().doubleValue();
			}

			double closeness = 1;
			if (sumDistance > 0)
				closeness = 1.0d / sumDistance;
			setRankScore(u, closeness);
		}
		return 0;
	}

	public String getRankScoreKey() {
		return KEY;
	}
}
