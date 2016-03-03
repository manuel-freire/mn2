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

import edu.umd.cs.hcil.manynets.model.PGraph.NodeTableWrapper;
import edu.umd.cs.hcil.manynets.model.SchemaInstance.EntityTableWrapper;

/**
 * A reference to a node. Since tables may be copied over when they suffer
 * changes, nodeRefs are designed to survive these copies.
 *
 * @author Manuel Freire
 */
public class NodeRef extends Ref<NodeTableWrapper> {

    /**
     * Build a NodeRef given its datasource (pg), its node table, and the
     * node ID.
     * @param pg
     * @param tw
     * @param id
     */
    public NodeRef(PGraph pg, NodeTableWrapper tw, int id) {
        super(pg, tw, id);
    }

    @Override
    public Ref getParentRef() {
        return ((PGraph)source).getRef();
    }

    public Ref<EntityTableWrapper> getEntityRef() {
        return ((PGraph)source).getEntityRef(id);
    }
}
