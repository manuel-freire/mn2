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

import edu.umd.cs.hcil.manynets.Utils;
import edu.umd.cs.hcil.manynets.model.TableWrapper;
import edu.umd.cs.hcil.manynets.model.TransformOptions;
import edu.umd.cs.hcil.manynets.model.TransformResults;
import prefuse.data.Tuple;

/**
 * Retain only those rows that satisfy a filter.
 *
 * @author Manuel Freire
 */
public class ElementFilterTransform extends AbstractTransform {

    private TableWrapper next;

    @Override
    public void init(TableWrapper tw, TransformOptions options, TransformResults results) {
        super.init(tw, options, results);

        // FIXME: would need to create a copy of the passed-in TW, of the same type.
        next = null;
    }

    /**
     * Filters a given graph.
     *
     * @param id
     */
    @Override
    public synchronized void apply(int id, ProgressMonitor pm) {
        ElementFilterOptions opts = (ElementFilterOptions)options;
        pm.setProgress(id, "filtering", 0);
                
        if (opts.accepts(tw.getTuple(id))) {
            Utils.copyRow(tw.getTable(), next.getTable(), tw.getRow(id));
        }

        pm.setProgress(id, "finished", 1);
    }

    @Override
    public TableWrapper getResultWrapper() {
        return next;
    }

    public interface ElementFilterOptions extends TransformOptions {
        public boolean accepts(Tuple t);
    }    
}
