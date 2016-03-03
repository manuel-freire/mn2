package edu.umd.cs.hcil.socialaction.action;

import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.data.Graph;
import prefuse.data.Table;
import edu.umd.cs.hcil.socialaction.SocialAction;

/**
 * An Action that can be parameterized to process a particular group of items.
 * 
 * @author Adam Perer
 */
public class GraphChangerAction extends Action {

	protected static final String SRC = Graph.DEFAULT_SOURCE_KEY;

	protected static final String TRG = Graph.DEFAULT_TARGET_KEY;

	Graph m_graph;

	Graph[] m_graphs;

	Table m_bipartiteTable;

	SocialAction m_app;

	// WE NEED TO FIX THIS TO MAKE USE OF THE KEY...

	public GraphChangerAction(SocialAction app, Visualization vis, Graph graph) {
		super(vis);

		m_app = app;
		m_graph = graph;

	}

	// ------------------------------------------------------------------------

	/**
	 * @see prefuse.action.Action#run(double)
	 */
	public void run(double frac) {

		System.out.println("Changing graph to " + m_graph.getNodeCount() + " " + m_app.getNodeRangeSliderSetSize());

		this.m_app.setupGraph(m_graph, m_app.getLabelField());

		System.out.println("Graph Changed to " + this.m_app.getGraph().getNodeCount());

	}

} // end of class GroupAction
