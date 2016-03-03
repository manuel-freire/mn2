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

package edu.umd.cs.hcil.manynets.model;

import edu.umd.cs.hcil.manynets.model.PGraph.EdgeTableWrapper;
import edu.umd.cs.hcil.manynets.model.SchemaInstance.RelTableWrapper;

/**
 * An edge reference, similar in goals to a node reference. Notice that edges
 * are assigned an ID (different from their row number) when loaded into
 * ManyNets. Otherwise, checking for duplicate edges is impossible...
 *
 * @author Manuel Freire
 */
public class EdgeRef extends Ref<EdgeTableWrapper> {

    /**
     * Build a EdgeRef given its datasource (pg), its edge table, and the
     * node ID.
     * @param pg
     * @param tw
     * @param id
     */
    public EdgeRef(PGraph pg, EdgeTableWrapper tw, int id) {
        super(pg, tw, id);
    }

    public boolean isDirected() {
        return ((PGraph)source).getGraph().isDirected();
    }

    @Override
    public Ref getParentRef() {
        return ((PGraph)source).getRef();
    }

    public Ref<RelTableWrapper> getRelationshipRef() {
        return ((PGraph)source).getRelRef(id);
    }   
}
