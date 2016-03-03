package edu.umd.cs.hcil.socialaction.ui.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXTable;

import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.Tuple;
import edu.umd.cs.hcil.socialaction.SocialAction;
import edu.umd.cs.hcil.socialaction.jung.algorithms.importance.AbstractRanker;
import edu.umd.cs.hcil.socialaction.jung.algorithms.importance.DegreeDistributionRanker;
import edu.umd.cs.hcil.socialaction.jung.algorithms.importance.NodeRanking;
import edu.umd.cs.hcil.socialaction.jung.algorithms.importance.Ranking;
import edu.umd.cs.hcil.socialaction.ui.tables.GraphTableModel;

/**
 * CommunityPanel (modified from Jeff Heer's Vizster)
 */
public class MultiplexPanel extends JPanel implements ChangeListener, ActionListener {

	/** Never change */
	private static final long serialVersionUID = 3270818913822937950L;

	public final static int MULTIPLEX_STEP_NUMBER = 7;

	public final static String MULTIPLEX_ACTION = "Multiplex Selection";

	private JButton timelineButton;

	private JButton selectButton;

	private GraphTableModel tableModel;

	private JXTable subgraphStatsTable;

	private SocialAction m_app;

	private Integer m_integerSelected;

	private String m_multiplexFieldSelected = null;

	int TYPE_COLUMN = 0;

	String TYPE_COLUMN_NAME = "Type";

	int NODECOUNT_COLUMN = 1;

	String NODECOUNT_COLUMN_NAME = "# of Nodes";

	int EDGECOUNT_COLUMN = 2;

	String EDGECOUNT_COLUMN_NAME = "# of Edges";

	int NUMBER_COLUMN = 3;

	String NUMBER_COLUMN_NAME = "Number";

	String[] columnNames = { TYPE_COLUMN_NAME, NODECOUNT_COLUMN_NAME, EDGECOUNT_COLUMN_NAME, NUMBER_COLUMN_NAME };

	public MultiplexPanel(SocialAction app, int width) {
		this.m_app = app;
		initUI();
		// setContent();
	}

