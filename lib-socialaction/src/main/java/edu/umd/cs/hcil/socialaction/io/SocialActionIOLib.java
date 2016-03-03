package edu.umd.cs.hcil.socialaction.io;

import java.awt.Component;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.JFileChooser;

import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.io.CSVTableReader;
import prefuse.data.io.DelimitedTextTableReader;
import prefuse.data.io.GraphReader;
import prefuse.data.io.TableReader;
import prefuse.util.StringLib;
import prefuse.util.io.IOLib;
import prefuse.util.io.SimpleFileFilter;

/**
 * @author Adam Perer
 */
public class SocialActionIOLib {

	/**
	 * Present a file chooser dialog for loading a Table data set.
	 * 
	 * @param c
	 *            user interface component from which the request is being made
	 * @return a newly loaded Table, or null if not found or action canceled
	 */
	public static Table getTableFilePreview(Component c) {
		JFileChooser jfc = new JFileChooser();
		jfc.setDialogType(JFileChooser.OPEN_DIALOG);
		jfc.setDialogTitle("Open Table File");
		jfc.setAcceptAllFileFilterUsed(false);

		SimpleFileFilter ff;

		// TODO have this generate automatically
		// tie into PrefuseConfig??

		// Pipe-Delimited
		ff = new SimpleFileFilter("txt", "Pipe-Delimited Text File (*.txt)", new DelimitedTextTableReader("|"));
		ff.addExtension("gz");
		jfc.setFileFilter(ff);

		// Tab-Delimited
		ff = new SimpleFileFilter("txt", "Tab-Delimited Text File (*.txt)", new DelimitedTextTableReader());
		ff.addExtension("gz");
		jfc.setFileFilter(ff);

		// CSV
		ff = new SimpleFileFilter("csv", "Comma Separated Values (CSV) File (*.csv)", new CSVTableReader());
		ff.addExtension("gz");
		jfc.setFileFilter(ff);

		int retval = jfc.showOpenDialog(c);
		if (retval != JFileChooser.APPROVE_OPTION)
			return null;

		File f = jfc.getSelectedFile();
		ff = (SimpleFileFilter) jfc.getFileFilter();
		TableReader tr = (TableReader) ff.getUserData();

		try {
			return tr.readTable(IOLib.streamFromString(f.getAbsolutePath()));
		} catch (Exception e) {
			Logger.getLogger(IOLib.class.getName()).warning(e.getMessage() + "\n" + StringLib.getStackTrace(e));
			return null;
		}
	}

	/**
	 * Uses a JFileChooser to read in and parse an HCIL format table file
	 * 
	 * @param c
	 *            the calling component
	 * @param jfc
	 *            the JFileChooser to use
	 * @return the parsed table
	 */
	public static Table getHCILFormatTableFilePreview(Component c, JFileChooser jfc) {
		// JFileChooser jfc = new JFileChooser();
		// jfc.setDialogType(JFileChooser.OPEN_DIALOG);
		// jfc.setDialogTitle("Open Table File");
		// jfc.setAcceptAllFileFilterUsed(false);

		SimpleFileFilter ff;

		// Tab-Delimited
		ff = new SimpleFileFilter("txt", "HCIL Format Tab-Delimited Text File (*.txt)", new HCILFormatTableReader());
		ff.addExtension("node");
		ff.addExtension("link");
		ff.addExtension("tsv");
		jfc.setFileFilter(ff);

		int retval = jfc.showOpenDialog(c);
		if (retval != JFileChooser.APPROVE_OPTION)
			return null;

		File f = jfc.getSelectedFile();
		ff = (SimpleFileFilter) jfc.getFileFilter();
		TableReader tr = (TableReader) ff.getUserData();

		try {
			return tr.readTable(IOLib.streamFromString(f.getAbsolutePath()));
		} catch (Exception e) {
			Logger.getLogger(IOLib.class.getName()).warning(e.getMessage() + "\n" + StringLib.getStackTrace(e));
			System.err.println(f.getAbsolutePath());
			return null;
		}
	}

	public static Graph getPajekGraphFile(Component c) {
		JFileChooser jfc = new JFileChooser();
		jfc.setDialogType(JFileChooser.OPEN_DIALOG);
		jfc.setDialogTitle("Open Pajek File");
		jfc.setAcceptAllFileFilterUsed(false);

		SimpleFileFilter ff;

		ff = new SimpleFileFilter("net", "Pajek Net File (*.txt)", new PajekReader());
		ff.addExtension("gz");
		jfc.setFileFilter(ff);

		int retval = jfc.showOpenDialog(c);
		if (retval != JFileChooser.APPROVE_OPTION)
			return null;

		File f = jfc.getSelectedFile();
		ff = (SimpleFileFilter) jfc.getFileFilter();
		GraphReader gr = (GraphReader) ff.getUserData();

		try {
			return gr.readGraph(IOLib.streamFromString(f.getAbsolutePath()));
		} catch (Exception e) {
			Logger.getLogger(IOLib.class.getName()).warning(e.getMessage() + "\n" + StringLib.getStackTrace(e));
			return null;
		}
	}

}
