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
import edu.umd.cs.hcil.manynets.engines.AvgDistancesCalculator;
import edu.umd.cs.hcil.manynets.engines.BetweennessCentralityCalculator;
import edu.umd.cs.hcil.manynets.engines.ComponentCalculator;
import edu.umd.cs.hcil.manynets.engines.ClusteringStatsCalculator;
import edu.umd.cs.hcil.manynets.engines.DiameterCalculator;
import edu.umd.cs.hcil.manynets.engines.EdgeDuplicateCalculator;
import edu.umd.cs.hcil.manynets.engines.SimpleStatsCalculator;
import edu.umd.cs.hcil.manynets.engines.VertexDegreeCalculator;
import edu.umd.cs.hcil.manynets.model.PGraph.EdgeTableWrapper;
import edu.umd.cs.hcil.manynets.model.PGraph.GraphElementWrapper;
import edu.umd.cs.hcil.manynets.model.PGraph.NodeTableWrapper;
import edu.umd.cs.hcil.manynets.model.Population.PopulationTableWrapper;
import edu.umd.cs.hcil.manynets.model.TableWrapper.Level;
import edu.umd.cs.hcil.manynets.hist.DefaultHistogramModel;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.Tuple;

/**
 * Creates Stats for graphs, nodes and e-r tables. Static methods only, being
 * a factory class.
 *
 * Stats are level-specific; some of them are 'aggregation stats', in the sense
 * that they require the results of a previous level to be available. Aggregation
 * stats will try to aggregate any stats already available in the stats list.
 *
 * @author Manuel Freire
 */
public class StatsFactory {

    /**
     * Main entry point. What stats are available for this tableWrapper at this
     * specific level? the level must be lower or equal thant the wrapper's level
     * Guaranteed to return all possible stats, including ones in current use.
     * @param dest destination wrapper
     * @param source wrapper (of the same or a lower level)
     * @return
     */
    public static Set<Stat> availableStats(TableWrapper from, TableWrapper to) {
        Set<Stat> stats = new TreeSet<Stat>();
        switch (from.getLevel()) {
            case Network:
                (new SimpleStatsCalculator()).addStatsTo(stats);
                if ( ! to.getLevel().equals(Level.Network)) {
                    throw new IllegalArgumentException("Expected Level.Graph");
                }
                (new ComponentCalculator()).addStatsTo(stats);
                (new DiameterCalculator()).addStatsTo(stats);
                break;
            case Node:
                switch (to.getLevel()) {
                    case Network: {
                        // basic node-to-graph stats - could go to prev level
                        (new ClusteringStatsCalculator()).addStatsTo(stats);
                        (new AvgDistancesCalculator()).addStatsTo(stats);
                        // FIXME: b-centrality is currently undirected
                        (new BetweennessCentralityCalculator(true)).addStatsTo(stats);
                        // aggregate existing node stats
                        addAggregateStats(stats, from, to, true);
                        break;

                    }
                    case Node: {
                        boolean directed = ((NodeTableWrapper)from)
                                .getPGraph().getGraph().isDirected();
                        // currently only basic vertex things
                        (new VertexDegreeCalculator(directed)).addStatsTo(stats);
                        break;
                    }
                    case Edge:
                        addNodeToEdgeColumns(stats, from, to);
                        break;
                }
                break;
            case Edge:
                switch (to.getLevel()) {
                    case Network:
                        (new EdgeDuplicateCalculator()).addStatsTo(stats);
                        // aggregate existing edge stats
                        addAggregateStats(stats, from, to, false);
                        break;
                    case Node:
                        // each edge-col produces both 'incoming' and 'outgoing'
                        addEdgeToNodeColumns(stats, from, to);
                        break;
                    case Edge:
                        // edge-to-edge: currently none
                        break;
                }
                break;
            case Entity:
                switch (to.getLevel()) {
                    case Network:
                        addAggregateStats(stats, from, to, true);
                        break;
                    case Node:
                        addERColumns(stats, from, to);
                        break;
                    case Edge:
                        addEntityToEdgeColumns(stats, from, to);
                        break;
                }
                break;
            case Relationship:
                switch (to.getLevel()) {
                    case Network:
                        addAggregateStats(stats, from, to, false);
                        break;
                    case Node:
                        addRelationshipToNodeColumns(stats, from, to);
                        break;
                    case Edge:
                        addERColumns(stats, from, to);
                        break;
                }
                break;
        }
        for (Stat s : to.getStats()) {
            if (s.getSourceLevel().equals(from.getLevel())) {
                stats.add(s);
            }
        }
        return stats;
    }

