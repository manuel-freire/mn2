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

package edu.umd.cs.hcil.manynets.transforms;

import edu.umd.cs.hcil.manynets.Utils;
import edu.umd.cs.hcil.manynets.model.PGraph;
import edu.umd.cs.hcil.manynets.transforms.SplitNetworkTransform.SplitOptions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import org.jdom.Element;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;

/**
 *
 * @author Manuel Freire
 */
public class Vast09EgoSplit implements SplitOptions {

    private boolean aIsOk;
    private boolean bIsOk;

    public Vast09EgoSplit(boolean aIsOk, boolean bIsOk) {
        this.aIsOk = aIsOk;
        this.bIsOk = bIsOk;
    }

    public boolean isSeedNode(PGraph g, Node n) {
        int d = n.getInt("ODEG");
        return d > 37 && d < 43;
    }

    public void addNodesForEdges(PGraph pg, int generation,
            HashMap<Integer, Integer> nodes,
            HashSet<Node> goodNodes, HashSet<Edge> goodEdges,
            Table nodeTable, Table edgeTable, Graph sg) {

        goodNodes.clear();
        for (Edge e : goodEdges) {
            int src = e.getSourceNode().getRow();
            int dst = e.getTargetNode().getRow();
            if ( ! nodes.containsKey(src)) {
                goodNodes.add(e.getSourceNode());
                nodes.put(src, nodes.size());
                Utils.copyRow(sg.getNodeTable(), nodeTable, src);
                nodeTable.setInt(nodeTable.getRowCount()-1, "ID",
                        nodes.get(src));
                nodeTable.setInt(nodeTable.getRowCount()-1, "GEN",
                        generation);
            }
            if ( ! nodes.containsKey(dst)) {
                goodNodes.add(e.getTargetNode());
                nodes.put(dst, nodes.size());
                Utils.copyRow(sg.getNodeTable(), nodeTable, dst);
                nodeTable.setInt(nodeTable.getRowCount()-1, "ID",
                        nodes.get(dst));
                nodeTable.setInt(nodeTable.getRowCount()-1, "GEN",
                        generation);
            }
            Utils.copyRow(sg.getEdgeTable(), edgeTable, e.getRow());
            edgeTable.setInt(edgeTable.getRowCount()-1, "ID1",
                    nodes.get(src));
            edgeTable.setInt(edgeTable.getRowCount()-1, "ID2",
                    nodes.get(dst));
        }
        System.err.println("\t" + generation +
                " - Total nodes " + nodeTable.getRowCount() +
                " total edges " + edgeTable.getRowCount());
    }

