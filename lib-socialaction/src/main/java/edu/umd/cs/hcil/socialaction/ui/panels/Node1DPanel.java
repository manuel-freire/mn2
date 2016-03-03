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
import edu.umd.cs.hcil.socialaction.algorithms.importance.ClosenessCentrality;
import edu.umd.cs.hcil.socialaction.algorithms.importance.ClusteringCoefficient;
import edu.umd.cs.hcil.socialaction.jung.algorithms.importance.AbstractRanker;
import edu.umd.cs.hcil.socialaction.jung.algorithms.importance.BetweennessCentrality;
import edu.umd.cs.hcil.socialaction.jung.algorithms.importance.DegreeDistributionRanker;
import edu.umd.cs.hcil.socialaction.jung.algorithms.importance.FarnessCentrality;
import edu.umd.cs.hcil.socialaction.jung.algorithms.importance.HITS;
import edu.umd.cs.hcil.socialaction.jung.algorithms.importance.Ranking;
import edu.umd.cs.hcil.socialaction.ui.RankTable;

/**
 * Displays a list of ranked nodes/vertices
 * 
 * @version 1.0
 * @author Adam Perer
 */
public class Node1DPanel extends JPanel implements ActionListener, ChangeListener {

	/** Never change */
	private static final long serialVersionUID = -1050398364629133415L;
	public final static int NODE1D_STEP_NUMBER = 2;
	public final static String NODE1D_RANK_ACTION = "Rank";
	public final static String NODE1D_FILTER_ACTION = "Filter";

	public final static String RANKER_BETWEENNESS = "Betweenness Centrality";
	public final static String RANKER_BETWEENNESS_DESC = "The number of shortest paths between pairs of nodes that pass through a given node.";

	public final static String RANKER_BETWEENNESS_DIRECTED = "Betweenness Centrality (Directed)";
	public final static String RANKER_BETWEENNESS_DIRECTED_DESC = "The number of shortest directed paths between pairs of nodes that pass through a given node.";

	public final static String RANKER_DEGREE = "Degree Distribution";

	public final static String RANKER_INDEGREE = "In-degree";
	public final static String RANKER_INDEGREE_DESC = "The number of inbound edges connected to the node";

	public final static String RANKER_OUTDEGREE = "Out-degree";
	public final static String RANKER_OUTDEGREE_DESC = "The number of outbound edges connected to the node";

	public final static String RANKER_PAGERANK = "PageRank";

	public final static String RANKER_HITS_AUTHORITY = "HITS Authority";
	public final static String RANKER_HITS_AUTHORITY_DESC = "Authority is the degree to which a node pointed to by important hubs";

	public final static String RANKER_HITS_HUB = "HITS Hub";
	public final static String RANKER_HITS_HUB_DESC = "Hub is the degree to which a node links to important authorities";

	public final static String RANKER_ST = "S-T Betweenness";

	public final static String RANKER_WEIGHTED_NIPATHS = "Weighted NI Paths";

	public final static String RANKER_CUTPOINTS = "Cutpoints";
	public final static String RANKER_CUTPOINTS_DESC = "Cutpoints";

	public final static String RANKER_BRIDGES = "Bridges";
	public final static String RANKER_BRIDGES_DESC = "Bridges";

	public final static String RANKER_CLOSENESS = "Closeness Centrality";
	public final static String RANKER_CLOSENESS_DESC = "Closeness Centrality";

	public final static String RANKER_FARNESS = "Farness Centrality";
	public final static String RANKER_FARNESS_DESC = "Farness Centrality";

	public final static String RANKER_COEFFICIENT = "Clustering Coefficient";
	public final static String RANKER_COEFFICIENT_DESC = "Clustering Coefficient";

	public final static String RANKER_POWER = "Power Centrality";

	public final static String[] RANKER_TYPES_1D = { RANKER_INDEGREE, RANKER_OUTDEGREE, RANKER_BETWEENNESS,
			RANKER_BETWEENNESS_DIRECTED, RANKER_CLOSENESS, RANKER_FARNESS, RANKER_COEFFICIENT, RANKER_HITS_AUTHORITY,
			RANKER_HITS_HUB }; // ,
	// RANKER_DEGREE,
	// RANKER_HITS,
	// RANKER_ST,
	// RANKER_WEIGHTED_NIPATHS
	// };
	// RANKER_CUTPOINTS, RANKER_BRIDGES
	private JTextArea title;

