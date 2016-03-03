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

import edu.umd.cs.hcil.manynets.model.Population;
import edu.umd.cs.hcil.manynets.model.Stat;
import edu.umd.cs.hcil.manynets.model.TableWrapper;
import edu.umd.cs.hcil.manynets.model.TableWrapper.Level;
import prefuse.data.Graph;
import prefuse.data.Table;

/**
 * Calculates a graph's basic stats.
 * Level: Network.
 * @author Manuel Freire
 */
public class SimpleStatsCalculator extends AbstractCalculator {

    public static Stat nodeCount = new Stat("Node Count",
                "Number of nodes", Integer.TYPE, Level.Network, Level.Network);
    public static Stat edgeCount = new Stat("Edge Count",
                "Number of edges", Integer.TYPE, Level.Network, Level.Network);
    public static Stat edgeNodeRatio = new Stat("Edge-Node Ratio",
                "Ratio of edges to nodes", Double.TYPE, Level.Network, Level.Network);
    
    public SimpleStatsCalculator() {
        super(new Stat[] {nodeCount, edgeCount, edgeNodeRatio}, "O(1)");
    }

    @Override
    public void calculate(TableWrapper tw, int gid) {
        int r = tw.getRow(gid);
        Graph g = ((Population.PopulationTableWrapper)tw)
                .getGraph(gid).getGraph();

        checkAddStat(nodeCount, tw);
        checkAddStat(edgeCount, tw);
        checkAddStat(edgeNodeRatio, tw);
        Table t = tw.getTable();
        t.set(r, nodeCount.getName(), g.getNodeCount());
        t.set(r, edgeCount.getName(), g.getEdgeCount());
        t.set(r, edgeNodeRatio.getName(),
                g.getEdgeCount() * 1.0 / g.getNodeCount());
    }
}
