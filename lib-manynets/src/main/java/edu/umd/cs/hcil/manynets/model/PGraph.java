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

import edu.umd.cs.hcil.manynets.model.Population.GraphRef;
import edu.umd.cs.hcil.manynets.model.Population.PopulationTableWrapper;
import edu.umd.cs.hcil.manynets.model.Schema.Entity;
import edu.umd.cs.hcil.manynets.model.Schema.Relationship;
import edu.umd.cs.hcil.manynets.model.SchemaInstance.RelTableWrapper;
import edu.umd.cs.hcil.manynets.model.SchemaInstance.EntityTableWrapper;
import java.util.ArrayList;
import java.util.TreeSet;
import javax.swing.ImageIcon;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.util.collections.IntIterator;

/**
 * A view of an instantiated E-R schema.
 *
 * @author Manuel Freire
 */
public class PGraph {

    // constant used for node and edge IDs in the graph
    public static final String idField = "ID";
    // constant used to reference prior IDs in the graph (if not same as ID)
    public static final String originalIdField = "OID";
    // constant used to reference renumbered edge sources
    public static final String sourceField = "ID1";
    // constant used to reference renumbered edge targets
    public static final String targetField = "ID2";
    // used to reference actual source of nodes and targets (if confusion possible)
    public static final String sourceTableField = "Source";
    // used to assign node labels (taken from the Entity labelCol field)
    public static final String labelField = "Label";
    // the name that identifies this graph
    private String name;
    // the data and schema for this pgraph
    private SchemaInstance si;
    // node tables used in this view
    private ArrayList<EntityTableWrapper> nodeTables =
            new ArrayList<EntityTableWrapper>();
    // node tables used for source-to-target edges
    private ArrayList<RelTableWrapper> edgeTables =
            new ArrayList<RelTableWrapper>();
    // table wrapper for nodes (produces noderefs)
    private NodeTableWrapper nodeTableWrapper;
    // table wrapper for edges (produces edgerefs)
    private EdgeTableWrapper edgeTableWrapper;
    // graph node IDs to source EntityRefs
    private DoubleMap<Integer, Ref<EntityTableWrapper>> nodeMapping = new DoubleMap<Integer, Ref<EntityTableWrapper>>();
    // graph edge IDs to source RelRefs
    private DoubleMap<Integer, Ref<RelTableWrapper>> edgeMapping = new DoubleMap<Integer, Ref<RelTableWrapper>>();
    // reference to population that this graph is attached to (needed for refs)
    private GraphRef ref;
    /**
     * The graph. Should be directed if any of the edgeTables is directed
     * Prefuse does not allow mixing directed and undirected edges; duplicate
     * edges can be used to convert an undirected into a directed graph...
     */
    private Graph graph;

    /**
     * Build a derived PGraph, using the nodes and edges from the passed-in
     * graph directly. Notice that no recalculation is performed.
     * @param id
     * @param g
     */
    public PGraph(String name, PGraph parent, Graph g) {
        this.name = name;
        this.si = parent.si;
        this.edgeMapping = parent.edgeMapping;
        this.nodeMapping = parent.nodeMapping;
        this.nodeTables = parent.nodeTables;
        this.edgeTables = parent.edgeTables;
        edgeTableWrapper = new EdgeTableWrapper(g.getEdgeTable());
        nodeTableWrapper = new NodeTableWrapper(g.getNodeTable());
        this.graph = g;
    }

    /**
     * General-case constructor. Create mappings for a series of node tables and
     * edge tables, and combine everything into a named graph.
     * @param name
     * @param nodeSources
     * @param nodeTargets
     * @param edgeTables
     */
    public PGraph(String name, SchemaInstance si) {
        this.name = name;
        this.nodeTables.addAll(si.getEntities());
        this.edgeTables.addAll(si.getRelationships());
        this.si = si;

        assignNodeMappings();
        boolean directed = assignEdgeMappings();
        graph = buildGraph(directed, false);
    }