    /**
     * Ego graph constructor - only copies single vertex
     * Does copy all attributes, just in case
     * @param pg
     * @param node
     */
    public PGraph build(PGraph pg, Node node, String id) {
        Graph sg = pg.getGraph();

        Table nodeTable = Utils.createTableWithCols(sg.getNodeTable());
        Utils.copyRow(sg.getNodeTable(), nodeTable, node.getRow());
        nodeTable.setInt(0, "ID", 0);
        Table edgeTable = Utils.createTableWithCols(sg.getEdgeTable());
        System.err.println("\tTotal nodes " + nodeTable.getRowCount() +
                " total edges " + edgeTable.getRowCount());

        HashMap<Integer, Integer> nodes = new HashMap<Integer, Integer>();
        nodes.put(node.getRow(), 0);

        nodeTable.addColumn("GEN", Integer.TYPE);

        HashSet<Node> goodNodes = new HashSet<Node>();
        HashSet<Edge> goodEdges = new HashSet<Edge>();
        goodNodes.add(node);

// ------------- first addition --------------
        for (Node n : goodNodes) {
            for (Iterator eit=sg.edges(n); eit.hasNext(); /**/) {
                Edge e = (Edge)eit.next();
                if (e.getString("ET").equals("BB")) {
                    goodEdges.add(e);
                }
            }
        }
        if (goodEdges.size() < 3) {
            throw new IllegalArgumentException("not enough edges in 1st gen");
        }
        addNodesForEdges(pg, 1, nodes, goodNodes, goodEdges,
                nodeTable, edgeTable, sg);

// ------------- round 4 --------------
        HashSet<Node> copyNodes = new HashSet<Node>(goodNodes);
        goodEdges.clear();
        for (Node n : copyNodes) {
            for (Iterator eit=sg.edges(n); eit.hasNext(); /**/) {
                Edge e = (Edge)eit.next();
                if (e.getString("ET").equals("BB") &&
                        (nodes.containsKey(e.getSourceNode().getRow()) &&
                        nodes.containsKey(e.getTargetNode().getRow())) &&
                        (e.getSourceNode() != node) &&
                        (e.getTargetNode() != node)) {
                    goodEdges.add(e);
                }
            }
        }
        System.err.println("!!!!! Found " + goodEdges.size() + " extra edges");
        addNodesForEdges(pg, 4, nodes, copyNodes, goodEdges,
                nodeTable, edgeTable, sg);

// ------------- second round --------------
        goodEdges.clear();
        for (Node n : goodNodes) {
            System.err.println("checking outgoing for node " + n.getString("NAME"));
            for (Iterator eit=sg.edges(n); eit.hasNext(); /**/) {
                Edge e = (Edge)eit.next();
                if (e.getString("ET").equals("BD") ||
                        e.getString("ET").equals("BC")) {
                    goodEdges.add(e);
                }
            }
        }
        if (goodEdges.size() < 3) {
            throw new IllegalArgumentException("not enough edges in 2nd gen");
        }
        addNodesForEdges(pg, 2, nodes, goodNodes, goodEdges,
                nodeTable, edgeTable, sg);
        copyNodes = new HashSet<Node>(goodNodes);

// ------------- last round --------------
        goodEdges.clear();
        for (Node n : goodNodes) {
            for (Iterator eit=sg.edges(n); eit.hasNext(); /**/) {
                Edge e = (Edge)eit.next();
                if (e.getString("ET").equals("AD") ||
                        e.getString("ET").equals("AC")) {
                    goodEdges.add(e);
                }
            }
        }
        addNodesForEdges(pg, 3, nodes, goodNodes, goodEdges,
                nodeTable, edgeTable, sg);

        Graph graph = new Graph(nodeTable, edgeTable, sg.isDirected(),
                    "ID",  // nodefield
                    sg.getEdgeSourceField(),  // edgefield1
                    sg.getEdgeTargetField()); // edgefield2

// ------------- sanity checks --------------

        boolean foundOne = false;

// ------------- type A --------------
        for (Node n : copyNodes) {
            Node m = graph.getNode(nodes.get(n.getRow()));
            int good = 0;
            for (Iterator eit = graph.edges(m); eit.hasNext(); /**/) {
                Edge e = (Edge)eit.next();
                System.err.print(e.getString("ET"));
                if (e.getString("ET").equals("BD") ||
                        e.getString("ET").equals("BC")) {
                    good ++;
                }
            }
            if (good >= 3) {
                System.err.println(":::::::::: goody - " + m.getString("NAME"));
                if (aIsOk) foundOne = true;
                break;
            }
        }

// ------------- type B --------------
        for (Node n : goodNodes) {
            Node m = graph.getNode(nodes.get(n.getRow()));
            if (m.getDegree() >= 3) {
                if (bIsOk) foundOne = true;
                break;
            }
        }
        if ( ! foundOne) {
            throw new IllegalArgumentException("no A or B candidate");
        }

// ------------- Removed B edges with degree 1 ------------
        
        ArrayList<Node> toRemove = new ArrayList<Node>();
        Iterator ni = graph.nodes();
//        while (ni.hasNext()) {
//            Node n = (Node)ni.next();
//            System.err.println("Considering removal of "
//                    + n.getString("NAME") + " from gen " + n.getInt("GEN")
//                    + " with degree " + n.getDegree());
//            if (n.getInt("GEN") != 3) {
//                if (n.getDegree() == 1) {
//                    System.err.println("Removed " + n.getString("NAME"));
//                    toRemove.add(n);
//                }
//            }
//        }
        for (Node n : toRemove) {
            graph.removeNode(n);
        }

        throw new UnsupportedOperationException();
        // return new PGraph(id, graph);
    }


    public PGraph buildGraph(PGraph source, Node seed) {
        String id = seed.getString("NAME");
        try {
            return build(source, seed, id);
        } catch (Exception e) {
            System.err.println("\t elided " + id + ": " + e.getMessage());
            return null;
        }
    }

    public void save(Element e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void load(Element e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getDescription() {
        return "Vast 2009 type "
                + (aIsOk ? "A" : "")
                + (bIsOk ? "B" : "") + " ego nets";
    }
}
