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

package edu.umd.cs.hcil.manynets.engines;

import edu.umd.cs.hcil.manynets.model.Distribution;
import edu.umd.cs.hcil.manynets.model.Stat;
import edu.umd.cs.hcil.manynets.model.DistributionFactory;
import edu.umd.cs.hcil.manynets.model.PGraph;
import edu.umd.cs.hcil.manynets.model.Population;
import edu.umd.cs.hcil.manynets.model.TableWrapper;
import edu.umd.cs.hcil.manynets.model.TableWrapper.Level;
import edu.umd.cs.hcil.socialaction.jung.algorithms.cluster.ClusterSet;
import edu.umd.cs.hcil.socialaction.jung.algorithms.cluster.WeakComponentClusterer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;

/**
 * Component count, sizes.
 * Level is Network - although some nodes are annotated as part of the process
 * @author Manuel Freire
 */
public class ComponentCalculator extends AbstractCalculator {

    public static final Stat componentCount = new Stat("Component count",
            "Number of connected components", 
            Integer.TYPE, Level.Network, Level.Network);
    public static final Stat componentSizes = new Stat("Component sizes",
            "Connected component sizes", 
            Distribution.class, Level.Network, Level.Network);
    public static final Stat parentComponent = new Stat("Parent component",
            "Parent component of node", 
            Integer.TYPE, Level.Node, Level.Node);

    public ComponentCalculator() {
        super(new Stat[]
            {componentCount, componentSizes}, "O(E + V)");
    }

    @Override
    public void calculate(TableWrapper tw, int gid) {
        int r = tw.getRow(gid);
        PGraph pg = ((Population.PopulationTableWrapper)tw).getGraph(gid);
        Graph g = pg.getGraph();

        checkAddStat(parentComponent, pg.getNodeTable());
        checkAddStat(componentCount, tw);
        checkAddStat(componentSizes, tw);
        Table t = tw.getTable();

        // build a decreasing-size set of clusters for all nodes
        WeakComponentClusterer wcc = new WeakComponentClusterer();
        ClusterSet cs = wcc.extract(g);
        ArrayList<Collection<Node>> clusters =
                new ArrayList<Collection<Node>>();
        for (int i=0; i<cs.size(); i++) {
            clusters.add((Collection<Node>)cs.getCluster(i));
        }
        Collections.sort(clusters, new Comparator<Collection<Node>>() {
            @Override
            public int compare(Collection<Node> o1, Collection<Node> o2) {
                return o2.size() - o1.size();
            }
        });

        Table nt = g.getNodeTable();
        int d[] = new int[cs.size()];
        for (int i=0; i<cs.size(); i++) {
            d[i] = cs.getCluster(i).size();
            for (Node n : (Collection<Node>)cs.getCluster(i)) {
                nt.set(n.getRow(), parentComponent.getName(), i);
            }
        }
        
        t.set(r, componentCount.getName(), cs.size());
        t.set(r, componentSizes.getName(),
                DistributionFactory.build(d, tw.getRef(gid)));
    }
}