    public static Set<Stat> addGraphStats(Set<Stat> stats, Population pop) {
        (new SimpleStatsCalculator()).addStatsTo(stats);
        (new ClusteringStatsCalculator()).addStatsTo(stats);
        (new AvgDistancesCalculator()).addStatsTo(stats);
        return stats;
    }

    /**
     * Create 'stats' for all E/R columns to transcribe into the corresponding
     * Node/Edge (assumes Entity to Node, and Relationship to Edge).
     * @param stats
     * @param pop
     */
    public static Set<Stat> addERColumns(Set<Stat> stats,
            TableWrapper from, TableWrapper to) {
        Table fromTable = from.getTable();
        for (int i=0; i<fromTable.getColumnCount(); i++) {
            String name =fromTable.getColumnName(i);

            Stat ns = getERStat(name, from, to.getLevel());
            if (ns != null && ! stats.contains(ns)) {
                stats.add(ns);
            }
        }
        return stats;
    }

    /**
     * Creates a Stat (with calculator) to calculate an Entity-to-Node or
     * Relationship-To-Edge stat. This stat is reusable for any destination
     * node/edge tw.
     * @param name Col name in from Ent or Rel table
     * @param from
     * @param to
     * @return
     */
    public static Stat getERStat(String name, TableWrapper from, Level toLevel) {
        Stat ns = new Stat(from.getName() + ":" + name,
            from.getLevel().toString() + " column " + name,
            from.getTable().getColumnType(name), toLevel, from.getLevel());
        new ERCalculator(ns, name, "O(1)");
        return ns;
    }

    /**
     * Create an aggregate of the stats in one table for use in another table
     */
    public static Set<Stat> addAggregateStats(Set<Stat> stats,
            TableWrapper from, TableWrapper to, boolean nodes) {
        Table fromTable = from.getTable();
        for (int i=0; i<fromTable.getColumnCount(); i++) {
            String name = fromTable.getColumnName(i);

            Level fromLevel = from.getLevel();
            Stat ns = new Stat(fromLevel + ":" + name,
                    fromLevel.toString() + " column " + name,
                    Distribution.class, to.getLevel(), from.getLevel());
            if ( ! stats.contains(ns)) {
                Stat underStat = null;
                if (fromLevel.equals(Level.Relationship)) {
                    underStat = getERStat(name, from, Level.Edge);
                } else if (fromLevel.equals(Level.Entity)) {
                    underStat = getERStat(name, from, Level.Node);
                }
                String complexity = nodes ? "O(V)" : "O(E)";
                (new AggregateCalculator(
                        ns, name, complexity, nodes, underStat)).addStatsTo(stats);
            }
        }
        return stats;
    }

    /**
     * Add relationship columns to nodes; very similar to 'addEdgeToNode'...
     */
    public static Set<Stat> addRelationshipToNodeColumns(
            Set<Stat> stats, TableWrapper from, TableWrapper to) {
        PGraph pg = ((NodeTableWrapper)to).getPGraph();
        Table relTable = from.getTable();
        for (int i=0; i<relTable.getColumnCount(); i++) {
            String name = relTable.getColumnName(i);
            addRelationshipToNodeStats(stats, from, name);
        }
        return stats;
    }

    public static Set<Stat> addRelationshipToNodeStats(Set<Stat> stats,
            TableWrapper from, String name) {

        if (stats == null) {
            stats = new TreeSet<Stat>();
        }
        Stat ns[] = new Stat[] {
//                FIXME: requires other node in getParentRef to work...
//                new AnyEdgesStat("Total " + name, name + " in all edges",
//                        Distribution.class, Level.Node, Level.Edge),
            new IncomingEdgesStat(from.getName() + ": Incoming " + name, name
                    + " in all in-coming edges from rel. " + from.getName(),
                Distribution.class, Level.Node, Level.Edge),
            new OutgoingEdgesStat(from.getName() + ": Outgoing " + name, name
                    + " in all out-going edges from rel. " + from.getName(),
                Distribution.class, Level.Node, Level.Edge)
        };

        Stat underStat = getERStat(name, from, Level.Edge);
        if (underStat == null) {
            return null;
        }
        (new EdgeToNodeCalculator(ns, name, "O(E)", underStat))
                .addStatsTo(stats);
        return stats;
    }

