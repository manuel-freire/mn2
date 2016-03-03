package edu.umd.cs.hcil.socialaction.ui.panels;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import prefuse.data.Graph;
import prefuse.data.Table;
import edu.umd.cs.hcil.socialaction.SocialAction;
import edu.umd.cs.hcil.socialaction.jung.algorithms.importance.AbstractRanker;

/**
 * DisplayPanel
 */
public class Rank2DScatterplotPanel extends JPanel implements ActionListener {

	/** Never change */
	private static final long serialVersionUID = -4282653542405165709L;
	public static final int NODE2D_STEP_NUMBER = 4;
	public final static String NODE2D_SCATTERPLOT_ACTION = "Scatterplot";
	public final static String NODE2D_FILTER_ACTION = "Filter";

	public final static String NODE2D_XFILTER_STATE = "Filter X-Axis";
	public final static String NODE2D_YFILTER_STATE = "Filter Y-Axis";

	public static String SCATTERPLOT_COLOR = "SCATTERPLOT_COLOR";

	private JComboBox xRankType, yRankType;

	private JButton recomputeVis;

	private SocialAction m_app;

	private Graph m_graph;

	// private ScattergramCanvas vis;

	private ScatterPlotVisPanel scatterPlotPanel;

	private int panelWidth;

	public Rank2DScatterplotPanel(SocialAction app, Graph graph, int width) {
		this.m_app = app;
		this.panelWidth = width;
		this.m_graph = graph;
		initUI();

	}

	public void setContent(Graph graph) {
		m_graph = graph;

		Table nodeTable = graph.getNodeTable();
		for (int i = 0; i < nodeTable.getColumnCount(); i++) {
			if ((nodeTable.getColumnType(i) == int.class) || (nodeTable.getColumnType(i) == double.class)) {

				boolean found = false;
				for (int j = 0; j < xRankType.getItemCount(); j++) {
					if (nodeTable.getColumnName(i).equals(xRankType.getItemAt(j))) {
						found = true;
						break;
					}
				}
				if (!found) {

					m_app.addNode2DRankEvent(nodeTable.getColumnName(i));
					xRankType.addItem(nodeTable.getColumnName(i));
					yRankType.addItem(nodeTable.getColumnName(i));

				}

			}
		}

		// plotRankings();

		// vis.initialize();
	}

	private void initUI() {

		final Box rank2d_panel = Box.createVerticalBox();
		rank2d_panel.setBorder(BorderFactory.createTitledBorder(SocialAction.NODE_2D_PANEL));

		JPanel selectedPanel = new JPanel();
		selectedPanel.setLayout(new BoxLayout(selectedPanel, BoxLayout.PAGE_AXIS));
		JLabel xLabel = new JLabel("X Axis: ");
		JLabel yLabel = new JLabel("Y Axis: ");

		xRankType = new JComboBox(Node1DPanel.RANKER_TYPES_1D);
		xRankType.setSelectedItem(Node1DPanel.RANKER_INDEGREE);
		xRankType.setPreferredSize(new Dimension(120, 30));
		xRankType.addActionListener(this);

		yRankType = new JComboBox(Node1DPanel.RANKER_TYPES_1D);
		yRankType.setSelectedItem(Node1DPanel.RANKER_CLOSENESS);
		yRankType.setPreferredSize(new Dimension(120, 30));
		yRankType.addActionListener(this);

		JPanel xRankPanel = new JPanel();
		xRankPanel.setLayout(new BoxLayout(xRankPanel, BoxLayout.LINE_AXIS));
		xRankPanel.add(xLabel);
		xRankPanel.add(xRankType);

		JPanel yRankPanel = new JPanel();
		yRankPanel.setLayout(new BoxLayout(yRankPanel, BoxLayout.LINE_AXIS));
		yRankPanel.add(yLabel);
		yRankPanel.add(yRankType);

		JPanel xAndYPanels = new JPanel();
		xAndYPanels.setLayout(new BoxLayout(xAndYPanels, BoxLayout.PAGE_AXIS));
		xAndYPanels.add(xRankPanel);
		xAndYPanels.add(yRankPanel);

		selectedPanel.add(xAndYPanels);

		scatterPlotPanel = new ScatterPlotVisPanel(m_app, m_graph, panelWidth - 100, 350);
		selectedPanel.add(scatterPlotPanel);
		// selectedPanel.add(p, BorderLayout.CENTER);

		recomputeVis = new JButton("Recompute Scattergram");
		recomputeVis.addActionListener(this);
		// selectedPanel.add(recomputeVis);

		rank2d_panel.add(selectedPanel);

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		this.add(rank2d_panel);
	}

	// private void setXContent() {
	// vis.setXRanker((String) xRankType.getSelectedItem());
	// vis.initialize();
	// }
	//
	// private void setYContent() {
	// vis.setYRanker((String) yRankType.getSelectedItem());
	// vis.initialize();
	// }

	public void setRankType(String state) {

		String[] split = state.split(" x ", 2);
		xRankType.setSelectedItem(split[0]);
		yRankType.setSelectedItem(split[1]);
		plotRankings();

	}

	public void plotRankings() {
		String xColumn = (String) xRankType.getSelectedItem();
		String yColumn = (String) yRankType.getSelectedItem();
		String xColumnName = Node1DPanel.getFieldName(xColumn);
		String yColumnName = Node1DPanel.getFieldName(yColumn);

		Table t = m_graph.getNodeTable();
		if (t.getColumn(xColumnName) == null) {

			AbstractRanker ranker = null;

			ranker = Node1DPanel.getRanker(m_graph, xColumn);
			ranker.evaluate();
		}

		if (t.getColumn(yColumnName) == null) {

			AbstractRanker ranker = null;

			ranker = Node1DPanel.getRanker(m_graph, yColumn);
			ranker.evaluate();
		}

		if (t.getColumn("SCATTERPLOT_COLOR") == null) {
			t.addColumn(SCATTERPLOT_COLOR, double.class);
		}
		int col1 = t.getColumnNumber(xColumnName);
		int col2 = t.getColumnNumber(yColumnName);
		int col3 = t.getColumnNumber(SCATTERPLOT_COLOR);

		double minX = 10000000, maxX = 0, minY = 100000000, maxY = 0;
		for (int i = 0; i < t.getRowCount(); i++) {

			double x = t.getDouble(i, col1);
			double y = t.getDouble(i, col2);
			if (x > maxX)
				maxX = x;
			if (x < minX)
				minX = x;
			if (y > maxY)
				maxY = y;
			if (y < minY)
				minY = y;
			t.setDouble(i, col3, x - y);
		}

		for (int i = 0; i < t.getRowCount(); i++) {

			double x = t.getDouble(i, col1);
			double y = t.getDouble(i, col2);
			double realX = x / maxX;
			double realY = y / maxY;

			t.setDouble(i, col3, realX - realY);
		}

		scatterPlotPanel.initialize(t, xColumnName, yColumnName, SCATTERPLOT_COLOR, m_graph);
		scatterPlotPanel.revalidate();
		Double[] blah = { 0.0, 1.0 };
		m_app.addEventToHistory(4, xColumn + " x " + yColumn, NODE2D_SCATTERPLOT_ACTION, blah);

	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {

		if (e.getSource().equals(xRankType))
			plotRankings();
		else if (e.getSource().equals(yRankType))
			plotRankings();
		else if (e.getSource().equals(recomputeVis)) {
			plotRankings();

			// vis.initialize();

		}

	}

} // end of class DisplayPanel
