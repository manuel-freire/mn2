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
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

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
public class SYFOverviewPanelOld2 extends JPanel implements ActionListener, MouseListener {
	/** Never change */
	private static final long serialVersionUID = -8006077827880419230L;

	private JButton thumbsUpButton, thumbsDownButton;

	private SocialAction m_app;

	private int m_width;

	JProgressBar progressBars[];
	JStepPanel stepPanels[];
	JPanel historyButton;
	JPanel annotationButton;
	StepInfo[] stepsInfo;

	JLabel currentStepLabel, nextStepLabel;
	SYFEvent currentStep, nextStep;
	JLabel historyCountLabel, annotationCountLabel;

	JStepPanel oldPanelSelected;
	private JPanel m_tabbedPane;

	JButton nextButton, backButton;

	Table stepsTable;

	JXEditorPane editor;

	JTextField tagTextField;

	public static final String STEPS_STEP_COLUMN = "Step";
	public static final String STEPS_STATE_COLUMN = "State";
	public static final String STEPS_ACTION_COLUMN = "Action";
	public static final String STEPS_COMPLETED_COLUMN = "Completed";

	private TupleManager stepsTableTuplemanager;

	public SYFOverviewPanelOld2(SocialAction app, int width, JPanel tabbedPane) {
		this.m_app = app;
		this.m_width = width;
		this.m_tabbedPane = tabbedPane;

		initUI();
		initStepsTable();
		registerStepsTable(); // fix and move out of this class later.

		this.nextStep = new SYFEvent();
		this.currentStep = new SYFEvent();

		setContent();
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
		Date date = new Date(2007, 7, 27, 18, 30, 0);
		aPanel.addEventToHistory(date, 2, Node1DPanel.RANKER_TYPES_1D[0], Node1DPanel.NODE1D_RANK_ACTION, 0, 2);
		date = new Date(2007, 7, 27, 18, 35, 0);
		aPanel.addEventToHistory(date, 2, Node1DPanel.RANKER_TYPES_1D[2], Node1DPanel.NODE1D_RANK_ACTION, 0, 1);
		date = new Date(2007, 7, 27, 18, 42, 0);
		aPanel.addEventToHistory(date, 4, (Node1DPanel.RANKER_TYPES_1D[0] + " x " + Node1DPanel.RANKER_TYPES_1D[2]),
				Rank2DScatterplotPanel.NODE2D_SCATTERPLOT_ACTION, 0, 1);
		date = new Date(2007, 7, 28, 12, 12, 0);
		aPanel.addEventToHistory(date, 4, (Node1DPanel.RANKER_TYPES_1D[0] + " x " + Node1DPanel.RANKER_TYPES_1D[4]),
				Rank2DScatterplotPanel.NODE2D_SCATTERPLOT_ACTION, 0, 2);
		date = new Date(2007, 7, 28, 12, 20, 0);
		aPanel.addEventToHistory(date, 4, (Node1DPanel.RANKER_TYPES_1D[0] + " x " + Node1DPanel.RANKER_TYPES_1D[5]),
				Rank2DScatterplotPanel.NODE2D_SCATTERPLOT_ACTION, 0, 2);
		date = new Date(2007, 7, 28, 12, 25, 0);
		aPanel.addEventToHistory(date, 4, (Node1DPanel.RANKER_TYPES_1D[2] + " x " + Node1DPanel.RANKER_TYPES_1D[3]),
				Rank2DScatterplotPanel.NODE2D_SCATTERPLOT_ACTION, 0, 1);
		date = new Date(2007, 8, 5, 8, 52, 0);
		aPanel.addEventToHistory(date, 4, (Node1DPanel.RANKER_TYPES_1D[2] + " x " + Node1DPanel.RANKER_TYPES_1D[4]),
				Rank2DScatterplotPanel.NODE2D_SCATTERPLOT_ACTION, 0, 1);

		date = new Date(2007, 8, 5, 9, 5, 0);
		aPanel.addEventToHistory(date, 6, CommunityPanel.SUBGRAPH_COMMUNITY, CommunityPanel.COMMUNITY_ACTION, 0, 1);
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
		tuple.set(STEPS_COMPLETED_COLUMN, true);

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
				progressBars[i].setValue(0);
			else if (i == 0)
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

		// this.setPreferredSize(new Dimension(m_width, 400));
		this.setMaximumSize(new Dimension(m_width, 1200));
		// setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		// setAlignmentX(Component.LEFT_ALIGNMENT);

		JXTaskPaneContainer taskPaneContainer = new JXTaskPaneContainer();
		taskPaneContainer.setMaximumSize(new Dimension(m_width, 1500));
		// taskPaneContainer.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

		JXTaskPane stepTaskPane = new JXTaskPane();
		stepTaskPane.setMaximumSize(new Dimension(m_width, 1500));
		stepTaskPane.setTitle("Systematic Steps");
		// stepTaskPane.setSpecial(true);
		// stepTaskPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

		String[] stepsString = SocialAction.STEPS_PANELS;

		stepsInfo = new StepInfo[stepsString.length];
		for (int i = 0; i < stepsInfo.length; i++)
			stepsInfo[i] = new StepInfo();

		// stepsString[4] = "Multiplex";

		// add(Box.createRigidArea(new Dimension(0,3)));

		progressBars = new JProgressBar[stepsString.length];
		stepPanels = new JStepPanel[stepsString.length];

		JXHyperlink[] stepLinks = new JXHyperlink[stepsString.length];

		JPanel stepPanelsPanel = new JPanel();
		stepPanelsPanel.setLayout(new BoxLayout(stepPanelsPanel, BoxLayout.PAGE_AXIS));

		JButton buttons[] = new JButton[9];
		buttons[0] = new JButton("1. Overview", new ImageIcon(
                        SocialAction.class.getResource(SocialAction.IMAGE_BASE + "/table.png")));
		buttons[1] = new JButton("2. Rank Nodes", new ImageIcon(
                        SocialAction.class.getResource(SocialAction.IMAGE_BASE + "/cog.png")));
		buttons[2] = new JButton("3. Rank Edges", new ImageIcon(
                        SocialAction.class.getResource(SocialAction.IMAGE_BASE + "/link.png")));
		buttons[3] = new JButton("4. Plot Nodes", new ImageIcon(
                        SocialAction.class.getResource(SocialAction.IMAGE_BASE + "/chart_line.png")));
		buttons[4] = new JButton("5. Plot Edges", new ImageIcon(
                        SocialAction.class.getResource(SocialAction.IMAGE_BASE + "/chart_line.png")));
		buttons[5] = new JButton("6. Find Communities", new ImageIcon(
                        SocialAction.class.getResource(SocialAction.IMAGE_BASE + "/chart_pie.png")));
		buttons[6] = new JButton("7. Edge Types", new ImageIcon(
                        SocialAction.class.getResource(SocialAction.IMAGE_BASE + "/link_break.png")));
		buttons[7] = new JButton("History", new ImageIcon(
                        SocialAction.class.getResource(SocialAction.IMAGE_BASE + "/date.png")));
		buttons[8] = new JButton("Annotations", new ImageIcon(
                        SocialAction.class.getResource(SocialAction.IMAGE_BASE + "/comment.gif")));

		for (int i = 0; i < stepsString.length; i++) {

			stepPanels[i] = new JStepPanel();
			stepPanels[i].setLayout(new BoxLayout(stepPanels[i], BoxLayout.LINE_AXIS));
			stepPanels[i].add(Box.createHorizontalGlue());
			stepPanels[i].addMouseListener(this);
			stepPanels[i].setStepNumber(i);

			// stepPanels[i].setBackground(null);

			stepLinks[i] = new JXHyperlink();
			stepLinks[i].setText("" + (i + 1) + ". " + stepsString[i]);
			stepLinks[i].setPreferredSize(new Dimension(150, 20));
			stepLinks[i].setAlignmentX(Component.LEFT_ALIGNMENT);

			progressBars[i] = new JProgressBar();
			progressBars[i].setMinimumSize(new Dimension(50, 15));
			progressBars[i].setMaximumSize(new Dimension(50, 15));
			progressBars[i].setPreferredSize(new Dimension(50, 15));
			// progressBars[i].setAlignmentY(Component.TOP_ALIGNMENT);

			progressBars[i].setValue((int) (Math.random() * 100.0));
			progressBars[i].setStringPainted(true);
			progressBars[i].setBorderPainted(true);

			// stepPanels[i].add(tempNumberLabel);
			// stepPanels[i].add(tempLabel);

			// stepPanels[i].add(stepLinks[i]);
			buttons[i].setPreferredSize(new Dimension(150, 30));
			buttons[i].setHorizontalAlignment(JLabel.LEFT);
			stepPanels[i].add(buttons[i]);

			stepPanels[i].add(Box.createRigidArea(new Dimension(0, 5)));
			stepPanels[i].add(progressBars[i]);

			// stepPanels[i].setBorder(BorderFactory.createCompoundBorder(
			// BorderFactory.createLineBorder(Color.gray),
			// stepPanels[i].getBorder()));

			// stepPanels[i].setPreferredSize(new Dimension(m_width, 40));

			// stepPanelsPanel.add(Box.createRigidArea(new Dimension(0,3)));
			stepPanelsPanel.add(stepPanels[i]);
			// stepPanelsPanel.add(stepLinks[i]);

		}
		stepPanelsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		oldPanelSelected = stepPanels[0].select();

		// create button for history panel

		// historyButton.setAlignmentX(Component.LEFT_ALIGNMENT);

		historyCountLabel = new JLabel("(" + (31) + ")");
		historyCountLabel.setPreferredSize(new Dimension(50, 20));
		historyCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		annotationCountLabel = new JLabel("(" + (4) + ")");
		annotationCountLabel.setPreferredSize(new Dimension(50, 20));
		annotationCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		historyButton = new JStepPanel();
		historyButton.setLayout(new BoxLayout(historyButton, BoxLayout.LINE_AXIS));
		historyButton.add(Box.createHorizontalGlue());
		historyButton.addMouseListener(this);

		JXHyperlink historyLink = new JXHyperlink();
		historyLink.setText("History");
		historyLink.setPreferredSize(new Dimension(150, 20));
		historyLink.setMinimumSize(new Dimension(150, 20));

		buttons[7].setPreferredSize(new Dimension(150, 30));
		buttons[7].setHorizontalAlignment(JLabel.LEFT);
		historyButton.add(buttons[7]);
		// historyLink.setAlignmentX(Component.LEFT_ALIGNMENT);

		// historyButton.add(historyLink);
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
		annotationLink.setMinimumSize(new Dimension(150, 20));
		// annotationLink.setAlignmentX(Component.LEFT_ALIGNMENT);

		buttons[8].setPreferredSize(new Dimension(150, 30));
		buttons[8].setHorizontalAlignment(JLabel.LEFT);
		annotationButton.add(buttons[8]);

		// annotationButton.add(annotationLink);
		annotationButton.add(annotationCountLabel);
		// stepPanelsPanel.add(Box.createRigidArea(new Dimension(0,3)));
		stepPanelsPanel.add(annotationButton);

		//
		// stepPanelsPanel.add(Box.createRigidArea(new Dimension(0,10)));

		JXTaskPane currentPane = new JXTaskPane();
		currentPane.setTitle("Current Status");

		currentStepLabel = new JLabel("");
		nextStepLabel = new JLabel("");

		JLabel temp = new JLabel("Last Completed Step:");
		temp.setFont(FontLib.getFont(temp.getFont().getName(), Font.ITALIC, temp.getFont().getSize() - 2));
		currentPane.add(temp);
		currentPane.add(currentStepLabel);
		temp = new JLabel("Next Uncompleted Step:");
		temp.setFont(FontLib.getFont(temp.getFont().getName(), Font.ITALIC, temp.getFont().getSize() - 2));
		currentPane.add(temp);
		currentPane.add(nextStepLabel);

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
		stepButtonPanel.setLayout(new BoxLayout(stepButtonPanel, BoxLayout.LINE_AXIS));
		stepButtonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		stepButtonPanel.add(backButton);
		stepButtonPanel.add(nextButton);

		currentPane.add(stepButtonPanel);

		JXTaskPane annotationPane = new JXTaskPane();
		annotationPane.setTitle("Annotate");

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
		thumbsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		temp = new JLabel("Useful?:");
		temp.setFont(FontLib.getFont(temp.getFont().getName(), Font.ITALIC, temp.getFont().getSize() - 2));
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
		temp.setFont(FontLib.getFont(temp.getFont().getName(), Font.ITALIC, temp.getFont().getSize() - 2));
		tagPanel.add(temp);
		tagTextField = new JTextField("    ");
		// tagTextField.setSize(new Dimension(100,30));
		tagPanel.add(tagTextField);

		// annotationPane.setAlignmentX(Component.CENTER_ALIGNMENT);

		currentPane.setMaximumSize(new Dimension(m_width, 1500));
		annotationPane.setMaximumSize(new Dimension(m_width, 1500));

		editor = new JXEditorPane();
		// editor.setMaximumSize(new Dimension(100, 100));
		editor.setPreferredSize(new Dimension(m_width - 75, 100));
		annotationPane.add(editor);

		JScrollPane scrollEditor = new JScrollPane(editor);
		scrollEditor.setPreferredSize(new Dimension(m_width - 75, 100));
		annotationPane.add(scrollEditor);

		annotationPane.add(thumbsPanel);
		annotationPane.add(tagPanel);

		imageURL = SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/comment.gif");
		ImageIcon icon = new ImageIcon(imageURL);
		annotationPane.setIcon(icon);

		stepTaskPane.add(stepPanelsPanel);

		taskPaneContainer.add(stepTaskPane);
		taskPaneContainer.add(currentPane);
		taskPaneContainer.add(annotationPane);

		add(taskPaneContainer);

		// panel.add(taskPaneContainer);

		// add(stepPanelsPanel);

		// add(panel);

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

	public void mousePressed(MouseEvent e) {

		if (e.getSource() instanceof JStepPanel) {

			if (oldPanelSelected != null)
				oldPanelSelected.unselect();

			JStepPanel panel = (JStepPanel) e.getSource();
			panel.select();

			oldPanelSelected = panel;

			CardLayout cl = (CardLayout) (m_tabbedPane.getLayout());

			if (e.getSource() == historyButton) {
				cl.show(m_tabbedPane, SocialAction.HISTORY_PANEL);
			} else if (e.getSource() == annotationButton) {
				cl.show(m_tabbedPane, SocialAction.ANNOTATION_PANEL);
			} else {
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

	class StepInfo {
		public int numActions = 0;
		public int numCompletedActions = 0;

	}

	class JStepPanel extends JPanel {
		/** Never change */
		private static final long serialVersionUID = 76691021281966321L;
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
			// setBackground(Color.gray);
			return this;
		}

		public void unselect() {
			// setBackground(oldBackground);
		}

	}

	class SYFEvent {
		public int step;
		public String state;
		public String action;
		public double[] parameters;
	}

}