    /**
     * Add node columns to edges - FIXME: currently not implmented
     */
    public static Set<Stat> addNodeToEdgeColumns(
            Set<Stat> stats, TableWrapper from, TableWrapper to) {
        return stats;
    }

    /**
     * Add entity columns to edges - FIXME: currently not implmented
     */
    public static Set<Stat> addEntityToEdgeColumns(
            Set<Stat> stats, TableWrapper from, TableWrapper to) {
        return stats;
    }

    public static Set<Stat> addEdgeToNodeColumns(
            Set<Stat> stats, TableWrapper from, TableWrapper to) {

        Table edgeTable = from.getTable();
        for (int i=0; i<edgeTable.getColumnCount(); i++) {
            String name = edgeTable.getColumnName(i);

            Stat ns[] = new Stat[] {
//                FIXME: requires other node in getParentRef to work...
//                new AnyEdgesStat("Total " + name, name + " in all edges",
//                        Distribution.class, Level.Node, Level.Edge),
                new IncomingEdgesStat("Incoming " + name, name
                        + " in all in-coming edges",
                    Distribution.class, Level.Node, Level.Edge),
                new OutgoingEdgesStat("Outgoing " + name, name
                        + " in all out-going edges",
                    Distribution.class, Level.Node, Level.Edge)
            };
            (new EdgeToNodeCalculator(ns, name, "O(E)", null)).addStatsTo(stats);
        }

        return stats;
    }

    public static class IncomingEdgesStat extends Stat {
        public IncomingEdgesStat(String n, String d, Class t, Level tl, Level sl) {
            super(n, d, t, tl, sl);
            presentations = new Presentation[] {
                presentations[0],
                new Presentation("source node") {
                    @Override
                    public Ref present(Ref r) {
                        int nid = ((EdgeRef)r).getTuple().getInt(PGraph.sourceField);
                        return ((PGraph)r.getSource()).getNodeTable().getRef(nid);
                    }
                }, new Presentation("source entity") {
                    @Override
                    public Ref present(Ref r) {
                        int nid = ((EdgeRef)r).getTuple().getInt(PGraph.sourceField);
                        Ref nr = ((PGraph)r.getSource()).getNodeTable().getRef(nid);
                        return ((NodeRef)nr).getEntityRef();
                    }
                },
            };
        }
        @Override
        public Ref getParentRef(Ref r) {
            int nid = ((EdgeRef)r).getTuple().getInt(PGraph.targetField);
            return ((PGraph)r.getSource()).getNodeTable().getRef(nid);
        }
    }
    public static class OutgoingEdgesStat extends Stat {
        public OutgoingEdgesStat(String n, String d, Class t, Level tl, Level sl) {
            super(n, d, t, tl, sl);
            presentations = new Presentation[] {
                presentations[0], 
                new Presentation("target node") {
                    @Override
                    public Ref present(Ref r) {
                        int nid = ((EdgeRef)r).getTuple().getInt(PGraph.targetField);
                        return ((PGraph)r.getSource()).getNodeTable().getRef(nid);
                    }
                },
                new Presentation("target entity") {
                    @Override
                    public Ref present(Ref r) {
                        int nid = ((EdgeRef)r).getTuple().getInt(PGraph.targetField);
                        Ref nr = ((PGraph)r.getSource()).getNodeTable().getRef(nid);
                        return ((NodeRef)nr).getEntityRef();
                    }
                },
            };
        }
        @Override
        public Ref getParentRef(Ref r) {
            int nid = ((EdgeRef)r).getTuple().getInt(PGraph.sourceField);
            return ((PGraph)r.getSource()).getNodeTable().getRef(nid);
        }
    }

    /**
     * From a lot of incoming or outgoing edges to their node.
     * Builds a distribution with the values, using either node-refs or
     * edge-refs, as required.
     * Level: Node
     */
    private static class EdgeToNodeCalculator extends AbstractCalculator {
        protected String source;
        protected Stat[] stats;
        protected Stat underStat;

