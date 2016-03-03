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
import java.util.HashSet;
import java.util.Iterator;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Table;

/**
 * Edge duplicates and duplicate-ignoring density.
 * Level is Network.
 * @author Manuel Freire
 */
public class EdgeDuplicateCalculator extends AbstractCalculator {

    public static Stat dupEdgeCount = new Stat("Duplicate edge count",
                "Number of duplicate edges", 
                Integer.TYPE, Level.Network, Level.Network);
    public static Stat edgeDensity =  new Stat("Edge density",
                "Ratio of existing vs. possible non-duplicate edges",
                Double.TYPE, Level.Network, Level.Network);

    public EdgeDuplicateCalculator() {
        super(new Stat[] {dupEdgeCount, edgeDensity}, "O(E)");
    }

    @Override
    public void calculate(TableWrapper tw, int gid) {
        int r = tw.getRow(gid);
        Graph g = ((Population.PopulationTableWrapper)tw)
                .getGraph(gid).getGraph();

        checkAddStat(dupEdgeCount, tw);
        checkAddStat(edgeDensity, tw);
        int de = 0;
        HashSet<String> hs = new HashSet<String>();
        for (Iterator it = g.edges(); it.hasNext(); /**/) {
            Edge e = (Edge)it.next();
            int a = e.getSourceNode().getRow();
            int b = e.getTargetNode().getRow();
            if ( ! g.isDirected() && (a > b)) {
                if ( ! hs.add("" + b + "_" + a)) de ++;
            } else {
                if ( ! hs.add("" + a + "_" + b)) de ++;
            }
        }
               
        int n = g.getNodeCount();
        int total = g.isDirected() ? n*(n-1) : (n*(n-1)) / 2;
        Table t = tw.getTable();
        checkAddStat(dupEdgeCount, tw);
        checkAddStat(edgeDensity, tw);
        t.set(r, dupEdgeCount.getName(), de);
        t.set(r, edgeDensity.getName(),
                (g.getEdgeCount() - de) * 1.0 / total);
    }
}
