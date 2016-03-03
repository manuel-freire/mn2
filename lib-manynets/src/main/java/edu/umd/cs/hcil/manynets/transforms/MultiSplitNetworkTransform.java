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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.jdom.Element;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;

/**
 * Splits networks into subnetworks, on a more-than-one-vertex per edge pattern.
 *
 * This transform does not modify its TW -- it builds a new population instead.
 *
 * @author Manuel Freire
 */
public class MultiSplitNetworkTransform extends AbstractTransform {

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
        synchronized (mutex) {
            PGraph pg = oldPop.getGraph(id);
            Graph og = pg.getGraph();

            pm.setProgress(id, "analyzing", 0);
            SplitOptions sp = (SplitOptions)options;
            HashSet<Candidate> candidates = new HashSet<Candidate>();
            for (Iterator ni = og.nodes(); ni.hasNext(); /**/) {
                Node n = (Node)ni.next();
                sp.getCandidates(pg, n, candidates);
            }

            double delta = 1.0 / candidates.size();
            double progress = 0;
            int added = 0;
            for (Candidate c : candidates) {
                if (canceled) {
                    pm.setProgress(id, "canceled", progress);
                    break;
                }
                pm.setProgress(id, "expanding", progress);
                PGraph npg = sp.buildGraph(pg, c);
                if (npg != null) {
                    pop.addGraph(npg);
                    added ++;
                }
                progress += delta;
            }
            pm.setProgress(id, "finished", 1);
            System.err.println("Added " + added + " networks (pop = "
                    + pop.getAttributes().getRowCount() + ")");
            }
    }

    /**
     * A subnet, identifed only by a set of nodes.
     * Found by SplitOptions.getCandidates
     * 
     * Each candidate can contain several nodes (at least one). No duplicate
     * nodes are allowed.
     */
    public static class Candidate extends HashSet<Integer> {
        private Graph g;
        private int h; // a weak hashcode, for faster candidate comparison
        public Candidate(Graph g) {
            this.g = g;
        }
        public void add(Node n) {
            add(n.getRow());
            h += n.getRow() + 177;
        }
        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if ( ! (o instanceof Candidate)) return false;
            Candidate b = (Candidate)o;
            return g == b.g && containsAll(b);
        }
        @Override
        public int hashCode() {
            return h;
        }
        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            for (int i : this) { sb.append("" + i + "_"); }
            if ( ! isEmpty()) sb.setLength(sb.lastIndexOf("_"));
            return sb.toString();
        }
    }

    public static HashSet<Integer> getNeighbors(Node n) {
        HashSet<Integer> set = new HashSet<Integer>();
        for (Iterator<Node> ni = (Iterator<Node>)n.neighbors(); ni.hasNext(); /**/) {
            set.add(ni.next().getRow());
        }
        return set;
    }

    public interface SplitOptions extends TransformOptions {
        public void getCandidates(PGraph g, Node n, Set<Candidate> cs);
        public PGraph buildGraph(PGraph source, Candidate c);
    }

    /**
     * Build all pairs or triplets
     */
    public static class PieceMealOptions implements SplitOptions {

        private int size;

        public PieceMealOptions(int size) {
            this.size = size;
        }

        @Override
        public void getCandidates(PGraph g, Node n, Set<Candidate> cs) {
            Graph sg = g.getGraph();
            HashSet<Integer> nn = null;
            if (size == 3) {
                nn = getNeighbors(n);
            }
            for (Iterator<Node> ni = (Iterator<Node>)n.neighbors(); ni.hasNext(); /**/) {
                Node o = ni.next();
                if (size == 3) {
                    HashSet<Integer> on = getNeighbors(o);
                    on.retainAll(nn);
                    for (int k : on) {
                        Candidate c = new Candidate(sg);
                        c.add(n);
                        c.add(o);
                        c.add(k);
                        cs.add(c);
                    }
                } else {
                    Candidate c = new Candidate(sg);
                    c.add(n);
                    c.add(o);
                    cs.add(c);
                }
            }
        }

        @Override
        public PGraph buildGraph(PGraph source, Candidate c) {
            Graph sg = source.getGraph();
            Table nodeTable = Utils.createTableWithCols(sg.getNodeTable());
            Table edgeTable = Utils.createTableWithCols(sg.getEdgeTable());

            for (int r : c) {
                Utils.copyRow(sg.getNodeTable(), nodeTable, r);
                Node n = sg.getNode(r);
                for (Iterator ei = n.outEdges(); ei.hasNext(); /**/) {
                    Edge e = (Edge)ei.next();
                    if (c.contains(e.getAdjacentNode(n).getRow())) {
                        Utils.copyRow(sg.getEdgeTable(), edgeTable, e.getRow());
                    }
                }
            }

            // build, identify, and return network
            Graph g = new Graph(nodeTable, edgeTable, sg.isDirected(),
                        "ID",  // nodefield
                        sg.getEdgeSourceField(),  // edgefield1
                        sg.getEdgeTargetField()); // edgefield2
            return new PGraph(size + "-" + c, source, g);
        }

        @Override
        public void save(Element e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void load(Element e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getDescription() {
            return "all " + size + "-plets";

        }
    }
    
    public static class SplitResults implements TransformOptions {

        @Override
        public void save(Element e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void load(Element e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getDescription() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
