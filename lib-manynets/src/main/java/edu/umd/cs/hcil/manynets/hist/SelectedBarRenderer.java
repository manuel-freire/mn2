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
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 * Designed to paint bars on top of a BarRenderer, or to be used stand-alone
 * in place of a BarRenderer (while retaining its own specific 'selected hue')
 * @author mfreire
 */
public class SelectedBarRenderer extends AbstractRenderer {

    @Override
    public void paint(Graphics g, Histogram histogram) {

        int h = histogram.getHeight();

        g.setColor(barColor);
        Rectangle2D rectangle = new Rectangle2D.Double();
        Graphics2D g2d = (Graphics2D)g;
        double[] b = histogram.getBars();

        // setup for main drawing loop
        double max = 0;
        for (int i=0; i<b.length; i++) {
            max = Math.max(max, histogram.getSelectionBars()[i]);
        }
        double factor = 1;
        if (histogram.getRenderer() instanceof NullRenderer && max > 0) {
            factor = 1 / max;
        }
        
        double bh;
        int adjustedPixelsPerBin = (int)Math.min(maxPixelsPerBin,
                Math.max(1, pixelsPerBin-idealBinMarginInPixels));
        int preMargin = (int)Math.ceil((pixelsPerBin - adjustedPixelsPerBin)/2);

        // draw all those pesky bars
        // System.err.println("drawing selections");
        for (int bin = 0; bin<numBins ; bin++) {
            bh = (histogram.getSelectionBars()[bin] * factor * h) - 1;
            g2d.setColor(barColor);
            rectangle.setRect(pixelsPerBin * bin + preMargin, h - bh, adjustedPixelsPerBin, bh);
            g2d.fill(rectangle);
            // System.err.println("\t" + rectangle + " :: " + histogram.getSelectionBars()[bin]);
        }
    }
}