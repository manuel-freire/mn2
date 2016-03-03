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
import edu.umd.cs.hcil.manynets.model.PGraph;
import edu.umd.cs.hcil.manynets.model.TableWrapper;
import edu.umd.cs.hcil.manynets.model.TransformOptions;
import edu.umd.cs.hcil.manynets.model.TransformResults;
import java.util.ArrayList;
import java.util.Iterator;
import org.jdom.Element;
import prefuse.data.Graph;
import prefuse.data.Node;

/**
 * Generic transform to build subnetworks based on a set of seed nodes. At most
 * one network per seed will be created. An initial pass scan for seeds before
 * transformation begins.
 * 
 * @author Manuel Freire
 */
public class SplitNetworkTransform extends AbstractTransform {

    private Population pop;
    private Population oldPop;

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
            ArrayList<Node> seeds = new ArrayList<Node>();
            for (Iterator ni = og.nodes(); ni.hasNext(); /**/) {
                Node n = (Node)ni.next();
                if (sp.isSeedNode(pg, n)) {
                    seeds.add(n);
                }
            }

            double delta = 1.0 / seeds.size();
            double progress = 0;
            int added = 0;
            for (Node n : seeds) {
                if (canceled) {
                    pm.setProgress(id, "cancelled", progress);
                    break;
                }
                pm.setProgress(id, "expanding", progress);
                PGraph npg = sp.buildGraph(pg, n);
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

    public interface SplitOptions extends TransformOptions {
        public boolean isSeedNode(PGraph g, Node n);
        public PGraph buildGraph(PGraph source, Node seed);
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
