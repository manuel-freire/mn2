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
import edu.umd.cs.hcil.manynets.model.DistributionFactory.LookupValueMapper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A generic histogram model, supporting datapoint labelling and efficient
 * range-count operations 
 *
 * @author mfreire
 */
public class DefaultHistogramModel implements HistogramModel {

    // the actual data that gets displayed; can be optionally highlighted
    private TreeMap<Double,DataPoint> data
            = new TreeMap<Double,DataPoint>();

    // reverse mapping from labels to points
    private HashMap<Object,ArrayList<Double>> labels
            = new HashMap<Object,ArrayList<Double>>();

    // mapper from points to point-labels (good for, eg, categorical data)
    private ValueMapper valueMapper;

    private double med = Double.NaN;
    private double avg = Double.NaN;
    private double variance = Double.NaN;
    private double kurtosis = Double.NaN;
    private double skewness = Double.NaN;
    private double KSstat = Double.NaN;
    private double binSize = Double.NaN;

    @Override
    public String toString() {
        return "<distribution>";
    }

    /**
     * hook for external operations on this data-type; ugly but necessary
     * @return
     */
    public TreeMap<Double, DataPoint> getData() {
        return data;
    }

    public void applyValueMapper(DefaultHistogramModel dhm) {
        LookupValueMapper next = (LookupValueMapper)dhm.valueMapper;
        LookupValueMapper mine = (LookupValueMapper)valueMapper;
        if (next.isCompatible(mine)) {
            return;
        }
        
        DefaultHistogramModel merged = new DefaultHistogramModel();
        merged.setValueMapper(next);
        ArrayList al = new ArrayList(1);
        al.add(null);
        for (Entry<Double,DataPoint> e : points()) {
            al.set(0, e.getKey());
            merged.addValues(next.getObjectValue(mine.getValueLabel(e.getKey())),
                    al, e.getValue().count);
        }
        data = merged.data;
        labels = merged.labels;
        valueMapper = next;
    }

    public void dump() {
        System.err.println("Displaying " + data.size() + " values...");
        for (Entry<Double,DataPoint> e : points()) {
            System.err.println("\t" + e.getKey() + " " + e.getValue().count);
        }
    }

    public void union(DefaultHistogramModel d) {

        if (d == null) return;

        if (valueMapper == null || ! d.isNominal()
                || valueMapper.isCompatible(d.valueMapper)) {
            valueMapper = d.valueMapper;
            for (Entry<Double,DataPoint> e : d.points()) {
                addValues(e.getKey(), e.getValue().labels, e.getValue().count);
            }
        } else {
            // if (true) throw new IllegalArgumentException();
            // nominal value mapper; must re-encode everything
            DefaultHistogramModel merged = new DefaultHistogramModel();
            LookupValueMapper mine = (LookupValueMapper)valueMapper;
            LookupValueMapper his = (LookupValueMapper)d.valueMapper;
            LookupValueMapper next = new LookupValueMapper(mine, his);
            merged.setValueMapper(next);
            for (Entry<Double,DataPoint> e : points()) {
                merged.addValues(next.getObjectValue(mine.getValueLabel(e.getKey())),
                        e.getValue().labels, e.getValue().count);
            }
            for (Entry<Double,DataPoint> e : d.points()) {
                merged.addValues(next.getObjectValue(mine.getValueLabel(e.getKey())),
                        e.getValue().labels, e.getValue().count);
            }

            // System.err.println("Full merge complete; from " +
            //        data.keySet().size() + ", " + dhm.data.keySet().size() + " to " + merged.data.keySet().size());
            data = merged.data;
            labels = merged.labels;
            valueMapper = next;
        }
    }

    public Set<Entry<Double,DataPoint>> points() {
        return data.entrySet();
    }

    @Override
    public int compareTo(Distribution o) {
        return Double.compare(getAvg(), o.getAvg());
    }

    public void clear() {
        avg = med = Double.NaN;
        data.clear();
        labels.clear();
    }
    
    public void addValue(double f, Object label) {
        DataPoint v = data.get(f);
        if (v == null) {
            v = new DataPoint();
            data.put(f, v);
        }
        if (label != null) {
            ArrayList<Double> fs = labels.get(label);
            if (fs == null) {
                fs = new ArrayList<Double>();
                labels.put(label, fs);
            }
            fs.add(f);
        }
//        System.err.print(" add increament ");
        v.increment(label);
    }

    public void addValues(double f, ArrayList<Object> ls, int count) {
        DataPoint v = data.get(f);
        if (v == null) {
            v = new DataPoint();
//            System.err.print(" new dp ");
            data.put(f, v);
        }

        if (ls != null) {
            for (Object label : ls) {
                ArrayList<Double> fs = labels.get(label);
                if (fs == null) {
                    fs = new ArrayList<Double>();
                    labels.put(label, fs);
                }
                fs.add(f);
            }
            v.add(ls, count);
//            System.err.println(" ls adding " +count);
        } else {           
            v.add(null, count);
//            System.err.println(" null adding " +count);
        }
    }