	private RankTable nodeRankTable;

	private JComboBox rankType;

	private JRangeSlider slider;

	// private JButton exportVisibleNetworkButton;

	private List<Ranking> currentRankings;

	double currentRankingsMin;

	double currentRankingsMax;

	private Graph graph;

	private SocialAction m_app;

	RangeQueryBinding m_nodeQ;

	public Node1DPanel(SocialAction app, Graph graph) {
		this.m_app = app;
		this.graph = graph;

		initUI();
	}

	Box rank1d_panel;

	private void initUI() {

		rank1d_panel = Box.createVerticalBox();
		rank1d_panel.setBorder(BorderFactory.createTitledBorder(SocialAction.NODE_1D_PANEL));

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
		rankType = new JComboBox(RANKER_TYPES_1D);// , icon, yo, blankicon, comment);
		rankType.setSelectedItem(RANKER_INDEGREE);
		rankType.addActionListener(this);

		// rankType.setFont(new Font("Tahoma", Font.PLAIN, 12));
		titlebox.add(rankType, BorderLayout.NORTH);

		titlebox.add(title, BorderLayout.SOUTH);
		titlebox.add(Box.createHorizontalGlue());
		titlebox.setMaximumSize(new Dimension(800, 75));

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		rank1d_panel.add(titlebox);
		nodeRankTable = new RankTable(this.m_app, true, false);// pickedState, true, false);
		nodeRankTable.setOpaque(true); // content panes must be opaque
		rank1d_panel.add(nodeRankTable);

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
		nodeRankTable.selectCell(null);
		nodeRankTable.setContent(currentRankings, currentRankingsMin, currentRankingsMax, m_nodeQ.getNumberModel()
				.getLowValue(), m_nodeQ.getNumberModel().getHighValue());
		nodeRankTable.revalidate();
		nodeRankTable.repaint();
		m_app.updateAggregateColorPalette();

		if (m_app.animateLayoutFalseButton.isSelected())
			m_app.getVisualization().run("draw");

		/*
		 * if (m_app.isAppletMode()) { if (m_app.m_appletLabel != null) m_app.m_appletLabel.setText(Math.floor((Double)
		 * (m_edgeQ.getNumberModel().getLowValue())) + ""); }
		 */// XXX determine necessity
	}

	public void setSelectedRanking(String ranking) {
		rankType.setSelectedItem(ranking);
		setContent();
	}

	public String getSelectedRanking() {
		return (String) rankType.getSelectedItem();
	}

	public static AbstractRanker getRanker(Graph jungGraph, String rankTypeString) {
		AbstractRanker ranker = null;

		if (rankTypeString.equals(RANKER_BETWEENNESS)) {
			ranker = new BetweennessCentrality(jungGraph, true, false, false);
			// title.setText(RANKER_BETWEENNESS_DESC);
		} else if (rankTypeString.equals(RANKER_BETWEENNESS_DIRECTED)) {
			ranker = new BetweennessCentrality(jungGraph, true, false, true);
			// title.setText(RANKER_BETWEENNESS_DIRECTED_DESC);
		} else if (rankTypeString.equals(RANKER_OUTDEGREE)) {
			ranker = new DegreeDistributionRanker(jungGraph, false);
			// title.setText(RANKER_OUTDEGREE_DESC);
		} else if (rankTypeString.equals(RANKER_INDEGREE)) {
			ranker = new DegreeDistributionRanker(jungGraph, true);
			// title.setText(RANKER_INDEGREE_DESC);
		}
		// else if (rankTypeString.equals(RANKER_CUTPOINTS)) {
		// ranker = new CutpointRanker(jungGraph);
		// //title.setText(RANKER_CUTPOINTS_DESC);
		//
		// } else if (rankTypeString.equals(RANKER_BRIDGES)) {
		// ranker = new BridgeRanker(jungGraph);
		// //title.setText(RANKER_BRIDGES_DESC);
		// }
		else if (rankTypeString.equals(RANKER_CLOSENESS)) {
			ranker = new ClosenessCentrality(jungGraph);
		} else if (rankTypeString.equals(RANKER_FARNESS)) {
			ranker = new FarnessCentrality(jungGraph);
		} else if (rankTypeString.equals(RANKER_COEFFICIENT)) {
			ranker = new ClusteringCoefficient(jungGraph);
		} else if (rankTypeString.equals(RANKER_HITS_AUTHORITY)) {
			ranker = new HITS(jungGraph);
		} else if (rankTypeString.equals(RANKER_HITS_HUB)) {
			ranker = new HITS(jungGraph, false);
		} else {
			ranker = new AttributeRanker(jungGraph, rankTypeString, true);
			System.err.println("Using Attribute Ranking for " + rankTypeString);
			// title.setText(RANKER_INDEGREE_DESC);
		}
		// else if (rankTypeString.equals(RANKER_PAGERANK)) {
		// // ranker = new PageRank((DirectedGraph)jungGraph, 0.15);
		// }
		return ranker;
	}

