package edu.umd.cs.hcil.socialaction.ui.panels.syf;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.JXEditorPane;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.JXTitledPanel;

import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.tuple.TableTuple;
import prefuse.data.tuple.TupleManager;
import prefuse.util.FontLib;
import edu.umd.cs.hcil.socialaction.SocialAction;
import edu.umd.cs.hcil.socialaction.ui.panels.CommunityPanel;
import edu.umd.cs.hcil.socialaction.ui.panels.MultiplexPanel;
import edu.umd.cs.hcil.socialaction.ui.panels.NetworkOverviewPanel;
import edu.umd.cs.hcil.socialaction.ui.panels.Node1DPanel;
import edu.umd.cs.hcil.socialaction.ui.panels.Rank2DScatterplotPanel;

/**
 * @version 1.0
 * @author Adam Perer
 */
public class SYFOverviewPanel extends JXTitledPanel implements ActionListener, MouseListener {
	/** Never change */
	private static final long serialVersionUID = 355679878142336668L;

	private JButton thumbsUpButton, thumbsDownButton;

	private SocialAction m_app;

	private int m_width;

	JProgressBar progressBars[];

	JStepPanel stepPanels[];

	JPanel historyButton;

	JPanel annotationButton;

	JPanel displayButton;

	StepInfo[] stepsInfo;

	JLabel currentStepLabel, nextStepLabel;

	SYFEvent currentStep, nextStep;

	JLabel historyCountLabel, annotationCountLabel;

	JStepPanel oldPanelSelected;

	private JPanel m_tabbedPane;

	JButton nextButton, backButton, submitAnnotationButton, cancelAnnotationButton;

	Table stepsTable;

	public static final String STEPS_STEP_COLUMN = "Step";

	public static final String STEPS_STATE_COLUMN = "State";

	public static final String STEPS_ACTION_COLUMN = "Action";

	public static final String STEPS_COMPLETED_COLUMN = "Completed";

	private TupleManager stepsTableTuplemanager;

	private JXEditorPane editor;
	private JTextField tagTextField;

	public SYFOverviewPanel(SocialAction app, int width, JPanel tabbedPane) {

		this.setTitle("SYF System");
		setMinimumSize(new Dimension(0, 0));
		setMaximumSize(new Dimension(width, 5000));

		this.m_app = app;
		this.m_width = width;
		this.m_tabbedPane = tabbedPane;

		initUI();
		initStepsTable();

		this.nextStep = new SYFEvent();
		this.currentStep = new SYFEvent();

	}

	public void reset(Graph g) {

		for (int i = 0; i < stepsTable.getRowCount(); i++) {
			stepsTable.set(i, STEPS_COMPLETED_COLUMN, false);
		}

		m_app.getHistoryPanel().clearHistoryTable();
		m_app.getHistoryPanel().setTableContent();

		registerStepsTable(g);

		updateProgressSteps();
	}

	public void resetStep(int step) {

		// Stack tupleStack = new Stack<Tuple>();
		//        
		// // find all tuples of the given step
		// for (int i=0; i<stepsTable.getRowCount(); i++) {
		// Tuple tuple = stepsTable.getTuple(i);
		// if (tuple.getInt(STEPS_STEP_COLUMN) == step) {
		// tupleStack.add(tuple);
		// }
		// }
		//        
		// // remove them in reverse order, to not mess up the row values
		// while (!tupleStack.empty()) {
		// Tuple tuple = (Tuple) tupleStack.pop();
		// stepsTable.removeTuple(tuple);
		// }

	}

