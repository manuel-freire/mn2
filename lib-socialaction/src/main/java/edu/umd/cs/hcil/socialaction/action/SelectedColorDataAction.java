package edu.umd.cs.hcil.socialaction.action;

import java.awt.Color;

import prefuse.action.assignment.DataColorAction;
import prefuse.data.tuple.TupleSet;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;
import prefuse.visual.tuple.TableEdgeItem;
import prefuse.visual.tuple.TableNodeItem;
import edu.umd.cs.hcil.socialaction.SocialAction;

public class SelectedColorDataAction extends DataColorAction {

	private SocialAction m_app;

	public SelectedColorDataAction(SocialAction app, String group, String dataField, int dataType, String colorField,
			int[] palette) {
		super(group, dataField, dataType, colorField, palette);
		m_app = app;
	}

	public int getColor(VisualItem item) {
		int color = super.getColor(item);

		String sliderGroup;
		if (item instanceof TableNodeItem) {
			sliderGroup = SocialAction.RANGE_NODE_SLIDER_GROUP;
		} else if (item instanceof TableEdgeItem) {
			sliderGroup = SocialAction.RANGE_EDGE_SLIDER_GROUP;
		} else {
			return color;
		}

		TupleSet sliderSet = m_app.getVisualization().getFocusGroup(sliderGroup);

		if (!sliderSet.containsTuple(item.getSourceTuple())) {
			Color c = ColorLib.getColor(color);
			Color newColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), 100);
			color = newColor.getRGB();
		}

		return color;
	}
}
