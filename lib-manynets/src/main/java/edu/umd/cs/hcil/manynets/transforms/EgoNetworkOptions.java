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
import edu.umd.cs.hcil.manynets.engines.ExpressionCalculator;
import edu.umd.cs.hcil.manynets.model.PGraph;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import org.jdom.Element;
import org.python.core.Py;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;

/**
 * Build ego network split
 */
public class EgoNetworkOptions implements SplitNetworkTransform.SplitOptions {

    private int radius;
    private boolean strict;
    private boolean egoless;
    private ExpressionCalculator nodeFilter;

    public EgoNetworkOptions(int radius) {
        this(radius, false);
    }

    public EgoNetworkOptions(int radius, boolean strict) {
        this(radius, strict, false);
    }

    public EgoNetworkOptions(int radius, boolean strict, boolean egoless) {
        this(radius, strict, egoless, null);
    }

    public EgoNetworkOptions(int radius, boolean strict, boolean egoless, ExpressionCalculator nodeFilter) {
        this.radius = radius;
        this.strict = strict;
        this.egoless = egoless;
        this.nodeFilter = nodeFilter;
    }

    /** in an ego network, each node produces a network */
    @Override
    public boolean isSeedNode(PGraph g, Node n) {
        boolean good = true;
        if (nodeFilter != null) {
            good = Py.py2boolean(nodeFilter.evalTuple(n, g.getNodeTable()));
        }
        return good;
    }

    /**
     * Builds a graph from a given seed node; the new graph will contain a
     * subset of the edges of the original graph. As such, all edges and
     * nodes will retain the same IDs assigned to them in the source graph.
     * @param source
     * @param seed
     * @return
     */
    @Override
    public PGraph buildGraph(PGraph source, Node seed) {
        Graph sg = source.getGraph();
        Table nodeTable = Utils.createTableWithCols(sg.getNodeTable());
        Table edgeTable = Utils.createTableWithCols(sg.getEdgeTable());
        // build list of good ids and their generations
        int generation = 0;
        HashMap<Integer, Integer> nodes = new HashMap<Integer, Integer>();
        HashSet<Integer> edges = new HashSet<Integer>();
        Queue<Node> q = new ArrayDeque<Node>();
        q.offer(seed);
        if (!egoless) {
            //   System.err.println("with ego "+ seed.getRow());
            Utils.copyRow(sg.getNodeTable(), nodeTable, seed.getRow());
        }
        nodes.put(seed.getRow(), generation);
        // seed starts in the queue with gen = 0
        while (!q.isEmpty()) {
            Node n = q.remove();
            generation = nodes.get(n.getRow());
            // once radius is first exceeded, all others will also exceed it
            if (generation > radius) {
                break;
            }
            for (Iterator ei = n.edges(); ei.hasNext();) {
                Edge e = (Edge) ei.next();
                Node m = e.getAdjacentNode(n);
                int otherGen = 0;
                if (!nodes.containsKey(m.getRow())) {
                    // first encounter; cannot be at the limit
                    otherGen = generation + 1;
                    if (otherGen <= radius) {
                        q.offer(m);
                        Utils.copyRow(sg.getNodeTable(), nodeTable, m.getRow());
                        nodes.put(m.getRow(), otherGen);
                    }
                } else {
                    // seen before
                    otherGen = nodes.get(m.getRow());
                }
                // avoid adding duplicates or exceeding radius
                if (!edges.contains(e.getRow()) && otherGen <= radius) {
                    if (otherGen < radius || !strict) {
                        edges.add(e.getRow());
                        if (!(egoless)) {
                            Utils.copyRow(sg.getEdgeTable(), edgeTable, e.getRow());
                        } else {
                            //                       System.err.println("ego less , seed= "+ seed.getRow() + " node n = "+n.getRow());
                            if (!(n == seed)) {
                                //                         System.err.println("not seed = " + n.getRow());
                                Utils.copyRow(sg.getEdgeTable(), edgeTable, e.getRow());
                            }
                        }
                    }
                }
            }
        }
        // build, identify, and return network
        String rid = getFullNodeName(seed) + "-" + source.getName();
        System.err.println(rid + ": " + nodeTable.getRowCount() + " x " + edgeTable.getRowCount());
        Graph g = new Graph(nodeTable, edgeTable, sg.isDirected(), sg.getNodeKeyField(), sg.getEdgeSourceField(), sg.getEdgeTargetField()); // edgefield2
        return new PGraph("ego-" + rid, source, g);
    }

    private String getFullNodeName(Node n) {
        if (n.getColumnIndex("OID") != -1) {
            String source = (n.getColumnIndex("Source") != -1) ? n.getString("Source") + "-" : "";
            return source + n.getString("Label");
        } else {
            return n.getString("Label");
        }
    }

    public void save(Element e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void load(Element e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getDescription() {
        return "all radius " + radius + (!strict ? ".5" : ".0") + " ego nets" + (egoless ? " without the ego node" : "") + (nodeFilter != null ? " filtered by " + nodeFilter.getExpression() : "");
    }
}