    /**
     * Create mappings from a single relationship, and whatever its entities are
     * and combines everything into a named graph.
     * @param name
     * @param nodeSources
     * @param nodeTargets
     * @param edgeTables
     */
    public PGraph(String name, RelTableWrapper rel) {
        this.name = name;
        this.nodeTables.add(rel.getSourceTW());
        if (rel.getSourceTW() != rel.getTargetTW()) {
            this.nodeTables.add(rel.getTargetTW());
        }
        this.edgeTables.add(rel);
        this.si = rel.getSchemaInstance();

        assignNodeMappings();
        boolean directed = assignEdgeMappings();
        graph = buildGraph(directed, false);
    }

    /**
     * Create mappings from a set of relationships, and whatever their entities
     * are, and combines everything into a named graph.
     * @param name
     * @param edgeTables
     */
    public PGraph(String name, ArrayList<RelTableWrapper> rels, boolean elideSingle) {
        this.name = name;
        for (RelTableWrapper rel : rels) {
            this.edgeTables.add(rel);
            if (this.si == null) {
                this.si = rel.getSchemaInstance();
            }
            this.nodeTables.add(rel.getSourceTW());
            if (rel.getSourceTW() != rel.getTargetTW()) {
                this.nodeTables.add(rel.getTargetTW());
            }
        }

        assignNodeMappings();
        boolean directed = assignEdgeMappings();
        graph = buildGraph(directed, elideSingle);
    }

    /**
     * Assigns mappings to all node tables included in this view. Node IDs will
     * be assigned from 0 to total-1, inclusive... unless there is only one node table,
     * in which case original IDs will be preserved. Similar to assignEdgeMappings...
     * @param pg
     */
    private void assignNodeMappings() {

        // start assigning new IDs to every node
        nodeMapping.clear();
        int assignedIds = 1;
        if (nodeTables.size() > 1) {
            for (EntityTableWrapper ntw : nodeTables) {
                Table t = ntw.getTable();
                int idColumn = t.getColumnNumber(ntw.getIdField());
                for (IntIterator ri = t.rows(); ri.hasNext(); /**/) {
                    int i = ri.nextInt();
                    int nid = t.getInt(i, idColumn);
                    Ref<EntityTableWrapper> er = ntw.getRef(nid);
                    if (!nodeMapping.containsValue(er)) {
                        nodeMapping.put(assignedIds++, er);
                    }
                }
            }
        } else {
            EntityTableWrapper ntw = nodeTables.get(0);
            Table t = ntw.getTable();
            int idColumn = t.getColumnNumber(ntw.getIdField());
            for (IntIterator ri = t.rows(); ri.hasNext(); /**/) {
                int i = ri.nextInt();
                int nid = t.getInt(i, idColumn);
                Ref<EntityTableWrapper> er = ntw.getRef(nid);
                if (!nodeMapping.containsValue(er)) {
                    nodeMapping.put(nid, er);
                    assignedIds = Math.max(assignedIds, nid);
                }
            }
        }
    }