        /**
         * @param stats - order MUST be in, out.       
         * @param complexity
         */
        private EdgeToNodeCalculator(Stat[] stats, String source, String complexity,
                Stat underStat) {
            super(stats, complexity);
            this.source = source;
            this.underStat = underStat;
            this.stats = stats;
        }

        @Override
        public void calculate(TableWrapper tw, int id) {
            checkAddStat(stats[0], tw);
            checkAddStat(stats[1], tw);

            Node n = ((NodeTableWrapper)tw).getNode(id);
            EdgeTableWrapper etw = ((NodeTableWrapper)tw)
                    .getPGraph().getEdgeTable();
            
            // Make sure underlying column will be found
            if (underStat != null) {
                source = underStat.getName();
                StatCalculator underCalc = underStat.getProvider();
                TableWrapper underTable = ((NodeTableWrapper)tw)
                        .getPGraph().getEdgeTable();
                //if (underTable.getStatCol(underStat) < 0) {
                    for (Iterator it = underTable.getTable().tuples(); it.hasNext(); /**/) {
                        Tuple t = (Tuple)it.next();
                        underCalc.calculate(underTable, t.getInt(PGraph.idField));
                    }
                //}
                underStat = null;
            }

            // colIndex now guaranteed to be valid
            int colIndex = etw.getTable().getColumnNumber(source);
            n.set(stats[0].getName(),
                DistributionFactory.build(etw, colIndex,
                    n.inEdges()));
            n.set(stats[1].getName(),
                DistributionFactory.build(etw, colIndex,
                    n.outEdges()));
        }
    }

    /**
     * From a column of an ER table to a same-named column in a N or E table.
     * Notice that the ER tables to use and the NE tables are graph-specific,
     * and should therefore be found at calculation-time, not initalization-time.
     * Level: Node or Edge.
     */
    private static class ERCalculator extends AbstractCalculator {
        protected String sourceCol;    // name of source col. in original tuple

        private ERCalculator(Stat stat, String sourceCol, String complexity) {
            super(new Stat[] { stat }, complexity);
            this.sourceCol = sourceCol;
        }

        @Override
        public void calculate(TableWrapper tw, int id) {
            checkAddStat(provides[0], tw);
            Tuple st = ((GraphElementWrapper)tw).getBaseTuple(id);
            // if graph is multimodal, some tuples may be missing columns
            if (st.getColumnIndex(sourceCol) >= 0) {
                tw.getTuple(id).set(provides[0].getName(), st.get(sourceCol));
            }
        }
    }

    /**
     * Aggregates a E/R/N/E column for an entire graph. In the case of E/R,
     * N/E is required to make aggregation possible.
     */
    private static class AggregateCalculator extends AbstractCalculator {
        protected String source;    // name of source col. in original tuple
        protected boolean nodes;
        protected Stat underStat;

        private AggregateCalculator(Stat stat, String source, String complexity,
                boolean nodes, Stat underStat) {
            super(new Stat[] { stat }, complexity);
            this.source = source;
            this.nodes = nodes;
            this.underStat = underStat;
        }

        @Override
        public void calculate(TableWrapper tw, int gid) {
            checkAddStat(provides[0], tw);

            PGraph pg = ((PopulationTableWrapper)tw).getGraph(gid);

            TableWrapper underTable = nodes ?
                pg.getNodeTable() : pg.getEdgeTable();
            // add under-stat if not there; subsumes a call to ERCalculator...
            if (underStat != null) {
                source = underStat.getName();
                StatCalculator underCalc = underStat.getProvider();
                for (Iterator it = underTable.getTable().tuples(); it.hasNext(); /**/) {
                    Tuple t = (Tuple)it.next();
                    underCalc.calculate(underTable, t.getInt(PGraph.idField));
                }
            }

            // Not all graphs will be able to calculate all aggregate columns...
            int colIndex = underTable.getTable().getColumnNumber(source);
            Distribution value = (colIndex >= 0) ?
                DistributionFactory.build(underTable, colIndex) :
                new DefaultHistogramModel();
            if (colIndex >= 0) {
                tw.getTuple(gid).set(provides[0].getName(), value);
            }
        }
    }
}