	public static String getFieldName(String rankType) {

		if (rankType.equals(RANKER_BETWEENNESS)) {
			return BetweennessCentrality.CENTRALITY;
		} else if (rankType.equals(RANKER_BETWEENNESS_DIRECTED)) {
			return BetweennessCentrality.CENTRALITY_DIRECTED;
		} else if (rankType.equals(RANKER_OUTDEGREE)) {
			return DegreeDistributionRanker.OUT_KEY;
		} else if (rankType.equals(RANKER_INDEGREE)) {
			return DegreeDistributionRanker.IN_KEY;
		} else if (rankType.equals(RANKER_CLOSENESS)) {
			return ClosenessCentrality.KEY;
		} else if (rankType.equals(RANKER_CUTPOINTS)) {
			return "";
		} else if (rankType.equals(RANKER_BRIDGES)) {
			return "";
		} else if (rankType.equals(RANKER_HITS_AUTHORITY)) {
			return HITS.AUTHORITY_KEY;
		} else if (rankType.equals(RANKER_HITS_HUB)) {
			return HITS.HUB_KEY;
		} else if (rankType.equals(RANKER_FARNESS)) {
			return FarnessCentrality.KEY;
		} else if (rankType.equals(RANKER_COEFFICIENT)) {
			return ClusteringCoefficient.KEY;
		} else {
			return rankType;
		}
	}

	public void setContent(Graph graph) {
		this.graph = graph;
		// setContent(); // TODO why commented, vs Edge1D?
	}

