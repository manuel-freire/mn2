package edu.umd.cs.hcil.socialaction.ui.panels.syf;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import edu.umd.cs.hcil.socialaction.SocialAction;

public class SYFProgressPanel extends JPanel implements ActionListener, PropertyChangeListener {

	/** Never change */
	private static final long serialVersionUID = -1011335156077700368L;
	private JProgressBar progressBar, annotatedProgressBar;
	private JButton thumbsUpButton, thumbsDownButton;

	public SYFProgressPanel() {
		super(new BorderLayout());

		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		progressBar.setMaximumSize(new Dimension(200, 30));

		annotatedProgressBar = new JProgressBar(0, 100);
		annotatedProgressBar.setValue(0);
		annotatedProgressBar.setStringPainted(true);

		java.net.URL imageURL = SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/thumb_up.png");
		ImageIcon thumbsUpIcon = new ImageIcon(imageURL);
		imageURL = SocialAction.class.getResource(
                        SocialAction.IMAGE_BASE + "/thumb_down.png");
		ImageIcon thumbsDownIcon = new ImageIcon(imageURL);

		thumbsUpButton = new JButton("Yes", thumbsUpIcon);
		thumbsUpButton.addActionListener(this);
		thumbsDownButton = new JButton("No", thumbsDownIcon);

		JPanel panel = new JPanel();
		// panel.setLayout(new BorderLayout());

		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BorderLayout());

		JPanel progressPanel = new JPanel();
		progressPanel.setLayout(new BorderLayout());

		JPanel thumbsPanel = new JPanel();
		thumbsPanel.add(new JLabel("Useful?"));
		thumbsPanel.add(thumbsUpButton);
		thumbsPanel.add(thumbsDownButton);

		JPanel tagPanel = new JPanel();
		tagPanel.add(new JLabel("Tag:"));
		JTextField tagTextField = new JTextField("                ");
		tagTextField.setSize(new Dimension(100, 30));
		tagPanel.add(tagTextField);

		buttonsPanel.add(thumbsPanel, BorderLayout.NORTH);
		buttonsPanel.add(tagPanel, BorderLayout.SOUTH);

		progressPanel.add(progressBar, BorderLayout.NORTH);
		progressPanel.add(annotatedProgressBar, BorderLayout.SOUTH);

		panel.add(buttonsPanel);// , BorderLayout.WEST);
		panel.add(progressPanel);// , BorderLayout.EAST);

		// panel.add(annotatedProgressBar);

		add(panel, BorderLayout.PAGE_START);

	}

	public void actionPerformed(ActionEvent evt) {

		progressBar.setValue(progressBar.getValue() + 10);
		// startButton.setEnabled(false);
		// setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		// Instances of javax.swing.SwingWorker are not reusuable, so
		// we create new instances as needed.
		// task = new Task();
		// task.addPropertyChangeListener(this);
		// task.execute();
	}

	/**
	 * Invoked when task's progress property changes.
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress" == evt.getPropertyName()) {
			int progress = (Integer) evt.getNewValue();
			progressBar.setValue(progress);
			// taskOutput.append(String.format(
			// "Completed %d%% of task.\n", task.getProgress()));
		}
	}

}
