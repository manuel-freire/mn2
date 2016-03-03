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

/**
 * Merges several nets together into a larger net. The graphs must share
 * node names. The OID and Source columns (or OID alone, if no Source exists) of
 * each network will be used for the merge.
 *
 * @author mfreire
 */
public class MergeTransform extends AbstractTransform {

    @Override
    public void apply(int id, ProgressMonitor pm) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