    /**
     * Assigns the IDs to the next generation of edges; if many relationships
     * are to be merged, then overlaps are avoided by renumbering;
     * Source and OID fields will be added to preserve the old mappings.
     * Otherwise original ordering is preserved.
     * @return
     */
    private boolean assignEdgeMappings() {

        boolean directed = false;

        // start assigning new IDs to every edge
        edgeMapping.clear();
        int assignedIds = 1;
        if (edgeTables.size() > 1) {
            for (RelTableWrapper etw : edgeTables) {
                // a single directed edge table forces a fully directed graph
                // since both can not be mixed in Prefuse
                directed |= etw.isDirected();
                Table t = etw.getTable();
                int idColumn = t.getColumnNumber(etw.getIdField());
                for (IntIterator ri = t.rows(); ri.hasNext(); /**/) {
                    int i = ri.nextInt();
                    int eid = t.getInt(i, idColumn);
                    Ref<RelTableWrapper> er = etw.getRef(eid);
                    if (!edgeMapping.containsValue(er)) {
                        edgeMapping.put(assignedIds++, er);
                    }
                }
            }
        } else if (edgeTables.size() == 1) {
            RelTableWrapper rtw = edgeTables.get(0);
            Table t = rtw.getTable();
            int idColumn = t.getColumnNumber(rtw.getIdField());
            for (IntIterator ri = t.rows(); ri.hasNext(); /**/) {
                int i = ri.nextInt();
                int eid = t.getInt(i, idColumn);
                Ref<RelTableWrapper> rr = rtw.getRef(eid);
                if (!edgeMapping.containsValue(rr)) {
                    edgeMapping.put(eid, rr);
                    assignedIds = Math.max(assignedIds, eid);
                }
            }
        } else {
            throw new IllegalArgumentException("No edges to build graph with, therefore no graph");
        }
        return directed;
    }

    public Population getPopulation() {
        return ((PopulationTableWrapper) ref.getTableWrapper()).getPopulation();
    }

    private Graph buildGraph(boolean directed, boolean elideSingleNodes) {

        // create node & edge wrappers
        edgeTableWrapper = new EdgeTableWrapper();
        nodeTableWrapper = new NodeTableWrapper();

        // add all edges, keeping track of known nodes
        Table et = edgeTableWrapper.getTable();

        TreeSet<Integer> knownNodes = new TreeSet<Integer>();
        for (RelTableWrapper etw : edgeTables) {
            if (directed && !etw.isDirected()) {
                System.err.println(
                        "Warning: adding undirected edges (table "
                        + etw.getName() + ") to a directed graph");
            }
            Table t = etw.getTable();
            for (IntIterator ri = t.rows(); ri.hasNext(); /**/) {
                int i = ri.nextInt();
                int rid = t.getInt(i, etw.getIdField());
                int eid = edgeMapping.getKey(etw.getRef(rid));

                int sid, tid;
                try {
                    sid = nodeMapping.getKey(etw.getSourceTW().getRef(
                            t.getInt(i, etw.getSourceField())));
                    if (elideSingleNodes) {
                        knownNodes.add(sid);
                    }
                    tid = nodeMapping.getKey(etw.getTargetTW().getRef(
                            t.getInt(i, etw.getTargetField())));
                    if (elideSingleNodes) {
                        knownNodes.add(tid);
                    }
                } catch (Exception e) {
                    String ns = nodeMapping.getKey(etw.getSourceTW().getRef(
                            t.getInt(i, etw.getSourceField()))) != null
                            ? "(OK)" : "(NULL)";
                    String nt = nodeMapping.getKey(etw.getSourceTW().getRef(
                            t.getInt(i, etw.getSourceField()))) != null
                            ? "(OK)" : "(NULL)";
                    System.err.println("at " + etw.getName() + " looking for edge from "
                            + t.getInt(i, etw.getSourceField()) + ns + " to "
                            + t.getInt(i, etw.getTargetField()) + nt);
                    for (Ref<EntityTableWrapper> dr : nodeMapping.values()) {
                        System.err.println("source key found: "
                                + dr.getId() + " -> " + nodeMapping.getKey(dr));
                    }
                    for (Ref<EntityTableWrapper> dr : nodeMapping.values()) {
                        System.err.println("target key found: "
                                + dr.getId() + " -> " + nodeMapping.getKey(dr));
                    }
                    throw new IllegalArgumentException(e);
                }

                int er = et.addRow();
                et.setInt(er, idField, eid);
//                System.err.println("eid " +
//                        eid +
//                        "assigned for rid " +
//                        rid);
                et.setInt(er, sourceField, sid);
                et.setInt(er, targetField, tid);
                if (edgeTables.size() > 1) {
                    et.setInt(er, originalIdField, rid);
                    et.setString(er, sourceTableField, etw.getName());
                }
            }
        }

        // add all nodes
        Table nt = nodeTableWrapper.getTable();
        boolean severalNodeSources = nodeTables.size() > 1;
        for (int i : (elideSingleNodes ? knownNodes : nodeMapping.keySet())) {
            Ref<EntityTableWrapper> er = nodeMapping.get(i);
            EntityTableWrapper ntw = er.getTableWrapper();

            int nr = nt.addRow();
            nt.setInt(nr, idField, i);
            nt.setString(nr, labelField,
                    er.getTuple().getString(ntw.getLabelField()));
            if (severalNodeSources) {
                nt.setInt(nr, originalIdField,
                        er.getTuple().getInt(ntw.getIdField()));
                nt.setString(nr, sourceTableField, ntw.getName());
            }
        }

        return new Graph(nt, et, directed, idField, sourceField, targetField);
    }