	public void setContent() {
		Table nodeTable = graph.getNodeTable();

		// adding numerical attributes to the end of the statistical measurement options in combo box
		for (int i = 0; i < nodeTable.getColumnCount(); i++) {
			if ((nodeTable.getColumnType(i) == int.class) || (nodeTable.getColumnType(i) == double.class)) {

				boolean found = false;
				for (int j = 0; j < rankType.getItemCount(); j++) {
					if (nodeTable.getColumnName(i).equals(rankType.getItemAt(j))) {
						found = true;
						break;
					}
				}
				if (!found) {
					// System.err.println("Need to update SYF with this column: " + nodeTable.getColumnName(i));
					m_app.addNode1DRankEvent(nodeTable.getColumnName(i));
					rankType.addItem(nodeTable.getColumnName(i));
				}
			}
		}

		AbstractRanker ranker = null;

		nodeRankTable.setRankType((String) rankType.getSelectedItem());

		ranker = getRanker(this.graph, (String) rankType.getSelectedItem());
		ranker.evaluate();
		currentRankings = ranker.getRankings();
		// System.out.println(currentRankings);

		Double[] blah = { 0.0, 1.0 };

		m_app.addEventToHistory(NODE1D_STEP_NUMBER, (String) rankType.getSelectedItem(), NODE1D_RANK_ACTION, blah);

		if (rankType.getSelectedItem().equals(RANKER_BETWEENNESS)) {
			title.setText(RANKER_BETWEENNESS_DESC);
			m_app.setNodeColorField(getFieldName((String) rankType.getSelectedItem()));// BetweennessCentrality.CENTRALITY);
		} else if (rankType.getSelectedItem().equals(RANKER_BETWEENNESS_DIRECTED)) {
			title.setText(RANKER_BETWEENNESS_DIRECTED_DESC);
			m_app.setNodeColorField(getFieldName((String) rankType.getSelectedItem()));// BetweennessCentrality.CENTRALITY);
		} else if (rankType.getSelectedItem().equals(RANKER_OUTDEGREE)) {
			title.setText(RANKER_OUTDEGREE_DESC);
			m_app.setNodeColorField(getFieldName((String) rankType.getSelectedItem()));// sne.setColorField(DegreeDistributionRanker.KEY);
		} else if (rankType.getSelectedItem().equals(RANKER_INDEGREE)) {
			title.setText(RANKER_INDEGREE_DESC);
			m_app.setNodeColorField(getFieldName((String) rankType.getSelectedItem()));// sne.setColorField(DegreeDistributionRanker.KEY);
		} else if (rankType.getSelectedItem().equals(RANKER_CUTPOINTS)) {
			title.setText(RANKER_CUTPOINTS_DESC);
		} else if (rankType.getSelectedItem().equals(RANKER_BRIDGES)) {
			title.setText(RANKER_BRIDGES_DESC);
		} else if (rankType.getSelectedItem().equals(RANKER_CLOSENESS)) {
			title.setText(RANKER_CLOSENESS_DESC);
			m_app.setNodeColorField(getFieldName((String) rankType.getSelectedItem()));// BetweennessCentrality.CENTRALITY);
		} else if (rankType.getSelectedItem().equals(RANKER_HITS_AUTHORITY)) {
			title.setText(RANKER_HITS_AUTHORITY_DESC);
			m_app.setNodeColorField(getFieldName((String) rankType.getSelectedItem()));// sne.setColorField(HITS.AUTHORITY_KEY);
		} else if (rankType.getSelectedItem().equals(RANKER_HITS_HUB)) {
			title.setText(RANKER_HITS_HUB_DESC);
			m_app.setNodeColorField(getFieldName((String) rankType.getSelectedItem()));// sne.setColorField(HITS.AUTHORITY_KEY);
		} else if (rankType.getSelectedItem().equals(RANKER_FARNESS)) {
			title.setText("");
			m_app.setNodeColorField(getFieldName((String) rankType.getSelectedItem()));// sne.setColorField(BaryCenter.KEY);
		} else if (rankType.getSelectedItem().equals(RANKER_COEFFICIENT)) {
			title.setText("");
			m_app.setNodeColorField(getFieldName((String) rankType.getSelectedItem()));// sne.setColorField(BaryCenter.KEY);
		} else {
			title.setText("Attribute values for " + rankType.getSelectedItem());
			m_app.setNodeColorField((String) rankType.getSelectedItem());
		}

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

		nodeRankTable.setContent(currentRankings, currentRankingsMin, currentRankingsMax, currentRankingsMin,
				currentRankingsMax);
		m_app.setUpdateNodeTable(true);

		m_app.setNodeRangeBinding(m_app.getNodeColorField());
		TupleSet ts = graph.getNodeTable();
		String field = m_app.getNodeColorField();
		m_nodeQ = new RangeQueryBinding(ts, field);
		if (slider != null)
			rank1d_panel.remove(slider);

		slider = m_nodeQ.createHorizontalRangeSlider();

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
				m_app.addEventToHistory(NODE1D_STEP_NUMBER, (String) rankType.getSelectedItem(), NODE1D_FILTER_ACTION,
						blah);
			}
		});
		slider.addChangeListener(this);
		slider.setEnabled(true);

		slider.setMaximumSize(new Dimension(1000, 50));
		rank1d_panel.add(slider);
	}

	public void selectCell(Node vertex) {
		nodeRankTable.selectCell(vertex);
	}

	public void setRankType(String rankTypeString) {
		rankType.setSelectedItem(rankTypeString);
	}

	public JComboBox getRankType() {
		return rankType;

	}

	public RankTable getRankTable() {
		return nodeRankTable;
	}
}