/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umd.cs.hcil.manynets.model;

import edu.umd.cs.hcil.manynets.Utils;
import edu.umd.cs.hcil.manynets.model.TableWrapper.Level;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import prefuse.data.Table;

/**
 *
 * @author Manuel Freire
 */
public class TableWrapperTest {

    private static Table stable = new Table();

    public TableWrapperTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
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
     * Test of getStats method, of class TableWrapper.
     */
    @Test
    public void testGetStats_0args() {
//        System.out.println("getStats");
//        TableWrapper instance = null;
//        ArrayList expResult = null;
//        ArrayList result = instance.getStats();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of getStats method, of class TableWrapper.
     */
    @Test
    public void testGetStats_Table() {
//        System.out.println("getStats");
//        Table t = null;
//        ArrayList expResult = null;
//        ArrayList result = TableWrapper.getStats(t);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of getRow method, of class TableWrapper.
     */
    @Test
    public void testGetRow() {
        System.out.println("getTuple");
        int id = 1;
        TableWrapper instance = new TableWrapper("test", Utils.copyTable(stable),
                true, "ID", Level.Entity);
        int expResult = 1;
        int result = instance.getRow(id);
        assertEquals(expResult, result);
    }

    /**
     * Test of addStat method, of class TableWrapper.
     */
    @Test
    public void testAddStat() {
        System.out.println("addStat");
        int id = 1;
        TableWrapper instance = new TableWrapper("test", Utils.copyTable(stable),
                true, "ID", Level.Entity);

        Stat s1 = new Stat("TestStat", "A description", Integer.class,
                Level.Entity, Level.Entity);
        Stat s2 = new Stat("TestStat", "A description", Integer.class,
                Level.Entity, Level.Entity);
        assertEquals(2, instance.getStats().size());
        assertTrue(instance.addStat(s1));
        assertEquals(3, instance.getStats().size());
        assertFalse(instance.addStat(s2));
        assertEquals(3, instance.getStats().size());
        assertEquals(true, instance.getTable().getColumnNumber(s1.getName()) >= 0);
    }

    /**
     * Test of getTuple method, of class TableWrapper.
     */
    @Test
    public void testGetTuple() {
    }

    /**
     * Test of detach method, of class TableWrapper.
     */
    @Test
    public void testDetach() {
//        System.out.println("detach");
//        TableWrapper instance = null;
//        instance.detach();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }
}