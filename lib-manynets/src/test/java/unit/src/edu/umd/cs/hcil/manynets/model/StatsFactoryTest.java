/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umd.cs.hcil.manynets.model;

import edu.umd.cs.hcil.manynets.model.TableWrapper.Level;
import edu.umd.cs.hcil.manynets.util.MiscUtils;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import prefuse.util.collections.IntIterator;

/**
 *
 * @author Manuel Freire
 */
public class StatsFactoryTest {

    private static Dataset ds;
    private static Population pop;

    public StatsFactoryTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        ds = TestUtils.loadDataset("movies");
        pop = new Population("test");
        for (SchemaInstance si : ds.getInstances()) {
            pop.addGraph(new PGraph(si.getName(), si));
        }
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
     * Test of addVertexAndEdgeColumns method, of class StatsFactory.
     */
    @Test
    public void testAvailableStats() {
        System.err.println("availableStats");

        TableWrapper popWrapper = pop.getWrappedAttributes();
        ArrayList<TableWrapper> tws = popWrapper.getChildWrappers();
        tws.add(0, popWrapper);

        // list all combinations
        for (TableWrapper fw : tws) {
            for (TableWrapper tw : tws) {
                if (fw.getLevel().compareTo(tw.getLevel()) > 0) continue;
                if (tw.getLevel().equals(Level.Relationship) ||
                        tw.getLevel().equals(Level.Entity)) continue;

                System.err.print("from " + fw.getName() + " (" + fw.getLevel() + ") ");
                System.err.println("to " + tw.getName() + " (" + tw.getLevel() + "):");
                Set<Stat> cstats = StatsFactory.availableStats(fw, tw);
                for (Stat st : cstats) {
                    // some of these are expensive...
                    if (st.isExpensive()) continue;

                    long start = System.currentTimeMillis();
                    System.err.println("\tStat " + st);
                    for (IntIterator ri = tw.getTable().rows(); ri.hasNext(); /**/) {
                        int id = tw.getId(ri.nextInt());
                        if (st.getProvider() != null) {
                            st.getProvider().calculate(tw, id);
                        }
                    }
                    long end = System.currentTimeMillis();
                    System.err.println("\t - ended in " + ((end - start) / 1000.0) + " s");
                    System.err.println("\t - memory: " + MiscUtils.getMemUsage());
                }
            }
        }
    }

    /**
     * Test of addVertexAndEdgeColumns method, of class StatsFactory.
     */
    @Test
    public void testAddEdgeToVertexColumns() {
        addEntityAndRelationshipColumns();
        System.err.println("addEdgeToVertexStats");
        TreeSet<Stat> stats = new TreeSet<Stat>();
        //StatsFactory.addEdgeToVertexColumns(stats, pop);
        for (Stat st : stats) {
            if (st.getTargetLevel() == Level.Network) {
                System.err.println("Stat " + st.getName() + ": calculating");
                for (int i : pop.getGraphIds()) {
                    st.getProvider().calculate(pop.getWrappedAttributes(), i);
                }
                System.err.println("Stat " + st.getName() + " successfully calculated");
            }
        }
    }

    /**
     * Test of addEntityAndRelationshipColumns method, of class StatsFactory.
     */
    @Test
    public void addEntityAndRelationshipColumns() {
        System.err.println("addEntityAndRelationshipColumns");
        TreeSet<Stat> stats = new TreeSet<Stat>();
        //StatsFactory.addEntityAndRelationshipColumns(stats, pop);
        for (Stat st : stats) {
            if (st.getTargetLevel() == Level.Network) {
                System.err.println("Stat " + st.getName() + ": calculating");
                for (int i : pop.getGraphIds()) {
                    st.getProvider().calculate(pop.getWrappedAttributes(), i);
                }
                System.err.println("Stat " + st.getName() + " successfully calculated");
            }
        }
    }
}