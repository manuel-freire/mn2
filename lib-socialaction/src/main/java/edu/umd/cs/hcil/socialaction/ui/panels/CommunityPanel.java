package edu.umd.cs.hcil.socialaction.ui.panels;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXTable;

import prefuse.data.Node;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.VisualItem;
import edu.umd.cs.hcil.socialaction.SocialAction;
import edu.umd.cs.hcil.socialaction.analysis.community.CommunityConstructor;
import edu.umd.cs.hcil.socialaction.analysis.community.SubgraphSet;
import edu.umd.cs.hcil.socialaction.ui.LayoutCommunitiesListener;
import edu.umd.cs.hcil.socialaction.ui.tables.GraphTableModel;

/**
 * Modified from Jeff Heer's Vizster. Allows the user to select the community finding algorithm, though it currently
 * offers only Newman's fast heuristic (doi:10.1103/PhysRevE.69.066133). A slider picks the cutoff for the algorithm.
 * The panel displays the community numbers and the count of the number of nodes they contain. Buttons allow for graph
 * compression, subgraph selection, and community outputs to the console or file in the program directory.
 * 
 * @author Jeff Heer
 * @author Adam Perer
 * @author Cody Dunne
 */
public class CommunityPanel extends JPanel implements ChangeListener, ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final static int COMMUNITY_STEP_NUMBER = 6;
	public final static String COMMUNITY_ACTION = "Cluster";

	public final static String COMMUNITY_ACTION_PARAMETER = "Adjust Parameter";

	private static final String ENABLED = "Disable";

	private static final String DISABLED = "Enable";

	// private static final String UPDATE = "Update";

	public static final String SUBGRAPH_COMPONENTS = "Components";

	public static final String SUBGRAPH_COMMUNITY = "Newman's Community";

	public final String[] SUBGRAPH_TYPES = { SUBGRAPH_COMMUNITY, SUBGRAPH_COMPONENTS };

	private static String TOGGLE_SHOW_SUBGRAPH = "Show Subgraph Only";
	private static String TOGGLE_SHOW_ORIGINAL = "Show Entire Graph";

	private JComboBox rankType;

	private JSlider commSlider;

	private JButton enableButton, unselectButton, subgraphButton;
	private JButton compressButton;
	private JButton outputCommunitiesButton, outputCommunitiesAllValsButton;

	private GraphTableModel tableModel;

	private JXTable subgraphStatsTable;

	private SocialAction m_app;

	// private int panelWidth;

	public CommunityPanel(SocialAction app, int width) {
		this.m_app = app;
		// this.panelWidth = width;

		initUI();
		setContent();
	}

	private void initUI() {

		final Box communityBox = Box.createVerticalBox();
		communityBox.setBorder(BorderFactory.createTitledBorder("Community"));

		JPanel communityPanel = new JPanel();
		communityPanel.setLayout(new BoxLayout(communityPanel, BoxLayout.PAGE_AXIS));

		rankType = new JComboBox(SUBGRAPH_TYPES);
		rankType.setSelectedItem(SUBGRAPH_COMMUNITY);
		rankType.addActionListener(this);
		rankType.setMaximumSize(new Dimension(1000, 30));
		communityPanel.add(rankType);

		commSlider = new JSlider();
		commSlider.setValue(0);
		commSlider.setPreferredSize(new Dimension(200, 25));
		commSlider.setMaximumSize(new Dimension(200, 25));
		commSlider.addChangeListener(this);
		commSlider.setEnabled(false);
		commSlider.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				// m_display.setHighQuality(false);
			}

			public void mouseReleased(MouseEvent e) {

				Double[] blah = { 0.0, 1.0 };
				m_app.addEventToHistory(COMMUNITY_STEP_NUMBER, COMMUNITY_ACTION_PARAMETER, COMMUNITY_ACTION_PARAMETER,
						blah);

			}
		});

		enableButton = new JButton(DISABLED);
		enableButton.addActionListener(this);

		final SubgraphSet comm = m_app.getSubgraphs();
		if (comm != null) {
			comm.addTupleSetListener(new TupleSetListener() {
				public void tupleSetChanged(TupleSet tset, Tuple[] added, Tuple[] removed) {
					commSlider.setModel(comm.getRange());

				}

			});
		}

		JPanel sliderPanel = new JPanel();
		sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.LINE_AXIS));

		sliderPanel.add(commSlider);
		sliderPanel.add(enableButton);

		communityPanel.add(sliderPanel);

		communityPanel.add(Box.createHorizontalGlue());

		String[] columnNames = { "ID", "Number of Nodes", "Entities" };
		tableModel = new GraphTableModel(columnNames);

		// TableSorter sorter = new TableSorter(tableModel);
		subgraphStatsTable = new JXTable(tableModel);
		subgraphStatsTable.getColumnExt("Entities").setVisible(false);

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

					// pickedState.pick()
					// e.

					// subgraphStatsTable.getColumnExt(0).setCellEditor(new JX)

					subgraphStatsTable.getColumnExt("Entities").setVisible(true);

					Integer selected = (Integer) subgraphStatsTable.getValueAt(selectedRow, 0);
					// Set members = (Set) subgraphStatsTable.getValueAt(selectedRow, 2);

					subgraphStatsTable.getColumnExt("Entities").setVisible(false);

					System.err.println("NEED TO SELECT COMMUNITY");
					m_app.setSelectedCommunity(selected.intValue());

					unselectButton.setEnabled(true);
					subgraphButton.setEnabled(true);

					/*
					 * Object pickedValue = null;
					 * 
					 * if (ignoreActionListener) { // ignoreActionListener = false; } else
					 * updatePickedState(value.vertex);
					 */

				}
			}
		});

		// communityPanel.setPreferredSize(new Dimension(350, 400));

		communityPanel.add(scrollPane);

		unselectButton = new JButton("Unselect Table");
		unselectButton.addActionListener(this);
		// unselectButton.setPreferredSize(new Dimension(100, 25));
		unselectButton.setEnabled(false);

		communityPanel.add(unselectButton);

		subgraphButton = new JButton(TOGGLE_SHOW_SUBGRAPH);
		subgraphButton.addActionListener(this);
		// subgraphButton.setPreferredSize(new Dimension(100, 25));
		subgraphButton.setEnabled(false);

		communityPanel.add(subgraphButton);

		compressButton = new JButton("Compress Graph");
		compressButton.addActionListener(this);
		// subgraphButton.setPreferredSize(new Dimension(100, 25));
		compressButton.setEnabled(false);

		communityPanel.add(compressButton);

		outputCommunitiesButton = new JButton("Output Communities");
		outputCommunitiesButton.addActionListener(this);
		// outputCommunitiesButton.setPreferredSize(new Dimension(100, 25));
		outputCommunitiesButton.setEnabled(false);

		communityPanel.add(outputCommunitiesButton);

		outputCommunitiesAllValsButton = new JButton("Output Communities (All Vals)");
		outputCommunitiesAllValsButton.addActionListener(this);
		// outputCommunitiesAllValsButton.setPreferredSize(new Dimension(100, 25));
		outputCommunitiesAllValsButton.setEnabled(false);

		communityPanel.add(outputCommunitiesAllValsButton);

		JButton assignWeightsButton = new JButton("Layout communities");
		assignWeightsButton.addActionListener(new LayoutCommunitiesListener());
		communityPanel.add(assignWeightsButton);

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
		m_app.updateAggregateColorPalette();
	}

	public void clearSelected() {

		// EEEK
		// sne.setCommunitySelected(false);

		subgraphStatsTable.clearSelection();
		unselectButton.setEnabled(false);
		subgraphButton.setEnabled(false);
		// compressButton.setEnabled(false);
		// outputCommunitiesButton.setEnabled(false);
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {

		Double[] blah = { 0.0, 1.0 };

		if (evt.getSource() == enableButton) {
			boolean enabled = (enableButton.getText() == ENABLED);

			enableButton.setText(enabled ? DISABLED : ENABLED);
			// commSlider.setEnabled(!enabled);
			if (!enabled) {
				if (rankType.getSelectedItem().equals(SUBGRAPH_COMPONENTS)) {
					// sne.constructCommunities(CommunityConstructor.INIT_COMPONENTS);
					// commSlider.setEnabled(false);
					setContent();
					m_app.addEventToHistory(6, SUBGRAPH_COMPONENTS, COMMUNITY_ACTION, blah);
				} else {
					createCommunities();
				}
				outputCommunitiesButton.setEnabled(true);
				outputCommunitiesAllValsButton.setEnabled(true);
			} else {
				m_app.constructCommunities(CommunityConstructor.CLEAR);
				clearSelected();
				outputCommunitiesButton.setEnabled(false);
				outputCommunitiesAllValsButton.setEnabled(false);
			}

			System.err.println("Should I run a filter?");
			// sne.runFilter();

			compressButton.setEnabled(true);

		} /*
		 * else if (evt.getSource() == updateButton) { sne.constructCommunities(CommunityConstructor.INIT_COMMUNITY);
		 * updateButton.setVisible(false);
		 * 
		 * sne.runFilter(); }
		 */else if (evt.getSource() == rankType) {
			setContent();

		} else if (evt.getSource() == unselectButton) {
			clearSelected();

		} else if (evt.getSource() == subgraphButton) {

			clearSelected();
			subgraphButton.setEnabled(true);

			if (subgraphButton.getText().equals(TOGGLE_SHOW_SUBGRAPH)) {

				// show subgraph
				// sne.changeToSubgraphGraph();

				m_app.createCommunityGraphs();

				// change label to new option
				subgraphButton.setText(TOGGLE_SHOW_ORIGINAL);

			} else if (subgraphButton.getText().equals(TOGGLE_SHOW_ORIGINAL)) {

				// show original graph
				// sne.revertToOriginalGraph();

				// change label to new option
				subgraphButton.setText(TOGGLE_SHOW_SUBGRAPH);
			}

		} else if (evt.getSource() == compressButton) {

			// sne.compressGraph();

		} else if (evt.getSource() == outputCommunitiesButton) {
			StringBuffer tc = textCommunities(0, "");
			System.out.println(tc);
			return;// Skip the "Should I run a filter? (2)" line for copy & paste

		} else if (evt.getSource() == outputCommunitiesAllValsButton) {
			// StringBuffer tc = textCommunitiesAllVals(5, 10);// TODO text box for the increment?
			// StringBuffer tc = textCommunitiesAllVals(0, 10);
			StringBuffer tc = textCommunitiesAllVals(2, 5);
			System.out.println(tc);

			try {
				Calendar cal = new GregorianCalendar();
				String newFolderPath = "";
				File f = new File(newFolderPath);
				f.mkdir();

				File f2 = new File(/* newFolderPath + "\\" + */"CommunityExport_"
						+ SocialAction.getFilenameFromDate(cal) + ".txt");

				BufferedWriter bw = new BufferedWriter(new FileWriter(f2));

				bw.write(tc.toString());

				try {
					bw.close();
				} catch (IOException er) {
					System.out.println(er.getMessage());
				}

			} catch (FileNotFoundException er) {
				System.out.println(er.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
			}

			return;// Skip the "Should I run a filter? (2)" line for copy & paste
		}

		System.err.println("Should I run a filter? (2)");

		// sne.runFilter();
	} //

	public void createCommunities() {
		m_app.constructCommunities(CommunityConstructor.INIT_COMMUNITY);
		// commSlider.setEnabled(true);
		setContent();
		m_app.createCommunityGraphs();

		Double[] blah = { 0.0, 1.0 };
		m_app.addEventToHistory(6, SUBGRAPH_COMMUNITY, COMMUNITY_ACTION, blah);

	}

	public void setListener(final SubgraphSet comm) {

		if (comm != null) {
			comm.addTupleSetListener(new TupleSetListener() {
				public void tupleSetChanged(TupleSet tset, Tuple[] added, Tuple[] removed) {
					commSlider.setModel(comm.getRange());
				}
			});
		}
	}

	public void setContent() {

		clearSelected();

		boolean enabled = (enableButton.getText() == ENABLED);

		if (enabled) {

			if (rankType.getSelectedItem().equals(SUBGRAPH_COMMUNITY)) {

				m_app.constructCommunities(CommunityConstructor.INIT_COMMUNITY);
				commSlider.setEnabled(true);
				// remove later...
				subgraphButton.setText(TOGGLE_SHOW_SUBGRAPH);

			} else if (rankType.getSelectedItem().equals(SUBGRAPH_COMPONENTS)) {

				m_app.constructCommunities(CommunityConstructor.INIT_COMPONENTS);
				commSlider.setEnabled(false);

			}
		}
	}

	public void setTableContent() {

		SubgraphSet subgraphs = m_app.getSubgraphs();

		if (subgraphs == null) {
			tableModel.data = null;

		} else {
			int numSubgraphs = subgraphs.getCommunityCount();

			tableModel.data = new Object[numSubgraphs][3];

			for (int i = 0; i < numSubgraphs; i++) {
				Set<Node> members = subgraphs.getCommunityMembers(i);
				tableModel.setValueAt(new Integer(i), i, 0);
				tableModel.setValueAt(new Integer(members.size()), i, 1);
				tableModel.setValueAt(members, i, 2);
			}
		}
		subgraphStatsTable.revalidate();
		subgraphStatsTable.repaint();
	}

	/**
	 * Returns a StringBuffer with a text representation of the members of all the current communities.
	 * 
	 * @return a new StringBuffer with the community information
	 */
	public StringBuffer textCommunities() {
		return textCommunities(0, "");
	}

	/**
	 * Returns a StringBuffer with a text representation of the members of all the big enough current communities. Each
	 * line has prefix prepended.
	 * 
	 * @param minMembers
	 *            The minimum number of members to count as a community
	 * @param prefix
	 *            A prefix to attach to each community line
	 * @return a new StringBuffer with the community information
	 */
	public StringBuffer textCommunities(int minMembers, String prefix) {
		StringBuffer ret = new StringBuffer();

		for (int row = 0; row < subgraphStatsTable.getRowCount(); row++) {
			subgraphStatsTable.getColumnExt("Entities").setVisible(true);// Needed for getValueAt?
			Set<VisualItem> members = (Set<VisualItem>) subgraphStatsTable.getValueAt(row, 2);
			subgraphStatsTable.getColumnExt("Entities").setVisible(false);// Needed for getValueAt?

			if (members.size() < minMembers)
				continue;

			ret.append(prefix);
			ret.append(row);
			ret.append('\t');

			for (VisualItem viMember : members) {
				String label = viMember.getSourceTuple().getString(m_app.getLabelField());
				if (label == null)
					label = viMember.toString();

				ret.append(label);
				ret.append(',');
			}
			ret.deleteCharAt(ret.length() - 1);// Remove the last comma
			ret.append('\n');
		}
		return ret;
	}

	/**
	 * Returns a StringBuffer with a text representation of the members of all the communities at all slider settings
	 * 
	 * @return a new StringBuffer with each of the community information
	 */
	public StringBuffer textCommunitiesAllVals() {
		// Have a min of 0 members and increment by 1 (see them all)
		return textCommunitiesAllVals(0, 1);
	}

	/**
	 * Returns a StringBuffer with a text representation of the members of all the big enough communities at all slider
	 * settings that are multiples of increment
	 * 
	 * @param minMembers
	 *            The minimum number of members to count as a community
	 * @param increment
	 *            How many values to skip each time
	 * @return a new StringBuffer with each of the community information
	 */
	public StringBuffer textCommunitiesAllVals(int minMembers, int increment) {
		StringBuffer ret = new StringBuffer();

		for (int val = commSlider.getMinimum(); val <= commSlider.getMaximum(); val += increment) {
			SubgraphSet comm = (SubgraphSet) m_app.getVisualization().getFocusGroup(SocialAction.community);
			comm.reconstructCommunity(val);

			m_app.updateAggregateColorPalette(comm);
			setTableContent();

			ret.append(textCommunities(minMembers, val + "\t"));
		}

		return ret;
	}

} // end of class CommunityPanel