    @Override
    public int count(double min, double max) {
        int n = 0;
        if (min > max) {
            System.err.println("Error: min > max in HistogramModel.count()");
            return 0;
        }
        
        for (Entry<Double, DataPoint> e : data.subMap(min, max).entrySet()) {
            n += e.getValue().count;
        }
        
        return n;
    }

    /**
     * Both endpoints inclusive
     * @param low
     * @param high
     * @return
     */
    @Override
    public ArrayList<Object> getLabelsBetween(double low, double high) {
        ArrayList<Object> result = new ArrayList<Object>();
        for (Map.Entry<Double, DataPoint> e : data.subMap(low, true, high, true).entrySet()) {
            result.addAll(e.getValue().labels);
        }
        return result;
    }

    @Override
    public Collection<Double> getValuesForLabel(Object label) {
        ArrayList<Double> f = labels.get(label);
        return (f == null) ? null : f;
    }

    @Override
    public ArrayList<Object> getLabelsForValue(double f) {
        DataPoint v = data.get(f);
        // if (v != null) System.err.println(v.labels.size() + "labels found for " + f);
        return (v != null) ? v.labels : null;
    }


    @Override
    public double getMin() {
        if (data.isEmpty()) return 0;
        if(Double.isNaN(data.firstKey() ) ){
         return 0;
        }
        return data.firstKey();
    }

    @Override
    public double getMax() {
        if (data.isEmpty()) return 0;
        if(Double.isNaN(data.lastKey() ) ){
         return 0;
        }
        return data.lastKey();
    }

    @Override
    public int getValueCount() {
        return data.size();
    }

    public int getValueCount(double f) {
        return data.get(f).count;
    }

    @Override
    public double getNearestValueUp(double f) {
        SortedMap<Double,DataPoint> m = data.subMap(f, Double.POSITIVE_INFINITY);
        Iterator<Double> ki = m.keySet().iterator();
        return (m.isEmpty()) ?
            Double.POSITIVE_INFINITY : ki.next();
    }

    @Override
    public double getNearestValueDown(double f) {
        SortedMap<Double,DataPoint> m = data.subMap(Double.NEGATIVE_INFINITY, f);
        return (m.isEmpty()) ?
            Double.NEGATIVE_INFINITY : m.lastKey();
    }

    @Override
    public double getNearestValue(double f) {
        double higher = getNearestValueUp(f);
        double lower = getNearestValueDown(f);
        return (f - lower > higher - f) ? higher : lower;
    }

    /**
     * Used to set a mapping from values to labels, to be used when the histogram
     * is produced from categorical data (strings, dates, ...)
     * @param mapping
     */
    public void setValueMapper(ValueMapper mapper) {
        this.valueMapper = mapper;
    }

    /**
     * Returns the current valueMapper assigned to this DHM
     * @return the current valueMapper
     */
    public ValueMapper getValueMapper() {
        return valueMapper;
    }

    public Object getValueLabel(double f) {
        return valueMapper == null ? f : valueMapper.getValueLabel(f);
    }

    public double getMappedValue(Object o) {
        return ((LookupValueMapper)valueMapper).getObjectValue(o);
    }

    /**
     * Returns true if this distribution was *not* built from numbers or dates,
     * and therefore uses a value mapper to keep the labels for its values
     * @return
     */
    public boolean isNominal() {
        return valueMapper instanceof LookupValueMapper;
    }

    /**
     * returns an iterator over the possible values.
     * Values will be returned in ascending order.
     * @return
     */
    public ArrayList<Object> getValues() {
        ArrayList<Object> values = new ArrayList<Object>();
        for (Double d : data.keySet()) {
            values.add(valueMapper != null ? valueMapper.getValueLabel(d) : d);
        }
        return values;
    }
    
    /**
     * Returns true if there are no datapoints in this distribution
     * @return
     */
    public boolean isEmpty() {
        return data.isEmpty();
    }

    /**
     * Returns the minimal gap between different values in this distribution,
     * and the number of bins that use that number
     *
     * @return
     */
    @Override
    public double getMinBinSize() {
        if (Double.isNaN(binSize)) {
            double prev = Double.NEGATIVE_INFINITY;
            binSize = Double.POSITIVE_INFINITY;
            for (double d : data.keySet()) {
                binSize = Math.min(binSize, Math.abs( d - prev));
                prev = d;
            }
        }
        return binSize;
    }



    @Override
    public double getMed() {
        if (Double.isNaN(med)) {
            med = 0;
            int n = count(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY) / 2;
            for (Entry<Double, DataPoint> di : data.entrySet()) {
                n -= di.getValue().count;
                if (n <= 0) {
                    med = di.getKey();
                    break;
                }
            }
        }
        return med;
    }

