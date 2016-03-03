package edu.umd.cs.hcil.socialaction.analysis.community;

import java.util.Iterator;

import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;
import edu.umd.cs.hcil.socialaction.SocialAction;

/**
 * Based on Jeff Heer's Vizter (vizster.action.CommunityEdgeLabeler)
 * 
 * @version 1.0
 * @author Adam Perer
 */
public class CommunityEdgeLabeler extends Action {

	// private Object m_key;

	private String m_group;

	Visualization m_vis;
	SocialAction m_app;

	public CommunityEdgeLabeler(SocialAction app, Visualization vis, String group, Object communityKey) { // ,
		// SocialColorFunction
		// color) {
		super(vis);
		m_app = app;
		m_vis = vis;
		m_group = group;
		// m_key = communityKey;
	} //

	/**
	 * @see edu.berkeley.guir.prefuse.action.Action#run(edu.berkeley.guir.prefuse.ItemRegistry, double)
	 */
	public void run(double frac) {

		m_vis.getGroup(m_group);

		VisualGraph g = (VisualGraph) m_vis.getVisualGroup(SocialAction.graph);

		SubgraphSet comm = (SubgraphSet) m_vis.getFocusGroup(SocialAction.community);

		// catch early exit case
		if (comm.getCommunityCount() == 0)
			return;

		// iterate over edges, mark those that are between communities
		Iterator iter = g.edges();
		while (iter.hasNext()) {
			EdgeItem ei = (EdgeItem) iter.next();
			NodeItem n1 = (NodeItem) ei.getSourceNode();
			NodeItem n2 = (NodeItem) ei.getTargetNode();
			int c1 = comm.getCommunity(n1);// (Node)n1.getEntity());
			int c2 = comm.getCommunity(n2);
			boolean b = (c1 != c2 && c1 != -1 && c2 != -1);
			Boolean val = b ? Boolean.TRUE : Boolean.FALSE;
			ei.setBoolean("extraCommunity", val);
			// System.out.println("extraCommunity " + c1 + " " + val);
			// ei.setVizAttribute("extraCommunity", val);
		}
	} //

} // end of class CommunityEdgeLabeler
