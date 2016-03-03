/*
 *  This file is part of ManyNets.
 *
 *  ManyNets is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as 
 *  published by the Free Software Foundation, either version 3 of the 
 *  License, or (at your option) any later version.
 *
 *  ManyNets is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with ManyNets.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  ManyNets was created at the Human Computer Interaction Lab, 
 *  University of Maryland at College Park. See the README file for details
 */

package edu.umd.cs.hcil.manynets.model;

import edu.umd.cs.hcil.manynets.engines.AbstractCalculator;
import edu.umd.cs.hcil.manynets.model.Dataset.DatasetListener;
import edu.umd.cs.hcil.manynets.model.PGraph.NodeTableWrapper;
import edu.umd.cs.hcil.manynets.model.SchemaInstance.EntityTableWrapper;
import edu.umd.cs.hcil.manynets.model.SchemaInstance.RelTableWrapper;
import edu.umd.cs.hcil.manynets.model.TableWrapper.Level;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import prefuse.data.Tuple;
import prefuse.util.collections.IntIterator;

/**
 * Can load a dataset
 * 
 * @author Manuel Freire
 */
public class DatasetLoader {

    private Dataset dataset;
    private Population pop;

    // used only for two-part loading (good for adding a progress listener)
    private File descriptor;
    private boolean entTables;
    private boolean relTables;
    private boolean graphPerRel; 
    private boolean fullGraph;
    private boolean defaultStats;
    private HashSet<String> instances;

    // default monitor does not echo anything
    private StatCalculationMonitor monitor = new StatCalculationMonitor() {
        @Override
        public void notify(String statName, int row) {}
        @Override
        public boolean isCancelled() { return false; }
    };

    public Dataset getDataset() {
        return dataset;
    }

    public void setStatMonitor(StatCalculationMonitor monitor) {
        this.monitor = monitor;
    }

    public interface StatCalculationMonitor {
        public void notify(String statName, int row);
        public boolean isCancelled();
    }

    /**
     * Default population stats:
     * number of nodes and edges, ratio thereof
     * incoming, outgoing edge distributions
     * components and sizes thereof
     * @param pop
     */
    public void addDefaultStats(Population pop) {
        if (pop.getGraphIds().size() == 0) return;
        
        PGraph pg = pop.getGraph(pop.getGraphIds().iterator().next());

        // inexpensive graph stats (does not include node degrees, i'm afraid)
        for (Stat s : StatsFactory.availableStats(pop.getWrappedAttributes(),
                pop.getWrappedAttributes())) {
            if ( ! s.isExpensive() && s.getProvider() != null) {
                calculateStat(s, pop.getWrappedAttributes());
            } else {
                System.err.println("Discarded calculating " + s + ": way too expensive");
            }
        }

        // edge stats
        for (Stat s : StatsFactory.availableStats(
                pg.getEdgeTables().get(0), pop.getWrappedAttributes())) {
            if (s.getProvider() != null) {
                calculateStat(s, pop.getWrappedAttributes());
            }
        }

        // node stats
        for (Stat s : StatsFactory.availableStats(
                pg.getEdgeTables().get(0), pop.getWrappedAttributes())) {
            if (s.getProvider() != null) {
                calculateStat(s, pop.getWrappedAttributes());
            }
        }

        // node degrees for each node table
        for (PGraph pg2 : pop.getGraphs()) {
            for (Stat s : StatsFactory.availableStats(
                    pg2.getNodeTable(),
                    pg2.getNodeTable())) {
                if (s.getProvider() != null) {
                    calculateStat(s, pg2.getNodeTable());
                }
            }
        }
        for (Stat s : StatsFactory.availableStats(
                pg.getNodeTable(), pop.getWrappedAttributes())) {
            if ( ! s.isExpensive() && s.getProvider() != null) {
                calculateStat(s, pop.getWrappedAttributes());
            }
        }
    }

    /**
     * FIXME --
     * Pending a better interface...
     */
    public void addJenStats(Population pop) {
        // Get node tables
        PGraph uug = pop.getGraph(0);
        PGraph ufg = pop.getGraph(1);
        if (uug.getNodeTables().size() > 1) {
            PGraph aux = uug;
            uug = ufg;
            ufg = aux;
        }

        Stat stat;
        TableWrapper tw;

        // Add node stats for user-user-trust, user-film-rating
        System.err.println("adding Trust");
        tw = uug.getEdgeTables().get(0);
        stat = StatsFactory.addRelationshipToNodeStats(null, tw, "TRUST")
                .iterator().next();
        calculateStat(stat, uug.getNodeTable());
        System.err.println("adding Rating");
        tw = ufg.getEdgeTables().get(0);
        stat = StatsFactory.addRelationshipToNodeStats(null, tw, "RATING")
                .iterator().next();
        calculateStat(stat, ufg.getNodeTable());

        // Cross-ref into each node in uug
        System.err.println("cross-pollinating");
        stat = createNodeColCopyStat(ufg.getNodeTable(), uug.getNodeTable(), stat);
        calculateStat(stat, uug.getNodeTable());
    }