    /**
     * Computes (if needed) median and average in a single pass; returns avg
     * @return
     */
    @Override
    public double getAvg() {
        if (Double.isNaN(avg)) {
            avg = getSubAvg(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        }
        return avg;
    }

    private double getSubAvg(double min, double max) {
        double a=0;
        int n=0;
        for (Entry<Double, DataPoint> di : data.entrySet()) {
            double v = di.getKey();
            if (v < min) continue;
            if (v >= max) break;
            a += v * di.getValue().count;
            n += di.getValue().count;
        }
        return a/n;
    }

    public double getBimodality() {
// FIXME: broken
//            med = 0;
//            int n = count(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY) / 2;
//            for (Entry<Double, DataPoint> di : data.entrySet()) {
//                n -= di.getValue().count;
//                if (n <= 0) {
//                    med = di.getKey();
//                    break;
//                }
//            }
        getMed();
        double la = getSubAvg(Double.NEGATIVE_INFINITY, med);
        double ha = getSubAvg(med, Double.POSITIVE_INFINITY);
        return Double.isNaN(la) ? 0 : ha - la;
    }


 //kurtosis, skewness formula from http://www.itl.nist.gov/div898/handbook/eda/section3/eda35b.htm

    public double getKurtosis() {

        if (Double.isNaN(kurtosis)) {
            kurtosis = 0;

            double average = getAvg();
            int n = count(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
            double v;
            for (Entry<Double,DataPoint> e : points()) {
                if (Double.isNaN(e.getKey())) {
                    n -= e.getValue().count;
                    continue;
                }
                v = Math.pow((average - e.getKey()), 4);
                kurtosis += v*e.getValue().count;
            }
            kurtosis /= (n - 1) * getVariance() * getVariance();
            kurtosis -= 3;  //  as normal dist has kurtosis 3 .
        }

        return kurtosis;
    }

    /**
     * returns the skewness of the distribution
     */
    public double getSkewness() {

        if (Double.isNaN(skewness)) {
            skewness = 0;

            double average = getAvg();
            int n = count(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
            for (Entry<Double,DataPoint> e : points()) {
                if (Double.isNaN(e.getKey())) {
                    n -= e.getValue().count;
                    continue;
                }
                double v = Math.pow((average - e.getKey()), 3);
                skewness += v*e.getValue().count;
            }
            skewness /= (n - 1) * Math.pow(getStandardDeviation(), 3);
        }

        return skewness;
    }

    /**
     * returns the variance of the distribution
     */
    public double getVariance() {
        if (Double.isNaN(variance)) {
            variance = 0;

            double average = getAvg();
            int n = count(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
            for (Entry<Double,DataPoint> e : points()) {
                if (Double.isNaN(e.getKey())) {
                    n -= e.getValue().count;
                    continue;
                }
                double v = Math.pow((average - e.getKey()), 2);
                variance += v*e.getValue().count;
            }
            variance /= n;
        }

        return variance;
    }

    /**
     * returns the standard deviation of the distribution
     */
    public double getStandardDeviation() {
        return Math.sqrt(getVariance());
    }
    
//-----------------------------------

    public interface ValueMapper {
        /**
         * Map a foat value to a categorical label (can also be used for
         * certain non-categorical data, such as time); notice that -1 is used
         * to denote an 'unmapped' value, and should always return label 'null'
         * @param f
         * @return
         */
        public Object getValueLabel(double f);
        public boolean isCompatible(ValueMapper vm);
    }

    public static class DateValueMapper implements ValueMapper {
        private static SimpleDateFormat sdf
                = new SimpleDateFormat("yyyy.MM.dd'@'HH:mm:ss");
        @Override
        public Object getValueLabel(double f) {
            long t = (long)(f);
            return t == -1 ? null : sdf.format(new Date(t));
        }
        @Override
        public boolean isCompatible(ValueMapper vm) {
            return vm instanceof DateValueMapper;
        }
    }

    /**
     * Used internally within iterators to provide simple serial access
     */
    private static class ReusableEntry implements Entry<Object, Double> {
        private Object o;
        private double d;
        public Object getKey() { return o; }
        public Double getValue() { return d; }
        public Double setValue(Double value) {
            throw new UnsupportedOperationException("Read-only, sorry");
        }
    }

    /**
     * A single data-point, used to map points->labels (by definition,
     * labels->points cannot have a count).
     */
    public static class DataPoint {
        public int count = 0;
        public ArrayList labels = new ArrayList();
        public void increment(Object label) {
//            System.err.println("incremented!");
            increment(label, 1);
        }
        public void increment(Object label, int n) {
            count += n;
            if (label != null) labels.add(label);
        }
        public void add(ArrayList<Object> ls, int n) {
            count += n;
//            System.err.print(" !count= " + count );

            if (ls != null) labels.addAll(ls);
        }
    }
}
