/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umd.cs.hcil.manynets.model;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Manuel Freire
 */
public class PopulationTest {

    public PopulationTest() {
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

    /**
     * Test of addGraph method, of class Population.
     */
    @Test
    public void testAddGraph() throws Exception {
        System.out.println("addGraph");
        
        Dataset ds = TestUtils.loadDataset("movies");

        Population instance = new Population("test");

        for (SchemaInstance si : ds.getInstances()) {
            instance.addGraph(new PGraph(si.getName(), si));
        }
    }

    /**
     * Test of removeGraph method, of class Population.
     */
    @Test
    public void testRemoveGraph() {
//        System.out.println("removeGraph");
//        PGraph pg = null;
//        Population instance = null;
//        instance.removeGraph(pg);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of getGraph method, of class Population.
     */
    @Test
    public void testGetGraph() {
//        System.out.println("getGraph");
//        String id = "";
//        Population instance = null;
//        PGraph expResult = null;
//        PGraph result = instance.getGraph(id);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of getGraphRow method, of class Population.
     */
    @Test
    public void testGetGraphRow() {
//        System.out.println("getGraphRow");
//        String id = "";
//        Population instance = null;
//        int expResult = 0;
//        int result = instance.getGraphRow(id);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of isDirected method, of class Population.
     */
    @Test
    public void testIsDirected() {
//        System.out.println("isDirected");
//        Population instance = null;
//        boolean expResult = false;
//        boolean result = instance.isDirected();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }
}