    /**
     * FIXME --
     * Adds default stats for Many dataset
     */
    public void addManyStats(Population pop) {
        PGraph pg = pop.getGraph(pop.getGraphIds().iterator().next());
        for (Stat s : StatsFactory.availableStats(
                pg.getEdgeTables().get(0), pop.getWrappedAttributes())) {
            calculateStat(s, pop.getWrappedAttributes());
        }
    }

    public static Stat createNodeColCopyStat(
            NodeTableWrapper from, NodeTableWrapper to, final Stat stat) {
        NodeColCopyStat ns = new NodeColCopyStat(stat, from, to);
        new ColToColStatCalculator(ns, from, "O(V)", stat.getName());
        return ns;
    }

    private static class NodeColCopyStat extends Stat {
        private Stat original;
        private NodeTableWrapper currentWrapper;
        public NodeColCopyStat(Stat original, NodeTableWrapper ow, NodeTableWrapper dest) {
            super(ow.getName() + ":" + original.getName(),
                "Node column " + original.getName() + " from " + ow.getPGraph().getName(),
                original.getType(), original.getTargetLevel(), original.getSourceLevel());
            this.original = original;
            this.currentWrapper = dest;
            presentations = original.getPresentations();
        }
        public Ref toCurrent(Ref r) {
            int cid = currentWrapper.getPGraph().getNodeId(
                    ((NodeRef)r).getEntityRef());
            return currentWrapper.getRef(cid);
        }
        @Override
        public Ref getParentRef(Ref r) {
            return toCurrent(original.getParentRef(r));
        }
    }

    private static class ColToColStatCalculator extends AbstractCalculator {
        private NodeTableWrapper otherNodeTable;
        private String source;
        public ColToColStatCalculator(NodeColCopyStat stat, NodeTableWrapper otherNodeTable,
                String complexity, String source) {
            super(new Stat[] { stat }, complexity);
            this.otherNodeTable = otherNodeTable;
            this.source = source;
        }
        public void calculate(TableWrapper ntw, int id) {
            checkAddStat(provides[0], ntw);
            NodeRef nr = (NodeRef)ntw.getRef(id);
            int oid = otherNodeTable.getPGraph().getNodeId(nr.getEntityRef());
            Tuple ot = otherNodeTable.getTuple(oid);
            nr.getTuple().set(provides[0].getName(), ot.get(source));
        }
    }

    public void calculateStat(Stat s, TableWrapper tw) {
        try {
            IntIterator ri = tw.getTable().rows();
            while (ri.hasNext() && ! monitor.isCancelled()) {
                int id = tw.getId(ri.nextInt());
                monitor.notify(s.getName() + " for " + tw.getName(), id);
                s.getProvider().calculate(tw, id);
            }
        } catch (Exception e) {
            System.err.println("WARNING: Cannot calculate stat " + s + ":");
            e.printStackTrace();
        }
    }

