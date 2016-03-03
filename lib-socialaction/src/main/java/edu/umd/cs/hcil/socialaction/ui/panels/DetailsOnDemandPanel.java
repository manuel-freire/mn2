package edu.umd.cs.hcil.socialaction.ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import prefuse.data.Node;
import prefuse.util.io.IOLib;
import prefuse.visual.VisualItem;
import edu.umd.cs.hcil.socialaction.SocialAction;
import edu.umd.cs.hcil.socialaction.ui.tables.GraphTableModel;

public class DetailsOnDemandPanel extends JPanel {

	/** Never change */
	private static final long serialVersionUID = -2051363395609288159L;

	public final static int DETAILS_STEP_NUMBER = 8;

	public final static String DETAILS_ACTION = "Details Selection";

	private SocialAction m_app;

	private int panelWidth;

	private JLabel label;

	private GraphTableModel nodeDetailsTableModel;

	String nodeDetails[] = { "Attribute", "Value" };

	public DetailsOnDemandPanel(SocialAction app, int width) {
		this.m_app = app;
		this.panelWidth = width;

		initUI();
		// setContent();

	}

	private void initUI() {

		final Box graphStats_panel = Box.createVerticalBox();
		graphStats_panel.setBorder(BorderFactory.createTitledBorder(SocialAction.DETAILS_PANEL));

		JXTaskPaneContainer taskPaneContainer = new JXTaskPaneContainer();

		JXTaskPane networkOverview = new JXTaskPane();
		networkOverview.setTitle("Node Details");
		taskPaneContainer.add(networkOverview);

		nodeDetailsTableModel = new GraphTableModel(nodeDetails);

		JXTable networkTable = new JXTable(nodeDetailsTableModel);
		// networkTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
		networkTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		networkTable.setBackground(null);
		networkTable.setSortable(true);
		networkTable.packAll();

		networkOverview.add(networkTable);

		label = new JLabel();
		label.setBorder(BorderFactory.createLineBorder(Color.black));
		graphStats_panel.add(label);

		// put the action list on the left
		graphStats_panel.add(taskPaneContainer, BorderLayout.EAST);

		// and a file browser in the middle
		// graphStats_panel.add(fileBrowser, BorderLayout.CENTER);

		Box titlebox = new Box(BoxLayout.Y_AXIS);// BoxLayout.X_AXIS);
		titlebox.add(Box.createHorizontalStrut(2));

		titlebox.add(Box.createHorizontalGlue());
		titlebox.setMaximumSize(new Dimension(panelWidth, 75));

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		graphStats_panel.add(titlebox);

		this.add(graphStats_panel);

	}

	public void setContent(VisualItem item) {

		Image image;

		if (m_app.getImageField() != null) {
			String imageLocation = item.getString(m_app.getImageField());
			URL imageURL = IOLib.urlFromString(imageLocation);
			if (imageURL == null) {
				// System.err.println("Null image: " + imageLocation);
				image = null;
			} else
				image = Toolkit.getDefaultToolkit().createImage(imageURL);

			image = image.getScaledInstance(100, -1, Image.SCALE_SMOOTH);

			ImageIcon icon = null;
			if (image != null) {
				icon = new ImageIcon(image);

				label.setIcon(icon);
			}
		}

		Node node = (Node) item.getSourceTuple();

		int numColumns = 2;

		nodeDetailsTableModel.data = new Object[node.getColumnCount()][numColumns];

		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);

		// label.setText(item.getSourceTuple().getString(m_app.getLabelField()));

		for (int i = 0; i < node.getColumnCount(); i++) {

			nodeDetailsTableModel.setValueAt(node.getColumnName(i), i, 0);
			if (node.getColumnName(i).equals("party")) {
				if (node.getInt(i) == 0)
					nodeDetailsTableModel.setValueAt("Democrat", i, 1);
				if (node.getInt(i) == 1)
					nodeDetailsTableModel.setValueAt("Independent", i, 1);
				if (node.getInt(i) == 2)
					nodeDetailsTableModel.setValueAt("Republican", i, 1);

			} else if (node.getColumnType(i) == double.class) {
				// node.canGetDouble(node.getColumnName(i))) {
				nodeDetailsTableModel.setValueAt(df.format(node.getDouble(i)), i, 1);

			} else {

				nodeDetailsTableModel.setValueAt(node.get(i), i, 1);
			}

		}

		m_app.addEventToHistory(DETAILS_STEP_NUMBER, DETAILS_ACTION, DETAILS_ACTION, 1);

	}

	// private JEditorPane createEditorPane() {
	// JEditorPane editorPane = new JEditorPane();
	// editorPane.setEditable(false);
	// java.net.URL helpURL = SocialAction.class.getResource("/akaka.html");
	// if (helpURL != null) {
	// try {
	// editorPane.setPage(helpURL);
	// } catch (IOException e) {
	// System.err.println("Attempted to read a bad URL: " + helpURL);
	// }
	// } else {
	// System.err.println("Couldn't find file: TextSampleDemoHelp.html");
	// }
	//
	// return editorPane;
	// }

	// private JEditorPane createEditorPane() {
	// JEditorPane editorPane = new JEditorPane();
	// editorPane.setEditable(false);
	// editorPane.setEditorKit(new HTMLEditorKit());
	//
	// java.net.URL helpURL = null;
	// try {
	// helpURL = new URL("http://en.wikipedia.org/w/index.php?title=Zacarias_Moussaoui&printable=yes");
	// } catch (MalformedURLException e) {
	// System.out.println(e);
	// }
	// if (helpURL != null) {
	// try {
	// editorPane.setPage(helpURL);
	// } catch (IOException e) {
	// System.err.println("Attempted to read a bad URL: " + helpURL);
	// }
	// } else {
	// System.err.println("Couldn't find file: TextSampleDemoHelp.html");
	// }
	//
	// return editorPane;
	// }

}
