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

import java.awt.Graphics;
import java.awt.Dimension;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import javax.swing.JPanel;

/**
 * A histogram component. Renderers and Models are separate entities; the 
 * component itself brings them together, and is expected to support selection
 * operations and tooltips.
 *
 * @author mfreire
 */
public class Histogram extends JPanel {
    public enum Scaling {
        Linear(new LinearHeightFunction()),
        Sqroot(new SqrtHeightFunction()),
        Log(new LogHeightFunction());
        
        private HeightFunction heightFunction;

        Scaling(HeightFunction hf){
            heightFunction = hf;
        }
        public HeightFunction getFunction(){
            return heightFunction;
        }
    }    

    private HistogramModel model;
    private HistogramModel selectionModel;
    private HistogramModel referenceModel;

    private HistogramRenderer renderer;
    private HistogramRenderer selectionRenderer;

    /**
     * set externally from the renderer, both to cache values for further
     * renderings and to enable fast lookup behavior (eg.: how many values at X)
     * levels == bins. Just another name.
     */
    private int levels = -1;
    private boolean dirty = true;

    /**
     * used to store the last-known-good width; without it, recalculation of
     * sizes becomes hard (since getWidth cannot always be relied on due to reuse).
     */
    private int lastWidth = 0;

    private String text;

    protected boolean selActive = false;
    protected int selStart;
    protected int selEnd;
    
    private double[] bars = new double[0];
    private double[] selectionBars = new double[0];

    private int forceMaxCount = -1;
    private int oldMaxCount = -1;
    private double max;
    private double min;

    private HeightFunction heightFunction = new LinearHeightFunction();
    private HeightFunction deltaFunction = new LinearHeightFunction();

    private static HashMap<Class, HistogramRenderer> renderers =
            new HashMap<Class, HistogramRenderer>();
    private static HashMap<Class, HistogramRenderer> selectionRenderers =
            new HashMap<Class, HistogramRenderer>();

    public int getMaxCount() {
        return oldMaxCount;
    }

    /** used to map bars to heights */
    public interface HeightFunction {
        public double f(double v);
        public double i(double v);
    }

    public double getValueForPoint(Point p, Rectangle bounds) {
        return getValueForPoint(p, bounds, false);
    }

    public double getValueForPoint(Point p, Rectangle bounds, boolean selected) {

        double o = denormalize(p.getX()/bounds.width);
        updateCache(levels);
        //System.err.println("entering getValueForPoint... oldLevels = " + oldLevels);

        // now have to convert to bin
        boolean found = false;
        double x0 = stepper.init(min, max, levels);
        for (int i=0; i<levels; i++) {
            double x1 = (double)stepper.next();
            if (x0 <= o && o < x1) {
                o = (x0 + x1) / 2f;
                //System.err.println("Point found: " + o);
                found = true;
                break;
            } else {
              //System.err.println("Not in " + x0 + " " + x1);
            }
            x0 = x1;
        }
        if ( ! found) {
            o = max;
        }

        return selected ?
            selectionModel.getNearestValue(o) : model.getNearestValue(o);
    }

    /**
     * Create a new histogram
     * @param model to use; may be null (nothing gets painted)
     * @param renderClass the renderer to use.
     */
    public Histogram(HistogramModel model, Class renderClass,
            Class selectionRendererClass) {
        setModel(model);
        setRenderer(renderClass, selectionRendererClass);
        if (renderer == null) {
            throw new IllegalArgumentException("No renderer!");
        } else {
//            System.err.println("Built new histogram!");
        }
    }

    public void setForceMaxCount(int maxCount) {
        this.forceMaxCount = maxCount;
        dirty = true;
    }

    public void setHeightFunction(HeightFunction af) {
        this.heightFunction = af;
        dirty = true;
    }

    public HeightFunction getHeightFunction() {
        return heightFunction;
    }

    public void setDeltaFunction(HeightFunction af) {
        this.deltaFunction = af;
        dirty = true;
    }

    public HeightFunction getDeltaFunction() {
        return deltaFunction;
    }

    /**
     * refreshes this histogram (as updateCache is not public). Will do nothing
     * if the histogram's number of levels has not changed.
     * Should be called whenever the histogram (as opposed to the model) is to be queried
     */
    public void refresh() {        
        refresh(false, lastWidth);
    }

    public void refresh(boolean force) {
        refresh(force, lastWidth);
    }

