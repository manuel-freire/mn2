package edu.umd.cs.hcil.socialaction.ui.panels.syf;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;

import org.jdesktop.swingx.JXHyperlink;

import prefuse.util.FontLib;

public class SYFTagCloud extends JPanel {

	/** Never change */
	private static final long serialVersionUID = -4748865743239961741L;

	SYFTagCloud() {

		// setSize(new Dimension(400, 1600));
		// setMaximumSize(new Dimension(400, 1600));
		setMinimumSize(new Dimension(300, 400));
		initUI();

	}

	public void initUI() {

		// String[] tagCloudWords = { "africa", "community", "indian", "insight", "latin", "middle east",
		// "node ranking", "scatterplot", "south america" };

		String[] tagCloudWords = { "9/11", "afghanistan", "al-qaeda", "alive", "contradicts", "dead", "family",
				"friends", "hijackers", "insight", "iraq", "religious", "supports", "united states", "unknown" };

		for (int i = 0; i < tagCloudWords.length; i++) {

			JXHyperlink link = new JXHyperlink();
			link.setText(tagCloudWords[i]);
			link.setUnclickedColor(Color.black);
			int size = (int) ((Math.random() * 100) % 25) + 8;
			link.setFont(FontLib.getFont(link.getFont().getName(), size));
			add(link);

		}
	}

}
