package edu.umd.cs.hcil.socialaction.action;

import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.data.Graph;
import prefuse.data.Table;
import edu.umd.cs.hcil.socialaction.SocialAction;

/**
 * @author Adam Perer
 */
public class RepaintNodeAndEdgeTableAction extends Action {

	protected static final String SRC = Graph.DEFAULT_SOURCE_KEY;

	protected static final String TRG = Graph.DEFAULT_TARGET_KEY;

	Graph m_graph;

	Graph[] m_graphs;

	Table m_bipartiteTable;

	SocialAction m_app;

	// WE NEED TO FIX THIS TO MAKE USE OF THE KEY...

	public RepaintNodeAndEdgeTableAction(SocialAction app, Visualization vis) {
		super(vis);

		m_app = app;
	}

	// ------------------------------------------------------------------------

	/**
	 * @see prefuse.action.Action#run(double)
	 */
	public void run(double frac) {

		if (m_app.isUpdateNodeTable()) {
			m_app.setUpdateNodeTable(false);
			m_app.updateNodeTable();
		}

		if (m_app.isUpdateEdgeTable()) {
			m_app.setUpdateEdgeTable(false);
			m_app.updateEdgeTable();
		}

	}

} // end of class GroupAction
