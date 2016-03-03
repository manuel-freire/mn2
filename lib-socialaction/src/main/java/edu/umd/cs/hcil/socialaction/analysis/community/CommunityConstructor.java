package edu.umd.cs.hcil.socialaction.analysis.community;

import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.data.Graph;
import edu.umd.cs.hcil.socialaction.SocialAction;

/**
 * Based on Jeff Heer's Vizter (vizster.action.CommunityConstructor)
 * 
 * @author Adam Perer
 */
public class CommunityConstructor extends Action {

	public static final int INIT_COMMUNITY = -1;
	public static final int INIT_COMPONENTS = -3;
	public static final int CLEAR = -2;

	// private SocialColorFunction m_color = null;
	private String m_group;
	private int m_idx = -1;

	Visualization m_vis;
	SocialAction m_app;

	public CommunityConstructor(SocialAction app, Visualization vis, String group) { // , SocialColorFunction color) {
		super(vis);
		m_app = app;
		m_vis = vis;
		m_group = group;

	} //

	public void setIndex(int idx) {
		m_idx = idx;
	} //

	/*
	 * Constructs the communities (non-Javadoc)
	 * 
	 * @see prefuse.action.Action#run(double)
	 */
	public void run(double frac) {
		// FocusManager fman = registry.getFocusManager();

		m_vis.getGroup(m_group);

		Graph g = (Graph) m_vis.getGroup(SocialAction.graph);

		if (m_app.getGraph().getNodeTable().getColumn(SocialAction.COMMUNITY_COLUMN_NAME) == null) {
			m_app.getGraph().getNodeTable().addColumn(SocialAction.COMMUNITY_COLUMN_NAME, int.class);
		}

		SubgraphSet comm = (SubgraphSet) m_vis.getFocusGroup(SocialAction.community);

		if (m_idx == INIT_COMMUNITY) {

			comm.initCommunity(m_vis, g);

		} else if (m_idx == CLEAR) {
			comm.clear();
		} else {
			// System.out.println("ELSE-reconstructing -- might be bad for components");
			// how is it bad and why?
			comm.reconstructCommunity(m_idx);
		}

		m_app.updateAggregateColorPalette(comm);
		m_app.updateCommunityTable();

	} //

} // end of class CommunityConstructor
