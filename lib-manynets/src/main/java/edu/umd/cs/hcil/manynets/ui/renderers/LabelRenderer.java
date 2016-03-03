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

package edu.umd.cs.hcil.manynets.ui.renderers;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.SwingUtilities;
import edu.umd.cs.hcil.manynets.hist.DefaultHistogramModel;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import javax.swing.JPanel;

/**
 * Renders a label, horizontal or vertically.
 *
 * @author mfreire
 */
public class LabelRenderer extends JPanel {

    private boolean isVertical;
    private LabelOverviewRenderer parent;
    private ArrayList<AxisLabel> axisLabels = new ArrayList<AxisLabel>();
    private ArrayList<AxisLabel> axisLabels2 = new ArrayList<AxisLabel>();

    public LabelRenderer(LabelOverviewRenderer parent, boolean isVertical) {
        this.parent = parent;
        this.isVertical = isVertical;
//        setBackground(isVertical
//                ? new Color(255, 250, 250) : new Color(250, 250, 255));
        setFont(getFont().deriveFont(9f));
    }

    /**
     * Helps format text to fit within tight bounds
     */
    public static class Chopper {

        private int size = 3;
        private String output = "";

        public int getSize() {
            return size;
        }

        public String getOutput() {
            return output;
        }

        public String chop(FontMetrics fm, Object o, double width) {
            if (o instanceof Double) {
                return chop(fm, ((Double)o), width);
            } else {
                String s = chop(fm, "" + o, width);
//                System.err.println("Chopping of " + o + " to " + width + " yielded " + s + " sz= " + this.size);
                return s;
            }
        }
        
        public String chop(FontMetrics fm, Double d, double width) {

            for (int i = 0; i < smallFormatter.length; i++) {
                output = formatDouble(d, i);
                int trialWidth = SwingUtilities.computeStringWidth(fm, output);
//             System.err.println("txt wd " + trialWidth + "window = " + width );             
                if (trialWidth < width) {
                    break;
                }
            }

            size = SwingUtilities.computeStringWidth(fm, output);
            return output;
        }

        public String chop(FontMetrics fm, String str, double width) {
            output = str;
            int trialWidth = SwingUtilities.computeStringWidth(fm, output);

            while (trialWidth > width && output.length() > 0) {
                output = output.substring(0, output.length() - 1);
                trialWidth = SwingUtilities.computeStringWidth(fm, output);
                if (trialWidth < width) {
                    output += ".";
                    break;
                }
            }
            size = SwingUtilities.computeStringWidth(fm, output);
            return output;
        }
        private static NumberFormat smallFormatter[] = {new DecimalFormat("0.00000E0"),
            new DecimalFormat("0.0000E0"), new DecimalFormat("0.000E0")};
        private static NumberFormat largeFormatter[] = {new DecimalFormat("0.#######"),
            new DecimalFormat("0.#####"), new DecimalFormat("0.###")};

        public static String formatDouble(double v, int i) {
            String s = (v != 0 && (v <= 0.1 && v >= -0.1))
                    ? smallFormatter[i].format(v) : largeFormatter[i].format(v);
            return s.replaceAll("E", "");
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        FontMetrics fm = g.getFontMetrics(getFont());
        float fh = fm.getHeight();
        double w = getWidth();
        double h = getHeight();
        double spacing = SwingUtilities.computeStringWidth(fm, "x");
        if (w < fh || h < fh) {
            return;
        }

        Chopper chopper = new Chopper();

        String caption = isVertical
                ? parent.getYCaption() : parent.getXCaption();
        double cw = fm.stringWidth(caption);
        boolean paintCaptions = cw > 0 && (isVertical ? w >= fh * 2 : h >= fh * 2);

        if (parent.getInner() instanceof AxisLabeller) {
            clearAxisLabels();
            clearAxisLabels2();
            ((AxisLabeller) parent.getInner()).setAxisLabels(this, isVertical);
        }

        AffineTransform oldTransform = g2d.getTransform();
        float vOffsetAxisLabels = fh - fm.getDescent();
        float vOffsetCaption = fh * 2 - fm.getDescent();
        int vOffsetLine = 0;
        int vOffsetLineTick = 2;
        if (isVertical) {
            AffineTransform at = AffineTransform.getQuadrantRotateInstance(-1);
            at.concatenate(AffineTransform.getTranslateInstance(-h, 0));
            g2d.transform(at);
            double aux = w;
            w = h;
            h = aux;
            if (h > fh * 2) {
                vOffsetCaption = fh - fm.getDescent();
                vOffsetAxisLabels = fh * 2 - fm.getDescent();
            } else {
                // this will not get drawn
                vOffsetCaption = fh * 2- fm.getDescent();
                vOffsetAxisLabels = fh - fm.getDescent();
            }
            vOffsetLine = (int) h - 2;
            vOffsetLineTick = vOffsetLine - 2;
        }

        g2d.drawLine(0, vOffsetLine, (int) w, vOffsetLine);
        //System.err.println("w = " + w + " al " + axisLabels.size()
        // + " spacing " + spacing + " dw = " + dw);
        paintStringLabels(g2d, fm, chopper, vOffsetAxisLabels, w, axisLabels);
        for (int i = 0; i < axisLabels.size(); i++) {
            AxisLabel al = axisLabels.get(i);
            g2d.drawLine((int) Math.min(w * al.pos, w - 2), vOffsetLine,
                    (int) Math.min(w * al.pos, w - 2), vOffsetLineTick);
        }
        if (paintCaptions) {
            if ( ! axisLabels2.isEmpty()) {
                paintStringLabels(g2d, fm, chopper, vOffsetCaption, w, axisLabels2);
            } else {
                String chopped = chopper.chop(fm, caption, w);
                float tw = chopper.getSize();
                float extra = ((float) w - tw) / 2;
                g2d.drawString(chopped, extra, vOffsetCaption);
            }
        }
        g2d.setTransform(oldTransform);
    }

