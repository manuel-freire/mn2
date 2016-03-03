package edu.umd.cs.hcil.socialaction.algorithms.importance;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import prefuse.data.Graph;
import prefuse.data.Node;
import edu.umd.cs.hcil.socialaction.jung.algorithms.importance.AbstractRanker;
import edu.umd.cs.hcil.socialaction.jung.statistics.GraphStatistics;
import edu.umd.cs.hcil.socialaction.ui.panels.Node1DPanel;

/**
 * Clustering Coefficient
 * 
 * @author Adam Perer
 */
public class ClusteringCoefficient extends AbstractRanker {

	public final static String KEY = Node1DPanel.RANKER_COEFFICIENT;

	// "edu.uci.ics.jung.algorithms.importance.ClosenessCentrality.RankScore";

	/**
	 * Constructor which initializes the algorithm
	 * 
	 * @param g
	 *            the graph whose nodes are to be analyzed
	 */
	public ClusteringCoefficient(Graph g) {
		initialize(g, true, false);

		if (g.getNodeTable().getColumn(getRankScoreKey()) == null)
			g.getNodeTable().addColumn(getRankScoreKey(), double.class);

	}

	protected double evaluateIteration() {

		Map<Node, Double> m = GraphStatistics.clusteringCoefficients(getGraph());

		Set<Node> nodes = m.keySet();

		Iterator<Node> i = nodes.iterator();

		while (i.hasNext()) {

			Node n = i.next();
			setRankScore(n, m.get(n));

		}

		return 0;
	}

	public String getRankScoreKey() {
		return KEY;
	}
}
