package edu.umd.cs.hcil.socialaction.ui.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableColumn;

import prefuse.data.Graph;
import prefuse.data.Table;
import edu.umd.cs.hcil.socialaction.SocialAction;
import edu.umd.cs.hcil.socialaction.ui.JSocialActionTable;

/**
 * Displays a preview of the table and allows users to select import options
 * 
 * @version 1.0
 * @author Adam Perer
 */
public class ImportPreviewPanel extends JPanel implements ActionListener, ChangeListener {

	/** Never change */
	private static final long serialVersionUID = 4280647224179718632L;

	private JTextArea title;

	// private JPrefuseTable prefuseTable;

	private JSocialActionTable prefuseTable;

	private JComboBox partitionAComboBox;

	private JComboBox partitionBComboBox;

	private JComboBox multiplexComboBox, labelComboBox, edgeWeightComboBox, edge1ComboBox, edge2ComboBox, nodeComboBox;

	private JButton importButton;

	private JCheckBox directedCheckBox;

	double currentRankingsMin;

	double currentRankingsMax;

	private SocialAction m_app;

	private Table m_table;
	String[] nodeColumnNames, edgeColumnNames;
	private Table m_edgeTable;

	private Graph m_graph;

	private boolean m_isTable, m_isBipartite, m_isHCILFormat;

	public ImportPreviewPanel(SocialAction app, Table table) {
		this.m_app = app;
		this.m_table = table;

		initUI(true, true, false);
	}

	public ImportPreviewPanel(SocialAction app, Graph g) {
		this.m_app = app;
		this.m_table = g.getNodeTable();
		this.m_graph = g;

		initUI(false, false, false);
	}

	public ImportPreviewPanel(SocialAction app, Table nodeTable, Table edgeTable) {
		this.m_app = app;
		this.m_table = nodeTable;
		this.m_edgeTable = edgeTable;

		initUI(true, false, true);
	}

