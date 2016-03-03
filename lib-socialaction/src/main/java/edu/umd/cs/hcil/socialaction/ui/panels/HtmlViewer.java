package edu.umd.cs.hcil.socialaction.ui.panels;

/**
 * Demo program to explore HTML printing. Based on code in _Swing_ by Matthew Robinson and Pavel Vorobiev.
 * www.manning.com/sbe
 * 
 * Author: David Eisner cradle@glue.umd.edu 2/19/2002
 */

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.plaf.basic.BasicTextUI;
import javax.swing.text.BoxView;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

/**
 * A JEditorPane for displaying and printing HTML documents.
 */
class PrintablePane extends JEditorPane implements Printable {

	/** Never change */
	private static final long serialVersionUID = -1618084489778499113L;
	PrintView m_printView = null;

	PrintablePane() {

		super();

		setContentType("text/html");
		HTMLEditorKit edKit = (HTMLEditorKit) getEditorKit();

		StyleSheet sheet = edKit.getStyleSheet();

		sheet.addRule("p {font-size: small}");
		sheet.addRule("body {font-size: 12pt}");
		sheet.addRule("pre {font-size: 10pt}");

	}

	public int print(Graphics pg, PageFormat pageFormat, int pageIndex) throws PrinterException {

		pg.translate((int) pageFormat.getImageableX(), (int) pageFormat.getImageableY());
		int wPage = (int) pageFormat.getImageableWidth();
		int hPage = (int) pageFormat.getImageableHeight();
		pg.setClip(0, 0, wPage, hPage);

		if (m_printView == null) {
			BasicTextUI btui = (BasicTextUI) getUI();
			View root = btui.getRootView(this);

			m_printView = new PrintView(getDocument().getDefaultRootElement(), root, wPage, hPage);
		}

		boolean bContinue = m_printView.paintPage(pg, hPage, pageIndex);
		System.gc();

		if (bContinue)
			return PAGE_EXISTS;
		else {
			m_printView = null;
			return NO_SUCH_PAGE;
		}

	}
}

/**
 * A view that can print itself.
 */
class PrintView extends BoxView {

	protected int m_pageBeginY = 0;
	protected int m_pageEndY = 0;
	protected int m_pageIndex = -1;
	protected Rectangle m_rootAlloc;

	// When childless view spans pages, we add a "spacer"
	// to the end of the page on which it begins to push it
	// down to the next page. This vector has an element for
	// each page, and grows dynamically.

	Vector<Integer> spacers = new Vector<Integer>(); // vector of Integer

	public PrintView(Element elem, View rootView, int w, int h) {
		super(elem, Y_AXIS);
		setParent(rootView);
		setSize(w, h);
		layout(w, h);

		int nChildren = getViewCount();
		for (int i = 0; i < nChildren; i++) {
			View v = getView(i);
			v.setSize(w, h);
		}

		m_rootAlloc = new Rectangle(0, 0, w, h);
	}

	public boolean paintPage(Graphics g, int hPage, int pageIndex) {

		boolean didPaint = false;

		if (spacers.size() < pageIndex + 1) {
			Integer i = new Integer(0);
			spacers.add(i);
		}

		if (pageIndex > m_pageIndex) {
			m_pageBeginY = m_pageEndY;
			m_pageEndY += hPage;
			m_pageIndex = pageIndex;
		}

		System.out.println("## printPage, page " + pageIndex + ", " + m_pageBeginY + " to " + m_pageEndY);

		int spaceSoFar = 0;
		for (int i = 0; i < pageIndex; i++)
			spaceSoFar += spacers.get(i).intValue();

		didPaint = visitView(g, this, m_rootAlloc, spaceSoFar, m_pageBeginY, m_pageEndY, pageIndex);

		return didPaint;
	}