    private void paintStringLabels(Graphics2D g2d, FontMetrics fm, Chopper chopper,
            float y, double w, ArrayList<AxisLabel> als) {
        for (int i = 0; i < als.size(); i++) {
            AxisLabel al = als.get(i);
            // available width (dw) is distance between stops for all but last
            float dw = (float)((i == als.size()-1) ? 1-al.pos :
                als.get(i+1).pos - al.pos);
            dw *= w;
            String chopped = chopper.chop(fm, al.value, dw);
            float tw = chopper.getSize();
            float extra = (dw - tw) / 2;
            if ( ! al.centered) {
                extra = (i==0) ? 0 : dw-tw;
            }
//            System.err.println("extra for " + chopped + " is " + extra);
            g2d.drawString(chopped, (int)(al.pos * w + extra), y+1);
        }
    }

    //--- Axis labelling mechanism
    private static class AxisLabel {

        public double pos;
        public Object value;
        public boolean centered;

        public AxisLabel(double pos, Object value, boolean centered) {
            this.pos = pos;
            this.value = value;
            this.centered = centered;
        }
    }

    public void clearAxisLabels() {
        axisLabels.clear();
    }
    
    public void clearAxisLabels2() {
        axisLabels2.clear();
    }

    public void addAxisLabel(double pos, Object value) {
        axisLabels.add(new AxisLabel(pos, value, true));
    }

    public void addAxisLabel2(double pos, Object value) {
        axisLabels2.add(new AxisLabel(pos, value, true));
    }

    public void addAxisLabel(double pos, Object value, boolean centered) {
        axisLabels.add(new AxisLabel(pos, value, centered));
    }

    interface AxisLabeller {

        public void setAxisLabels(LabelRenderer lr, boolean isVertical);
    }

    public void generateHistogramLabels(DefaultHistogramModel dhm) {
        generateHistogramLabels(dhm, 0, 1, true);
    }

    public void generateHistogramLabels(DefaultHistogramModel dhm,
            double start, double end, boolean clear) {
        if (clear) {
            axisLabels.clear();
        }
        if (dhm.isEmpty()) {
            return;
        }

        int nBins = (int)Math.ceil(
                (dhm.getMax() - dhm.getMin()) / dhm.getMinBinSize() + 1);

        if (dhm.isNominal() || nBins < 30) {
            int original = axisLabels.size();
            int i = 0;
            int total = 0;
            for (Object o : dhm.getValues()) {
                addAxisLabel(i++, "" + o);
                total++;
            }
            double delta = (end-start) / total;
            for (int j=original; j<original+total; j++) {
                AxisLabel al = axisLabels.get(j);
                al.pos = start + al.pos * delta;
//                System.err.println("axis pos for " + j + " set to " + al.pos);
            }
        } else {
            addAxisLabel(start, dhm.getValueLabel(dhm.getMin()), false);
            addAxisLabel(end, dhm.getValueLabel(dhm.getMax()), false);
        }
    }
}
