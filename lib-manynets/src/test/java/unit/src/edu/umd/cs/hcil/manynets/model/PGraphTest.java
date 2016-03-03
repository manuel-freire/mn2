/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umd.cs.hcil.manynets.model;

import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Manuel Freire
 */
public class PGraphTest {

    private static Dataset ds1;
    private static Dataset ds2;

    public PGraphTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        ds1 = TestUtils.loadDataset("movies");
        ds2 = TestUtils.loadDataset("many");
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
     * Test of getNodeStats method, of class PGraph.
     */
    @Test
    public void testCreateFromInstance() {
        System.out.println("CreateFromInstance");
        PGraph pg = new PGraph("test", ds1.getInstances().get(0));
        assertEquals(2844, pg.getGraph().getNodeCount());
        assertEquals(30543, pg.getGraph().getEdgeCount());
    }


    /**
     * Test of getNodeStats method, of class PGraph.
     */
    @Test
    public void testCreateFromInstance1() {
        System.out.println("CreateFromInstance");
        PGraph pg = new PGraph("test", ds1.getInstances().get(1));
        assertEquals(3250, pg.getGraph().getNodeCount());
        assertEquals(29083, pg.getGraph().getEdgeCount());
    }

    /**
     * Test of getNodeStats method, of class PGraph.
     */
    @Test
    public void testCreateFromInstance2() {
        System.out.println("CreateFromInstance");
//        TestUtils.printTable(
//            ds2.getInstances().get(0).getMapping().get("caller").getTable()
//                );
        PGraph pg = new PGraph("test", ds2.getInstances().get(0));
        assertEquals(213, pg.getGraph().getNodeCount());
        assertEquals(193, pg.getGraph().getEdgeCount());
    }

    /**
     * Test of getNodeStats method, of class PGraph.
     */
    @Test
    public void testCreateFromInstance3() {
        System.out.println("CreateFromInstance");
        PGraph pg = new PGraph("test", ds2.getInstances().get(1));
        assertEquals(270, pg.getGraph().getNodeCount());
        assertEquals(312, pg.getGraph().getEdgeCount());
    }
//
//    /**
//     * Test of getEdgeStats method, of class PGraph.
//     */
//    @Test
//    public void testGetEdgeStats() {
//        System.out.println("getEdgeStats");
//        PGraph instance = null;
//        ArrayList expResult = null;
//        ArrayList result = instance.getEdgeStats();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}