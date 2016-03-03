package edu.umd.cs.hcil.socialaction.control;

import java.util.Iterator;

import prefuse.controls.NeighborHighlightControl;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import edu.umd.cs.hcil.socialaction.SocialAction;

public class SocialActionNeighborControl extends NeighborHighlightControl {

	SocialAction m_app;

	public SocialActionNeighborControl(SocialAction app) {
		super();
		m_app = app;

	}

	protected void setNeighborHighlight(NodeItem n, boolean state) {
		Iterator iter = n.edges();
		while (iter.hasNext()) {
			EdgeItem eitem = (EdgeItem) iter.next();
			NodeItem nitem = eitem.getAdjacentItem(n);

			if (eitem.isVisible() || highlightWithInvisibleEdge) {

				TupleSet sliderSet = m_app.getVisualization().getFocusGroup(SocialAction.RANGE_EDGE_SLIDER_GROUP);

				boolean as = sliderSet.containsTuple(eitem.getSourceTuple()); // item.isInGroup(SocialAction.RANGE_SLIDER_GROUP);

				if (as) {
					eitem.setHighlighted(state);
					nitem.setHighlighted(state);
				}
			}
		}
		if (activity != null)
			n.getVisualization().run(activity);
	}
}