	@SuppressWarnings("deprecation")
	private void initStepsTable() {

		stepsTable = new Table();

		stepsTable.addColumn(STEPS_STEP_COLUMN, int.class);
		stepsTable.addColumn(STEPS_STATE_COLUMN, String.class);
		stepsTable.addColumn(STEPS_ACTION_COLUMN, String.class);
		stepsTable.addColumn(STEPS_COMPLETED_COLUMN, Boolean.class);

		stepsTableTuplemanager = new TupleManager(stepsTable, null, TableTuple.class);

		SYFAnnotationPanel aPanel = m_app.getAnnotationPanel();
		Date date = new Date(1907, 10, 3, 18, 30, 0);
		aPanel.addEventToHistory(date, 2, Node1DPanel.RANKER_TYPES_1D[0], Node1DPanel.NODE1D_RANK_ACTION, 0, 2);
		date = new Date(1907, 10, 3, 18, 35, 0);
		aPanel.addEventToHistory(date, 2, Node1DPanel.RANKER_TYPES_1D[2], Node1DPanel.NODE1D_RANK_ACTION, 0, 1);
		date = new Date(2007, 10, 3, 18, 42, 0);
		aPanel.addEventToHistory(date, 4, (Node1DPanel.RANKER_TYPES_1D[0] + " x " + Node1DPanel.RANKER_TYPES_1D[2]),
				Rank2DScatterplotPanel.NODE2D_SCATTERPLOT_ACTION, 0, 1);
		date = new Date(2007, 10, 3, 12, 12, 0);
		aPanel.addEventToHistory(date, 4, (Node1DPanel.RANKER_TYPES_1D[0] + " x " + Node1DPanel.RANKER_TYPES_1D[4]),
				Rank2DScatterplotPanel.NODE2D_SCATTERPLOT_ACTION, 0, 2);
		date = new Date(1, 1, 1, 12, 20, 0);
		aPanel.addEventToHistory(date, 4, (Node1DPanel.RANKER_TYPES_1D[0] + " x " + Node1DPanel.RANKER_TYPES_1D[5]),
				Rank2DScatterplotPanel.NODE2D_SCATTERPLOT_ACTION, 0, 2);
		date = new Date(1, 1, 1, 12, 27, 0);
		aPanel.addEventToHistory(date, 4, (Node1DPanel.RANKER_TYPES_1D[2] + " x " + Node1DPanel.RANKER_TYPES_1D[3]),
				Rank2DScatterplotPanel.NODE2D_SCATTERPLOT_ACTION, 0, 1);
		date = new Date(1, 1, 1, 8, 52, 0);
		aPanel.addEventToHistory(date, 4, (Node1DPanel.RANKER_TYPES_1D[2] + " x " + Node1DPanel.RANKER_TYPES_1D[4]),
				Rank2DScatterplotPanel.NODE2D_SCATTERPLOT_ACTION, 0, 1);

		date = new Date(1, 1, 1, 9, 07, 0);
		aPanel.addEventToHistory(date, 4, (Node1DPanel.RANKER_TYPES_1D[3] + " x " + Node1DPanel.RANKER_TYPES_1D[4]),
				Rank2DScatterplotPanel.NODE2D_SCATTERPLOT_ACTION, 0, 1);
		date = new Date(1, 1, 1, 18, 62, 0);
		aPanel.addEventToHistory(date, 4, (Node1DPanel.RANKER_TYPES_1D[3] + " x " + Node1DPanel.RANKER_TYPES_1D[5]),
				Rank2DScatterplotPanel.NODE2D_SCATTERPLOT_ACTION, 0, 1);

		date = new Date(1, 1, 1, 18, 59, 0);
		aPanel.addEventToHistory(date, 6, CommunityPanel.SUBGRAPH_COMMUNITY, CommunityPanel.COMMUNITY_ACTION, 0, 1);

		registerStepsTable(); // fix and move out of this class later.

	}

	public void registerCommunitySteps() {

		// registerStepsTable();

		Graph[] graphs = m_app.getCommunityGraphs();
		String[] names = m_app.getCommunityGraphsNames();
		if (graphs == null)
			return;
		if (graphs.length <= 1)
			return;

		TableTuple tuple = null;

		stepsTable.addRow();
		tuple = (TableTuple) stepsTableTuplemanager.getTuple(stepsTable.getRowCount() - 1);

		tuple.set(STEPS_STEP_COLUMN, CommunityPanel.COMMUNITY_STEP_NUMBER);
		tuple.set(STEPS_STATE_COLUMN, CommunityPanel.SUBGRAPH_COMMUNITY);
		tuple.set(STEPS_ACTION_COLUMN, CommunityPanel.COMMUNITY_ACTION);
		tuple.set(STEPS_COMPLETED_COLUMN, false);

		stepsTable.addTuple(tuple);

		// for (int i = 0; i < stepsTable.getRowCount(); i++) {
		//            
		// tuple = (TableTuple) stepsTable.getTuple(i);
		//            
		// if ((tuple.getInt(STEPS_STEP_COLUMN) == 6) && (tuple.getString(STEPS_STATE_COLUMN) ==
		// CommunityPanel.SUBGRAPH_COMMUNITY)) {
		// tuple.setBoolean(STEPS_COMPLETED_COLUMN, false);
		// }
		//            
		// }

		for (int i = 0; i < graphs.length; i++) {

			stepsTable.addRow();
			tuple = (TableTuple) stepsTableTuplemanager.getTuple(stepsTable.getRowCount() - 1);

			tuple.set(STEPS_STEP_COLUMN, CommunityPanel.COMMUNITY_STEP_NUMBER);
			tuple.set(STEPS_STATE_COLUMN, names[i]);
			tuple.set(STEPS_ACTION_COLUMN, CommunityPanel.COMMUNITY_ACTION);
			tuple.set(STEPS_COMPLETED_COLUMN, false);

			stepsTable.addTuple(tuple);

		}

		updateProgressSteps();
	}

