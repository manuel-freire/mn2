package edu.umd.cs.hcil.socialaction.ui.panels.syf;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class JIconComboBox extends JComboBox {

	/** Never change */
	private static final long serialVersionUID = 7395393526009345115L;

	String[] m_strings;

	ImageIcon m_icon, m_blankicon, m_commenticon;
	int[] m_showIcon;

	public JIconComboBox(String[] strings, ImageIcon icon, int[] showIcon, ImageIcon blankicon, ImageIcon commentIcon) {
		super(strings);
		// super(new BorderLayout());

		this.m_strings = strings;
		this.m_icon = icon;
		this.m_showIcon = showIcon;
		this.m_blankicon = blankicon;
		this.m_commenticon = commentIcon;
		// Load the pet images and create an array of indexes.
		// images = new ImageIcon[strings.length];
		// Integer[] intArray = new Integer[strings.length];
		// for (int i = 0; i < strings.length; i++) {
		// intArray[i] = new Integer(i);
		// // if ((i % 2) == 0)
		// images[i] = icon;
		// // else
		// // images[i] = createImageIcon("cancel.png");
		// if (images[i] != null) {
		// images[i].setDescription(strings[i]);
		// }
		// }
		// this(intArray);

		// Create the combo box.
		// JComboBox petList = new JComboBox(intArray);
		ComboBoxRenderer renderer = new ComboBoxRenderer();
		// renderer.setPreferredSize(new Dimension(200, 130));
		this.setRenderer(renderer);

	}

	/** Returns an ImageIcon, or null if the path was invalid. */
	protected static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = JIconComboBox.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

	// private static void createAndShowGUI() {
	// //Create and set up the window.
	// JFrame frame = new JFrame("CustomComboBoxDemo");
	// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	//
	// //Create and set up the content pane.
	// ImageIcon icon = createImageIcon("accept.png");
	//        
	// String[] petStrings = {"Bird", "Cat", "Dog", "Rabbit", "Pig"};
	// JComponent newContentPane = new JIconComboBox(petStrings, icon);
	// newContentPane.setOpaque(true); //content panes must be opaque
	// frame.setContentPane(newContentPane);
	//
	// //Display the window.
	// frame.pack();
	// frame.setVisible(true);
	// }

	class ComboBoxRenderer extends JLabel implements ListCellRenderer {

		/** Never change */
		private static final long serialVersionUID = -348674921179880854L;

		public ComboBoxRenderer() {
			setOpaque(true);
			// setHorizontalAlignment(CENTER);
			// setVerticalAlignment(CENTER);
		}

		/*
		 * This method finds the image and text corresponding to the selected value and returns the label, set up to
		 * display the text and image.
		 */
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			// Get the selected index. (The index param isn't
			// always valid, so just use the value.)
			// int selectedIndex = index;//((Integer)value).intValue();
			// System.out.println(list + " " + value + " " + index);

			int arrayIndex = 0;
			for (int i = 0; i < m_strings.length; i++) {
				if (m_strings[i] == value)
					arrayIndex = i;
			}

			// System.out.println(value + " " + arrayIndex);

			int selectedIndex = arrayIndex;

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			// Set the icon and text. If icon was null, say so.

			if (m_showIcon[selectedIndex] == 0) {
				setIcon(m_icon);
				setText(m_strings[selectedIndex]);
				// setFont(FontLib.getFont(list.getFont().getName(),Font.PLAIN,list.getFont().getSize()));
			} else if (m_showIcon[selectedIndex] == 1) {
				setIcon(m_commenticon);
				setText(m_strings[selectedIndex]);
				// setFont(FontLib.getFont(list.getFont().getName(),Font.PLAIN,list.getFont().getSize()));
			} else {
				setIcon(m_blankicon);
				setText(m_strings[selectedIndex]);
				// setFont(FontLib.getFont(list.getFont().getName(),Font.ITALIC,list.getFont().getSize()));
			}

			// setHorizontalTextPosition(SwingConstants.LEFT);

			return this;
		}
	}

}
