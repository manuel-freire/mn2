package edu.umd.cs.hcil.socialaction.action.filter;

import java.util.Iterator;

import prefuse.Visualization;
import prefuse.action.filter.VisibilityFilter;
import prefuse.data.Node;
import prefuse.data.expression.Predicate;
import prefuse.util.PrefuseLib;
import prefuse.visual.VisualItem;
import edu.umd.cs.hcil.socialaction.SocialAction;

public class ScatterPlotVisibilityFilter extends VisibilityFilter {

	private SocialAction m_app;
	protected Visualization m_otherVis;
	protected String m_otherGroup;

	public ScatterPlotVisibilityFilter(String group, Predicate filter, SocialAction app, Visualization otherVis,
			String otherGroup) {
		super(group, filter);
		m_app = app;
		m_otherVis = otherVis;
		m_otherGroup = otherGroup;
	}

	public void run(double frac) {

		m_app.clearNodeRangeSliderSet();

		Iterator items = m_vis.items(m_group, m_filter);
		while (items.hasNext()) {
			VisualItem item = (VisualItem) items.next();

			VisualItem graphItem = m_app.getVisualization().getVisualItem(SocialAction.nodes, item.getSourceTuple());
			if (graphItem != null)
				m_app.addToNodeRangeSliderSet(graphItem.getSourceTuple());

			// m_app.addToRangeSliderSet(item.getSourceTuple());
			PrefuseLib.updateVisible(item, m_predicate.getBoolean(item));

			if (item.getSourceTuple() instanceof Node)
				System.out.println("node");

			// System.out.println(item.getSourceTuple());
			// System.out.println("1: " + m_app.m_graph.containsTuple(item.getSourceTuple()));
			// System.out.println("2: " +
			// m_app.getVisualization().getGroup(SocialAction.graph).containsTuple(item.getSourceTuple()));

		}

		System.out
				.println(m_app.getVisualization().getFocusGroup(SocialAction.RANGE_NODE_SLIDER_GROUP).getTupleCount());
	}
}