	public void addActionToSYF(int step, String state, String action) {
		TableTuple tuple = null;

		stepsTable.addRow();
		tuple = (TableTuple) stepsTableTuplemanager.getTuple(stepsTable.getRowCount() - 1);

		tuple.set(STEPS_STEP_COLUMN, step);
		tuple.set(STEPS_STATE_COLUMN, state);
		tuple.set(STEPS_ACTION_COLUMN, action);
		tuple.set(STEPS_COMPLETED_COLUMN, false);

		stepsTable.addTuple(tuple);

		updateProgressSteps();
	}

	private void registerStepsTable(Graph g) {

		registerStepsTable();

		Graph[] graphs = m_app.getMultiplexGraphs();
		String[] names = m_app.getMultiplexGraphsNames();
		if (graphs == null)
			return;
		if (graphs.length <= 1)
			return;

		TableTuple tuple = null;

		for (int i = 0; i < graphs.length; i++) {

			stepsTable.addRow();
			tuple = (TableTuple) stepsTableTuplemanager.getTuple(stepsTable.getRowCount() - 1);

			tuple.set(STEPS_STEP_COLUMN, MultiplexPanel.MULTIPLEX_STEP_NUMBER);
			tuple.set(STEPS_STATE_COLUMN, names[i]);
			tuple.set(STEPS_ACTION_COLUMN, MultiplexPanel.MULTIPLEX_ACTION);
			tuple.set(STEPS_COMPLETED_COLUMN, false);

			stepsTable.addTuple(tuple);

		}

		updateProgressSteps();
	}

	private void registerStepsTable() {
		TableTuple tuple = null;

		// register overview action
		stepsTable.addRow();
		tuple = (TableTuple) stepsTableTuplemanager.getTuple(stepsTable.getRowCount() - 1);

		tuple.set(STEPS_STEP_COLUMN, NetworkOverviewPanel.OVERVIEW_STEP_NUMBER);
		tuple.set(STEPS_STATE_COLUMN, NetworkOverviewPanel.OVERVIEW_STATE);
		tuple.set(STEPS_ACTION_COLUMN, NetworkOverviewPanel.OVERVIEW_ACTION);
		tuple.set(STEPS_COMPLETED_COLUMN, false);

		stepsTable.addTuple(tuple);

		// register node1D actions
		for (int i = 0; i < Node1DPanel.RANKER_TYPES_1D.length; i++) {

			stepsTable.addRow();
			tuple = (TableTuple) stepsTableTuplemanager.getTuple(stepsTable.getRowCount() - 1);

			tuple.set(STEPS_STEP_COLUMN, Node1DPanel.NODE1D_STEP_NUMBER);
			tuple.set(STEPS_STATE_COLUMN, Node1DPanel.RANKER_TYPES_1D[i]);
			tuple.set(STEPS_ACTION_COLUMN, Node1DPanel.NODE1D_RANK_ACTION);
			tuple.set(STEPS_COMPLETED_COLUMN, false);

			stepsTable.addTuple(tuple);

		}

		// register edge1D actions
		// for (int i = 0; i < Edge1DPanel.RANKER_TYPES_1D.length; i++) {
		//
		// stepsTable.addRow();
		// tuple = (TableTuple) stepsTableTuplemanager.getTuple(stepsTable.getRowCount() - 1);
		//
		// tuple.set(STEPS_STEP_COLUMN, Edge1DPanel.EDGE1D_STEP_NUMBER);
		// tuple.set(STEPS_STATE_COLUMN, Edge1DPanel.RANKER_TYPES_1D[i]);
		// tuple.set(STEPS_ACTION_COLUMN, Edge1DPanel.EDGE1D_RANK_ACTION);
		// tuple.set(STEPS_COMPLETED_COLUMN, false);
		//
		// stepsTable.addTuple(tuple);
		//
		// }

		// register node2D actions
		for (int i = 0; i < Node1DPanel.RANKER_TYPES_1D.length; i++) {
			for (int j = 0; j < Node1DPanel.RANKER_TYPES_1D.length; j++) {

				if (i < j) {
					stepsTable.addRow();
					tuple = (TableTuple) stepsTableTuplemanager.getTuple(stepsTable.getRowCount() - 1);

					tuple.set(STEPS_STEP_COLUMN, Rank2DScatterplotPanel.NODE2D_STEP_NUMBER);
					tuple.set(STEPS_STATE_COLUMN, Node1DPanel.RANKER_TYPES_1D[i] + " x "
							+ Node1DPanel.RANKER_TYPES_1D[j]);
					tuple.set(STEPS_ACTION_COLUMN, Rank2DScatterplotPanel.NODE2D_SCATTERPLOT_ACTION);
					tuple.set(STEPS_COMPLETED_COLUMN, false);

					stepsTable.addTuple(tuple);
				}

			}
		}

		// register community action
		stepsTable.addRow();
		tuple = (TableTuple) stepsTableTuplemanager.getTuple(stepsTable.getRowCount() - 1);

		tuple.set(STEPS_STEP_COLUMN, CommunityPanel.COMMUNITY_STEP_NUMBER);
		tuple.set(STEPS_STATE_COLUMN, CommunityPanel.SUBGRAPH_COMMUNITY);
		tuple.set(STEPS_ACTION_COLUMN, CommunityPanel.COMMUNITY_ACTION);
		tuple.set(STEPS_COMPLETED_COLUMN, false);

		stepsTable.addTuple(tuple);

		// stepsTable.addRow();
		// tuple = (TableTuple) stepsTableTuplemanager.getTuple(stepsTable.getRowCount() - 1);
		//        
		// tuple.set(STEPS_STEP_COLUMN, 2);
		// tuple.set(STEPS_STATE_COLUMN, GraphStatsPanel.OVERVIEW_STATE);
		// tuple.set(STEPS_ACTION_COLUMN, GraphStatsPanel.OVERVIEW_ACTION);
		//
		// stepsTable.addTuple(tuple);

		// JPrefuseTable.showTableWindow(stepsTable);

		updateProgressSteps();

	}