	private void initUI() {

		final Box communityBox = Box.createVerticalBox();
		communityBox.setBorder(BorderFactory.createTitledBorder("Multiplex"));

		JPanel communityPanel = new JPanel();
		communityPanel.setLayout(new BoxLayout(communityPanel, BoxLayout.PAGE_AXIS));

		timelineButton = new JButton("Overview");
		timelineButton.addActionListener(this);

		selectButton = new JButton("Select");
		selectButton.addActionListener(this);

		JPanel sliderPanel = new JPanel();
		sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.LINE_AXIS));

		sliderPanel.add(timelineButton);
		sliderPanel.add(selectButton);

		communityPanel.add(sliderPanel);

		communityPanel.add(Box.createHorizontalGlue());

		tableModel = new GraphTableModel(columnNames);

		// TableSorter sorter = new TableSorter(tableModel);
		subgraphStatsTable = new JXTable(tableModel);

		subgraphStatsTable.getColumnExt(NUMBER_COLUMN_NAME).setVisible(false);

		// Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(subgraphStatsTable);

		subgraphStatsTable.setOpaque(true); // content panes must be opaque

		// Ask to be notified of selection changes.
		ListSelectionModel rowSM = subgraphStatsTable.getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {

				// Ignore extra messages.
				if (e.getValueIsAdjusting())
					return;

				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty()) {
					// no rows are selected
				} else {
					int selectedRow = lsm.getMinSelectionIndex();

					subgraphStatsTable.getColumnExt(NUMBER_COLUMN_NAME).setVisible(true);

					m_integerSelected = (Integer) subgraphStatsTable.getValueAt(selectedRow, NUMBER_COLUMN);
					m_multiplexFieldSelected = (String) subgraphStatsTable.getValueAt(selectedRow, TYPE_COLUMN);

					subgraphStatsTable.getColumnExt(NUMBER_COLUMN_NAME).setVisible(false);

				}
			}
		});

		// communityPanel.setPreferredSize(new Dimension(350, 400));

		communityPanel.add(scrollPane);

		communityBox.add(communityPanel);

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		this.add(communityBox);

	}

	/**
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent arg0) {
		JSlider slider = (JSlider) arg0.getSource();
		clearSelected();
		m_app.constructCommunities(slider.getValue());

	} //

	public void clearSelected() {
		subgraphStatsTable.clearSelection();
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {

		if (evt.getSource() == timelineButton) {

			System.out.println("Selected Multiplex Field = " + m_multiplexFieldSelected);
			launchTimeline();
		} else if (evt.getSource() == selectButton) {

			showOnlySelected();

		}
	}

	public void setContent() {

		setTableContent();
	}

	public void showOnlySelected() {

		Graph[] graphs = m_app.getMultiplexGraphs();
		if (m_integerSelected >= 0) {
			Graph g = graphs[m_integerSelected.intValue()];
			m_app.setupGraphLocation(g);

			Double[] blah = { 0.0, 1.0 };
			m_app.addEventToHistory(MULTIPLEX_STEP_NUMBER,
					m_app.getMultiplexGraphsNames()[m_integerSelected.intValue()], MULTIPLEX_ACTION, blah);
		}
	}

	public static String NAME = "Name";

	public static String PARTITION = "M/F";

	public static String ID = "ID";

	public void launchTimeline() {

		Graph[] graphs = m_app.getMultiplexGraphs();
		if (graphs == null)
			return;
		if (graphs.length <= 1)
			return;

		Table nodes = new Table();
		nodes.addColumn(NAME, String.class);
		nodes.addColumn(PARTITION, String.class);
		nodes.addColumn(ID, int.class);

		ArrayList<String> multiplexList = new ArrayList<String>();
		String[] multiplexArray = new String[graphs.length - 1];
		String[] graphsNames = m_app.getMultiplexGraphsNames();

		for (int i = 1; i < graphs.length; i++) {

			String row = graphsNames[i];
			nodes.addColumn(row, int.class);

			// multiplexArray[i - 1] = row;

			if (!multiplexList.contains(row))
				multiplexList.add(row);

		}

		Collections.sort(multiplexList);
		Iterator<String> it = multiplexList.iterator();
		int cnt = 0;
		while (it.hasNext()) {
			multiplexArray[cnt] = (String) it.next();
			cnt++;
		}

		Table masterNodeTable = graphs[0].getNodeTable();

		Iterator masterNodeIterator = masterNodeTable.tuples();
		int idCnt = 0;
		while (masterNodeIterator.hasNext()) {

			String typeMult = "";
			Tuple masterNode = (Tuple) masterNodeIterator.next();
			String masterNodeName = masterNode.getString(m_app.getLabelField());
			if (masterNode.canGetInt(SocialAction.PARTITION_COLUMN_NAME)) {
				int partitionNum = masterNode.getInt(SocialAction.PARTITION_COLUMN_NAME);

				if (partitionNum == 0)
					typeMult = "M";
				else
					typeMult = "F";
			} else
				typeMult = "M";

			if (masterNode.canGetInt("Paraiso")) {
				int partitionNum = masterNode.getInt("Paraiso");

				if (partitionNum == 0)
					typeMult = "M";
				else
					typeMult = "F";
			} else
				typeMult = "M";

			int newRow = nodes.addRow();
			nodes.setString(newRow, NAME, masterNodeName);
			nodes.setString(newRow, PARTITION, typeMult);
			nodes.setInt(newRow, ID, idCnt++);

			for (int i = 1; i < graphs.length; i++) {
				AbstractRanker ranker = new DegreeDistributionRanker(graphs[i]);
				ranker.evaluate();
				List<Ranking> currentRankings = ranker.getRankings();

				Iterator<Ranking> i2 = currentRankings.iterator();
				boolean found = false;

				while (i2.hasNext()) {

					Object value = i2.next();

					NodeRanking ranking = (NodeRanking) value;

					if (ranking.vertex.getString(m_app.getLabelField()).equals(masterNodeName)) {

						// System.out.println(ranking.vertex.getString(BipartiteGraphCreatorAction.LABEL) + "!=" +
						// masterNodeName);
						// System.out.println(ranking.rankScore);

						nodes.setInt(newRow, graphsNames[i], new Double(ranking.rankScore).intValue());
						found = true;
						break;
					}

				}

				if (!found)
					nodes.setInt(newRow, graphsNames[i], 0);
			}
		}
		JFrame frame = NameVoyager.demoFrame(nodes, multiplexArray, m_app);

		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public void setTableContent() {

		Graph[] graphs = m_app.getMultiplexGraphs();

		if (graphs == null)
			return;

		String[] graphsNames = m_app.getMultiplexGraphsNames();

		TreeMap<String, Integer> map = new TreeMap<String, Integer>();

		for (int i = 0; i < graphs.length; i++) {
			String row = graphsNames[i];
			map.put(row, i);

		}

		// Collections.sort(list);

		tableModel.data = new Object[map.size()][4];
		int cnt = 0;
		for (Iterator<String> it = map.keySet().iterator(); it.hasNext(); cnt++) {
			String key = it.next();
			int number = map.get(key);

			tableModel.setValueAt(new Integer(number), cnt, NUMBER_COLUMN);
			tableModel.setValueAt(key, cnt, TYPE_COLUMN);
			tableModel.setValueAt(graphs[number].getNodeCount(), cnt, NODECOUNT_COLUMN);
			tableModel.setValueAt(graphs[number].getEdgeCount(), cnt, EDGECOUNT_COLUMN);
		}

		subgraphStatsTable.revalidate();
		subgraphStatsTable.repaint();
	}
}
