/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umd.cs.hcil.manynets;

import edu.umd.cs.hcil.manynets.model.Dataset;
import edu.umd.cs.hcil.manynets.model.TestUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import prefuse.data.Table;
import prefuse.data.util.TreeIndex;

/**
 *
 * @author Manuel Freire
 */
public class UtilsTest {

    private static Table stable = new Table();
    private static Dataset ds;

    public UtilsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        
        ds = TestUtils.loadDataset("movies");

        stable.addColumn("ID", Integer.TYPE);
        stable.addColumn("VALUE", String.class);
        for (int i=0; i<100; i++) {
            int r = stable.addRow();
            stable.set(r, "ID", i);
            stable.set(r, "VALUE", "" + i);
        }
        stable.index("ID");
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

    /**
     * Test of copyRow method, of class Utils.
     */
    @Test
    public void testCopyRow() {
//        System.out.println("copyRow");
//        Table src = null;
//        Table dest = null;
//        int row = 0;
//        Utils.copyRow(src, dest, row);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of createTableWithCols method, of class Utils.
     */
    @Test
    public void testCreateTableWithCols() {
//        System.out.println("createTableWithCols");
//        Table src = null;
//        Table expResult = null;
//        Table result = Utils.createTableWithCols(src);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of printTable method, of class Utils.
     */
    @Test
    public void testPrintTable() {
//        System.out.println("printTable");
//        Table t = null;
//        int maxRowsToPrint = 0;
//        Utils.printTable(t, maxRowsToPrint);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of getRowFor method, of class Utils.
     */
    @Test
    public void testGetRowFor() {
        System.out.println("getRowFor");
        String idField = "ID";
        int id = 8;
        Table t = stable;
        int expResult = 8;
        int result = Utils.getRowFor(idField, id, t);
        assertEquals(expResult, result);
    }

    /**
     * Test of getRowFor method, of class Utils.
     */
    @Test
    public void testGetRowFor2() {
        System.out.println("getRowFor2");
        String idField = "ID";
        int id = 8;

        System.err.println(ds.getInstances().get(0).getEntities().get(0).getName());
        Table t = ds.getInstances().get(0).getEntities().get(0).getTable();
        System.err.println(t.getColumnType("ID"));
        int expResult = 7;
        int result = Utils.getRowFor(idField, id, t);
        assertEquals(expResult, result);
    }

    /**
     * Test of copyTable method, of class Utils.
     */
    @Test
    public void testCopyTable() {
//        System.out.println("copyTable");
//        Table src = null;
//        Table expResult = null;
//        Table result = Utils.copyTable(src);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of deepCopyTable method, of class Utils.
     */
    @Test
    public void testDeepCopyTable() {
//        System.out.println("deepCopyTable");
//        Table src = null;
//        Duplicator d = null;
//        Table expResult = null;
//        Table result = Utils.deepCopyTable(src, d);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

}