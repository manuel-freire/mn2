package edu.umd.cs.hcil.socialaction.ui;

import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import prefuse.data.Graph;
import edu.umd.cs.hcil.socialaction.SocialAction;

public class JSocialActionTablePane extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public JSocialActionTablePane(SocialAction app, Graph g) {

		JSocialActionTable prefuseTable = new JSocialActionTable(app, g.getNodeTable());
		prefuseTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		TableColumn column = null;
		for (int i = 0; i < 10; i++) {
			column = prefuseTable.getColumnModel().getColumn(i);
			if (i == 2) {
				column.setPreferredWidth(100); // sport column is bigger
			} else {
				column.setPreferredWidth(50);
			}
		}

		JSocialActionTable prefuseTable2 = new JSocialActionTable(app, g.getNodeTable());
		JScrollPane tableScrollPane2 = new JScrollPane(prefuseTable2);

		JScrollPane tableScrollPane = new JScrollPane(prefuseTable);
		tableScrollPane.setPreferredSize(new Dimension(1300, 100));
		tableScrollPane.setMaximumSize(new Dimension(1300, 100));

		JSplitPane split = new JSplitPane();
		split.setLeftComponent(tableScrollPane);
		split.setRightComponent(tableScrollPane2);
		split.setOneTouchExpandable(true);
		split.setContinuousLayout(false);
		split.setDividerLocation(700);

		this.add(split);

	}

}
