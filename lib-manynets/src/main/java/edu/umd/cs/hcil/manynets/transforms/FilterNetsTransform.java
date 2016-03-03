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
import edu.umd.cs.hcil.manynets.model.Population;
import edu.umd.cs.hcil.manynets.model.TableWrapper;
import edu.umd.cs.hcil.manynets.model.TransformOptions;
import edu.umd.cs.hcil.manynets.model.TransformResults;
import java.util.ArrayList;
import java.util.Iterator;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

/**
 * Filter nodes and edges.
 * @author Manuel Freire
 */
public class FilterNetsTransform extends AbstractTransform {
    private Population pop;
    private Population oldPop;

    /**
     * Initialize the transform
     * @param tw - a PopulationTableWrapper
     * @param options
     * @param results
     */
    @Override
    public void init(TableWrapper tw, TransformOptions options, TransformResults results) {
        super.init((new Population("Derived")).getWrappedAttributes(), options, results);
        this.pop = ((Population.PopulationTableWrapper)this.tw).getPopulation();
        this.oldPop = ((Population.PopulationTableWrapper)tw).getPopulation();
    }
    
    @Override
    public void apply(int id, ProgressMonitor pm) {
        PGraph pg = oldPop.getGraph(id);
        Graph g = Utils.copyGraph(pg.getGraph());

        System.err.println("[[using " + options.getDescription() + "]]");

        pm.setProgress(id, "filtering", 0);

        double delta = 1.0 / (g.getEdgeCount() + g.getNodeCount());
        double progress = 0;
        ArrayList<Edge> badEdges = new ArrayList<Edge>();
        ArrayList<Node> badNodes = new ArrayList<Node>();

        // Process edges
        for (Iterator ei = g.edges(); ei.hasNext(); /**/) {
            Edge e = (Edge)ei.next();
            if (canceled) {
                pm.setProgress(id, "cancelled", progress);
                break;
            }
            boolean good = ((NetFilterOptions)options).accepts(g, e);
//            System.err.println("edge " + e.getInt(PGraph.idField) + " is " + (good?"good":"bad"));
            if ( ! good) {
                badEdges.add(e);
            }
            progress += delta;
            pm.setProgress(id, "filtering", progress);
        }
        // Process nodes
        for (Iterator ni = g.nodes(); ni.hasNext(); /**/) {
            Node n = (Node)ni.next();
            if (canceled) {
                pm.setProgress(id, "cancelled", progress);
                break;
            }
            boolean good = ((NetFilterOptions)options).accepts(g, n);
            if ( ! good) {
                badNodes.add(n);
            }
            progress += delta;
            pm.setProgress(id, "filtering", progress);
        }


        pm.setProgress(id, "clean up (" + badEdges.size() + " elements)", progress);

        // cleanup edges
        System.err.println("cleaning " + badEdges.size() + " bad edges (of " + g.getEdgeCount() + " total)");
        for (Edge e : badEdges) {
            g.removeEdge(e);
        }
        // add now-degree-0 nodes to list
        System.err.println("locating orphaned nodes ("
                + badNodes.size() + " of " + g.getNodeCount() + " already marked for removal)");
        for (Iterator ni = g.nodes(); ni.hasNext(); /**/) {
            Node n = (Node)ni.next();
            if (n.getDegree() == 0) {
                badNodes.add(n);
            }
        }
        // and purge nodes
        System.err.println("cleaning " + badNodes.size() + " bad nodes");
        for (Node n : badNodes) {
            if (g.containsTuple(n)) {
                g.removeNode(n);
            }
        }
        System.err.println("retained final " + g.getNodeCount() + " nodes, " + g.getEdgeCount() + " edges");
        
        // add to new pop
        if (g.getNodeCount() > 0) {
            pop.addGraph(new PGraph(pg.getName(), pg, Utils.copyGraph(g)));
        }
        System.err.println("pop now has " + pop.getGraphIds().size() + " element(s)");
        pm.setProgress(id, "finished", 1);
    }

    public interface NetFilterOptions extends TransformOptions {
        public boolean accepts(Graph g, Edge e);
        public boolean accepts(Graph g, Node n);
    }
}
