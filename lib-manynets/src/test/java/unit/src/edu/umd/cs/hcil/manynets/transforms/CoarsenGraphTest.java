/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umd.cs.hcil.manynets.transforms;

import edu.umd.cs.hcil.manynets.model.GraphCoarsener;
import edu.umd.cs.hcil.manynets.model.Dataset;
import edu.umd.cs.hcil.manynets.model.PGraph;
import edu.umd.cs.hcil.manynets.model.TestUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import prefuse.data.Graph;

/**
 *
 * @author mfreire
 */
public class CoarsenGraphTest {

    static Graph big;

    public CoarsenGraphTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        Dataset ds =
                TestUtils.loadDataset("movies", TestUtils.createSet("FilmTrust"), null);
        PGraph pg = new PGraph("test", ds.getInstances().get(0));
        big = pg.getGraph();
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
     * Test of coarsen method, of class GraphCoarsener.
     */
    @Test
    public void testCoarsen() {
        System.out.println("coarsen");

        Graph g = big;
        int nodeLimit = 1000;
        int edgeLimit = 5000;
        Graph result = GraphCoarsener.coarsen(g, nodeLimit, edgeLimit);
    }
}