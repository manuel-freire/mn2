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

import edu.umd.cs.hcil.manynets.model.PGraph;
import edu.umd.cs.hcil.manynets.model.Population;
import edu.umd.cs.hcil.manynets.model.Population.PopulationTableWrapper;
import edu.umd.cs.hcil.manynets.model.TableWrapper;
import edu.umd.cs.hcil.manynets.model.Transform.ProgressMonitor;
import edu.umd.cs.hcil.manynets.model.TransformOptions;
import edu.umd.cs.hcil.manynets.model.TransformResults;
import edu.umd.cs.hcil.socialaction.jung.algorithms.cluster.ClusterSet;
import edu.umd.cs.hcil.socialaction.jung.algorithms.cluster.WeakComponentClusterer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.jdom.Element;
import prefuse.data.Graph;
import prefuse.data.Node;

/**
 * Split a network into its component networks using a certain clustererer;
 * options allow result filtering based on cluster size (all, first, or rest).
 *
 * @author Manuel Freire
 */
public class FilterComponentsTransform extends AbstractTransform {

    private Population pop;

    @Override
    public void init(TableWrapper tw, TransformOptions options, TransformResults results) {
        super.init(tw, options, results);
        pop = new Population("derived", ((PopulationTableWrapper)tw).getPopulation());
    }
    
    @Override
    public void apply(int id, ProgressMonitor pm) {
        PGraph pg = pop.getGraph(id);
        Graph g = pg.getGraph();

        pm.setProgress(id, "analyzing", 0);
        ClusterSet cs = (new WeakComponentClusterer()).extract(g);
        cs.sort();
        
        double delta = 1.0 / cs.size();
        double progress = 1;
        Set<Node> toRemove = new HashSet<Node>();
        Iterator it = cs.iterator();
        for (int i=0; it.hasNext(); i++) {
            Set<Node> s = (Set<Node>)it.next();
            if (canceled) {
                pm.setProgress(id, "cancelled", progress);
                break;
            }
            pm.setProgress(id, "filtering", progress);
            if ( ! ((ClusterSetFilterOptions)options).accepts(s, i)) {
                toRemove.addAll(s);
            }
            progress += delta;
        }
        pm.setProgress(id, "clean up (" + toRemove.size() + " vertices)", progress);

        /*
         * FIXME: Fails if two threads remove vertices from
         * *DIFFERENT* and unrelated graphs...
         *
         * Traced error to Graph.removeNode(int node)
         * - Graph.getNodeTable().isValidRow(int node)
         * - m_links.get
         * - Graph.removeEdge
         * - ???
         */
        synchronized(mutex) {
            for (Node n : toRemove) {
                g.removeNode(n);
            }
        }
        pm.setProgress(id, "finished", 1);
    }

    public interface ClusterSetFilterOptions extends TransformOptions {
        public boolean accepts(Set<Node> set, int order);
    }

    public static class AllComponentsOption implements ClusterSetFilterOptions {
        public boolean accepts(Set<Node> set, int order) {
            return true;
        }
        public void save(Element e) {}
        public void load(Element e) {}
        public String getDescription() {
            return "all components";
        }
    }

    public static class OnlyLargestOption implements ClusterSetFilterOptions {
        public boolean accepts(Set<Node> set, int order) {
            return order == 0;
        }
        public void save(Element e) {}
        public void load(Element e) {}
        public String getDescription() {
            return "discard all except largest";
        }
    }
    
    public static class AllExceptLargestOption implements ClusterSetFilterOptions {
        public boolean accepts(Set<Node> set, int order) {
            return order != 0;
        }
        public void save(Element e) {}
        public void load(Element e) {}
        public String getDescription() {
            return "keep all except largest";
        }
    }
}
