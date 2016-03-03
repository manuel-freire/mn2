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
import edu.umd.cs.hcil.socialaction.jung.statistics.GraphStatistics;
import prefuse.data.Graph;

/**
 * Calculates a graph's diameter. Slow, slow, slow.
 * Level: Network.
 * @author Manuel Freire
 */
public class DiameterCalculator extends AbstractCalculator {

    public static final Stat diameter = new Stat("Diameter",
                "Maximum diameter, all components",
                Double.TYPE, Level.Network, Level.Network);
    
    public DiameterCalculator() {
        super(new Stat[] {diameter}, "O(V^3)");
    }

    @Override
    public void calculate(TableWrapper tw, int gid) {
        int r = tw.getRow(gid);
        Graph g = ((Population.PopulationTableWrapper)tw)
                .getGraph(gid).getGraph();

        checkAddStat(diameter, tw);
        tw.getTable().set(r, diameter.getName(),
                GraphStatistics.diameter(g, true));
    }
}
