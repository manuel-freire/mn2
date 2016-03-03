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


import edu.umd.cs.hcil.manynets.model.PGraph.NodeTableWrapper;
import edu.umd.cs.hcil.manynets.model.Stat;
import edu.umd.cs.hcil.manynets.model.TableWrapper;
import edu.umd.cs.hcil.manynets.model.TableWrapper.Level;
import prefuse.data.Node;
import prefuse.data.Table;

/**
 * Basic vertex stats: in/out/both degree.
 * Level is Node.
 * @author Manuel Freire
 */
public class VertexDegreeCalculator extends AbstractCalculator {

    private boolean directed = false;

    public static Stat inDegreeN = new Stat("In-degree",
                    "Node in-degree", Integer.TYPE, 
                    Level.Node, Level.Node);
    public static Stat outDegreeN = new Stat("Out-degree",
                    "Node out-degree", Integer.TYPE, 
                    Level.Node, Level.Node);
    public static Stat degreeN = new Stat("Degree",
                    "Node degree", Integer.TYPE, 
                    Level.Node, Level.Node);

    public VertexDegreeCalculator(boolean directed) {
        super(directed ?
            new Stat[] {inDegreeN, outDegreeN, degreeN} :
            new Stat[] {degreeN},
            "O(1)");
        this.directed = directed;
    }

    @Override
    public void calculate(TableWrapper tw, int id) {
        
        checkAddStat(degreeN, tw);
        if (directed) {
            checkAddStat(inDegreeN, tw);
            checkAddStat(outDegreeN, tw);
        }

        Node n = ((NodeTableWrapper)tw).getNode(id);
        Table t = tw.getTable();

        int d = n.getDegree();
        t.set(n.getRow(), degreeN.getName(), d);
        if (directed) {
            d = n.getInDegree();
            t.set(n.getRow(), inDegreeN.getName(), d);
            d = n.getOutDegree();
            t.set(n.getRow(), outDegreeN.getName(), d);
        }
    }
}