    private static void addStats(Population pop) {

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
                    int lastId = -1;
                    try {
                        for (IntIterator ri = tw.getTable().rows(); ri.hasNext(); /**/) {
                            lastId = tw.getId(ri.nextInt());
                            st.getProvider().calculate(tw, lastId);
                        }
                    } catch (Exception e) {
                        System.err.println("+++++ ERROR calculating" +
                                " " + st + " for ID " + lastId + " in " + tw);
                        e.printStackTrace();
                    }
                    long end = System.currentTimeMillis();
                    System.err.println("\t - ended in " + ((end - start) / 1000.0) + " s");
//                    System.err.println("\t - memory: " + MiscUtils.getMemUsage());
                }
            }
        }
    }

    /**
     * Returns a .xml descriptor for a dataset. If 'f' is already a xml file, 
     * 'f' is returned. If it is not, but it is a directory that contains an xml
     * file, that xml file is returned. If requireUnique is specified and the 
     * xml file is not unique within that directory, or in any other error 
     * null will be returned.
     * @param f
     * @param requireUniqueXml
     * @return
     */
    public static File fileToDescriptor(File f, boolean requireUniqueXml) {
        if (f.isFile() && f.canRead() &&
                f.getName().toLowerCase().endsWith(".xml")) return f;

        if (f.isDirectory()) {
            ArrayList<File> ds = getDescriptors(f);
            if (ds.size() == 1 || ((ds.size() > 1) && ! requireUniqueXml)) {
                return ds.get(0);
            }
        }

        return null;
    }

    public static ArrayList<File> getDescriptors(File d) {
        ArrayList<File> candidates = new ArrayList<File>();
        for (File f : d.listFiles()) {
            try {
                if (f.getName().toLowerCase().endsWith(".xml") &&
                        Dataset.readInstanceNames(f).size() > 0) {
                    candidates.add(f);
                }
            } catch (Exception e) {
                System.err.println("Discarded " + f + ": readInstances failed");
            }
        }
        return candidates;
    }

    /**
     * Hook to allow different types of components to be used to display TWs
     * @param tw
     * @param name
     * @param substitute
     */
    public void openTableComponent(TableWrapper tw, String name) {
        
    }

    /**
     * Hook to allow different types of components to be used to display pops
     * @param tw
     * @param name
     * @param substitute
     */
    public void openTableComponent(Population pop, String name) {
        
    }

    /**
     * Simplified version of loading
     * @param descriptor - uses defaults for loading dataset from descriptor
     * @throws Exception
     */
    public void load(final File descriptor) throws Exception {
        load(descriptor, true, true, true, true);
    }

    /**
     * Simplified version of loading
     * @param descriptor - uses less defaults for loading dataset from descriptor
     * @throws Exception
     */
    public void load(final File descriptor,
            boolean entTables, boolean relTables,
            boolean graphPerRel, boolean fullGraph) throws Exception {
        load(descriptor, entTables, relTables, graphPerRel, fullGraph, null);
    }

    /**
     * Complete version of loading
     * @param descriptor
     * @throws Exception
     */
    public void load(final File descriptor,
            boolean entTables, boolean relTables,
            boolean graphPerRel, boolean fullGraph, HashSet<String> instances) throws Exception {

        prepareLoad(descriptor, entTables, relTables, graphPerRel, fullGraph,
                true, instances);
        doLoad(new Dataset.StderrDatasetListener());
        doDisplay();
    }

    public void prepareLoad(final File descriptor,
            boolean entTables, boolean relTables,
            boolean graphPerRel, boolean fullGraph, boolean defaultStats,
            HashSet<String> instances) {
        this.descriptor = descriptor;
        this.entTables = entTables;
        this.relTables = relTables;
        this.graphPerRel = graphPerRel;
        this.fullGraph = fullGraph;
        this.instances = instances;
        this.defaultStats = defaultStats;
    }

    public void doDisplay() {
        if (pop == null) {
            throw new IllegalStateException("doLoad() should have been called before this");
        }

        int nInstance = 0;
        for (SchemaInstance si : dataset.getInstances()) {
            nInstance ++;

            for (EntityTableWrapper tw : si.getEntities()) {
                if (entTables) {
                    openTableComponent(tw, si.getName() + ":" + tw.getName());
                }
            }
            for (RelTableWrapper tw : si.getRelationships()) {
                if (relTables) {
                    openTableComponent(tw, si.getName() + ":" + tw.getName());
                }
            }
        }

        // Default set of stats
        if (pop.getGraphs().size() > 0) {
            openTableComponent(pop, dataset.getName() + ": graphs");
        }
    }

    public void doLoad(DatasetListener dsl) throws Exception {

        System.err.println("Loading data from "
                + descriptor.getAbsolutePath() + " ...");
        File f = descriptor;
        System.err.println(f.getCanonicalPath());
        dataset = new Dataset();
        dataset.load(f, instances, dsl);

        int nInstance = 0;
        pop = new Population(dataset.getName());

        for (SchemaInstance si : dataset.getInstances()) {
            nInstance ++;
            for (RelTableWrapper tw : si.getRelationships()) {
                if (graphPerRel) {
                    String graphName = si.getName() + " " + tw.getName();
                    try {
                        PGraph pg = new PGraph(graphName, tw);
                        pop.addGraph(pg);
                    } catch (Exception e) {
                        monitor.notify("WARNING: Error building graph " +
                                graphName + ": eliding at instance ", nInstance);
                        System.err.println("WARNING: Error building graph " + graphName + ": eliding");
                        e.printStackTrace();
                    }
                }
            }

            if (fullGraph) {
                String graphName = si.getName() + ": full";
                try {
                    PGraph pg = new PGraph(graphName, si);
                    pop.addGraph(pg);
                } catch (Exception e) {
                    monitor.notify("WARNING: Error building graph " +
                            graphName + ": eliding at instance ", nInstance);
                    System.err.println("WARNING: Error building graph " + graphName + ": eliding");
                    e.printStackTrace();
                }
            }
        }

        // UGLY
        if (defaultStats) {
            if (dataset.getName().toLowerCase().contains("filmtrust")) {
                addJenStats(pop);
            } else if (dataset.getName().toLowerCase().contains("vast 2008")) {
                addManyStats(pop);
            } else {
                addDefaultStats(pop);
            }
        }
    }
}
