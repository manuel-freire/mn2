package edu.umd.cs.hcil.socialaction.render;

import prefuse.Visualization;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.AggregateItem;
import prefuse.visual.DecoratorItem;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.sort.ItemSorter;
import edu.umd.cs.hcil.socialaction.SocialAction;

public class SocialActionItemSorter extends ItemSorter {

	protected static final int AGGREGATE = 0;
	protected static final int EDGE = 1;
	protected static final int ITEM = 2;
	protected static final int DECORATOR = 3;

	SocialAction m_app;

	public SocialActionItemSorter(SocialAction app) {
		super();
		m_app = app;

	}

	public int score(VisualItem item) {
		int type = ITEM;
		if (item instanceof EdgeItem) {
			type = EDGE;
		} else if (item instanceof AggregateItem) {
			type = AGGREGATE;
		} else if (item instanceof DecoratorItem) {
			type = DECORATOR;
		}

		int score = (1 << (26 + type));

		if (item.isHover()) {
			score += (1 << 25);
		}
		if (item.isHighlighted()) {
			score += (1 << 24);

		}

		TupleSet rangeSliderSet = m_app.getVisualization().getFocusGroup(SocialAction.RANGE_NODE_SLIDER_GROUP);

		if (rangeSliderSet.containsTuple(item.getSourceTuple())) {
			score += (1 << 23);

		}
		if (item.isInGroup(Visualization.FOCUS_ITEMS)) {
			score += (1 << 22);
		}
		// if ( item.isInGroup(Visualization.SEARCH_ITEMS) ) {
		// score += (1<<22);
		// }

		return score;
	}

}
