package edu.umd.cs.hcil.socialaction.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Set;

import prefuse.Visualization;
import prefuse.data.Node;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualItem;
import edu.umd.cs.hcil.socialaction.SocialAction;
import edu.umd.cs.hcil.socialaction.analysis.community.SubgraphSet;

public class LayoutCommunitiesListener implements ActionListener {

	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		SocialAction sa = SocialAction.getInstance();
		Visualization visualization = sa.m_vis;
		SubgraphSet subgraphs = (SubgraphSet) visualization.getFocusGroup(SocialAction.community);

		AggregateTable aaggregateTable = (AggregateTable) visualization.getGroup(SocialAction.communityAggr);

		int num = subgraphs.getCommunityCount();
		aaggregateTable.clear();

		// filter the aggregates to use
		for (int i = 0; i < num; i++) {
			Set<Node> set = subgraphs.getCommunityMembers(i);

			AggregateItem aitem = (AggregateItem) aaggregateTable.addItem();
			aitem.setInt(SocialAction.AGGREGATE_ID_COLUMN_NAME, i);
			aitem.setString(SocialAction.LABEL_COLUMN_NAME, new Integer(i).toString());

			Iterator<Node> it = set.iterator();
			while (it.hasNext()) {

				Node node = (Node) it.next();
				aitem.addItem((VisualItem) node);
			}
		}
	}

}