    /**
     * Forces this histogram to recalculate its levels -- strong version of
     * refresh.
     */
    public void refresh(boolean force, int width) {
        if (width == 0) {
            // no point refreshing if not drawable
            return;
        }

        if (force) {
            dirty = true;
        }
        HistogramModel renderCountModel = referenceModel != null ?
            referenceModel : getModel();
        int barCount = renderer.configure(renderCountModel, width);
        // System.err.println("Refreshed to " + barCount);
        updateCache(barCount);
    }

    public static class LinearHeightFunction implements HeightFunction {
        public double f(double v) { return v; };
        public double i(double v) { return v; };
    }

    public static class SqrtHeightFunction implements HeightFunction {
        public double f(double v) { return Math.sqrt(v); };
        public double i(double v) { return v*v; };
    }

    /**
     * actually implements log(v+1), since log(1) == 0, which is ugly
     * when dealing with histograms.
     */
    public static class LogHeightFunction implements HeightFunction {
        public double f(double v) { return Math.log(v+1); };
        public double i(double v) { return Math.exp(v+1); };
    }

    /**
     * Calculates max count, using this histogram's options, for a different
     * histogram's values. Useful to find the absolute highest in a large set
     * of models, without having to use each and every h-gram instance.
     * @param m
     * @param levs
     * @return
     */
    public int calculateMaxCount(HistogramModel m) {
        int maxCount = Integer.MIN_VALUE;
        float x0 = (float)stepper.init(min, max, levels);
        for (int i=0; i<levels; i++) {
            float x1 = (float)stepper.next();
            int count = m.count(x0, x1);
            maxCount = Math.max(maxCount, count);
            x0 = x1;
        }
        return maxCount;
    }

