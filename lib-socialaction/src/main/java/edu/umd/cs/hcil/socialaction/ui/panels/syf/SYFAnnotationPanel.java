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
import javax.swing.JEditorPane;
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
import prefuse.util.ui.JSearchPanel;
import edu.umd.cs.hcil.socialaction.SocialAction;
import edu.umd.cs.hcil.socialaction.ui.tables.GraphTableModel;

/**
 * SYF History Panel
 */
public class SYFAnnotationPanel extends JPanel implements ChangeListener, ActionListener {
	/** Never change */
	private static final long serialVersionUID = 5071563838115776997L;

	public static final String FAKE_COMMENT = "There is an interesting outlier in the upper left of this scatterplot -- Why is that group there?";

	public static final String HISTORY_TIME_COLUMN = "Time";

	public static final String HISTORY_STEP_COLUMN = "Step";

	public static final String HISTORY_STATE_COLUMN = "State";

	public static final String HISTORY_ACTION_COLUMN = "Action";

	public static final String HISTORY_PARAMETER_COLUMN = "Parameter";

	public static final String HISTORY_ANNOTATION_COLUMN = "Annotation";

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

	private SocialAction m_app;

	private JComboBox historyComboBox;
	private JEditorPane editor;
	private JButton goButton;
	private int m_renderMode = HISTORY_SHOW_ALL;

	public SYFAnnotationPanel(SocialAction app, int width) {
		this.m_app = app;
		initUI();
		setContent();

	}

	private void initUI() {

		final Box communityBox = Box.createVerticalBox();
		communityBox.setBorder(BorderFactory.createTitledBorder("Annotation"));

		JPanel communityPanel = new JPanel();
		communityPanel.setLayout(new BoxLayout(communityPanel, BoxLayout.Y_AXIS));

		communityPanel.add(Box.createVerticalGlue());

		JSearchPanel searchPanel = new JSearchPanel(m_app.getVisualization(), "hi");
		searchPanel.setMaximumSize(new Dimension(300, 50));
		searchPanel.setLabelText("Search:");
		searchPanel.setBackground(null);
		communityPanel.add(searchPanel);

		communityPanel.add(Box.createRigidArea(new Dimension(0, 5)));

		SYFTagCloud tagCloud = new SYFTagCloud();
		// tagCloud.setBorder(BorderFactory.createLineBorder(Color.black));
		communityPanel.add(tagCloud);

		communityPanel.add(Box.createRigidArea(new Dimension(0, 5)));

		JPanel comboBoxPanel = new JPanel();
		comboBoxPanel.setLayout(new BoxLayout(comboBoxPanel, BoxLayout.PAGE_AXIS));

		historyComboBox = new JComboBox(HISTORY_COMBOBOX);
		historyComboBox.setMaximumSize(new Dimension(450, 30));
		historyComboBox.addActionListener(this);

		comboBoxPanel.add(historyComboBox);
		communityPanel.add(comboBoxPanel);

		String[] columnNames = { "Date", "Time", "Step", "State", "Action", /* "Parameter", */"Rating" };
		tableModel = new GraphTableModel(columnNames);

		setContent();

		// TableSorter sorter = new TableSorter(tableModel);
		// historyJPrefuseTable = new JPrefuseTable(historyTable);

		historyJTable = new JXTable(tableModel);

		String DATE_FORMAT = "h:mm a";
		// String DATE_FORMAT = "HH:mm:ss MM-dd-yyyy";
		// DATE_FORMAT = "H:mm:ss:SSS";
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(DATE_FORMAT);

		historyJTable.setDefaultRenderer(Date.class, new SYFHistoryPanel.DateRenderer(sdf));
		historyJTable.setDefaultRenderer(YearDate.class, new SYFHistoryPanel.DateRenderer(new java.text.SimpleDateFormat(
				"MM/dd/yy")));

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
					editor.setText(FAKE_COMMENT);

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

		editor = new JEditorPane();
		// editor.setMaximumSize(new Dimension(100, 100));

		scrollPane.setPreferredSize(new Dimension(400, 225));
		scrollPane.setMaximumSize(new Dimension(1000, 300));

		JScrollPane editorScrollPane = new JScrollPane(editor);

		JPanel tableAndEditorPanel = new JPanel(); // new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane,
													// editorScrollPane);
		tableAndEditorPanel.setLayout(new BoxLayout(tableAndEditorPanel, BoxLayout.PAGE_AXIS));
		// tableAndEditorPanel.add(Box.createVerticalGlue());
		tableAndEditorPanel.add(scrollPane);
		tableAndEditorPanel.add(editorScrollPane);

		// tableAndEditorPanel.setMaximumSize(new Dimension(400,300));
		// tableAndEditorPanel.setPreferredSize(new Dimension(400,300));
		// tableAndEditorPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		// communityPanel.add(scrollPane);

		communityPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		communityPanel.add(tableAndEditorPanel);

		// communityPanel.add(editor);
		editor.setMaximumSize(new Dimension(1000, 600));
		editor.setPreferredSize(new Dimension(400, 375));
		editor.setSize(new Dimension(400, 300));
		// editor.setMinimumSize(new Dimension(400, 300));
		// editorScrollPane.setMaximumSize(new Dimension(1000, 600));
		// editorScrollPane.setSize(new Dimension(400, 600));
		// editorScrollPane.setMinimumSize(new Dimension(400, 600));

		// communityPanel.add(new JNEditor());

		// communityPanel.add(new JButton("yo!"));

		java.net.URL imageURL = SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/action_go.gif");
		ImageIcon goIcon = new ImageIcon(imageURL);

		goButton = new JButton("Go!", goIcon);
		goButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		goButton.addActionListener(this);

		communityPanel.add(goButton);

		communityBox.add(communityPanel);

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		// this.add(Box.createVerticalGlue());

		this.add(communityBox);

	}