	/**
	 * Recursively print the view tree.
	 */
	private boolean visitView(Graphics g, View v, Shape allocation, int leadingSpace, int pageBeginY, int pageEndY,
			int pageIndex) {

		int childCount = v.getViewCount();
		int cvBegin, cvEnd;
		View cv;
		Rectangle r = new Rectangle();
		boolean didPaint = false;

		/*
		 * There's probably a more efficent way to do this. We go through each child of the view. If the view located
		 * before this page, skip to the next child. If the view is entirely after this page, quit.
		 * 
		 * This makes assumptions about the view hierarchy that may not be valid.
		 */
		for (int i = 0; i < childCount; i++) {

			Shape childAlloc = v.getChildAllocation(i, allocation);
			if (childAlloc == null)
				continue;
			Rectangle cab = childAlloc.getBounds();

			cvBegin = cab.y + leadingSpace;
			cvEnd = cab.y + cab.height + leadingSpace;

			if (cvEnd <= pageBeginY)
				continue;
			else if (cvBegin > pageEndY)
				break;
			else {

				cv = v.getView(i);

				boolean spansPage = (cvBegin < pageBeginY || cvEnd > pageEndY);

				boolean hasChildren = (cv.getViewCount() > 0);

				if (!spansPage) {
					// If child fits entirely on a page, just paint it.

					r.x = cab.x;
					r.y = cvBegin - pageBeginY;
					r.width = cab.width;
					r.height = cab.height;
					cv.paint(g, r);
					didPaint = true;
				} else if (hasChildren)
					didPaint |= visitView(g, cv, childAlloc, leadingSpace, pageBeginY, pageEndY, pageIndex);
				else {

					// It spans the next page, and it doesn't have any
					// children. Add a spacer.

					// XXX: Not quite correct. Should be something like
					// a max over all such elements . . .
					// Also, breaks if the view spans more than one page.
					spacers.set(pageIndex, new Integer(pageEndY - cvBegin));
				}

			}

		}
		return didPaint;
	}
}

public class HtmlViewer extends JFrame {

	/** Never change */
	private static final long serialVersionUID = -7783924998717513104L;
	PrintablePane m_editorPane = new PrintablePane();
	JFileChooser m_fc = new JFileChooser();

	public HtmlViewer() {
		super("HtmlViewer v0.01");

		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());

		m_editorPane.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(m_editorPane);
		scrollPane.setPreferredSize(new Dimension(500, 300));
		cp.add(scrollPane, BorderLayout.CENTER);

		setJMenuBar(createMenuBar());

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {

				System.exit(0);
			}
		});
	}

	private JMenuBar createMenuBar() {

		JMenuBar menuBar = new JMenuBar();
		JMenuItem menuItem;

		JMenu menu = new JMenu("File");

		menuItem = new JMenuItem(new AbstractAction("Open") {
			/** Never change */
			private static final long serialVersionUID = -2482574478213187537L;

			public void actionPerformed(ActionEvent ae) {
				openFile();
			}

		});
		menu.add(menuItem);

		menuItem = new JMenuItem(new AbstractAction("Print") {
			/** Never change */
			private static final long serialVersionUID = 5763594209575232761L;

			public void actionPerformed(ActionEvent ae) {
				printPage();
			}
		});
		menu.add(menuItem);

		// menuItem = new JMenuItem( new AbstractAction( "Print Preview" ) {
		//
		// // public void
		// // actionPerformed( ActionEvent ae ) {
		// // new PrintPreview( m_editorPane );
		// // }
		// });
		// menu.add( menuItem );

		menu.addSeparator();

		menuItem = new JMenuItem(new AbstractAction("Exit") {
			/** Never change */
			private static final long serialVersionUID = 6101420219691866956L;

			public void actionPerformed(ActionEvent ae) {

				System.exit(0);
			}

		});
		menu.add(menuItem);

		menuBar.add(menu);
		return menuBar;
	}

	private void openFile() {

		int retval = m_fc.showOpenDialog(this);

		if (retval == JFileChooser.APPROVE_OPTION) {

			File f = m_fc.getSelectedFile();

			try {
				m_editorPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				m_editorPane.setPage(f.toURI().toURL());
				m_editorPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			} catch (Exception e) {
				System.err.println("ERROR: " + e);
				e.printStackTrace();
			}
		}
	}

	private void printPage() {

		try {

			PrinterJob printJob = PrinterJob.getPrinterJob();
			printJob.setPrintable(m_editorPane);
			if (!printJob.printDialog())
				return;
			printJob.print();
		} catch (Exception ex) {
			System.err.println("ERROR: Can't print: " + ex);
		}
	}

	public static void main(String args[]) {

		HtmlViewer app = new HtmlViewer();

		app.pack();
		app.setVisible(true);
	}
}
