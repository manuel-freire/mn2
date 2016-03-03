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

import edu.umd.cs.hcil.manynets.transforms.FilterNetsTransform.NetFilterOptions;
import java.util.Arrays;
import java.util.HashSet;
import org.jdom.Element;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

/**
 *
 * @author Manuel Freire
 */
public class Vast09EdgeFilter implements NetFilterOptions {

    private HashSet<String> validEdges = new HashSet<String>();

    public Vast09EdgeFilter() {
        validEdges.addAll(Arrays.asList(new String[] {
//            "AB","BB","AC","BC"
            "AB","BB","AD","BD","AC","BC"
        }));
    }

    public boolean accepts(Graph g, Edge e) {
        return validEdges.contains(e.getString("ET"));
    }

    public boolean accepts(Graph g, Node n) {
        return true;
    }

    public void save(Element e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void load(Element e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getDescription() {
        return "VAST'09 acceptable edges";
    }
}
