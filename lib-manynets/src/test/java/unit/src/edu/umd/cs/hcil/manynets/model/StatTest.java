/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umd.cs.hcil.manynets.model;

import edu.umd.cs.hcil.manynets.model.TableWrapper.Level;
import edu.umd.cs.hcil.manynets.util.FileUtils;
import edu.umd.cs.hcil.manynets.util.FileUtils.HtmlToText;
import edu.umd.cs.hcil.socialaction.io.HCILFormatTableReader;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.io.CSVTableWriter;
import static org.junit.Assert.*;

/**
 *
 * @author Manuel Freire
 */
public class StatTest {

    public StatTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    
    // Used to fix Jen's tables as a one-off script
    // You *really* don't want to run this as an actual test...
    // Having warned you, just uncomment the next line
    // @Test
    public void testBuildBetterFTTables() throws Exception {
        FileUtils.HtmlToText ht = new HtmlToText();

        HCILFormatTableReader r = new HCILFormatTableReader();
        File d = new File("c:/work/sa/mn/data/movies");
        File fn = new File(d, "ft-f-nodes.tsv");
        File fnm = new File(d, "movieMapping.tdf");
        File fum = new File(d, "ft-pm-edges.tsv");
        File fm = new File(d, "movietitles.tdf");
        Table tn = r.readTable(fn);
        Table tum = r.readTable(fum);
        Table tnm = r.readTable(fnm);
        Table tm = r.readTable(fm);
        tn.index("ID");
        tn.removeColumn("TYPE");
        tn.addColumn("Title", String.class);
        tn.addColumn("RELEASE", String.class);
        tnm.index("TRUEID");
        tnm.index("ID");
        tm.index("TRUEID");
        TreeMap<String, Integer> titles = new TreeMap<String, Integer>();
        Pattern yearPattern = Pattern.compile("\\(([12][0-9][0-9][0-9])\\)");
        for (Iterator ti=tn.tuples(); ti.hasNext(); /**/) {
            Tuple f = (Tuple)ti.next();
            int id = f.getInt("ID");
            try {
                String trueId =
                        tnm.getString(tnm.index("ID").get(id), "TRUEID");
                String title =
                        tm.getString(tnm.index("TRUEID").get(trueId), "TITLE");
                title = title.replaceAll("\"", "");
                ht.parse(new StringReader(title));
                title = ht.getText();
                Matcher ym = yearPattern.matcher(title);
                int year = 1970;
                if (ym.find()) {
                    year = Integer.parseInt(ym.group(1));
                }
                System.err.println("" + f.get("ID") + " "
                        + trueId + " " + title + " " + year);

                f.set("Title", title.trim());
                f.set("RELEASE", "01-Jan-" + year);
                if ( ! titles.containsKey(title)) {
                    titles.put(title, f.getRow());
                }
            } catch (Exception e) {
                System.err.println("TRUEID not found for " + id);
                e.printStackTrace();
                f.set("Title", "unknown (????)");
                f.set("RELEASE", "31-Dec-1969");
                // bad boy - bogus date and title for you
                if ( ! titles.containsKey("unknown (????)")) {
                    titles.put("unknown (????)", f.getRow());
                }
            }
        }
        tn.index("Title");
        int removed = 0;
        boolean changes = true;
        while (changes) {
            changes = false;
            for (Iterator ti=tn.tuples(); ti.hasNext(); /**/) {
                Tuple f = (Tuple)ti.next();
                int id = f.getInt("ID");
                int r1 = f.getRow();
                int r2 = titles.get(f.getString("Title"));
                int id2 = tn.getInt(r2, "ID");
                if (r1 != r2) {
                    // only one can remain!
                    if (r1 > r2) {
                        System.err.println("removing row " + r1 + " as duplicate of " + r2);
                        removed ++;

                        ArrayList<Integer> rows = new ArrayList<Integer>();
                        for (Iterator tmi=tum.tuples(); tmi.hasNext(); /**/) {
                            Tuple c = (Tuple)tmi.next();
                            if (c.getInt("ID2") == id) {
                                rows.add(c.getRow());
                            }
                        }
                        for (int row : rows) {
                            tum.set(row, "ID2", id2);
                        }
                        tn.removeRow(r1);
                        changes = true;
                        break;
                    }
                }
            }
        }
        System.err.println("removed " + removed + " duplicate rows");

        CSVTableWriter w = new CSVTableWriter();
        w.writeTable(tn, new File(d, "ft-f-nodes.csv"));
        w.writeTable(tum, new File(d, "ft-uf-edges.csv"));
    }

    /**
     * Test of equals method, of class Stat.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Stat s1 = new Stat("test", "another test", Integer.TYPE,
                Level.Edge, Level.Edge);
        Stat s2 = new Stat("test", "another test", Integer.TYPE,
                Level.Edge, Level.Edge);        
        Stat s3 = new Stat("test", "another test", Integer.TYPE,
                Level.Network, Level.Edge);
        boolean eq12 = s1.equals(s2);
        boolean eq13 = s1.equals(s3);
        assertTrue(eq12);
        assertFalse(eq13);
    }

    @Test
    public void testEquals2() {
        System.out.println("equals2");
        TreeSet<Stat> ts = new TreeSet<Stat>();
        Stat s1 = new Stat("test", "another test", Integer.TYPE,
                Level.Edge, Level.Edge);
        Stat s2 = new Stat("test", "another test", Integer.TYPE,
                Level.Edge, Level.Edge);
        Stat s3 = new Stat("test", "another test", Integer.TYPE,
                Level.Network, Level.Edge);
        ts.add(s1);
        ts.add(s2);
        ts.add(s3);
        assertEquals(2, ts.size());
    }

    @Test
    public void testCompareComplexity() {
        System.out.println("compareComplexity");
        Stat s1 = new Stat("test", "another test", Integer.TYPE,
                Level.Edge, Level.Edge);
        Stat s2 = new Stat("test", "another test", Integer.TYPE,
                Level.Edge, Level.Edge);
        s1.setProvider(new MockStatCalculator("O(1)"));
        s2.setProvider(new MockStatCalculator("O(V)"));
        assertTrue(s1.compareTo(s2) > 0);
        s1.setProvider(new MockStatCalculator("O(V)"));
        s2.setProvider(new MockStatCalculator("O(E + V)"));
        assertTrue(s1.compareTo(s2) > 0);
        s1.setProvider(new MockStatCalculator("O(E)"));
        s2.setProvider(new MockStatCalculator("O(V^2)"));
        assertTrue(s1.compareTo(s2) > 0);
    }

    private static class MockStatCalculator implements StatCalculator {
        private String complexity;
        public MockStatCalculator(String complexity) {
            this.complexity = complexity;
        }

        @Override
        public String getComplexity() {
            return complexity;
        }

        @Override
        public void calculate(TableWrapper tw, int id) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void cancel() {
            return;
        }
    }
}