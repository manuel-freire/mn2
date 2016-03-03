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
 * A distribution. Can be used to (say) draw histograms.
 * @author Manuel Freire
 */
public interface Distribution extends Comparable<Distribution> {

//    /**
//     * Build the union of two distributions
//     * @param d
//     */
//    public void union(Distribution d);
//
//    /**
//     * Returns once per value; if there are 23 M values, it will return 23 M
//     * times. This is obviously ugly.
//     * @return
//     */
//    public Iterator<Double> values();
//
//    /**
//     * Returns once per value; if there are 23 M values, it will return 23 M
//     * times. This is obviously ugly.
//     * @return
//     */
//    public Iterator<Object> labels();
//
//    /**
//     * Returns once per value; if there are 23 M values, it will return 23 M
//     * times. You get the idea
//     * @return
//     */
//    public Iterator<Map.Entry<Object,Double>> entries();

    public double getMed();
    public double getAvg();
    public double getMin();
    public double getMax();
}