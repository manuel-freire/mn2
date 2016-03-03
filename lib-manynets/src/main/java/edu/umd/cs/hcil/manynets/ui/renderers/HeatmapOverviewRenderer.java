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

import edu.umd.cs.hcil.manynets.hist.AvgHistogramModel;
import edu.umd.cs.hcil.manynets.model.Distribution;
import edu.umd.cs.hcil.manynets.ui.ColumnManager;
import edu.umd.cs.hcil.manynets.hist.DefaultHistogramModel;
import edu.umd.cs.hcil.manynets.hist.Histogram;
import edu.umd.cs.hcil.manynets.hist.IntensityRenderer;
import edu.umd.cs.hcil.manynets.hist.MaxHistogramModel;
import edu.umd.cs.hcil.manynets.hist.MinHistogramModel;
import edu.umd.cs.hcil.manynets.hist.OverlayHistogramModel;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Collection;
import javax.swing.JPanel;

/**
 *
 * @author Awalin Shopan, Manuel Friere
 */
public class HeatmapOverviewRenderer extends AbstractRowRenderer {

    public enum RowAggregator {
        maxModel("Max. value of aggregated rows", new MaxHistogramModel()),
        avgModel("Average. value of aggregated rows", new AvgHistogramModel()),
        minModel("Min. value of aggregated rows", new MinHistogramModel());
        public String name;
        public OverlayHistogramModel model;
        RowAggregator(String name, OverlayHistogramModel model) {
            this.name = name; this.model = model;
        }
        @Override
        public String toString() { return name; }
    }

    public enum ColorScheme {
        whiteBlue("white (-) to blue (+)", "white-blue"),
        whiteBlue2("white (-) to blue (+) variant", "white-blue2"),
        whiteRed2("white (-) to red (+) variant", "white-red2"),
        yellowGreen("yellow (-) to green (+)", "yellow-green"),
        orangeRed("orange (-) to red (-)", "orange-red"),
        blackGreen("black (-) to green(+)", "black-green"),
        blueWhiteOrange("blue (-) via white to orange (+)", "blue-white-orange"),
        redBlackGreen("red (-) via black to green (+)", "red-black-green");
        public String name, scheme;
        ColorScheme(String name, String scheme) {
            this.name = name; this.scheme = scheme;
        }
        @Override
        public String toString() { return name; }
    }

    public enum IntensityEmphasis {
        emphLowest("lowest", 3.0),
        emphLow("low", 2.0),
        emphNormal("none", 1.0),
        emphHigh("high", 0.5),
        emphHighest("highest", 0.25);
        public String name;
        public double value;
        IntensityEmphasis(String name, Double value) {
            this.name = name; this.value = value;
        }
        @Override
        public String toString() { return name; }
    }

    public enum IntensityType {
        global("Global-Scaled according to global max."),
        local("Local-Scaled according to each rows' max.");
        public String name;
        IntensityType(String name) { this.name = name; }
        @Override
        public String toString() { return name; }
    }

    private ColorScheme colorScheme;
    private RowAggregator rowAggregator;
    private IntensityEmphasis intensity;
    private IntensityType intensityType;

    private Histogram hist;
    private IntensityRenderer intensityRenderer;

    public HeatmapOverviewRenderer() {
        intensityRenderer = new IntensityRenderer();
        hist = new Histogram(null, IntensityRenderer.class, null);
        hist.setRenderer(intensityRenderer);
        colorScheme = ColorScheme.whiteBlue;
        intensityType = IntensityType.local;
        intensity = IntensityEmphasis.emphNormal;
        rowAggregator = RowAggregator.maxModel;
    }

    public IntensityType getIntensityType() {
        return intensityType;
    }

    public void setIntensityType(IntensityType type) {
        if (type != this.intensityType) {
            this.intensityType = type;
            dirty = true;
        }
    }

    public void setColorScheme(ColorScheme scheme) {
        if (scheme != this.colorScheme) {
            this.colorScheme = scheme;
            dirty = true;
        }
    }

    public ColorScheme getColorScheme() {
        return colorScheme;
    }

    public void setRowAggregator(RowAggregator aggregator) {
        if (aggregator != this.rowAggregator) {
            this.rowAggregator = aggregator;
            dirty = true;
        }
    }

    public RowAggregator getRowAggregator() {
        return rowAggregator;
    }

