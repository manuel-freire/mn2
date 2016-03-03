package edu.umd.cs.hcil.socialaction.ui.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.query.RangeQueryBinding;
import prefuse.data.tuple.TupleSet;
import prefuse.util.ui.JRangeSlider;
import edu.umd.cs.hcil.socialaction.SocialAction;
import edu.umd.cs.hcil.socialaction.algorithms.importance.AttributeRanker;
import edu.umd.cs.hcil.socialaction.jung.algorithms.importance.AbstractRanker;
import edu.umd.cs.hcil.socialaction.jung.algorithms.importance.BetweennessCentrality;
import edu.umd.cs.hcil.socialaction.jung.algorithms.importance.Ranking;
import edu.umd.cs.hcil.socialaction.ui.RankTable;

/**
 * Displays a list of ranked edges/links
 * 
 * @version 1.0
 * @author Adam Perer
 */
public class Edge1DPanel extends JPanel implements ActionListener, ChangeListener {

	/** Never change */
	private static final long serialVersionUID = -3448078862794226104L;

	public final static int EDGE1D_STEP_NUMBER = 3;

	public final static String EDGE1D_RANK_ACTION = "Rank Edge";

	public final static String EDGE1D_FILTER_ACTION = "Filter";

	public final static String RANKER_BETWEENNESS = "Betweenness Centrality";
	public final static String RANKER_BETWEENNESS_DESC = "The number of shortest paths between pairs of nodes that pass through a given edge.";

	public final static String RANKER_BETWEENNESS_DIRECTED = "Betweenness Centrality (Directed)";
	public final static String RANKER_BETWEENNESS_DIRECTED_DESC = "The number of shortest directed paths between pairs of nodes that pass through a given edge.";

	public final static String RANKER_WEIGHT = "Weight";
	public final static String RANKER_WEIGHT_DESC = "The weight of each edge";

	public final static String[] RANKER_TYPES_1D = { RANKER_BETWEENNESS, RANKER_BETWEENNESS_DIRECTED }; // ,
	// RANKER_DEGREE,
	// RANKER_HITS,
	// RANKER_ST,

	// RANKER_WEIGHTED_NIPATHS };

	// RANKER_CUTPOINTS, RANKER_BRIDGES
	private JTextArea title;

	private RankTable edgeRankTable;

	private JComboBox rankType;

	private JRangeSlider slider;

	// private JButton exportVisibleNetworkButton;

	private List<Ranking> currentRankings;

	double currentRankingsMin;

	double currentRankingsMax;

	private Graph graph;

	private SocialAction m_app;

	RangeQueryBinding m_edgeQ;

	public Edge1DPanel(SocialAction app, Graph graph) {
		this.m_app = app;
		this.graph = graph;

		initUI();
	}

	Box rank1d_panel;