    /**
     * Updates the lookup cache for bar heights, given the current number of
     * levels. Bar heights are given in the range 0-1.
     * @param len
     */
    protected void updateCache(int len) {
        if (! dirty && len == levels && bars.length == levels) {
            // cache was already up to date
            return;
        }

        if (len != levels || bars.length != levels) {
//            System.err.println("Updated levels from " + levels + " to " + len);
            levels = len;
            bars = new double[levels];
            selectionBars = new double[levels];
        } else {
//            System.err.println("No need to update levels, " + levels + " is fine");
        }
//        ((DefaultHistogramModel)model).dump();
//        if (levels < 5) {
//            Thread.dumpStack();
//        }

        if (max == min) {
            // no sense filling in a zero-range hist.
//            System.err.println("avoiding zero-range histogram");
            return;
        }

        // find maxCount, prepare for sanity check
        int maxCount = Integer.MIN_VALUE;
        int expectedTotal =
                model.count(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
        int realTotal = 0;
        double x0 = (double)stepper.init(min, max, levels);
        for (int i=0; i<levels; i++) {
            double x1 = (double)stepper.next();
            int count = model.count(x0, x1);
            realTotal += count;
            if (forceMaxCount == -1) {
                maxCount = Math.max(maxCount, count);
//                System.err.println("nmc = " + maxCount);
            } else {
//                System.err.println("fmc = " + forceMaxCount);
                maxCount = forceMaxCount;
            }
            x0 = x1;
        }

        // sanity check to find unrepresented values; NOT USED in sampling models
        if (realTotal != expectedTotal && (model instanceof DefaultHistogramModel)) {
            System.err.println("Lost " + (expectedTotal - realTotal) + " of " + expectedTotal
                    + " min " + min + " max " + max
                    + " mmin " + model.getMin() + " mmax " + model.getMax() + " in " + levels + " bins");
            if (levels < 1) {
                System.err.println("recalculating for < 1 bins makes no sense; more debugging details:");
                Thread.dumpStack();
            }
            x0 = (double)stepper.init(min, max, levels);
            for (int i=0; i<levels; i++) {
                double x1 = (double)stepper.next();
                int count = model.count(x0, x1);
                System.err.println("\t" + i + "(" + x0 + "," + x1+ ")" +
                        ":" + count);
                x0 = x1;
            }
        }

        // assign bar heights
        double maxBar = heightFunction.f(maxCount);
        x0 = (double)stepper.init(min, max, levels);
        for (int i=0; i<levels; i++) {
            double x1 = (double)stepper.next();
            int count = model.count(x0, x1);
            bars[i] = Math.min(1, heightFunction.f(count) / maxBar);
            x0 = x1;
        }
        if (selectionModel != null) {
            x0 = (double)stepper.init(min, max, levels);
            for (int i=0; i<levels; i++) {
                double x1 = (double)stepper.next();
                int count = selectionModel.count(x0, x1);
                selectionBars[i] = Math.min(1, heightFunction.f(count) / maxBar);
                x0 = x1;
            }
        }

        dirty = false;
        oldMaxCount = maxCount;
    }

    /**
     * Used to generate variable-sized stedu.umd.cs for x-scaling
     */
    private Stepper stepper = new Stepper();
    private class Stepper {
        private double offset;
        private double range, base;
        private double min;
        int last;
        int i;
        public double init(double min, double max, int n) {
            i = 0;
            last = n;
            offset = min < 1 ? -min + 1 : 0;
            min = deltaFunction.f(min+offset);
            max = deltaFunction.f(max+offset);
            range = max - min;
            base = range/n;
            this.min = min;
            return get(i);
        }

        public double next() {
            return get(++i);
        }

        public double get(int index) {
            return (index < last) ?
                deltaFunction.i(min + index*base) - offset :
                deltaFunction.i(min + index*base) - offset + 1;
        }
    }

    public double normalize(double v) {
        return (v - min) / (max - min);
    }

    private double denormalize(double v) {
        return v * (max - min) + min;
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // store in preparation of possible refreshes before being displayed again...
        lastWidth = getWidth();

        HistogramModel renderCountModel = referenceModel != null ?
            referenceModel : getModel();
        int barCount = renderer.configure(renderCountModel, lastWidth);
        updateCache(barCount);
        if (selectionRenderer != null) {
            selectionRenderer.configure(renderCountModel, lastWidth);
        }
        renderer.paint(g, this);
        if (selectionRenderer != null) {
            selectionRenderer.paint(g, this);
        }
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public void setLevels(int levels) {
        this.levels = levels;
    }
    
    public void setModel(HistogramModel model) {
        setModel(model, false);
    }

    public void setModel(HistogramModel model, boolean avoidRefresh) {
        if (model == null) return;
        boolean mustRepaint = (model != this.model);
        this.model = model;
        this.max = model.getMax();
        this.min = model.getMin();
        dirty = true;
        if (mustRepaint && ! avoidRefresh) {
            refresh(true);
            repaint();
        }
    }

    /**
     * Set the reference histogram that will be used for scaling and
     * level-counting purposes. Use 'null' to have no reference histogram
     * @param h
     */
    public void setReferenceModel(HistogramModel m) {
        this.referenceModel = m;
    }

    public void setSelectionModel(HistogramModel model) {
        if (model == null) return;

        boolean mustRepaint = (model != this.selectionModel);
        this.selectionModel = model;
        if (mustRepaint) {
            repaint();
        }
    }
    
    public HistogramModel getModel() {
        return model;
    }
    
    @Override
    public Dimension getPreferredSize() {
        int mw = model != null ? model.getValueCount() : 10;
        Dimension d = super.getPreferredSize();
        return new Dimension(
                Math.max(d.width, Math.min(70, mw*2)),
                Math.max(d.height, 20));
    }

    public HistogramModel getReferenceModel() {
        return referenceModel;
    }

    public int getLevels() {
        return levels;
    }
    
    public String getText() {
        return text;
    }

    public double[] getBars() {
        return bars;
    }

    public double[] getSelectionBars() {
        return selectionBars;
    }

    public HistogramRenderer getRenderer() {
        return renderer;
    }
    public HistogramRenderer getSelectionRenderer() {
        return selectionRenderer;
    }
    
    public void setRenderer(Class c, Class sc) {
        try {
            if ( ! renderers.containsKey(c)) {
                HistogramRenderer r = (HistogramRenderer)c.newInstance();
                renderers.put(c, r);
            }
            renderer = renderers.get(c);
            if (sc != null) {
                if ( ! selectionRenderers.containsKey(sc)) {
                    HistogramRenderer r = (HistogramRenderer)sc.newInstance();
                    selectionRenderers.put(sc, r);
                }
                selectionRenderer = selectionRenderers.get(sc);
            }
        } catch (Exception e) {
            System.err.println("Error: imposible to instantiate " +
                    "renderers of classes "+c.getName()+" "+sc.getName());
            e.printStackTrace();
        }
    }
    
    public void setRenderer(HistogramRenderer renderer) {
        this.renderer = renderer;
    }

    public void setRange(double min, double max) {
        this.min = min;
        this.max = max;
        dirty = true;
        repaint();
    }
}
