package edu.umd.cs.hcil.socialaction.control;

import java.awt.event.MouseEvent;

import prefuse.controls.FocusControl;
import prefuse.util.ui.UILib;
import prefuse.visual.AggregateItem;
import prefuse.visual.VisualItem;
import edu.umd.cs.hcil.socialaction.SocialAction;

public class SelectionDetailsControl extends FocusControl {

	private SocialAction m_app;

	public SelectionDetailsControl(int clicks, SocialAction app) {
		super(clicks);
		m_app = app;
	}

	public void itemClicked(VisualItem item, MouseEvent e) {
		super.itemClicked(item, e);
		if (!filterCheck(item))
			return;
		if (UILib.isButtonPressed(e, button) && e.getClickCount() == ccount) {
			if (!(item instanceof AggregateItem)) {
				m_app.setDetailsPanel(item);
				m_app.setReadabilityPanel(item);
			}
		}
	}
}
