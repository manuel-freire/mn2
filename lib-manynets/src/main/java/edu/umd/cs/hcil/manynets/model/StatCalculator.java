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

/**
 * A stat calculator.
 * A single calculator may write into only a single level.
 *
 * Notice that StatCalculators have no formal depencencies with tables
 */
public interface StatCalculator {

    /**
     * Returns the complexity of this stat calculator. Examples: O(E + V)
     * @return
     */
    public String getComplexity();

    /**
     * Calculate this stat for the element identified with 'id' in the
     * specified TableWrapper (of the same level as the Stat itself(
     * @param id
     */
    public void calculate(TableWrapper tw, int id);

    /**
     * force this calculation to end, now.
     */
    public void cancel();
}
