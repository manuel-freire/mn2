package edu.umd.cs.hcil.socialaction.ui.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import prefuse.Constants;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.query.RangeQueryBinding;
import edu.umd.cs.hcil.socialaction.SocialAction;
import edu.umd.cs.hcil.socialaction.algorithms.importance.AttributeRanker;
import edu.umd.cs.hcil.socialaction.jung.algorithms.importance.AbstractRanker;
import edu.umd.cs.hcil.socialaction.jung.algorithms.importance.Ranking;
import edu.umd.cs.hcil.socialaction.ui.RankTable;

/**
 * Displays attributes of nodes
 * 
 * @version 1.0
 * @author Adam Perer
 */
public class NodeAttributes1DPanel extends JPanel implements ActionListener, ChangeListener {

	/** Never change */
	private static final long serialVersionUID = -2762969062371416659L;
	public final static int NODE1D_STEP_NUMBER = 2;
	public final static String NODE1D_RANK_ACTION = "Attribute Rank";
	public final static String NODE1D_FILTER_ACTION = "Attribute Filter";
	// RANKER_CUTPOINTS, RANKER_BRIDGES
	private JTextArea title;

	private RankTable nodeRankTable;

	private JComboBox rankType;

	private JButton deleteFilteredNodesButton;

	private List<Ranking> currentRankings;

	double currentRankingsMin;

	double currentRankingsMax;

	private Graph graph;

	private SocialAction m_app;

	RangeQueryBinding m_nodeQ;

	public NodeAttributes1DPanel(SocialAction app, Graph graph) {
		this.m_app = app;
		this.graph = graph;

		initUI();
	}

	Box rank1d_panel;

	private void initUI() {

		rank1d_panel = Box.createVerticalBox();
		rank1d_panel.setBorder(BorderFactory.createTitledBorder(SocialAction.NODE_ATTRIBUTE_1D_PANEL));

		title = new JTextArea();
		title.setFont(new Font(SocialAction.DEFAULT_FONTNAME, Font.ITALIC, 12));
		title.setMaximumSize(new Dimension(300, 100));
		title.setWrapStyleWord(true);
		title.setLineWrap(true);
		title.setOpaque(false);

		Box titlebox = new Box(BoxLayout.Y_AXIS);// BoxLayout.X_AXIS);
		titlebox.add(Box.createHorizontalStrut(2));

		rankType = new JComboBox();
		// rankType.setSelectedItem(RANKER_INDEGREE);
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

		deleteFilteredNodesButton = new JButton("Delete Filtered Nodes");
		deleteFilteredNodesButton.addActionListener(this);
		// rank1d_panel.add(deleteFilteredNodesButton);

		this.add(rank1d_panel);

	}

	public void actionPerformed(ActionEvent e) {

		if (e.getSource().equals(rankType))
			setContent();
		else if (e.getSource().equals(deleteFilteredNodesButton)) {
			m_app.exportVisibleNetwork(/* getFieldName((String) rankType.getSelectedItem()) */);

		}

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

	}

	public void setContent(Graph graph) {
		this.graph = graph;
		// setContent();
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

		ranker = new AttributeRanker(jungGraph, rankTypeString, true);
		System.err.println("Using Attribute Ranking for " + rankTypeString);
		// title.setText(RANKER_INDEGREE_DESC);

		return ranker;
	}

	public static String getFieldName(String rankType) {

		return rankType;

	}

	public void setContent() {

		Table nodeTable = graph.getNodeTable();
		for (int i = 0; i < nodeTable.getColumnCount(); i++) {
			if (true/* (nodeTable.getColumnType(i) ==int.class) || (nodeTable.getColumnType(i) ==double.class) */) {

				boolean found = false;
				for (int j = 0; j < rankType.getItemCount(); j++) {
					if (nodeTable.getColumnName(i).equals(rankType.getItemAt(j))) {
						found = true;
						break;
					}
				}
				if (!found) {

					// System.err.println("Need to update SYF with this column: " + nodeTable.getColumnName(i));
					// sne.addNode1DRankEvent(nodeTable.getColumnName(i));
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

		title.setText("Attribute values for " + rankType.getSelectedItem());
		m_app.setNodeColorField((String) rankType.getSelectedItem(), Constants.NOMINAL);

		Iterator<Ranking> i = currentRankings.iterator();
		currentRankingsMin = 10000000;
		currentRankingsMax = 0;
		while (i.hasNext()) {

			Ranking ranking = i.next();

			if (ranking.rankScore > currentRankingsMax)
				currentRankingsMax = ranking.rankScore;
			if (ranking.rankScore < currentRankingsMin)
				currentRankingsMin = ranking.rankScore;

		}

		nodeRankTable.setContent(currentRankings, currentRankingsMin, currentRankingsMax, currentRankingsMin,
				currentRankingsMax);
		m_app.setUpdateNodeTable(true);

		// REMOVE BELOW COMMENTS LATER

		// sne.setRangeBinding(sne.getColorField());
		// TupleSet ts = graph.getNodeTable();
		// String field = sne.getColorField();
		// m_nodeQ = new RangeQueryBinding(ts, field);
		// if (slider != null)
		// rank1d_panel.remove(slider);
		//        
		// slider = m_nodeQ.createHorizontalRangeSlider();

		// slider.setMaximumSize(new Dimension(1000, 50));
		//        
		//        
		//        
		// slider.addMouseListener(new MouseAdapter() {
		// public void mousePressed(MouseEvent e) {
		// //m_display.setHighQuality(false);
		// }
		// public void mouseReleased(MouseEvent e) {
		//
		// Double [] blah = {0.0,1.0};
		// // sne.addEventToHistory(NODE1D_STEP_NUMBER, (String) rankType.getSelectedItem(), NODE1D_FILTER_ACTION,
		// blah);
		//
		//                
		// }
		// });
		// slider.addChangeListener(this);
		// slider.setEnabled(true);

		// rank1d_panel.add(slider);

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