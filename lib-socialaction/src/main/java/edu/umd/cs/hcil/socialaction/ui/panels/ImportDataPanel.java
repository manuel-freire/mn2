package edu.umd.cs.hcil.socialaction.ui.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import prefuse.visual.VisualItem;
import edu.umd.cs.hcil.socialaction.SocialAction;

public class ImportDataPanel extends JPanel implements ActionListener {

	/** Never change */
	private static final long serialVersionUID = -8908013502034680318L;

	public final static int IMPORT_DATA_STEP_NUMBER = 8;

	public final static String IMPORT_DATA_ACTION = "Import Data Selection";

	private int panelWidth;

	private JLabel label;

	// String nodeDetails[] = { "Attribute", "Value" };

	public ImportDataPanel(int width) {
		this.panelWidth = width;

		initUI();
		// setContent();

	}

	private void initUI() {

		final Box graphStats_panel = Box.createVerticalBox();
		graphStats_panel.setBorder(BorderFactory.createTitledBorder(SocialAction.IMPORT_DATA_PANEL));

		label = new JLabel();
		label.setBorder(BorderFactory.createLineBorder(Color.black));
		graphStats_panel.add(label);

		Box titlebox = new Box(BoxLayout.Y_AXIS);// BoxLayout.X_AXIS);
		titlebox.add(Box.createHorizontalStrut(2));

		titlebox.add(Box.createHorizontalGlue());
		titlebox.setMaximumSize(new Dimension(panelWidth, 75));

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		graphStats_panel.add(titlebox);

		JButton selectNodeFileButton = new JButton("Select Node File", new ImageIcon(SocialAction.class
				.getResource(SocialAction.IMAGE_BASE + "/cog.png")));
		selectNodeFileButton.addActionListener(this);
		graphStats_panel.add(selectNodeFileButton);

		JButton selectEdgeFileButton = new JButton("Select Edge File", new ImageIcon(SocialAction.class
				.getResource(SocialAction.IMAGE_BASE + "/link.png")));
		selectEdgeFileButton.addActionListener(this);
		graphStats_panel.add(selectEdgeFileButton);

		this.add(graphStats_panel);

	}

	public void setContent(VisualItem item) {

	}

	public void actionPerformed(ActionEvent e) {

	}

}