	public void updateProgressSteps() {

		Table historyTable = m_app.getHistoryPanel().getHistoryTable();

		for (int i = 0; i < stepsInfo.length; i++) {
			stepsInfo[i].numActions = 0;
			stepsInfo[i].numCompletedActions = 0;
		}

		for (int i = 0; i < stepsTable.getRowCount(); i++) {
			Tuple tuple = stepsTable.getTuple(i);
			boolean completed = (Boolean) tuple.get(STEPS_COMPLETED_COLUMN);
			int step = ((Integer) tuple.get(STEPS_STEP_COLUMN));
			String state = (String) tuple.get(STEPS_STATE_COLUMN);
			String action = (String) tuple.get(STEPS_ACTION_COLUMN);

			stepsInfo[step - 1].numActions++;
			if (completed) {
				stepsInfo[step - 1].numCompletedActions++;
				continue;
			}

			for (int j = 0; j < historyTable.getRowCount(); j++) {
				Tuple historyTuple = historyTable.getTuple(j);
				int historyStep = (Integer) historyTuple.get(SYFHistoryPanel.HISTORY_STEP_COLUMN);
				if (historyStep != step)
					continue;

				String historyState = (String) historyTuple.get(SYFHistoryPanel.HISTORY_STATE_COLUMN);
				if (!historyState.equals(state))
					continue;

				String historyAction = (String) historyTuple.get(SYFHistoryPanel.HISTORY_ACTION_COLUMN);
				if (!historyAction.equals(action))
					continue;
				else {
					stepsInfo[step - 1].numCompletedActions++;
					tuple.set(STEPS_COMPLETED_COLUMN, true);

					if (true) { // currentLabelFound == false) {
						currentStepLabel.setText(action + " " + state);
						currentStep.action = action;
						currentStep.step = step;
						currentStep.state = state;

						for (int k = 0; k < stepsTable.getRowCount(); k++) {
							Tuple nextTuple = stepsTable.getTuple(k);
							if (!(Boolean) nextTuple.get(STEPS_COMPLETED_COLUMN)) {
								nextStepLabel.setText(nextTuple.get(STEPS_ACTION_COLUMN) + " "
										+ nextTuple.get(STEPS_STATE_COLUMN));

								nextStep.action = (String) nextTuple.get(STEPS_ACTION_COLUMN);
								nextStep.step = (Integer) nextTuple.get(STEPS_STEP_COLUMN);
								nextStep.state = (String) nextTuple.get(STEPS_STATE_COLUMN);
								// nextStep.parameters = (double []) nextTuple.get(STEPS_PARAMETERS_COLUMN);

								break;
							}

						}

					}

				}

			}
		}

		for (int i = 0; i < stepsInfo.length; i++) {
			if (stepsInfo[i].numActions == 0)
				progressBars[i].setValue(100);
			else
				progressBars[i]
						.setValue((int) (100.0 * (1.0 * stepsInfo[i].numCompletedActions / stepsInfo[i].numActions)));
		}

		historyCountLabel.setText("(" + historyTable.getRowCount() + ")");
		annotationCountLabel.setText("(" + m_app.getAnnotationPanel().getHistoryTable().getRowCount() + ")");

		// currentStepLabel.setText("Mortgage Interest");
		// nextStepLabel.setText("Real Estate Taxes");

	}