	/**
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent arg0) {

	} //

	public Table getHistoryTable() {
		return historyTable;
	}

	public void addEventToHistory(Date time, int step, String state, String action, double parameter, int annotation) {// ,
																														// boolean
		// annotation)
		// {

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

			// int jTableRow = historyJTable.getSelectedRow();
			int selectedRow = 3; // (Integer) tableModel.getValueAt(jTableRow, 5);

			int step = historyTable.getInt(selectedRow, HISTORY_STEP_COLUMN);
			String state = historyTable.getString(selectedRow, HISTORY_STATE_COLUMN);
			String action = historyTable.getString(selectedRow, HISTORY_ACTION_COLUMN);

			// System.out.println("GO! " + step + " " + state + " " + action);

			double[] parameters = { 0.0, 1.0 };
			m_app.doSYFAction(step, state, action, parameters);
			m_app.doSYFComment();

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

		tableModel.data = new Object[historyTable.getRowCount()][6 + 1];

		int rowPos = 0;
		for (int i = 0; i < historyTable.getRowCount(); i++) {

			Tuple tuple = historyTable.getTuple(i);

			int annotationStatus = ((Integer) tuple.get(HISTORY_ANNOTATION_COLUMN)).intValue();

			// System.out.println("renderMode: " + m_renderMode);
			// System.out.println("annotationStatus: " + annotationStatus);

			if ((m_renderMode == HISTORY_SHOW_ALL)
					|| ((m_renderMode == HISTORY_SHOW_POS) && (annotationStatus == SocialAction.ANNOTATION_POS))
					|| ((m_renderMode == HISTORY_SHOW_NEG) && (annotationStatus == SocialAction.ANNOTATION_NEG))) {

				tableModel.setValueAt(new YearDate((Date) tuple.get(0)), rowPos, 0);
				for (int j = 0; j < 4; j++) {
					tableModel.setValueAt(tuple.get(j), rowPos, j + 1);

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
					tableModel.setValueAt(icon, rowPos, 5);

				rowPos++;

			}
		}

		historyJTable.getRowCount();

		historyJTable.revalidate();
		historyJTable.repaint();

	}

	class YearDate extends Date {
		/**
		 * 
		 */
		private static final long serialVersionUID = -4168482515302176723L;

		@SuppressWarnings("deprecation")
		YearDate(Date d) {
			this.setDate(d.getDate());
			this.setYear(d.getYear());
			this.setMonth(d.getMonth());
			// this.setDate(d.get);
		}
	}

} // end of class CommunityPanel
