package edu.umd.cs.hcil.socialaction.ui;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.BevelBorder;

import prefuse.data.Table;
import prefuse.util.ui.JPrefuseTable;
import edu.umd.cs.hcil.socialaction.SocialAction;

public class JSocialActionTable extends JPrefuseTable {

	/** Never change */
	private static final long serialVersionUID = 1532188147205315980L;

	JPopupMenu popupMenu = new JPopupMenu();

	SocialAction m_app;

	Table m_table;

	public JSocialActionTable(SocialAction app, Table t) {
		super(t);
		m_table = t;

		m_app = app;

		createPopupMenu();

		// add the listener to the jtable
		MouseListener popupListener = new PopupListener();
		// add the listener specifically to the table
		addMouseListener(popupListener);

	}

	/*
	 * public void setTable(Table t) {
	 * 
	 * PrefuseTableModel model = new PrefuseTableModel(); super.setModel(model); m_table.addTableListener(model);
	 * 
	 * }
	 */

	JCheckBoxMenuItem noneItem, labelItem, colorItem, relationshipItem, keyItem;

	private void createPopupMenu() {

		PopupMenuItemListener popupMenuItemListener = new PopupMenuItemListener();

		java.net.URL imageURL = SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/text_dropcaps.png");
		ImageIcon labelIcon = new ImageIcon(imageURL);
		imageURL = SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/color_swatch.png");
		ImageIcon colorIcon = new ImageIcon(imageURL);
		imageURL = SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/link.png");
		ImageIcon relationshipIcon = new ImageIcon(imageURL);
		imageURL = SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/key.png");
		ImageIcon keyIcon = new ImageIcon(imageURL);

		noneItem = new JCheckBoxMenuItem("None");
		noneItem.addActionListener(popupMenuItemListener);
		popupMenu.add(noneItem);

		keyItem = new JCheckBoxMenuItem("Set as Key", keyIcon);
		keyItem.addActionListener(popupMenuItemListener);
		popupMenu.add(keyItem);

		labelItem = new JCheckBoxMenuItem("Set as Label", labelIcon);
		labelItem.addActionListener(popupMenuItemListener);
		popupMenu.add(labelItem);

		colorItem = new JCheckBoxMenuItem("Set as Color", colorIcon);
		colorItem.addActionListener(popupMenuItemListener);
		popupMenu.add(colorItem);

		relationshipItem = new JCheckBoxMenuItem("Set as Relationship", relationshipIcon);
		relationshipItem.addActionListener(popupMenuItemListener);
		popupMenu.add(relationshipItem);

		popupMenu.setLabel("Justification");
		popupMenu.setBorder(new BevelBorder(BevelBorder.RAISED));
		// menuItem = new JMenuItem("yo");
		// popupMenu.add(menuItem);
		// menuItem = new JMenuItem("bo");
		// popupMenu.add(menuItem);
		// menuItem = new JMenuItem("fo");
		// popupMenu.add(menuItem);

	}

	/**
	 * Provides a popup menu listener for the table preview that allows selection of node labels, coloring,
	 * relationships, and keys.
	 * 
	 * @author Adam Perer
	 */
	class PopupMenuItemListener extends AbstractAction {// XXX WHOA! COOL!

		/** Never change */
		private static final long serialVersionUID = 5143678425588362991L;

		public void actionPerformed(ActionEvent event) {
			unselectPopupMenuItems();
			System.out.println(getColumnName(currentColumn));

			if (event.getSource() == noneItem) {
				noneItem.setSelected(true);
			} else if (event.getSource() == labelItem) {
				labelItem.setSelected(true);
				m_app.setLabelField(getColumnName(currentColumn));
			} else if (event.getSource() == colorItem) {
				colorItem.setSelected(true);
				m_app.setNodeColorField(getColumnName(currentColumn));
			} else if (event.getSource() == relationshipItem) {
				relationshipItem.setSelected(true);
				m_app.setRelationshipField(getColumnName(currentColumn));
			} else if (event.getSource() == keyItem) {
				keyItem.setSelected(true);
				m_app.setKeyField(getColumnName(currentColumn));
			}
		}
	}

	private void unselectPopupMenuItems() {
		noneItem.setSelected(false);
		colorItem.setSelected(false);
		labelItem.setSelected(false);
		relationshipItem.setSelected(false);
	}

	int currentColumn = 0;

	class PopupListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			showPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			showPopup(e);
		}

		private void showPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {

				currentColumn = columnAtPoint(e.getPoint());

				unselectPopupMenuItems();

				if (getColumnName(currentColumn) == m_app.getLabelField()) {
					labelItem.setSelected(true);
				} else if (getColumnName(currentColumn) == m_app.getRelationshipField()) {
					relationshipItem.setSelected(true);
				} else if (getColumnName(currentColumn) == m_app.getKeyField()) {
					keyItem.setSelected(true);
				} else if (getColumnName(currentColumn) == m_app.getNodeColorField()) {
					colorItem.setSelected(true);
				} else
					noneItem.setSelected(true);

				popupMenu.show(e.getComponent(), e.getX(), e.getY());

			}
		}
	}

	// class PopupPrintListener implements PopupMenuListener {
	// public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
	// System.out.println("Popup menu will be visible!");
	// }
	// public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
	// System.out.println("Popup menu will be invisible!");
	// }
	// public void popupMenuCanceled(PopupMenuEvent e) {
	// System.out.println("Popup menu is hidden!");
	// }
	// }
}