    public Schema getSchema() {
        return si.getSchema();
    }

    public String getName() {
        return name;
    }

    public Graph getGraph() {
        return graph;
    }

    public String getNodeLabelField() {
        return nodeTables.get(0).getLabelField();
    }

    private void complain(String message, Ref r) {
        System.err.println("Complaining about '" + message + "' regarding ref "
                + r.getId() + " on " + r.getTableWrapper().getName());
        System.err.println(r.getTableWrapper().getRow(r.getId()));
        StringBuilder sb = new StringBuilder();
        Tuple t = r.getTuple();
        for (int i = 0; i < t.getColumnCount(); i++) {
            sb.append("|" + t.get(i));
        }
        System.err.println(message + sb);
    }

    public Ref<EntityTableWrapper> getEntityRef(int nodeId) {
//        if ( ! nodeMapping.containsKey(nodeId))
//            complain("No entity for ", nodeTableWrapper.getRef(nodeId));

        return nodeMapping.get(nodeId);
    }

    public Ref<RelTableWrapper> getRelRef(int edgeId) {
//        if ( ! edgeMapping.containsKey(edgeId))
//            complain("No rel for ", edgeTableWrapper.getRef(edgeId));

        return edgeMapping.get(edgeId);
    }

    public ArrayList<EntityTableWrapper> getNodeTables() {
        return nodeTables;
    }

    public ArrayList<RelTableWrapper> getEdgeTables() {
        return edgeTables;
    }

    public int getNodeId(Ref entityRef) {
        return nodeMapping.getKey(entityRef);
    }

    public int getEdgeId(Ref relRef) {
        return edgeMapping.getKey(relRef);
    }

    public GraphRef getRef() {
        return ref;
    }

    public void setRef(GraphRef ref) {
        this.ref = ref;
    }

    public NodeTableWrapper getNodeTable() {
        return nodeTableWrapper;
    }

    public EdgeTableWrapper getEdgeTable() {
        return edgeTableWrapper;
    }

    public interface GraphElementWrapper {

        public Tuple getBaseTuple(int id);
    }

    /**
     * Describes a node table; mainly useful for mapping
     */
    public class NodeTableWrapper extends TableWrapper implements GraphElementWrapper {

        public NodeTableWrapper(NodeTableWrapper parent) {
            super(parent);
        }

        /**
         * copy from parent, should be subclasses for type-safety
         */
        @Override
        public NodeTableWrapper copy() {
            return new NodeTableWrapper(this);
        }

        public NodeTableWrapper() {
            this(new Table());
        }

        public NodeTableWrapper(Table table) {
            super("nodes@" + name, table, true, idField, Level.Node);

            addStat(new Stat(labelField, "Label of entity", String.class,
                    Level.Node, Level.Node));

            if (nodeTables.size() > 1) {
                addStat(new Stat(originalIdField, "ID of entity", Integer.TYPE,
                        Level.Node, Level.Node) {

                    @Override
                    public ImageIcon getIcon(Ref r) {
                        return ((NodeRef) r).getEntityRef().getTableWrapper().getIcon();
                    }
                }, true);
                addStat(new Stat(sourceTableField, "Source entity for node", String.class,
                        Level.Node, Level.Node), true);
            }
        }