	private void initUI() {

		rank1d_panel = Box.createVerticalBox();
		rank1d_panel.setBorder(BorderFactory.createTitledBorder(SocialAction.EDGE_1D_PANEL));

		title = new JTextArea();
		title.setFont(new Font(SocialAction.DEFAULT_FONTNAME, Font.ITALIC, 12));
		title.setMaximumSize(new Dimension(300, 100));
		title.setWrapStyleWord(true);
		title.setLineWrap(true);
		title.setOpaque(false);

		Box titlebox = new Box(BoxLayout.Y_AXIS);// BoxLayout.X_AXIS);
		titlebox.add(Box.createHorizontalStrut(2));

		// java.net.URL imageURL = SocialAction.class.getResource("/tick.png");
		// ImageIcon icon = new ImageIcon(imageURL);
		// imageURL = SocialAction.class.getResource("/ablank.png");
		// ImageIcon blankicon = new ImageIcon(imageURL);
		// imageURL = SocialAction.class.getResource("/comment.gif");
		// ImageIcon comment = new ImageIcon(imageURL);
		// int [] yo = {1, 0, 1, 0, 2, 2, 0, 2 };
		// rankType = new JIconComboBox(RANKER_TYPES_1D, icon, yo, blankicon, comment);
		rankType = new JComboBox(RANKER_TYPES_1D);// , icon, yo, blankicon, comment);// XXX overwritten before sued
		// rankType.setSelectedItem(RANKER_WEIGHT);
		rankType.addActionListener(this);
		// rankType.setFont(new Font("Tahoma", Font.PLAIN, 12));
		titlebox.add(rankType, BorderLayout.NORTH);

		titlebox.add(title, BorderLayout.SOUTH);
		titlebox.add(Box.createHorizontalGlue());
		titlebox.setMaximumSize(new Dimension(800, 75));

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		rank1d_panel.add(titlebox);
		edgeRankTable = new RankTable(this.m_app, false, true);// pickedState, true, false);
		edgeRankTable.setOpaque(true); // content panes must be opaque
		rank1d_panel.add(edgeRankTable);

		// exportVisibleNetworkButton = new JButton("Export Visible Network");
		// exportVisibleNetworkButton.addActionListener(this);
		// rank1d_panel.add(exportVisibleNetworkButton);

		this.add(rank1d_panel);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(rankType)) {
			setContent();
		} /*
		 * else if (e.getSource().equals(exportVisibleNetworkButton)) { m_app.exportVisibleNetwork(
		 * getFieldName((String) rankType.getSelectedItem()) ); }
		 */
	}

	public void stateChanged(ChangeEvent e) {
		edgeRankTable.selectCell(null);
		edgeRankTable.setContent(currentRankings, currentRankingsMin, currentRankingsMax, m_edgeQ.getNumberModel()
				.getLowValue(), m_edgeQ.getNumberModel().getHighValue());
		edgeRankTable.revalidate();
		edgeRankTable.repaint();
		//m_app.updateAggregateColorPalette();// TODO need?

		if (m_app.animateLayoutFalseButton.isSelected())
			m_app.getVisualization().run("draw");

		/*
		 * if (m_app.isAppletMode()) { if (m_app.m_appletLabel != null) m_app.m_appletLabel.setText(Math.floor((Double)
		 * (m_edgeQ.getNumberModel().getLowValue())) + ""); }
		 */
	}

	public void setSelectedRanking(String ranking) {
		rankType.setSelectedItem(ranking);
		setContent();
	}

	public String getSelectedRanking() {
		return (String) rankType.getSelectedItem();
	}

	public AbstractRanker getRanker(Graph jungGraph, String rankTypeString) {
		AbstractRanker ranker = null;

		if (rankTypeString.equals(RANKER_BETWEENNESS)) {
			ranker = new BetweennessCentrality(jungGraph, false, true, false);
		} else if (rankTypeString.equals(RANKER_BETWEENNESS_DIRECTED)) {
			ranker = new BetweennessCentrality(jungGraph, false, true, true);
		} else if (rankTypeString.equals(RANKER_WEIGHT)) {
			ranker = new AttributeRanker(jungGraph, m_app.getEdgeWeightField(), false);
		} else {
			ranker = new AttributeRanker(jungGraph, rankTypeString, false);
			System.err.println("Using Attribute Ranking for " + rankTypeString);
			// title.setText(RANKER_INDEGREE_DESC);
		} /*
		 * else { System.err.println("Using NOTHING instead of " + rankTypeString); }
		 */
		return ranker;
	}

	public String getFieldName(String rankType) {
		if (rankType.equals(RANKER_BETWEENNESS)) {
			return BetweennessCentrality.CENTRALITY;
		} else if (rankType.equals(RANKER_BETWEENNESS_DIRECTED)) {
			return BetweennessCentrality.CENTRALITY_DIRECTED;
		} else if (rankType.equals(RANKER_WEIGHT)) {
			// return AttributeRanker.KEY;
			return m_app.getEdgeWeightField();
		} else {
			return rankType;
			// return "";
		}
	}

	public void setContent(Graph graph) {
		this.graph = graph;
		m_app.setEdgeColorField(null);

		// Reset the contents on loading a new graph
		rankType.removeAllItems();
		for (String type : RANKER_TYPES_1D) {
			rankType.addItem(type);
		}
		// edge1DPanel.getRankType().setSelectedIndex(-1); // TODO why not commented, vs Node1D?
                setContent();
	}

	public void setContent() {
		Table edgeTable = graph.getEdgeTable();

		// adding numerical attributes to the end of the statistical measurement options in combo box
		for (int i = 0; i < edgeTable.getColumnCount(); i++) {
			if ((edgeTable.getColumnType(i) == int.class) || (edgeTable.getColumnType(i) == double.class)) {

				boolean found = false;

				for (int j = 0; j < rankType.getItemCount(); j++) {
					if (edgeTable.getColumnName(i).equals(rankType.getItemAt(j))) {
						found = true;
						break;
					}
				}
				if (!found) {
					// System.err.println("Need to update SYF with this column: " + nodeTable.getColumnName(i));
					// m_app.addNode1DRankEvent(edgeTable.getColumnName(i));
					rankType.addItem(edgeTable.getColumnName(i));
				}
			}
		}

		/*
		 * String field = m_app.getEdgeWeightField(); // sne.getColorField(); //
		 * System.out.println("sne.getEdgeWeightField());
		 * 
		 * if (-1 == m_app.getGraph().getEdgeTable().getColumnNumber(field)) field = null;
		 */// TODO is it really necessary?
		AbstractRanker ranker = null;

		if (rankType.getSelectedItem() == null) {
			System.out.println("Null selected item");
			return;
		}

		edgeRankTable.setRankType((String) rankType.getSelectedItem());

		ranker = getRanker(this.graph, (String) rankType.getSelectedItem());
		ranker.evaluate();
		currentRankings = ranker.getRankings();
		// System.out.println(currentRankings);

		Double[] blah = { 0.0, 1.0 };

		m_app.addEventToHistory(EDGE1D_STEP_NUMBER, (String) rankType.getSelectedItem(), EDGE1D_RANK_ACTION, blah);
		System.out.println("EVENT: + " + (String) rankType.getSelectedItem() + " " + EDGE1D_RANK_ACTION);

		if (rankType.getSelectedItem().equals(RANKER_BETWEENNESS)) {
			title.setText(RANKER_BETWEENNESS_DESC);
			m_app.setEdgeColorField(getFieldName((String) rankType.getSelectedItem()));
			// sne.setColorField(getFieldName((String) rankType.getSelectedItem()));//BetweennessCentrality.CENTRALITY);
		} else if (rankType.getSelectedItem().equals(RANKER_BETWEENNESS_DIRECTED)) {
			title.setText(RANKER_BETWEENNESS_DIRECTED_DESC);
			m_app.setEdgeColorField(getFieldName((String) rankType.getSelectedItem()));
			// sne.setColorField(getFieldName((String) rankType.getSelectedItem()));//BetweennessCentrality.CENTRALITY);
		} else if (rankType.getSelectedItem().equals(RANKER_WEIGHT)) {
			title.setText(RANKER_WEIGHT_DESC);
			m_app.setEdgeColorField(getFieldName((String) m_app.getEdgeWeightField()));
			// sne.setColorField(DegreeDistributionRanker.KEY);
		} else {
			title.setText("Attribute values for " + rankType.getSelectedItem());
			m_app.setEdgeColorField((String) rankType.getSelectedItem());
		} /*
		 * else { title.setText(""); }
		 */

		Iterator<?> i = currentRankings.iterator();
		currentRankingsMin = 10000000;
		currentRankingsMax = 0;
		while (i.hasNext()) {// TODO faster?
			Ranking ranking = (Ranking) i.next();

			if (ranking.rankScore > currentRankingsMax)
				currentRankingsMax = ranking.rankScore;
			if (ranking.rankScore < currentRankingsMin)
				currentRankingsMin = ranking.rankScore;
		}

		edgeRankTable.setContent(currentRankings, currentRankingsMin, currentRankingsMax, currentRankingsMin,
				currentRankingsMax);
		m_app.setUpdateEdgeTable(true); // XXX determine necessity

		m_app.setEdgeRangeBinding(m_app.getEdgeColorField());
		TupleSet ts = graph.getEdgeTable();
		String field = m_app.getEdgeColorField();
		// if (graph.getEdgeTable().getColumn(field) != null) { // TODO is it really necessary?
		m_edgeQ = new RangeQueryBinding(ts, field);
		if (slider != null)
			rank1d_panel.remove(slider);

		slider = m_edgeQ.createHorizontalRangeSlider();

		// if ((slider.getMaximum() - slider.getMinimum()) == 1) {
		//           
		// slider.setMaximum(slider.getMaximum()+1);
		// }

		slider.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				// m_display.setHighQuality(false);
			}

			public void mouseReleased(MouseEvent e) {

				Double[] blah = { 0.0, 1.0 };
				m_app.addEventToHistory(EDGE1D_STEP_NUMBER, (String) rankType.getSelectedItem(), EDGE1D_FILTER_ACTION,
						blah);
			}
		});
		slider.addChangeListener(this);
		slider.setEnabled(true);

		// if (!m_app.isAppletMode()) {
		slider.setMaximumSize(new Dimension(1000, 50));
		rank1d_panel.add(slider);
		// }
		// }
	}

	// rank1d_panel.revalidate();
	// rank1d_panel.repaint();
	// }

	public void selectCell(Node vertex) {
		edgeRankTable.selectCell(vertex);
	}

	public void setRankType(String rankTypeString) {
		rankType.setSelectedItem(rankTypeString);
	}

	public JComboBox getRankType() {
		return rankType;

	}

	public JRangeSlider getSlider() {
		return slider;
	}

	public RankTable getRankTable() {
		return edgeRankTable;
	}

}