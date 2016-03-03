package edu.umd.cs.hcil.socialaction.ui.panels;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.util.force.ForceSimulator;
import prefuse.util.ui.JForcePanel;
import edu.umd.cs.hcil.socialaction.SocialAction;

/**
 * Displays a list of stats about the Network
 * 
 * @version 1.0
 * @author Adam Perer
 */
public class DisplaySettingsPanel extends JPanel implements ActionListener {

	/** Never change */
	private static final long serialVersionUID = -7114674619282693803L;

	public final static int DISPLAY_STEP_NUMBER = 0;

	public final static String DISPLAY_NETWORKVIS_STATE = "NetworkViz Setting State";

	public final static String DISPLAY_FORCE_SETTING = "Layout Force";

	public final static String DISPLAY_FONT_SETTING = "Font Size";

	public final static String DISPLAY_EDGE_SETTING = "Edge Size";

	public final static String DISPLAY_LABEL_SETTING = "Label Size";

	public final static String DISPLAY_LAYOUT_SETTING = "Bounded Layout";

	public final static String DISPLAY_ANIMATE_SETTING = "Layout Animate";

	public final static String DISPLAY_WORDWRAP_SETTING = "Label Truncate";

	public final static String DISPLAY_EXPORT_STATE = "Export State";
	public final static String DISPLAY_EXPORT = "Export to CSV";

	JButton saveImageButton, setQualityButton, shrinkImageButton;

	JTextField scaleText;

	private JComboBox nodeImageColumns;

	private SocialAction m_app;

	private int m_panelWidth;

	private Box settingsBox;
	private JForcePanel fpanel;
	private ForceSimulator fsim;

	public DisplaySettingsPanel(SocialAction app, Graph graph, int width) {
		this.m_app = app;
		this.m_panelWidth = width;

		initUI();
	}