        public PGraph getPGraph() {
            return PGraph.this;
        }

        public Node getNode(int nid) {
            return graph.getNode(getRow(nid));
        }

        @Override
        public NodeRef createRef(int nid) {
            return new NodeRef(PGraph.this, this, nid);
        }

        @Override
        public ArrayList<TableWrapper> getChildWrappers() {
            ArrayList<TableWrapper> al = new ArrayList<TableWrapper>();
            al.addAll(getNodeTables());
            al.addAll(getEdgeTables());
            return al;
        }

        @Override
        public Tuple getBaseTuple(int nid) {
            return nodeMapping.get(nid).getTuple();
        }
    }

    /**
     * Describes an edge table; mainly useful for mapping
     */
    public class EdgeTableWrapper extends TableWrapper implements GraphElementWrapper {

        public EdgeTableWrapper(EdgeTableWrapper parent) {
            super(parent);
        }

        public EdgeTableWrapper() {
            this(new Table());
        }

        /**
         * copy from parent, should be subclasses for type-safety
         */
        @Override
        public EdgeTableWrapper copy() {
            return new EdgeTableWrapper(this);
        }

        public EdgeTableWrapper(Table table) {
            super("edges@" + name, table, true, idField, Level.Edge);

            addStat(new Stat(sourceField, "ID of source node", Integer.TYPE,
                    Level.Edge, Level.Edge) {

                @Override
                public ImageIcon getIcon(Ref r) {
                    int nid = ((EdgeRef) r).getTuple().getInt(PGraph.sourceField);
                    NodeRef nr = (NodeRef) getNodeTable().getRef(nid);
                    return nr.getEntityRef().getTableWrapper().getIcon();
                }
            }, true);

            addStat(new Stat(targetField, "ID of target node", Integer.TYPE,
                    Level.Edge, Level.Edge) {

                @Override
                public ImageIcon getIcon(Ref r) {
                    int nid = ((EdgeRef) r).getTuple().getInt(PGraph.targetField);
                    NodeRef nr = (NodeRef) getNodeTable().getRef(nid);
                    return nr.getEntityRef().getTableWrapper().getIcon();
                }
            }, true);

            if (edgeTables.size() > 1) {
                addStat(new Stat(originalIdField, "ID of relationship", Integer.TYPE,
                        Level.Edge, Level.Edge) {

                    @Override
                    public ImageIcon getIcon(Ref r) {
                        return ((EdgeRef) r).getRelationshipRef().getTableWrapper().getIcon();
                    }
                }, true);
                addStat(new Stat(sourceTableField, "Source relationship for edge", String.class,
                        Level.Edge, Level.Edge), true);
            }
        }

        public PGraph getPGraph() {
            return PGraph.this;
        }

        public Edge getEdge(int eid) {
            return graph.getEdge(getRow(eid));
        }

        @Override
        public EdgeRef createRef(int eid) {
            return new EdgeRef(PGraph.this, this, eid);
        }

        @Override
        public ArrayList<TableWrapper> getChildWrappers() {
            ArrayList<TableWrapper> al = new ArrayList<TableWrapper>();
            al.addAll(getEdgeTables());
            return al;
        }

        @Override
        public Tuple getBaseTuple(int eid) {
            return edgeMapping.get(eid).getTuple();
        }
    }

    // A few utility methods
    public static String getLabel(Node n) {
        return n.getString(PGraph.labelField);
    }

    public static int getId(Tuple t) {
        return t.getInt(PGraph.idField);
    }

    public Relationship getRel(int eid) {
        return getRelRef(eid).getTableWrapper().getRelationship();
    }

    public Entity getEnt(int nid) {
        return getEntityRef(nid).getTableWrapper().getEntity();
    }
}

