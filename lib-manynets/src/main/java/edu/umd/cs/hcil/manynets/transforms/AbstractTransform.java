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

import edu.umd.cs.hcil.manynets.model.TableWrapper;
import edu.umd.cs.hcil.manynets.model.Transform;
import edu.umd.cs.hcil.manynets.model.TransformOptions;
import edu.umd.cs.hcil.manynets.model.TransformResults;
import java.util.Set;

/**
 * Simplifies definition of new transforms.
 *
 * @author Manuel Freire
 */
public abstract class AbstractTransform implements Transform {

    protected double overallDelta;
    protected TableWrapper tw;
    protected TransformOptions options;
    protected TransformResults results;
    protected Set<Integer> restrictedTo;

    // handy to perform synchronization within 'apply'
    protected final Object mutex = new Object();

    // US spelling. Britons use 'cancelled' instead
    protected boolean canceled = false;

    @Override
    public void init(TableWrapper tw,
            TransformOptions options, TransformResults results) {
        this.tw = tw;
        this.results = results;
        this.options = options;
    }

    // restrict the transform to this set of ids
    @Override
    public void restrictTo(Set<Integer> ids) {
        this.restrictedTo = ids;
    }

    @Override
    public boolean shouldProcess(int id) {
        return restrictedTo == null || restrictedTo.contains(id);
    }

    @Override
    public void cancel() {
        canceled = true;
    }

    @Override
    public TableWrapper getResultWrapper() {
        return tw;
    }

    @Override
    public TransformOptions getOptions() {
        return options;
    }

    @Override
    public TransformResults getResults() {
        return results;
    }
}
