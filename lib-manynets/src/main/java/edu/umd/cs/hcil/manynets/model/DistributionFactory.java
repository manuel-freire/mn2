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

import edu.umd.cs.hcil.manynets.ui.ExtendedComparator;
import edu.umd.cs.hcil.manynets.hist.DefaultHistogramModel;
import edu.umd.cs.hcil.manynets.hist.DefaultHistogramModel.DateValueMapper;
import edu.umd.cs.hcil.manynets.hist.DefaultHistogramModel.ValueMapper;
import edu.umd.cs.hcil.manynets.hist.HistogramModel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.util.collections.IntIterator;

/**
 *
 * @author Manuel Freire
 */
public class DistributionFactory {

    /**
     * Build a distribution from a String array; try to sort by numerical values
     * if present within String, using special comparator.
     */
    public static HistogramModel build(String[] a, Object label) {
        DefaultHistogramModel dhm = new DefaultHistogramModel();
        TreeMap<String, Integer> m = new TreeMap<String, Integer>(new ExtendedComparator());
        for (int i=0; i<a.length; i++) {
            Integer prev = m.get(a[i]);
            if (prev == null) prev = 0;
            m.put(a[i], 1 + prev);
        }

        int i=0;
        for (Entry<String, Integer> e : m.entrySet()) {
            for (int j=0; j<e.getValue(); j++) {
                dhm.addValue(i, label);
            }
            i ++;
        }
        return dhm;
    }

    /**
     * Build a distribution from a double array
     */
    public static HistogramModel build(double[] a, Object label) {
        DefaultHistogramModel dhm = new DefaultHistogramModel();
        for (double d : a) { dhm.addValue((float)d, label); }
        return dhm;
    }

    /**
     * Build a distribution from an int array
     */
    public static HistogramModel build(int[] a, Object label) {
        DefaultHistogramModel dhm = new DefaultHistogramModel();
        for (int i : a) { dhm.addValue(i, label); }
        return dhm;
    }

    /**
     * Build a distribution from a table column
     */
    public static HistogramModel build(TableWrapper tw, int col) {
        
        // This is a bug in prefuse: tuples() from a CascadedTable is broken.
        return build(tw, col, tw.getTable().tuples());
    }

    public static ValueMapper getValueMapper(TableWrapper tw, int col) {
        Table t = tw.getTable();
        String field = t.getColumnName(col);
        if (tw.getValueMapper(field) == null) {
            ValueMapper mapper = null;
            if (tw.getTable().getColumnType(col).equals(Integer.TYPE)) {
                mapper = new IntValueMapper();
            } else if (tw.getTable().getColumnType(col).equals(Date.class)) {
                mapper = new DateValueMapper();
            } else {
                mapper = new LookupValueMapper(tw, col, -1);
            }
            tw.setValueMapper(field, mapper);
        }
        
        return tw.getValueMapper(field);
    }

    /**
     * Build a distribution from an iterator (obtained via predicate, or
     * through querying a node for its edges). Reuses the specified mapping.
     */
    public static HistogramModel build(TableWrapper tw, int col, Iterator ti) {

        
        DefaultHistogramModel dhm = new DefaultHistogramModel();
          Class type = tw.getTable().getColumnType(col);

        if (Distribution.class.isAssignableFrom(type)) {
            ArrayList<Tuple> al = new ArrayList<Tuple>();
            while( ti.hasNext()) {
                Tuple t = (Tuple)ti.next();
                al.add(t);
                dhm.union((DefaultHistogramModel)t.get(col));
            }
            if (dhm.isNominal()) {
                for (Tuple t : al) {
                    DefaultHistogramModel m = (DefaultHistogramModel)t.get(col);
                    if (m != null) {
                        m.applyValueMapper(dhm);
                    }
                }
            }
        } else if (type.equals(Double.TYPE)) {
            while (ti.hasNext()) {
                Tuple t = (Tuple)ti.next();
                Object v = t.get(col);
                dhm.addValue((float) ((type == Integer.TYPE) ?
                    (int)(Integer)v : (double)(Double)v),
                    tw.getRef(tw.getId(t.getRow())));
            }
            // no value mapper required for doubles
        } else if (type.equals(Integer.TYPE)) {
            while (ti.hasNext()) {
                Tuple t = (Tuple)ti.next();
                Object v = t.get(col);
                dhm.addValue((float) ((type == Integer.TYPE) ?
                    (int)(Integer)v : (double)(Double)v),
                    tw.getRef(tw.getId(t.getRow())));
            }
            dhm.setValueMapper(getValueMapper(tw, col));
        } else if (type.equals(Date.class)) {
            while( ti.hasNext()) {
                Tuple t = (Tuple)ti.next();
                Date v = t.getDate(col);
                dhm.addValue(v.getTime(), tw.getRef(tw.getId(t.getRow())));
            }
            dhm.setValueMapper(getValueMapper(tw, col));
        } else {
            LookupValueMapper lvm = (LookupValueMapper)getValueMapper(tw, col);
            while (ti.hasNext()) {
                Tuple t = (Tuple)ti.next();
                Object o = t.get(col);
                dhm.addValue(lvm.getObjectValue(o), tw.getRef(tw.getId(t.getRow())));
            }
            dhm.setValueMapper(lvm);
        }
        return dhm;
    }