    @Override
    protected void paintImage(BufferedImage bi) {

        int verticalPixels = height;

        // init histogram (will be used for all rows)
        hist.setReferenceModel(cm.getModel());
        hist.setBounds(0, 0, width, 1);
        hist.setForceMaxCount(-1);
        intensityRenderer.setEccentricity(intensity.value);
        int totalRows = cm.getTable().getRowCount();

        // calculate max of all rows (used to scale values when painting)
        int maxCount = 0;
        if (intensityType == IntensityType.global) {
            for (int i=0; i<totalRows; i++) {
                DefaultHistogramModel d = (DefaultHistogramModel) cm.getValue(indices[i]);
                hist.setModel(d, true);
                hist.setRange(min, max);
                hist.refresh(true, bi.getWidth());
                maxCount = Math.max(maxCount, hist.getMaxCount());
//                System.err.println("mc = " + maxCount);
            }
        }

        Graphics2D g2d = bi.createGraphics();

        AffineTransform ot = g2d.getTransform();

        
        // for each horizontal pixel, render max, min or avg of selections
        OverlayHistogramModel ohm = rowAggregator.model;
        intensityRenderer.setColor(colorScheme.scheme);
        double increment = Math.max(1f, (verticalPixels-1)*1.0/totalRows);
        
        // use bresenham; from wikipedia, and using pixels=deltay and rows=deltax,
        //     real error := 0
        //     real deltaerr := pixels / rows // Assume deltax != 0 (line is not vertical),
        //           // note that this division needs to be done in a way that preserves the fractional part
        //     for x from 0 to rows
        //         plot(x,y)
        //         error := error + deltaerr
        //         if abs(error) ≥ 0.5 then
        //             y := y + 1
        //             error := error - 1.0

        double error = 0;
        double deltaError = (verticalPixels-1)*1.0/totalRows;
        ohm.clear();
        for (int i = 0, j = 0; i < totalRows; i++) {
            // include row for aggregating in its current j-position
            ohm.add((DefaultHistogramModel)cm.getValue(indices[i]));
            error += deltaError;
            if (error > 0.5) {
                // paint the row
                hist.setModel(ohm, true);
                if (intensityType == IntensityType.global) {
                    hist.setForceMaxCount(maxCount);
                }
                hist.setRange(min, max);
                hist.refresh(true, bi.getWidth());
                intensityRenderer.paint(g2d, hist, increment);
                // prepare for next iteration
                g2d.translate(0, increment);
                ohm.clear();
                j ++;
                error -= 1.0;
            }
        }
        // paint last row
        hist.setModel(ohm, true);
        if (intensityType == IntensityType.global) {
            hist.setForceMaxCount(maxCount);
        }
        hist.setRange(min, max);
        hist.refresh(true, bi.getWidth());
        intensityRenderer.paint(g2d, hist, increment);

        g2d.setTransform(ot);
    }

    /**
     * v is a value between 0 an 1f
     */
    public void setIntensity(IntensityEmphasis emph) {
        if (emph != this.intensity) {
            intensity = emph;
            dirty = true;
        }
    }

    public IntensityEmphasis getIntensity() {
        return intensity;
    }

    @Override
    public String getXCaption() {
        return cm.getColumnName();
    }

    @Override
    public JPanel getSettingsPanel() {
        return new HeatmapOPanel(this);
    }

    @Override
    protected String getToolTipText(int row, Point p) {
        StringBuilder sb = new StringBuilder("<html>\n");
        sb.append("<b>ID</b> " + cm.getTable().getModel().getValueAt(row, 0));
        DefaultHistogramModel d = (DefaultHistogramModel) cm.getValue(row);
        hist.setModel(d);
        hist.refresh(true, getWidth());

        DefaultHistogramModel m = (DefaultHistogramModel)hist.getModel();
        if (m.isEmpty()) {
            sb.append("(empty)");
        } else {
            double f = hist.getValueForPoint(p, hist.getBounds(), false);
            sb = new StringBuilder("<html><b>" + m.getValueLabel(f) + "</b> (" +
                f + ")<br>");
            Collection<Object> labels = m.getLabelsForValue(f);

            if (labels == null) return "";
            int nsel = 0;
            int total = labels.size();
            String tip= cm.getTooltipFor(labels);
            sb.append(tip);
            sb.append("</html>");
            return sb.toString();
        }

        sb.append("</html>");
//
        return "" + sb.toString();
    }

    @Override
    public boolean canHandle(ColumnManager cm, boolean overview) {
        return overview
                && Distribution.class.isAssignableFrom(cm.getColumnClass());
    }

    @Override
    public String getRendererName() {
        return "Heatmap";
    }

    @Override
    public ColumnCellRenderer copy() {
        HeatmapOverviewRenderer r = new HeatmapOverviewRenderer();
        r.dirty = true;
        r.metric = metric;
        r.sortAttribute = sortAttribute;
        r.suspendSort = suspendSort;

        r.intensityType = intensityType;
        r.colorScheme = colorScheme;
        r.intensity = intensity;
        r.rowAggregator = rowAggregator;
        r.init(cm);
        return r;
    }
}
