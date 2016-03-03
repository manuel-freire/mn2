package edu.umd.cs.hcil.socialaction.ui.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import prefuse.Display;
import prefuse.data.Graph;
import prefuse.data.query.RangeQueryBinding;
import prefuse.util.ui.JRangeSlider;
import edu.umd.cs.hcil.socialaction.SocialAction;

/**
 * Displays a list of ranked nodes/vertices
 * 
 * @version 1.0
 * @author Adam Perer
 */
public class FilterPanel extends JPanel implements ActionListener, ChangeListener {

	/** Never chanage */
	private static final long serialVersionUID = -6569469870747854694L;
	private JTextArea title;
	private JButton updateButton;

	private SocialAction m_app;
	private Graph m_graph;
	private Display m_display;

	public FilterPanel(SocialAction app, Graph g, Display display) {
		this.m_app = app;
		this.m_graph = g;
		this.m_display = display;

		initUI();
	}

	private void initUI() {
		// setBorder(BorderFactory.createEmptyBorder(2,2,2,2));

		final Box overviewBox = Box.createVerticalBox();
		overviewBox.setBorder(BorderFactory.createTitledBorder("Filter"));

		// JPanel rank1d_panel = new JPanel();

		title = new JTextArea();
		title.setFont(new Font(SocialAction.DEFAULT_FONTNAME, Font.BOLD, 12));
		// title.setMaximumSize(new Dimension(300, 100));
		title.setWrapStyleWord(true);
		title.setLineWrap(true);
		title.setOpaque(false);
		title.setText("Sageman Database");
		// title.setVerticalAlignment(JLabel.CENTER);
		// title.setBackground(Color.LIGHT_GRAY);

		Box titlebox = new Box(BoxLayout.Y_AXIS);// BoxLayout.X_AXIS);
		titlebox.add(Box.createHorizontalStrut(2));

		titlebox.add(title, BorderLayout.SOUTH);
		titlebox.add(Box.createHorizontalGlue());
		titlebox.setMaximumSize(new Dimension(800, 75));

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		// rank1d_panel.add(titlebox);

		updateButton = new JButton("Update");
		updateButton.addActionListener(this);
		overviewBox.add(updateButton);

		// set up dynamic queries, search set
		// RangeQueryBinding betweennessQ = new RangeQueryBinding(m_graph.getNodeTable(), DegreeCentrality.DEGREE);

		// construct the filtering predicate
		// AndPredicate filter = new AndPredicate(betweennessQ.getPredicate());
		// filter.add(yearsQ.getPredicate());
		// filter.add(receiptsQ.getPredicate());

		// betweennessQ.getNumberModel().setValueRange(0,65000000,0,65000000);
		// update.add(new VisibilityFilter(group, filter));

		/* RangeQueryBinding rangeQ = */new RangeQueryBinding(m_graph.getNodeTable(), "DegreeCentrality");

		// JRangeSlider slider = rangeQ.createHorizontalRangeSlider();
		JRangeSlider slider = m_app.getRangeBinding().createHorizontalRangeSlider();
		slider.setThumbColor(null);
		// slider.setMinExtent(150000);
		slider.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				m_display.setHighQuality(false);
			}

			public void mouseReleased(MouseEvent e) {
				JRangeSlider slider = (JRangeSlider) e.getComponent();
				m_display.setHighQuality(true);
				m_display.repaint();
				System.out.println(slider.getHighValue());
			}
		});

		overviewBox.add(slider);

		this.add(overviewBox);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == updateButton) {
			m_app.updatePanels(/* true */);
		}
	}

	public void stateChanged(ChangeEvent e) {
	}
}