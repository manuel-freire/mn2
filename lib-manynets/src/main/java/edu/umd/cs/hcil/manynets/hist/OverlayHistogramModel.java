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
import java.util.ArrayList;
import java.util.Collection;

/**
 * A histogram model that keeps references to sub-histograms to avoid
 * duplicating their data.
 *
 * @author Manuel Freire
 */
public abstract class OverlayHistogramModel implements HistogramModel {

    protected ArrayList<HistogramModel> hists =
            new ArrayList<HistogramModel>();

    public void clear() {
        hists.clear();
    }

    public void add(HistogramModel model) {
        hists.add(model);
    }

    @Override
    public boolean isEmpty() {
        for (HistogramModel h : hists) {
            if ( ! h.isEmpty()) return false;
        }
        return true;
    }

    /**
     * Returns the minimal gap between different values in this distribution,
     * and the number of bins that use that number
     *
     * @return
     */
    @Override
    public double getMinBinSize() {
        double binSize = Double.POSITIVE_INFINITY;
        for (HistogramModel h : hists) {
            if (h.isEmpty()) continue;
            binSize = Math.min(binSize, h.getMinBinSize());
        }
        return binSize;
    }

    @Override
    public Collection<Double> getValuesForLabel(Object label) {
        ArrayList<Double> al = new ArrayList<Double>();
        for (HistogramModel h : hists) {
            al.addAll(h.getValuesForLabel(label));
        }
        return al;
    }

    @Override
    public Collection<Object> getLabelsForValue(double f) {
        ArrayList<Object> al = new ArrayList<Object>();
        for (HistogramModel h : hists) {
            al.addAll(h.getLabelsForValue(f));
        }
        return al;
    }

    @Override
    public double getMin() {
        double min = Float.MAX_VALUE;
        for (HistogramModel h : hists) {
            min = Math.min(h.getMin(), min);
        }
        return min;
    }

    @Override
    public double getMax() {
        double max = Float.MIN_VALUE;
        for (HistogramModel h : hists) {
            max = Math.max(h.getMax(), max);
        }
        return max;
    }

    @Override
    public double getMed() {
        return getAvg();
    }

    @Override
    public double getAvg() {
        double avg = 0;
        int t = 0;
        for (HistogramModel h : hists) {
            int n = h.count(Float.MIN_VALUE, Float.MAX_VALUE);
            avg += h.getAvg() * n;
            t += n;
        }
        avg = avg / t;
        return avg;
    }

    @Override
    public int getValueCount() {
        int t = 0;
        for (HistogramModel h : hists) {
            t += h.getValueCount();
        }
        return t;
    }

    @Override
    public double getNearestValue(double f) {
        double best = Float.POSITIVE_INFINITY;
        for (HistogramModel h : hists) {
            double candidate = h.getNearestValue(f);
            if (Math.abs(f-candidate) < Math.abs(best-candidate)) {
                best = candidate;
            }
        }
        return best;
    }

    @Override
    public double getNearestValueUp(double f) {
        double best = Float.POSITIVE_INFINITY;
        for (HistogramModel h : hists) {
            double candidate = h.getNearestValueUp(f);
            if (Math.abs(f-candidate) < Math.abs(best-candidate)) {
                best = candidate;
            }
        }
        return best;
    }

    @Override
    public double getNearestValueDown(double f) {
        double best = Float.POSITIVE_INFINITY;
        for (HistogramModel h : hists) {
            double candidate = h.getNearestValueDown(f);
            if (Math.abs(f-candidate) < Math.abs(best-candidate)) {
                best = candidate;
            }
        }
        return best;
    }

    @Override
    public abstract int count(double min, double max);

    @Override
    public Collection<Object> getLabelsBetween(double low, double high) {
        ArrayList<Object> al = new ArrayList<Object>();
        for (HistogramModel h : hists) {
            al.addAll(h.getLabelsBetween(low, high));
        }
        return al;
    }

    @Override
    public int compareTo(Distribution o) {
        return Double.compare(getAvg(), o.getAvg());
    }
}
