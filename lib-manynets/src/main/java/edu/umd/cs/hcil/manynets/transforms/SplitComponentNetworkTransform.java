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

import edu.umd.cs.hcil.manynets.model.Population;
import edu.umd.cs.hcil.manynets.Utils;
import edu.umd.cs.hcil.manynets.model.PGraph;
import edu.umd.cs.hcil.manynets.model.TableWrapper;
import java.util.ArrayDeque;
import edu.umd.cs.hcil.manynets.model.TransformResults;
import edu.umd.cs.hcil.manynets.model.TransformOptions;
import edu.umd.cs.hcil.socialaction.jung.algorithms.cluster.ClusterSet;
import edu.umd.cs.hcil.socialaction.jung.algorithms.cluster.WeakComponentClusterer;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import org.jdom.Element;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;

/**
 *
 * @author Awalin Sopan
 */
public class SplitComponentNetworkTransform extends AbstractTransform {

    private Population pop;
    private Population oldPop;

    @Override
    public void init(TableWrapper tw, TransformOptions options, TransformResults results) {
        super.init((new Population("Derived")).getWrappedAttributes(), options, results);
        this.pop = ((Population.PopulationTableWrapper) this.tw).getPopulation();
        this.oldPop = ((Population.PopulationTableWrapper) tw).getPopulation();
    }

    @Override
    public void apply(int id, ProgressMonitor pm) {
        synchronized (mutex) {
            PGraph pg = oldPop.getGraph(id);
            Graph g = pg.getGraph();

            pm.setProgress(id, "analyzing", 0);
            ClusterSet cs = (new WeakComponentClusterer()).extract(g);
            System.err.println("total components " + cs.size() + " in graph = " + id);

            double delta = 1.0 / cs.size();
            double progress = 0;
            int added = 0;
            Iterator it = cs.iterator();//for each cluster
            SplitOptions sp = (SplitOptions) options;
            for (int i = 0; it.hasNext(); i++) {
                Set<Node> s = (Set<Node>) it.next();//set of nodes of each cluster
                if (canceled) {
                    pm.setProgress(id, "cancelled", progress);
                    break;
                }
                //for each cluster , add a row in the table
                Iterator setIterator = s.iterator();
                pm.setProgress(id, "expanding", progress);
                //consider the first element of the set as seed
                PGraph npg = sp.buildGraph(pg, (Node) setIterator.next());
                if (npg != null) {
                    pop.addGraph(npg);
                    added++;
                }
                progress += delta;
                pm.setProgress(id, "filtering", progress);
            }
            pm.setProgress(id, "finished", 1);
            System.err.println("Added " + added + " networks (pop = "
                    + pop.getAttributes().getRowCount() + ")");
        }

    }

    public interface SplitOptions extends TransformOptions {

        public boolean isSeedNode(PGraph g, Node n);

        public PGraph buildGraph(PGraph source, Node seed);
    }

    /**
     * Build network split
     */
    public static class SplitComponentOptions implements SplitOptions {

        public SplitComponentOptions() {
        }

        /** if the node is a seed of the component */
        @Override
        public boolean isSeedNode(PGraph g, Node n) {
            return true; // use for sampling: (x++) % 101) == 0;
        }

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
            Utils.copyRow(sg.getNodeTable(), nodeTable, seed.getRow());
            nodes.put(seed.getRow(), generation);

            // seed starts in the queue with gen = 0
            while (!q.isEmpty()) {
                Node n = q.remove();
                generation = nodes.get(n.getRow());

                for (Iterator ei = n.edges(); ei.hasNext(); /**/) {

                    Edge e = (Edge) ei.next();
                    Node m = e.getAdjacentNode(n);
                    int otherGen = 0;
                    if (!nodes.containsKey(m.getRow())) {
                        // first encounter; cannot be at the limit
                        otherGen = generation + 1;
                        q.offer(m);
                        Utils.copyRow(sg.getNodeTable(), nodeTable, m.getRow());
                        nodes.put(m.getRow(), otherGen);

                    } else {
                        // seen before
                        otherGen = nodes.get(m.getRow());
                    }

                    // avoid adding duplicates 
                    if (!edges.contains(e.getRow())) {
                        edges.add(e.getRow());
                        Utils.copyRow(sg.getEdgeTable(), edgeTable, e.getRow());
                    }
                }

            }

            String rowId = seed.getColumnIndex("NAME") == -1
                    ? "" + seed.getString("ID") : seed.getString("NAME");
            System.err.println(rowId + ": " + nodeTable.getRowCount()
                    + " x " + edgeTable.getRowCount());
            Graph g = new Graph(nodeTable, edgeTable, sg.isDirected(),
                    "ID", // nodefield
                    sg.getEdgeSourceField(), // edgefield1
                    sg.getEdgeTargetField()); // edgefield2
            return new PGraph(source.getName() + " comp-" + rowId, source, g);
        }

        public void save(Element e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void load(Element e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String getDescription() {
            return "all component networks";

        }
    }

    public static class SplitResults implements TransformOptions {

        public void save(Element e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void load(Element e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String getDescription() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