    /**
     * A value mapper for integer data, bidirectional
     */
    public static class IntValueMapper implements ValueMapper {
        @Override
        public Object getValueLabel(double f) {
            return (int)f;
        }

        @Override
        public boolean isCompatible(ValueMapper vm) {
            return vm instanceof IntValueMapper;
        }
    }

    /**
     * A value mapper for categorical data, mainly strings. Bidirectional.
     */
    public static class LookupValueMapper implements ValueMapper {

        private Map<Object, Integer> m;
        private Map<Double, Object> rm = new TreeMap<Double, Object>();
        private int nullValue = -1;

        public LookupValueMapper(Object[] values, Class type) {
            m = Comparable.class.isAssignableFrom(type) ?
                (type.equals(String.class) ?
                    new TreeMap<Object, Integer>(new ExtendedComparator())
                    : new TreeMap<Object, Integer>())
                : new HashMap<Object, Integer>();

            boolean hasNulls = false;
            for (Object o : values) {
                Integer v = ((o == null) ? Integer.valueOf(-1) : m.get(o));
                if (v == null) {
                    m.put(o, null);
                } else if (v == -1) {
                    hasNulls = true;
                }
            }
            int i=0;
            for (Entry<Object, Integer> e : m.entrySet()) {
                e.setValue(i++);
                rm.put((double)e.getValue(), e.getKey());
            }
            if (hasNulls) {
                rm.put((double)nullValue, null);
            }
        }

        public LookupValueMapper(TableWrapper tw, int col, int nullValue) {
            // FIXME: nullValue parameter currently being ignored
            this.nullValue = -1;
            Table t = tw.getTable();
            Class type = t.getColumnType(col);

            m = Comparable.class.isAssignableFrom(type) ?
                (type.equals(String.class) ?
                    new TreeMap<Object, Integer>(new ExtendedComparator())
                    : new TreeMap<Object, Integer>())
                : new HashMap<Object, Integer>();

            boolean hasNulls = false;

            for (IntIterator ii = t.rows(); ii.hasNext(); /**/) {
                int i = ii.nextInt();
                Object o = t.get(i, col);
                Integer v = ((o == null) ? Integer.valueOf(-1) : m.get(o));
                if (v == null) {
                    m.put(o, null);
                } else if (v == -1) {
                    hasNulls = true;
                }
            }            
            int i=0;
            for (Entry<Object, Integer> e : m.entrySet()) {
                e.setValue(i++);
                rm.put((double)e.getValue(), e.getKey());
            }
            if (hasNulls) {
                rm.put((double)nullValue, null);
            }
        }

        /**
         * Builds the union of two value mappers, the first of which must /not/
         * be null.
         * @param a
         * @param b
         */
        public LookupValueMapper(LookupValueMapper a, LookupValueMapper b) {
            m = a.m instanceof HashMap ?
                new HashMap<Object, Integer>() :
                new TreeMap<Object, Integer>(((TreeMap<Object, Integer>)a.m).comparator());

            if (b == null) {
                for (Entry<Object, Integer> e : a.m.entrySet()) {
                    m.put(e.getKey(), e.getValue());
                    rm.put((double)e.getValue(), e.getKey());
                }
                if (a.rm.containsKey((double)nullValue)) {
                    rm.put((double)nullValue, null);
                }
            } else {
                for (Entry<Object, Integer> e : a.m.entrySet()) {
                    m.put(e.getKey(), null);
                }
                for (Entry<Object, Integer> e : b.m.entrySet()) {
                    m.put(e.getKey(), null);
                }
                int i=0;
                for (Entry<Object, Integer> e : m.entrySet()) {
                    e.setValue(i++);
                    rm.put((double)e.getValue(), e.getKey());
                }
                if (a.rm.containsKey((double)nullValue) ||
                        b.rm.containsKey((double)nullValue)) {
                    rm.put((double)nullValue, null);
                }
            }
        }

        @Override
        public boolean isCompatible(ValueMapper vm) {
            if ( ! (vm instanceof LookupValueMapper)) {
                return false;
            }
            LookupValueMapper lvm = (LookupValueMapper)vm;
            for (Entry<Object, Integer> e : m.entrySet()) {
                if (lvm.m.get(e.getKey()) != e.getValue()) return false;
            }
            for (Entry<Object, Integer> e : lvm.m.entrySet()) {
                if (m.get(e.getKey()) != e.getValue()) return false;
            }
            return true;
        }

        @Override
        public Object getValueLabel(double f) {
            return rm.get(f);
        }

        public int getObjectValue(Object o) {
            if (o == null) return nullValue;
            Integer r = m.get(o);
            if (r == null) {
                System.err.println("Added new mapping: unknown " + o);
                r = m.size();
                m.put(o, r);
                rm.put((double)r.intValue(), o);
            }
            return r;
        }
    }
}
