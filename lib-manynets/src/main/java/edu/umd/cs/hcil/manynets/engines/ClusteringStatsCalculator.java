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
import edu.umd.cs.hcil.manynets.model.PGraph;
import edu.umd.cs.hcil.manynets.model.Population;
import edu.umd.cs.hcil.manynets.model.Stat;
import edu.umd.cs.hcil.manynets.model.TableWrapper;
import edu.umd.cs.hcil.manynets.model.TableWrapper.Level;
import edu.umd.cs.hcil.socialaction.jung.statistics.GraphStatistics;
import edu.umd.cs.hcil.manynets.hist.DefaultHistogramModel;
import java.util.Map;
import java.util.Map.Entry;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;

/**
 * Calculates avg clustering coefficient for the whole graph, and for each
 * vertex.
 * Level is Network - although individual nodes are also updated. Rewriting
 * the algorithm could fix this.
 * @author Manuel Freire
 */
public class ClusteringStatsCalculator extends AbstractCalculator {

    public static Stat clusteringCoefficientG = new Stat("Clustering coefficient",
                "Clustering coefficient for each vertex",
                Distribution.class, Level.Network, Level.Network);
    public static Stat clusteringCoefficientN = new Stat("Clustering coefficient",
                "Clustering coefficient for each vertex",
                Integer.TYPE, Level.Node, Level.Network);

    public ClusteringStatsCalculator() {
        super(new Stat[] {clusteringCoefficientG, clusteringCoefficientN},
                "O(V^2)");
    }

    @Override
    public void calculate(TableWrapper tw, int gid) {
        int r = tw.getRow(gid);
        PGraph pg = ((Population.PopulationTableWrapper)tw).getGraph(gid);
        Graph g = pg.getGraph();

        checkAddStat(clusteringCoefficientG, tw);
        checkAddStat(clusteringCoefficientN, pg.getNodeTable());
        Table t = tw.getTable();
        Table nt = g.getNodeTable();
        Map<Node,Double> m = GraphStatistics.clusteringCoefficients(g);
        DefaultHistogramModel dhm = new DefaultHistogramModel();
        for (Entry<Node,Double> e : m.entrySet()) {
            int id = e.getKey().getInt(PGraph.idField);
            nt.set(e.getKey().getRow(), clusteringCoefficientN.getName(),
                e.getValue());
            dhm.addValue((double)e.getValue(),
                pg.getNodeTable().getRef(id));
        }
        t.set(r, clusteringCoefficientG.getName(), dhm);
    }
}
