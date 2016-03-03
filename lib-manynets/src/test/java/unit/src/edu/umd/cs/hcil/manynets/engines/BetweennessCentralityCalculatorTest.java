/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umd.cs.hcil.manynets.engines;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import prefuse.data.Graph;
import prefuse.data.Node;

/**
 *
 * @author mfreire
 */
public class BetweennessCentralityCalculatorTest {

    public BetweennessCentralityCalculatorTest() {
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

    private Graph createRandomConnectedGraph(Random r, int nodes, int edges) {
        Graph g = new Graph(false);

        if (edges < nodes-1) {
            throw new IllegalArgumentException(
                    "need edges >= nodes-1");
        }
        if (edges > ((nodes-1) * nodes) / 2) {
            throw new IllegalArgumentException(
                    "too many edges, would result in many duplicates");
        }

        // create nodes
        Node[] n = new Node[nodes];
        for (int i=0; i<nodes; i++) {
            n[i] = g.addNode();
        }

        // create connected component
        for (int i=1; i<nodes; i++) {
            Node connected = n[r.nextInt(i)];
            g.addEdge(connected, n[i]);
            edges --;
        }

        // add remaining edges; duplicates allowed
        while (edges > 0) {
            int s = r.nextInt(nodes);
            int t = r.nextInt(nodes-1);
            if (t == s) {
                t = nodes-1;
            }
            g.addEdge(n[s], n[t]);
            edges --;
        }

        return g;
    }

    private Map<Node, Double> parseNodeDoubleMap(Graph g, String s, String s1, String s2) {
        Map<Node, Double> r = new HashMap<Node, Double>();
        String[] entries = s.split(s1);
        for (String e : entries) {
            String[] kv = e.split(s2);
            r.put(g.getNode(Integer.parseInt(kv[0])), Double.parseDouble(kv[1]));
        }
        return r;
    }

    /**
     * Test of computeBetweenness method, of class BetweennessCentralityCalculator.
     */
    @Test
    public void testComputeBetweenness() {
        System.out.println("computeBetweenness");
        BetweennessCentralityCalculator instance 
                = new BetweennessCentralityCalculator(true);
        Graph graph = null;
        Map<Node, Double> result = null;
        Map<Node, Double> expected = null;

        graph = createRandomConnectedGraph(new Random(42), 10, 15);
        result = instance.computeBetweenness(graph);
        expected = parseNodeDoubleMap(graph,
            "4: 0.0|1: 2.5|7: 4.0|5: 3.5|2: 3.0|" +
            "3: 0.0|0: 0.0|9: 4.0|8: 0.0|6: 0.0", "[|]", ": ");
        assertEquals(expected, result);
                
        // uncomment to generate testcases
//        for (Entry<Node, Double> e : result.entrySet()) {
//            System.err.print(e.getKey().getRow() + ": " + e.getValue() + "|");
//        }
//        System.err.println();
        
    }
}