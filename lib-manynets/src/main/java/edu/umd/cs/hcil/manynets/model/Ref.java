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

import edu.umd.cs.hcil.manynets.Utils;
import prefuse.data.Table;
import prefuse.data.Tuple;

/**
 * A reference to a tuple in a TableWrapper in a PGraph
 * @author Manuel Freire
 */
public class Ref<W extends TableWrapper> {
    protected Object source;
    protected W tw;
    protected int id;

    /**
     * Build a Ref 
     * @param parent
     * @param tw
     * @param id
     */
    public Ref(Object source, W tw, int id) {
        this.source = source;
        this.tw = tw;
        this.id = id;
    }

    /**
     * Returns the 'original' tuple for this node
     * @return
     */
    public Tuple getTuple() {        
        return tw.getTuple(id);
    }

    public Table getTable() {
        return tw.getTable();
    }

    public Object getSource() {
        return source;
    }

    /**
     * returns the parent refs of this one, or null if none
     * @return
     */
    public Ref getParentRef() {
        return null;
    }

    public W getTableWrapper() {
        return tw;
    }

    /**
     * @return tuple id
     */
    public int getId() {
        return id;
    }

    /**
     * @return tuple row in model
     */
    public int getRow() {
        return getTuple().getRow();
    }

    @Override
    public int hashCode() {
        return getTable().hashCode() ^ id;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Ref) {
            Ref r = (Ref)o;
            return (r.source == source || r.source.equals(source)) &&
                    getTable().equals(r.getTable()) &&
                    id == r.id;
        } else {
            return false;
        }
    }
}
