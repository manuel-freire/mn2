package edu.umd.cs.hcil.socialaction.ui.panels.syf;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXTable;

import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.tuple.TableTuple;
import prefuse.data.tuple.TupleManager;
import edu.umd.cs.hcil.socialaction.SocialAction;
import edu.umd.cs.hcil.socialaction.ui.tables.GraphTableModel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * SYF History Panel
 */
public class SYFHistoryPanel extends JPanel implements ChangeListener, ActionListener {
	/** Never change */
	private static final long serialVersionUID = -8368137549590482771L;

	public static final String HISTORY_TIME_COLUMN = "Time";

	public static final String HISTORY_STEP_COLUMN = "Step";

	public static final String HISTORY_STATE_COLUMN = "State";

	public static final String HISTORY_ACTION_COLUMN = "Action";

	public static final String HISTORY_PARAMETER_COLUMN = "Parameter";

	public static final String HISTORY_ANNOTATION_COLUMN = "Rating";

	public static final String HISTORY_TABLEROW_COLUMN = "Row";

	private String HISTORY_COMBOBOX_ALL = "All";

	private String HISTORY_COMBOBOX_POS = "Interesting";

	private String HISTORY_COMBOBOX_NEG = "Uninteresting";

	private String[] HISTORY_COMBOBOX = { HISTORY_COMBOBOX_ALL, HISTORY_COMBOBOX_POS, HISTORY_COMBOBOX_NEG };

	private static final int HISTORY_SHOW_ALL = 0;

	private static final int HISTORY_SHOW_POS = 1;

	private static final int HISTORY_SHOW_NEG = 2;

	private Table historyTable;

	private TupleManager historyTableTuplemanager;

	private GraphTableModel tableModel;

	// private JPrefuseTable historyJPrefuseTable;
	private JXTable historyJTable;
	private JButton goButton, reuseButton;

	private SocialAction m_app;

	private JComboBox historyComboBox;

	private int m_numberActiveRows;

	private int m_renderMode = HISTORY_SHOW_ALL;

	public SYFHistoryPanel(SocialAction app, int width) {
		this.m_app = app;
		initUI();
		setContent();

	}

        static class DateRenderer extends DefaultTableCellRenderer.UIResource {
            private DateFormat formatter;
            public DateRenderer(DateFormat formatter) {
                super();
                this.formatter = formatter;
            }

            @Override
            public void setValue(Object value) {
            if (formatter==null) {
                formatter = DateFormat.getDateInstance();
                }
                setText((value == null) ? "" : formatter.format(value));
            }
        }