	private void initUI() {

		settingsBox = Box.createVerticalBox();
		settingsBox.setBorder(BorderFactory.createTitledBorder(SocialAction.DISPLAY_PANEL));

		Box titlebox = new Box(BoxLayout.Y_AXIS);// BoxLayout.X_AXIS);
		titlebox.add(Box.createHorizontalStrut(2));

		titlebox.add(Box.createHorizontalGlue());
		titlebox.setMaximumSize(new Dimension(m_panelWidth, 75));

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		settingsBox.add(titlebox);

		// a container to put all JXTaskPane together
		JXTaskPaneContainer taskPaneContainer = new JXTaskPaneContainer();

		JXTaskPane nodeSettings = new JXTaskPane();
		nodeSettings.setTitle("Node Settings");

		JCheckBox showNodeText = new JCheckBox("Show Node Text", m_app.getNodeRenderProperties(1));
		showNodeText.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JCheckBox box = (JCheckBox) e.getSource();
				m_app.setNodeRenderProperties(1, box.isSelected());
			}
		});
		nodeSettings.add(showNodeText);

		JCheckBox showNodeImage = new JCheckBox("Show Node Images", m_app.getNodeRenderProperties(2));
		showNodeImage.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JCheckBox box = (JCheckBox) e.getSource();
				m_app.setNodeRenderProperties(2, box.isSelected());
			}
		});
		nodeSettings.add(showNodeImage);

		JCheckBox showFocusImage = new JCheckBox("Show Focus Images", m_app.getNodeRenderProperties(4));
		showFocusImage.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JCheckBox box = (JCheckBox) e.getSource();
				m_app.setNodeRenderProperties(4, box.isSelected());
			}
		});
		nodeSettings.add(showFocusImage);

		nodeImageColumns = new JComboBox();
		nodeSettings.add(nodeImageColumns);

		JCheckBox showFilteredNodes = new JCheckBox("Show Filtered Nodes", m_app.getNodeRenderProperties(3));
		showFilteredNodes.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JCheckBox box = (JCheckBox) e.getSource();
				m_app.setNodeRenderProperties(3, box.isSelected());
			}
		});
		nodeSettings.add(showFilteredNodes);

		JCheckBox layoutFilteredNodes = new JCheckBox("Layout Filtered Nodes", m_app.getLayoutFilteredNodes());
		layoutFilteredNodes.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JCheckBox box = (JCheckBox) e.getSource();
				m_app.setLayoutFilteredNodes(box.isSelected());

			}
		});
		nodeSettings.add(layoutFilteredNodes);

		JCheckBox realTimeGraphReadabilityMetrics = new JCheckBox("Real-Time Graph Readability Metrics", m_app
				.getRealTimeGraphReadabilityMetrics());
		realTimeGraphReadabilityMetrics.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JCheckBox box = (JCheckBox) e.getSource();
				m_app.setRealTimeGraphReadabilityMetrics(box.isSelected());

			}
		});
		nodeSettings.add(realTimeGraphReadabilityMetrics);

		JXTaskPane edgeSettings = new JXTaskPane();
		edgeSettings.setTitle("Edge Settings");

		JCheckBox showCurvedEdges = new JCheckBox("Show Curved Edges", m_app.getCurvedEdges());
		showCurvedEdges.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JCheckBox box = (JCheckBox) e.getSource();
				m_app.setCurvedEdges(box.isSelected());

			}
		});
		edgeSettings.add(showCurvedEdges);

		JCheckBox showArrows = new JCheckBox("Show Direction Arrows", m_app.isArrowRendered());
		showArrows.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JCheckBox box = (JCheckBox) e.getSource();
				m_app.setArrowRendered(box.isSelected());

			}
		});
		edgeSettings.add(showArrows);

		JCheckBox showConnectorMarkers = new JCheckBox("Show Connector Markers", m_app.isConnectorMarkerRendered());
		showConnectorMarkers.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JCheckBox box = (JCheckBox) e.getSource();
				m_app.setConnectorMarkerRendered(box.isSelected());

			}
		});
		edgeSettings.add(showConnectorMarkers);

		JCheckBox layoutFilteredEdges = new JCheckBox("Layout Filtered Edges", m_app.getLayoutFilteredEdges());
		layoutFilteredEdges.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JCheckBox box = (JCheckBox) e.getSource();
				m_app.setLayoutFilteredEdges(box.isSelected());

			}
		});
		edgeSettings.add(layoutFilteredEdges);

		JCheckBox showGradientEdges = new JCheckBox("Show Gradient Edges", true);
		showGradientEdges.setEnabled(false);
		edgeSettings.add(showGradientEdges);

		JXTaskPane overallSettings = new JXTaskPane();
		overallSettings.setTitle("Overall Settings");

		JCheckBox showHighQuality = new JCheckBox("Show High Quality", m_app.getHighQuality());
		showHighQuality.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JCheckBox box = (JCheckBox) e.getSource();
				m_app.setHighQuality(box.isSelected());

			}
		});
		overallSettings.add(showHighQuality);

		// JComboBox nodeImageColumns = new JComboBox();
		// nodeSettings.add(nodeImageColumns);
		//        
		// JCheckBox showFilteredNodes = new JCheckBox("Show Filtered Nodes", true);
		// nodeSettings.add(showFilteredNodes);

		taskPaneContainer.add(nodeSettings);
		taskPaneContainer.add(edgeSettings);
		taskPaneContainer.add(overallSettings);
		settingsBox.add(taskPaneContainer);

		saveImageButton = new JButton("Save Image");
		saveImageButton.addActionListener(this);

		scaleText = new JTextField();
		scaleText.setText("1.0");
		scaleText.setMaximumSize(new Dimension(45, 20));
		JLabel textFieldLabel = new JLabel("Scale: ");
		textFieldLabel.setLabelFor(scaleText);

		Box scaleTextBox = new Box(BoxLayout.X_AXIS);
		scaleTextBox.add(saveImageButton);
		scaleTextBox.add(textFieldLabel);
		scaleTextBox.add(scaleText);
		settingsBox.add(scaleTextBox);

		shrinkImageButton = new JButton("Shrink Images");
		shrinkImageButton.addActionListener(this);
		settingsBox.add(shrinkImageButton);
		if (m_app.getLayoutGraph().get(0) instanceof ForceDirectedLayout) {
			fsim = ((ForceDirectedLayout) m_app.getLayoutGraph().get(0)).getForceSimulator();
			fpanel = new JForcePanel(fsim);
			fpanel.setBackground(null);
			settingsBox.add(fpanel);
		}
		this.add(settingsBox);

	}

	public void setForceSimulator(ForceSimulator fsim) {
		this.fsim = fsim;
		settingsBox.remove(fpanel);
		fpanel = new JForcePanel(fsim);
		settingsBox.add(fpanel);
	}

	private ItemListener m_il = null;

	public void setContent(Graph graph) {
		nodeImageColumns.removeAllItems();
		if (m_il != null)
			nodeImageColumns.removeItemListener(m_il);

		Table nodeTable = graph.getNodeTable();
		for (int i = 0; i < nodeTable.getColumnCount(); i++) {
			nodeImageColumns.addItem(nodeTable.getColumnName(i));
		}

		nodeImageColumns.setSelectedItem(m_app.getImageField());

		m_il = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JComboBox box = (JComboBox) e.getSource();
				if (e.getStateChange() == ItemEvent.SELECTED) {
					if (box.getSelectedIndex() != -1)
						m_app.setImageField((String) box.getSelectedItem());
				}
			}
		};

		nodeImageColumns.addItemListener(m_il);

	}

	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == shrinkImageButton) {

			m_app.tr.setMaxImageDimensions(50, 50);
			m_app.tr.setImageField(m_app.getImageField());
			m_app.tr.setMaxImageDimensions(25, 25);
			System.out.println("Shrunk Images");

		}

		else if (e.getSource() == saveImageButton) {
			try {
				Calendar cal = new GregorianCalendar();
				String newFolderPath = "C:\\Program Files\\SocialAction";
				newFolderPath = "";
				File f = new File(newFolderPath);
				f.mkdir();

				File f2 = new File(/* newFolderPath + "\\" + */"ImageExport_" + SocialAction.getFilenameFromDate(cal)
						+ ".png");

				FileOutputStream stream = new FileOutputStream(f2);

				double scale = Double.parseDouble(scaleText.getText());

				m_app.getVisualization().getDisplay(0).saveImage(stream, "PNG", scale);
				try {
					stream.close();
				} catch (IOException er) {
					System.out.println(er.getMessage());
				}

			} catch (FileNotFoundException er) {
				System.out.println(er.getMessage());
			}
		}

	}

}