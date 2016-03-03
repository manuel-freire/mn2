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

package edu.umd.cs.hcil.manynets.hist;

import edu.umd.cs.hcil.manynets.model.Distribution;
import java.util.Collection;


/**
 * A generic histogram model, supporting datapoint labelling and efficient
 * range-count operations 
 *
 * @author mfreire
 */
public interface HistogramModel extends Distribution {

    /**
     * Returns the minimum bin width that can possibly capture everything
     * Computed simplistically as the smallest gap between two distinct values
     */
    public double getMinBinSize();

    /**
     * returns true if this histogram contains no datapoints at all
     */
    public boolean isEmpty();

    /**
     * get all points with the given label
     * @param f
     */
    public Collection<Double> getValuesForLabel(Object label);

    /**
     * return all labels for given value
     * @param f
     * @return
     */
    public Collection<Object> getLabelsForValue(double d);

    /**
     * get the number of distinct values in the model
     * @return
     */
    public int getValueCount();

    /**
     * nearest datapoint to given value
     * @param f
     * @return
     */
    public double getNearestValue(double d);

    /**
     * nearest datapoint to given value in positive direction
     * @param f
     * @return
     */
    public double getNearestValueUp(double f);

    /**
     * nearest datapoint to given value in negative direction
     * @param f
     * @return
     */
    public double getNearestValueDown(double f);

    /**
     * count all values between min (inclusive) and max (exclusive)
     * @param min
     * @param max
     * @return
     */
    public int count(double min, double max);
    
    /**
     * labels for all values between low and high
     * @param low
     * @param high
     * @return
     */
    public Collection<Object> getLabelsBetween(double low, double high);
}