	private void initUI(boolean isTable, boolean isBipartite, boolean isHCILFormat) {

		m_isTable = isTable;
		m_isBipartite = isBipartite;
		m_isHCILFormat = isHCILFormat;

		final Box import_panel = Box.createVerticalBox();
		import_panel.setBorder(BorderFactory.createTitledBorder("Import"));

		title = new JTextArea();
		title.setFont(new Font(SocialAction.DEFAULT_FONTNAME, Font.ITALIC, 12));
		title.setMaximumSize(new Dimension(300, 100));
		title.setWrapStyleWord(true);
		title.setLineWrap(true);
		title.setOpaque(false);

		Box titlebox = new Box(BoxLayout.Y_AXIS);// BoxLayout.X_AXIS);
		titlebox.add(Box.createHorizontalStrut(2));

		nodeColumnNames = new String[m_table.getColumnCount()];

		for (int i = 0; i < m_table.getColumnCount(); i++)
			nodeColumnNames[i] = m_table.getColumnName(i);

		Panel p = null;

		JLabel multiplexLabel = new JLabel("Multiplex:");
		multiplexLabel.setMaximumSize(new Dimension(75, 300));

		multiplexComboBox = new JComboBox(nodeColumnNames);
		multiplexComboBox.setMaximumSize(new Dimension(75, 400));
		multiplexComboBox.setSelectedIndex(-1);
		multiplexComboBox.addActionListener(this);

		JLabel edgeWeightLabel = new JLabel("Edge Weight:");
		edgeWeightLabel.setMaximumSize(new Dimension(75, 300));

		if (isBipartite) {
			JLabel partitionALabel = new JLabel("Partition A:");
			partitionALabel.setMaximumSize(new Dimension(75, 300));
			JLabel partitionBLabel = new JLabel("Partition B:");
			partitionBLabel.setMaximumSize(new Dimension(75, 300));

			partitionAComboBox = new JComboBox(nodeColumnNames);
			partitionBComboBox = new JComboBox(nodeColumnNames);

			partitionAComboBox.setSelectedItem(nodeColumnNames[0]);
			if (nodeColumnNames.length > 1)
				partitionBComboBox.setSelectedItem(nodeColumnNames[1]);
			partitionAComboBox.setMaximumSize(new Dimension(75, 400));
			partitionBComboBox.setMaximumSize(new Dimension(75, 400));
			partitionAComboBox.addActionListener(this);
			partitionBComboBox.addActionListener(this);

			p = new Panel();
			p.add(partitionALabel);
			p.add(partitionAComboBox);
			titlebox.add(p, BorderLayout.NORTH);

			p = new Panel();
			p.add(partitionBLabel);
			p.add(partitionBComboBox);
			titlebox.add(p, BorderLayout.NORTH);

			edgeWeightComboBox = new JComboBox(nodeColumnNames);

		} else {
			Table edgeTable;
			if ((m_graph != null) && (!m_isHCILFormat))
				edgeTable = m_graph.getEdgeTable();
			else
				edgeTable = m_edgeTable;

			edgeColumnNames = new String[edgeTable.getColumnCount()];

			multiplexComboBox.removeAllItems();
			for (int i = 0; i < edgeTable.getColumnCount(); i++) {
				edgeColumnNames[i] = edgeTable.getColumnName(i);
				multiplexComboBox.addItem(edgeColumnNames[i]);
			}
			multiplexComboBox.setSelectedIndex(-1);

			edgeWeightComboBox = new JComboBox(edgeColumnNames);

			JLabel labelLabel = new JLabel("Label:");
			labelLabel.setMaximumSize(new Dimension(75, 300));

			labelComboBox = new JComboBox(nodeColumnNames);
			labelComboBox.setMaximumSize(new Dimension(75, 400));
			labelComboBox.setSelectedIndex(0);
			labelComboBox.addActionListener(this);

			p = new Panel();
			p.add(labelLabel);
			p.add(labelComboBox);
			titlebox.add(p, BorderLayout.NORTH);
		}

		if (m_isHCILFormat) {

			JLabel labelLabel = new JLabel("Node ID Column:");
			labelLabel.setMaximumSize(new Dimension(75, 300));

			nodeComboBox = new JComboBox(nodeColumnNames);
			nodeComboBox.setMaximumSize(new Dimension(75, 400));
			nodeComboBox.setSelectedIndex(0);
			nodeComboBox.addActionListener(this);

			p = new Panel();
			p.add(labelLabel);
			p.add(nodeComboBox);
			titlebox.add(p, BorderLayout.NORTH);

			labelLabel = new JLabel("Edge 1 Column:");
			labelLabel.setMaximumSize(new Dimension(75, 300));

			edge1ComboBox = new JComboBox(edgeColumnNames);
			edge2ComboBox = new JComboBox(edgeColumnNames);

			edge1ComboBox.setSelectedItem(edgeColumnNames[0]);
			if (edgeColumnNames.length > 1)
				edge2ComboBox.setSelectedItem(edgeColumnNames[1]);

			p = new Panel();
			p.add(labelLabel);
			p.add(edge1ComboBox);
			titlebox.add(p, BorderLayout.NORTH);

			labelLabel = new JLabel("Edge 2 Column:");
			labelLabel.setMaximumSize(new Dimension(75, 300));

			p = new Panel();
			p.add(labelLabel);
			p.add(edge2ComboBox);
			titlebox.add(p, BorderLayout.NORTH);
		}

		p = new Panel();
		p.add(multiplexLabel);
		p.add(multiplexComboBox);
		titlebox.add(p, BorderLayout.NORTH);

		edgeWeightComboBox.setMaximumSize(new Dimension(75, 400));
		edgeWeightComboBox.setSelectedIndex(-1);
		edgeWeightComboBox.addActionListener(this);

		p = new Panel();
		p.add(edgeWeightLabel);
		p.add(edgeWeightComboBox);
		titlebox.add(p, BorderLayout.NORTH);

		directedCheckBox = new JCheckBox("Directed Graph?");
		if (isHCILFormat)
			directedCheckBox.setSelected(true);

		p = new Panel();
		p.add(directedCheckBox);
		titlebox.add(p, BorderLayout.NORTH);

		if (isTable)
			prefuseTable = new JSocialActionTable(this.m_app, this.m_table);
		else
			prefuseTable = new JSocialActionTable(this.m_app, this.m_graph.getNodeTable());

		prefuseTable.setSize(new Dimension(500, 400));
		prefuseTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		TableColumn column = null;
		for (int i = 0; i < prefuseTable.getColumnCount(); i++) {
			column = prefuseTable.getColumnModel().getColumn(i);
			column.setPreferredWidth(50);
		}

		titlebox.add(new JScrollPane(prefuseTable));

		titlebox.add(title, BorderLayout.SOUTH);

		importButton = new JButton("Import");
		importButton.addActionListener(this);

		titlebox.add(importButton);
		titlebox.add(Box.createHorizontalGlue());

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		import_panel.add(titlebox);

		this.add(import_panel);

	}

	public void actionPerformed(ActionEvent e) {

		/*
		 * if (e.getSource().equals(partitionAComboBox)) setContent(); else
		 */if (e.getSource().equals(importButton)) {

			if (m_isTable) {
				if (m_isBipartite)
					this.m_app.importBipartiteTable(this.m_table, (String) partitionAComboBox.getSelectedItem(),
							(String) partitionBComboBox.getSelectedItem(),
							(String) multiplexComboBox.getSelectedItem(),
							(String) edgeWeightComboBox.getSelectedItem(), directedCheckBox.isSelected());
				if (m_isHCILFormat)
					this.m_app.importGraphFromHCILFormat(this.m_table, this.m_edgeTable, (String) labelComboBox
							.getSelectedItem(), (String) nodeComboBox.getSelectedItem(), (String) edge1ComboBox
							.getSelectedItem(), (String) edge2ComboBox.getSelectedItem(), (String) multiplexComboBox
							.getSelectedItem(), (String) edgeWeightComboBox.getSelectedItem(), directedCheckBox
							.isSelected());

			} else {
				this.m_app.importGraph(m_graph, (String) labelComboBox.getSelectedItem(), (String) multiplexComboBox
						.getSelectedItem());
			}
		}
	}

	public void stateChanged(ChangeEvent e) {
	}
}