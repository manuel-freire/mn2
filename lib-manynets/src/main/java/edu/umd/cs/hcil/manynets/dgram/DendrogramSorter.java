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

package edu.umd.cs.hcil.manynets.dgram;

import edu.umd.cs.hcil.manynets.hist.DefaultHistogramModel;
import edu.umd.cs.hcil.manynets.hist.HistogramStats.DistanceMetric;
import edu.umd.cs.hcil.manynets.hist.HistogramStats.HistogramMetric;
import java.util.ArrayList;

/**
 * Sorts acording to a dendrogram
 * @author sopan
 */
public class DendrogramSorter {

    public int[] sortByDendrogram(ArrayList<DefaultHistogramModel> list,
            HistogramMetric m) {

        DendrogramModel dm = new DendrogramModel(list.size(),
                new DendrogramModel.CompleteLinkage()); // this dm is our tree.
        // build array of distances for nodes; algorithm runs after last is added
        // FIXME: can cut run-time in half...
        //for ( each column of the multicolumn overview , indexof the column= col )
        for (int i = 0; i < list.size(); i++) {
            float[] ds = new float[list.size()];
            for (int j = 0; j < list.size(); j++) {// j=i at init
                ds[j] = (float) ((i == j) ? 0 : m.distance(list.get(i), list.get(j)));
                // FIX ME: for multi column, add distance of all the columns
                // ds[j]= m.distance( list[col].get(i),list[col].get(j) );
            }
            dm.addLeaf(i, ds);
            //System.err.println("added leaf " + i);
        }
        float[] bestDist = new float[1];
        int[] result = dm.getRoot().returnOrder(bestDist);
        return result;
    }

    public int[] sortByDendrogramForMultiColumn(ArrayList<ArrayList> list,
            ArrayList<DistanceMetric> metrics) {

        //number of leaves = list.size
        int leaves = ((ArrayList<DefaultHistogramModel>) list.get(0)).size();
        DendrogramModel dm = new DendrogramModel(leaves,
                new DendrogramModel.CompleteLinkage());

        // this dm is our tree.
        // build array of distances for nodes; algorithm runs after last is added
        // FIXME: can cut run-time in half...
        //for ( each column of the multicolumn overview , indexof the column= col )
        for (int i = 0; i < leaves; i++) {// for each row
            float[] ds = new float[leaves];//its distance to other rows
            for (int j = 0; j < leaves; j++) {// j=i at init
                ds[j] = 0;
                int k = 0;
                for (Object dhm : list) {
                    DistanceMetric m = metrics.get(k);
                    ArrayList<DefaultHistogramModel> al = (ArrayList<DefaultHistogramModel>) dhm;
                    ds[j] += (float) ((i == j) ? 0 : m.distance(al.get(i), al.get(j)));
                    // FIX ME: for multi column, add distance of all the columns
                    // ds[j]= m.distance( list[col].get(i),list[col].get(j) );
                    k ++;
                }
                //System.err.println(ds[j] + ",");
            }
            dm.addLeaf(i, ds);
            //System.err.println("added leaf " + i);
        }

        float[] bestDist = new float[1];
        int[] result = dm.getRoot().returnOrder(bestDist);
        return result;
    }
}
