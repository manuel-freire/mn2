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

import edu.umd.cs.hcil.manynets.Utils;
import prefuse.data.Table;

import java.util.ArrayList;
import prefuse.data.Graph;
import prefuse.data.Edge;
import java.util.Collections;
import java.util.TreeSet;
import prefuse.util.collections.IntIterator;

/**
 * A graph coarsener, using several passes where, in each pass, a maximal
 * set of random, non-overlaping edges is collapsed. Edges from / to collapsed
 * nodes are retargeted to their counterparts only if they did not already
 * exist between the uncollapsed endpoints.
 *
 * @author awalin sopan, manuel freire
 */
public class GraphCoarsener {

   /**
    * Reduces the number of nodes in the graph (N) to less or equal to a given limit.
    * Only affects connected components - if the graph has many connected
    * components, this reduction may not be possible.
    * Coarsening is not always interesting; for instance, coarsening a bipartite
    * graph will destroy its bipartite structure. It is, however, useful in
    * the case of the scale-free, small-world graphs common in social networks.
    * 
    * @param g input graph
    * @param nodeLimit the maximum allowed number of nodes for the new coarsened graph
    * @return
    */
    public static Graph coarsen(Graph g, int nodeLimit, int edgeLimit) {

        while (g.getNodeCount() > nodeLimit && g.getEdgeCount() > edgeLimit) {
            g = coarsenOnce(g);
        }
        
        return g;
    }

    /**
     * Does one iteration of the coarsening.
     * @param g
     * @return
     */
    private static Graph coarsenOnce(Graph g) {
        System.err.println("\ncoarsening - "
              + g.getNodeCount() + " nodes, "
              + g.getEdgeCount() + " edges");

        // shuffle the list of edge indices, to start the coarsening process
        ArrayList<Integer> edgeIndices = new ArrayList<Integer>();
        for (IntIterator rows = g.edgeRows(); rows.hasNext(); /**/) {
            edgeIndices.add(rows.nextInt());
        }
        Collections.shuffle(edgeIndices);
        TreeSet<Integer> badEdges = new TreeSet<Integer>();

        // initialize new tables of nodes and edges (for next iteration of the graph)
        Table nextNodes = Utils.createTableWithCols(g.getNodeTable());
        Table nextEdges = Utils.createTableWithCols(g.getEdgeTable());

        // selected[i] == -1 if not selected, row-num of subsumed otherwise
        // subsumed[i] == -1 if not subsumed, row-num of selected otherwise
        // selected[i] != -1 => subsumed[selected[i]] == i; converse also holds
        int[] selected = new int[g.getNodeTable().getRowCount()];
        int[] subsumed = new int[g.getNodeTable().getRowCount()];
        for (int i=0; i<selected.length; i++) {
            // -1 is used to signify "not used".
            selected[i] = -1;
            subsumed[i] = -1;
        }

        // iterate over the (randomized) list of edge indices
        for (int i=0; i<edgeIndices.size(); i++) {
            Edge e = g.getEdge(edgeIndices.get(i));
            int r1 = e.getSourceNode().getRow();
            int r2 = e.getTargetNode().getRow();

            // if not previously collapsed, collapse now
            if (selected[r1] == -1 && subsumed[r1] == -1 &&
                selected[r2] == -1 && subsumed[r2] == -1) {
                selected[r1] = r2;
                subsumed[r2] = r1;
                badEdges.add(edgeIndices.get(i));
            }
        }

        // copy all non-subsumed nodes to new node-table
        for (IntIterator rows = g.nodeRows(); rows.hasNext(); /**/) {
            int r = rows.nextInt();
            if (subsumed[r] == -1) {
                Utils.copyRow(g.getNodeTable(), nextNodes, r);
            }
        }

        // copy all non-subsumed, non-duplicate edges to new node-table
        for (int er : edgeIndices) {
            if (badEdges.contains(er)) {
                // avoid copying subsumed edges
                System.err.print(".");
                continue;
            }

            Edge e = g.getEdge(er);
            int a = e.getSourceNode().getRow();
            int b = e.getTargetNode().getRow();
            int sa, sb;

            /*
             * What to copy?
             * a  b
             * x  x - keep a -> b
             * s  x - if no sa -> b present, retarget a -> b to sa -> b
             * x  s - if no a -> sb present, retarget a -> b to a -> sb
             * s  s - if no a -> sb or sa -> b or sa -> sb,
             *                               retarget a -> b to sa -> sb
             * legend: s = subsumed
             *         x = not subsumed
             */
            if ((sa = subsumed[a]) == -1) {
                // a not subsumed
                if ((sb = subsumed[b]) == -1) {
                    // easy case: no subsuming at all
                    Utils.copyRow(g.getEdgeTable(), nextEdges, er);
                } else {
                    // b subsumed; do not copy edge if a -> sb already exists
                    if ( ! Utils.edgeExists(g, g.getNode(a), g.getNode(sb))) {
                        // 'x s' case: copy & retarget a -> b to a -> sb
                        int r = Utils.copyRow(g.getEdgeTable(), nextEdges, er);
                        nextEdges.set(r, g.getEdgeTargetField(),
                                g.getNode(sb).get(g.getNodeKeyField()));
                    }
                }
            } else {
                // a subsumed into sa
                if ((sb = subsumed[b]) == -1) {
                    // a subsumed; do not copy edge if sa -> b already exists
                    if ( ! Utils.edgeExists(g, g.getNode(sa), g.getNode(b))) {
                        // 's x' case: copy & retarget a -> b to sa -> b
                        int r = Utils.copyRow(g.getEdgeTable(), nextEdges, er);
                        nextEdges.set(r, g.getEdgeSourceField(),
                                g.getNode(sa).get(g.getNodeKeyField()));
                    }
                } else if ( ! Utils.edgeExists(g, g.getNode(sa), g.getNode(sb))
                     &&  ! Utils.edgeExists(g, g.getNode(a), g.getNode(sb))
                     &&  ! Utils.edgeExists(g, g.getNode(sa), g.getNode(b))) {
                    // 's s' case: copy & retarget a -> b to sa -> sb
                    int r = Utils.copyRow(g.getEdgeTable(), nextEdges, er);
                    nextEdges.set(r, g.getEdgeSourceField(),
                            g.getNode(sa).get(g.getNodeKeyField()));
                    nextEdges.set(r, g.getEdgeTargetField(),
                            g.getNode(sb).get(g.getNodeKeyField()));
                }
            }
        }

        return new Graph(nextNodes, nextEdges, g.isDirected(),
                g.getNodeKeyField(), g.getEdgeSourceField(), g.getEdgeTargetField());
    }
}