	private void initUI() {

		// setBorder(new EmptyBorder(20,20,20,20));

		JPanel stepTaskPane = new JPanel();

		String[] stepsString = SocialAction.STEPS_PANELS;

		stepsInfo = new StepInfo[stepsString.length];
		for (int i = 0; i < stepsInfo.length; i++)
			stepsInfo[i] = new StepInfo();

		progressBars = new JProgressBar[stepsString.length];
		stepPanels = new JStepPanel[stepsString.length];

		JXHyperlink[] stepLinks = new JXHyperlink[stepsString.length];

		JPanel stepPanelsPanel = new JPanel();
		stepPanelsPanel.setLayout(new BoxLayout(stepPanelsPanel, BoxLayout.PAGE_AXIS));

		for (int i = 0; i < stepsString.length; i++) {

			stepPanels[i] = new JStepPanel();
			stepPanels[i].setLayout(new BoxLayout(stepPanels[i], BoxLayout.LINE_AXIS));
			stepPanels[i].add(Box.createHorizontalGlue());
			stepPanels[i].addMouseListener(this);
			stepPanels[i].setStepNumber(i);

			stepLinks[i] = new JXHyperlink();
			stepLinks[i].setText("" + (i + 1) + ". " + stepsString[i]);
			stepLinks[i].setPreferredSize(new Dimension(150, 20));
			stepLinks[i].setAlignmentX(Component.LEFT_ALIGNMENT);
			stepLinks[i].setClickedColor(Color.BLACK);
			stepLinks[i].setUnclickedColor(Color.BLACK);
			stepLinks[i].addMouseListener(this);

			progressBars[i] = new JProgressBar();
			progressBars[i].setMaximumSize(new Dimension(50, 15));
			progressBars[i].setPreferredSize(new Dimension(50, 15));
			progressBars[i].setSize(new Dimension(50, 15));

			progressBars[i].setValue((int) (Math.random() * 100.0));
			progressBars[i].setStringPainted(true);

			stepPanels[i].add(stepLinks[i]);
			stepPanels[i].add(Box.createRigidArea(new Dimension(0, 5)));
			stepPanels[i].add(progressBars[i]);

			stepPanelsPanel.add(Box.createRigidArea(new Dimension(0, 3)));
			stepPanelsPanel.add(stepPanels[i]);

		}
		stepPanelsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		oldPanelSelected = stepPanels[0].select();

		// create buttons for history&annotation panel
		historyCountLabel = new JLabel("(" + (31) + ")");
		historyCountLabel.setPreferredSize(new Dimension(50, 20));
		historyCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		annotationCountLabel = new JLabel("(" + (4) + ")");
		annotationCountLabel.setPreferredSize(new Dimension(50, 20));
		annotationCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel displayCountLabel = new JLabel("");
		displayCountLabel.setPreferredSize(new Dimension(50, 20));
		displayCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		historyButton = new JStepPanel();
		historyButton.setLayout(new BoxLayout(historyButton, BoxLayout.LINE_AXIS));
		historyButton.add(Box.createHorizontalGlue());
		historyButton.addMouseListener(this);

		JXHyperlink historyLink = new JXHyperlink();
		historyLink.setText("History");
		historyLink.setPreferredSize(new Dimension(150, 20));
		historyLink.setSize(new Dimension(150, 20));
		historyLink.setClickedColor(Color.BLACK);
		historyLink.setUnclickedColor(Color.BLACK);
		historyLink.addMouseListener(this);

		historyButton.add(historyLink);
		historyButton.add(historyCountLabel);
		stepPanelsPanel.add(Box.createRigidArea(new Dimension(0, 3)));
		stepPanelsPanel.add(historyButton);

		// create button for annotation panel

		annotationButton = new JStepPanel();
		annotationButton.setLayout(new BoxLayout(annotationButton, BoxLayout.LINE_AXIS));
		annotationButton.addMouseListener(this);
		annotationButton.add(Box.createHorizontalGlue());
		// annotationButton.setAlignmentX(Component.LEFT_ALIGNMENT);

		JXHyperlink annotationLink = new JXHyperlink();
		annotationLink.setText("Annotations");
		annotationLink.setPreferredSize(new Dimension(150, 20));
		// annotationLink.setMinimumSize(new Dimension(150,20));
		annotationLink.setSize(new Dimension(150, 20));
		annotationLink.setClickedColor(Color.BLACK);
		annotationLink.setUnclickedColor(Color.BLACK);
		annotationLink.addMouseListener(this);
		// annotationLink.setAlignmentX(Component.LEFT_ALIGNMENT);

		annotationButton.add(annotationLink);
		annotationButton.add(annotationCountLabel);
		stepPanelsPanel.add(Box.createRigidArea(new Dimension(0, 3)));
		stepPanelsPanel.add(annotationButton);

		displayButton = new JStepPanel();
		displayButton.setLayout(new BoxLayout(displayButton, BoxLayout.LINE_AXIS));
		displayButton.addMouseListener(this);
		displayButton.add(Box.createHorizontalGlue());
		// annotationButton.setAlignmentX(Component.LEFT_ALIGNMENT);

		JXHyperlink displayLink = new JXHyperlink();
		displayLink.setText(SocialAction.DISPLAY_PANEL);
		displayLink.setPreferredSize(new Dimension(150, 20));
		// annotationLink.setMinimumSize(new Dimension(150,20));
		displayLink.setSize(new Dimension(150, 20));
		displayLink.setClickedColor(Color.BLACK);
		displayLink.setUnclickedColor(Color.BLACK);
		displayLink.addMouseListener(this);
		// annotationLink.setAlignmentX(Component.LEFT_ALIGNMENT);

		displayButton.add(displayLink);
		displayButton.add(displayCountLabel);
		stepPanelsPanel.add(Box.createRigidArea(new Dimension(0, 3)));
		stepPanelsPanel.add(displayButton);

		JPanel currentPane = new JPanel();
		currentPane.setMinimumSize(new Dimension(0, 0));
		currentPane.setLayout(new BoxLayout(currentPane, BoxLayout.PAGE_AXIS));

		currentStepLabel = new JLabel("");
		nextStepLabel = new JLabel("");

		int LABEL_WIDTH = 400;
		JPanel currentStepLabelPanel = new JPanel();
		currentStepLabelPanel.setLayout(new BoxLayout(currentStepLabelPanel, BoxLayout.PAGE_AXIS));

		JLabel temp = new JLabel("Last Completed Step:   ");
		temp.setSize(new Dimension(LABEL_WIDTH, 50));
		temp.setFont(FontLib.getFont(SocialAction.DEFAULT_FONTNAME, Font.ITALIC, temp.getFont().getSize()));
		currentStepLabelPanel.add(temp);
		currentStepLabelPanel.add(currentStepLabel);
		currentStepLabel.setSize(new Dimension(LABEL_WIDTH, 50));
		nextStepLabel.setSize(new Dimension(LABEL_WIDTH, 50));
		temp = new JLabel("Next Uncompleted Step:   ");
		temp.setSize(new Dimension(LABEL_WIDTH, 50));
		temp.setFont(FontLib.getFont(SocialAction.DEFAULT_FONTNAME, Font.ITALIC, temp.getFont().getSize()));

		currentStepLabelPanel.add(temp);
		currentStepLabelPanel.add(nextStepLabel);
		currentStepLabelPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		currentStepLabelPanel.setSize(300, 200);

		java.net.URL imageURL = SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/arrow_left.png");
		ImageIcon backIcon = new ImageIcon(imageURL);
		imageURL = SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/arrow_right.png");
		ImageIcon nextIcon = new ImageIcon(imageURL);

		backButton = new JButton("Back", backIcon);
		backButton.addActionListener(this);
		nextButton = new JButton("Next", nextIcon);
		nextButton.addActionListener(this);
		nextButton.setHorizontalTextPosition(SwingConstants.LEFT);

		JPanel stepButtonPanel = new JPanel();
		// stepButtonPanel.setLayout(new BoxLayout(stepButtonPanel, BoxLayout.LINE_AXIS));
		// stepButtonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		stepButtonPanel.add(backButton);
		stepButtonPanel.add(nextButton);

		currentPane.add(currentStepLabelPanel);
		currentPane.add(stepButtonPanel);

		JPanel annotationPane = new JPanel();
		annotationPane.setLayout(new BoxLayout(annotationPane, BoxLayout.Y_AXIS));
		annotationPane.setMaximumSize(new Dimension(150, 20));

		imageURL = SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/thumb_up.png");
		ImageIcon thumbsUpIcon = new ImageIcon(imageURL);
		imageURL = SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/thumb_down.png");
		ImageIcon thumbsDownIcon = new ImageIcon(imageURL);

		thumbsUpButton = new JButton("Yes", thumbsUpIcon);
		thumbsUpButton.addActionListener(this);
		thumbsDownButton = new JButton("No", thumbsDownIcon);
		thumbsDownButton.addActionListener(this);

		JPanel thumbsPanel = new JPanel();
		// thumbsPanel.setLayout(new BoxLayout(thumbsPanel, BoxLayout.LINE_AXIS));
		thumbsPanel.add(Box.createHorizontalGlue());
		// thumbsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		temp = new JLabel("Useful?:");
		temp.setFont(FontLib.getFont(SocialAction.DEFAULT_FONTNAME, Font.ITALIC, temp.getFont().getSize()));
		// temp.setBorder(BorderFactory.createLineBorder(Color.black));
		thumbsPanel.add(temp);
		thumbsPanel.add(thumbsUpButton);
		thumbsPanel.add(thumbsDownButton);

		// thumbsPanel.setBorder(BorderFactory.createLineBorder(Color.black));

		JPanel tagPanel = new JPanel();
		// tagPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		tagPanel.setLayout(new BoxLayout(tagPanel, BoxLayout.LINE_AXIS));
		tagPanel.add(Box.createHorizontalGlue());
		temp = new JLabel("Tags: ");
		temp.setFont(FontLib.getFont(SocialAction.DEFAULT_FONTNAME, Font.ITALIC, temp.getFont().getSize()));
		tagPanel.add(temp);
		tagTextField = new JTextField("    ");
		// tagTextField.setSize(new Dimension(100,30));
		tagPanel.add(tagTextField);

		JPanel submitPanel = new JPanel();
		submitPanel.setLayout(new BoxLayout(submitPanel, BoxLayout.LINE_AXIS));

		imageURL = SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/accept.png");
		ImageIcon submitIcon = new ImageIcon(imageURL);
		imageURL = SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/cancel.png");
		ImageIcon cancelIcon = new ImageIcon(imageURL);

		submitAnnotationButton = new JButton("Submit", submitIcon);
		submitAnnotationButton.addActionListener(this);
		// submitAnnotationButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		cancelAnnotationButton = new JButton("Cancel", cancelIcon);
		cancelAnnotationButton.addActionListener(this);
		// cancelAnnotationButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		submitPanel.add(submitAnnotationButton);
		submitPanel.add(cancelAnnotationButton);
		// submitPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		editor = new JXEditorPane();
		// editor.setMaximumSize(new Dimension(100, 100));
		// editor.setPreferredSize(new Dimension(m_width-75, 100));
		// annotationPane.add(editor);

		JScrollPane scrollEditor = new JScrollPane(editor);
		scrollEditor.setPreferredSize(new Dimension(m_width - 75, 100));
		annotationPane.add(scrollEditor);

		annotationPane.add(thumbsPanel);
		annotationPane.add(tagPanel);
		annotationPane.add(submitPanel);

		imageURL = SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/comment.gif");
		new ImageIcon(imageURL);

		stepTaskPane.add(stepPanelsPanel);

		// DropShadowBorder border = new DropShadowBorder(
		// Color.BLACK, 3);

		JXTitledPanel stepTitledPanel = new JXTitledPanel();

		Font titledFont = (FontLib.getFont(SocialAction.DEFAULT_FONTNAME, Font.ITALIC, stepTitledPanel.getTitleFont()
				.getSize()));

		stepTitledPanel.setTitle("Systematic Steps");
		stepTitledPanel.setMaximumSize(new Dimension(220, 500));
		stepTitledPanel.add(stepTaskPane);
		stepTitledPanel.setTitleFont(titledFont);

		// stepTitledPanel.setBorder(border);

		JXTitledPanel currentTitledPanel = new JXTitledPanel();
		currentTitledPanel.setTitle("Current Status");
		currentTitledPanel.setMaximumSize(new Dimension(220, 500));
		currentTitledPanel.add(currentPane);
		currentTitledPanel.setTitleFont(titledFont);
		// stepTitledPanel.setBorder(border);

		JXTitledPanel annotationTitledPanel = new JXTitledPanel();
		annotationTitledPanel.setTitle("Annotation");
		annotationTitledPanel.setMaximumSize(new Dimension(220, 500));
		annotationTitledPanel.setContentContainer(annotationPane);
		annotationTitledPanel.setTitleFont(titledFont);
		// annotationTitledPanel.setBorder(border);

		// annotationPane.setBorder(new DropShadowBorder());

		JPanel tempPanel = new JPanel();
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.PAGE_AXIS));

		tempPanel.add(Box.createRigidArea(new Dimension(0, 15)));
		tempPanel.add(stepTitledPanel);
		tempPanel.add(Box.createRigidArea(new Dimension(0, 15)));
		tempPanel.add(currentTitledPanel);
		tempPanel.add(Box.createRigidArea(new Dimension(0, 15)));
		tempPanel.add(annotationTitledPanel);

		add(tempPanel);

	}

	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == nextButton) {
			m_app.doSYFAction(nextStep.step, nextStep.state, nextStep.action, nextStep.parameters);// ,new Double[]);
		} else if (e.getSource() == thumbsUpButton) {
			thumbsUpButton.setSelected(true);
			thumbsDownButton.setSelected(false);
		} else if (e.getSource() == thumbsDownButton) {
			thumbsUpButton.setSelected(false);
			thumbsDownButton.setSelected(true);
		}

	}

	public void setAnnotation(String comment, boolean interesting, String tags) {
		editor.setText(comment);
		if (interesting) {
			thumbsUpButton.setSelected(true);
			thumbsDownButton.setSelected(false);
		} else {
			thumbsDownButton.setSelected(true);
			thumbsUpButton.setSelected(false);
		}
		tagTextField.setText(tags);
	}

	public void setSelectedButton(int step) {
		if (oldPanelSelected != null)
			oldPanelSelected.unselect();

		JStepPanel panel = stepPanels[step];
		if (panel != null) {
			panel.select();

		}
		oldPanelSelected = panel;

	}

	public void mousePressed(MouseEvent e) {

		if ((e.getSource() instanceof JStepPanel) || (e.getSource() instanceof JXHyperlink)) {

			if (oldPanelSelected != null)
				oldPanelSelected.unselect();

			JStepPanel panel;
			if (e.getSource() instanceof JXHyperlink) {
				JXHyperlink link = (JXHyperlink) e.getSource();
				panel = (JStepPanel) link.getParent();
			} else
				panel = (JStepPanel) e.getSource();

			panel.select();

			oldPanelSelected = panel;

			CardLayout cl = (CardLayout) (m_tabbedPane.getLayout());

			if (panel == historyButton) {
				cl.show(m_tabbedPane, SocialAction.HISTORY_PANEL);
			} else if (panel == annotationButton) {
				cl.show(m_tabbedPane, SocialAction.ANNOTATION_PANEL);
			} else if (panel == displayButton) {
				cl.show(m_tabbedPane, SocialAction.DISPLAY_PANEL);
			} else {

				if (SocialAction.STEPS_PANELS[panel.getStepNumber()].equals(SocialAction.NODE_1D_PANEL)) {
					m_app.switchToNode1D();
				} else if (SocialAction.STEPS_PANELS[panel.getStepNumber()].equals(SocialAction.NODE_2D_PANEL)) {
					m_app.switchToNode2D();
				} else if (SocialAction.STEPS_PANELS[panel.getStepNumber()].equals(SocialAction.EDGE_1D_PANEL)) {
					m_app.switchToEdge1D();
				} else if (SocialAction.STEPS_PANELS[panel.getStepNumber()].equals(SocialAction.OVERVIEW_PANEL)) {
					m_app.switchToOverview();
				} else if (SocialAction.STEPS_PANELS[panel.getStepNumber()].equals(SocialAction.COMMUNITY_PANEL)) {
					m_app.switchToCommunity();
				}

				else
					System.err.println("Need to make this switching more consistent in SocialAction");

				cl.show(m_tabbedPane, SocialAction.STEPS_PANELS[panel.getStepNumber()]);

			}
			panel.revalidate();

			// m_tabbedPane.show.setSelectedIndex(panel.getStepNumber());

		}

	}

	public void mouseReleased(MouseEvent e) {

	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	public void mouseClicked(MouseEvent e) {

	}

	public void setContent() {

	}

	class StepInfo {
		public int numActions = 0;

		public int numCompletedActions = 0;

	}

	class JStepPanel extends RoundedPanel {
		/** Never change */
		private static final long serialVersionUID = -2545322703136313564L;
		private int stepNumber = -1;

		public int getStepNumber() {
			return stepNumber;
		}

		public void setStepNumber(int step) {
			stepNumber = step;
		}

		Color oldBackground = null;

		public JStepPanel select() {
			oldBackground = getBackground();
			setBackground(Color.gray);
			return this;
		}

		public void unselect() {
			setBackground(oldBackground);
		}

	}

	class SYFEvent {
		public int step;

		public String state;

		public String action;

		public double[] parameters;
	}

}