	private void initUI() {

		final Box historyBox = Box.createVerticalBox();
		historyBox.setBorder(BorderFactory.createTitledBorder("History"));

		JPanel historyPanel = new JPanel();
		historyPanel.setLayout(new BoxLayout(historyPanel, BoxLayout.PAGE_AXIS));

		historyPanel.add(Box.createHorizontalGlue());

		JPanel comboBoxPanel = new JPanel();
		comboBoxPanel.setLayout(new BoxLayout(comboBoxPanel, BoxLayout.PAGE_AXIS));

		historyComboBox = new JComboBox(HISTORY_COMBOBOX);
		historyComboBox.setMaximumSize(new Dimension(450, 30));
		historyComboBox.addActionListener(this);

		comboBoxPanel.add(historyComboBox);
		historyPanel.add(comboBoxPanel);

		String[] columnNames = { "Time", "Step", "State", "Action", /* "Parameter", */"Rating", "Row" };
		tableModel = new GraphTableModel(columnNames);

		setContent();

		// TableSorter sorter = new TableSorter(tableModel);
		// historyJPrefuseTable = new JPrefuseTable(historyTable);

		historyJTable = new JXTable(tableModel);

		String DATE_FORMAT = "h:mm a";// MM-dd-yyyy";
		// DATE_FORMAT = "H:mm:ss:SSS";
		SimpleDateFormat sdf = new java.text.SimpleDateFormat(DATE_FORMAT);
		historyJTable.setDefaultRenderer(Date.class, new DateRenderer(sdf));

		// historyJTable.revalidate();
		// historyJTable.repaint();

		// subgraphStatsTable.getColumnExt("Entities").setVisible(false);

		JScrollPane scrollPane = new JScrollPane(historyJTable);

		historyJTable.setOpaque(true); // content panes must be opaque

		// Ask to be notified of selection changes.
		ListSelectionModel rowSM = historyJTable.getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {

				// Ignore extra messages.
				if (e.getValueIsAdjusting())
					return;

				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty()) {
					// no rows are selected
				} else {
					lsm.getMinSelectionIndex();

					// subgraphStatsTable.getColumnExt("Entities").setVisible(true);
					//                    
					// Integer selected = (Integer) subgraphStatsTable.getValueAt(selectedRow, 0);
					// Set members = (Set) subgraphStatsTable.getValueAt(selectedRow, 2);
					//
					// subgraphStatsTable.getColumnExt("Entities").setVisible(false);

				}
			}
		});

		// communityPanel.setPreferredSize(new Dimension(350, 400));

		historyPanel.add(scrollPane);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));

		java.net.URL imageURL = SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/action_go.gif");
		ImageIcon goIcon = new ImageIcon(imageURL);

		imageURL = SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/arrow_rotate_clockwise.png");
		ImageIcon reuseIcon = new ImageIcon(imageURL);

		goButton = new JButton("Go!", goIcon);
		goButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		goButton.addActionListener(this);
		buttonPanel.add(goButton);

		reuseButton = new JButton("Reuse!", reuseIcon);
		reuseButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		reuseButton.addActionListener(this);
		buttonPanel.add(reuseButton);

		historyPanel.add(buttonPanel);
		historyBox.add(historyPanel);

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		this.add(historyBox);

	}

	/**
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent arg0) {

	} //

	public Table getHistoryTable() {
		return historyTable;
	}

	public void clearHistoryTable() {
		historyTable.clear();
	}

	public void addEventToHistory(Date time, int step, String state, String action, double parameter, int annotation) {// ,
																														// boolean
		// annotation)
		// {

		TableTuple previousTuple;
		if (historyTable.getRowCount() > 1) {
			previousTuple = (TableTuple) historyTableTuplemanager.getTuple(historyTable.getRowCount() - 1);

			if ((previousTuple.getInt(HISTORY_STEP_COLUMN) == step)
					&& (previousTuple.getString(HISTORY_STATE_COLUMN).equals(state))
					&& (previousTuple.getString(HISTORY_ACTION_COLUMN).equals(action))) {
				return;
			}

		}

		historyTable.addRow();
		TableTuple tuple = (TableTuple) historyTableTuplemanager.getTuple(historyTable.getRowCount() - 1);

		tuple.set(HISTORY_TIME_COLUMN, time);
		tuple.set(HISTORY_STEP_COLUMN, step);
		tuple.set(HISTORY_STATE_COLUMN, state);
		tuple.set(HISTORY_ACTION_COLUMN, action);
		// tuple.set(HISTORY_PARAMETER_COLUMN, parameter);
		tuple.set(HISTORY_ANNOTATION_COLUMN, annotation);

		historyTable.addTuple(tuple);

		setTableContent();

		System.out.println(historyTable.getRowCount());

	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */

	public void actionPerformed(ActionEvent evt) {
		// if (evt.getSource() instanceof JButton) {
		// historyJPrefuseTable.revalidate();
		// historyJPrefuseTable.repaint();
		// System.out.println(historyJPrefuseTable.getTable().getRowCount() + " " + historyJPrefuseTable.getRowCount());
		// }

		if (evt.getSource() == historyComboBox) {
			if (historyComboBox.getSelectedItem() == HISTORY_COMBOBOX_ALL)
				m_renderMode = HISTORY_SHOW_ALL;
			else if (historyComboBox.getSelectedItem() == HISTORY_COMBOBOX_POS)
				m_renderMode = HISTORY_SHOW_POS;
			else if (historyComboBox.getSelectedItem() == HISTORY_COMBOBOX_NEG)
				m_renderMode = HISTORY_SHOW_NEG;

			setTableContent();
		} else if (evt.getSource() == goButton) {

			int jTableRow = historyJTable.getSelectedRow();
			int selectedRow = (Integer) tableModel.getValueAt(jTableRow, 5);

			int step = historyTable.getInt(selectedRow, HISTORY_STEP_COLUMN);
			String state = historyTable.getString(selectedRow, HISTORY_STATE_COLUMN);
			String action = historyTable.getString(selectedRow, HISTORY_ACTION_COLUMN);

			// System.out.println("GO! " + step + " " + state + " " + action);

			double[] parameters = { 0.0, 1.0 };
			m_app.doSYFAction(step, state, action, parameters);

		} else if (evt.getSource() == reuseButton) {

			// if (m_renderMode != HISTORY_SHOW_POS) {
			// m_renderMode = HISTORY_SHOW_POS;
			// setTableContent();
			// }

			int numRows = m_numberActiveRows;
			int steps[] = new int[numRows];
			String states[] = new String[numRows];
			String actions[] = new String[numRows];

			for (int i = 0; i < numRows; i++) {
				// get row in History Table that corresponds to this action
				System.out.println("IIII: " + i);
				int historyTableRow = (Integer) tableModel.getValueAt(i, 5);

				steps[i] = historyTable.getInt(historyTableRow, HISTORY_STEP_COLUMN);
				states[i] = historyTable.getString(historyTableRow, HISTORY_STATE_COLUMN);
				actions[i] = historyTable.getString(historyTableRow, HISTORY_ACTION_COLUMN);
			}

			m_app.doSYFActionsOnNewGraph(steps, states, actions);

		}

	} //

	public void setContent() {

		historyTable = new Table();

		historyTable.addColumn(HISTORY_TIME_COLUMN, Date.class);
		historyTable.addColumn(HISTORY_STEP_COLUMN, int.class);
		historyTable.addColumn(HISTORY_STATE_COLUMN, String.class);
		historyTable.addColumn(HISTORY_ACTION_COLUMN, String.class);
		// historyTable.addColumn(HISTORY_PARAMETER_COLUMN, double.class);
		historyTable.addColumn(HISTORY_ANNOTATION_COLUMN, int.class);

		historyTableTuplemanager = new TupleManager(historyTable, null, TableTuple.class);

	}

	public void setTableContent() {

		tableModel.data = new Object[historyTable.getRowCount()][7];

		int rowPos = 0;
		for (int i = 0; i < historyTable.getRowCount(); i++) {

			Tuple tuple = historyTable.getTuple(i);

			int annotationStatus = ((Integer) tuple.get(HISTORY_ANNOTATION_COLUMN)).intValue();
			int historyTableRow = tuple.getRow();
			//            
			// System.out.println("renderMode: " + m_renderMode);
			// System.out.println("annotationStatus: " + annotationStatus);

			if ((m_renderMode == HISTORY_SHOW_ALL)
					|| ((m_renderMode == HISTORY_SHOW_POS) && (annotationStatus == SocialAction.ANNOTATION_POS))
					|| ((m_renderMode == HISTORY_SHOW_NEG) && (annotationStatus == SocialAction.ANNOTATION_NEG))) {

				for (int j = 0; j < 4; j++) {
					tableModel.setValueAt(tuple.get(j), rowPos, j);

				}
				java.net.URL imageURL;
				ImageIcon icon;

				if (annotationStatus == SocialAction.ANNOTATION_NONE)
					imageURL = SocialAction.class.getResource(
                                                SocialAction.IMAGE_BASE + "/ablank.png");
				else if (annotationStatus == SocialAction.ANNOTATION_NEG)
					imageURL = SocialAction.class.getResource(
                                                SocialAction.IMAGE_BASE + "/thumb_down.png");
				else if (annotationStatus == SocialAction.ANNOTATION_POS)
					imageURL = SocialAction.class.getResource(
                                                SocialAction.IMAGE_BASE + "/thumb_up.png");
				else
					imageURL = SocialAction.class.getResource(
                                                SocialAction.IMAGE_BASE + "/ablank.png");

				icon = new ImageIcon(imageURL);
				if ((m_renderMode == HISTORY_SHOW_ALL)
						|| (((m_renderMode == HISTORY_SHOW_POS) && (annotationStatus == SocialAction.ANNOTATION_POS)))
						|| (((m_renderMode == HISTORY_SHOW_NEG) && (annotationStatus == SocialAction.ANNOTATION_NEG))))
					tableModel.setValueAt(icon, rowPos, 4);

				tableModel.setValueAt(historyTableRow, rowPos, 5);

				rowPos++;

			}

			m_numberActiveRows = rowPos;
		}

		historyJTable.revalidate();
		historyJTable.repaint();

	}

} // end of class CommunityPanel
