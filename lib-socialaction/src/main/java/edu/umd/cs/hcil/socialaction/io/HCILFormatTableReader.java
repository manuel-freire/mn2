package edu.umd.cs.hcil.socialaction.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import prefuse.data.io.DelimitedTextTableReader;
import prefuse.data.io.TableReadListener;
import prefuse.data.parser.DataParseException;

/**
 * @author Adam Perer
 */
public class HCILFormatTableReader extends DelimitedTextTableReader {

	/**
	 * @see prefuse.data.io.AbstractTextTableReader#read(java.io.InputStream, prefuse.data.io.TableReadListener)
	 */

	private String m_delim = "\t";
        
        public boolean ignoreSecond;

	public HCILFormatTableReader() {
            this(true);
	}

        public HCILFormatTableReader(boolean ignoreSecond) {
            this.ignoreSecond = ignoreSecond;
	}

	protected void read(InputStream is, TableReadListener trl) throws IOException, DataParseException {
		String line;
		int lineno = 0;

		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		while ((line = br.readLine()) != null) {
			// increment the line number
			++lineno;

			if (lineno == 2 && ignoreSecond) {
				continue;
			}

			// split on tab character
			String[] cols = line.split(m_delim);
			for (int i = 0; i < cols.length; ++i) {
				trl.readValue(lineno, i + 1, cols[i]);
				// System.out.print(arg0)
			}
		}
	}

}
