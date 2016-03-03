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
 * Paints histogram values as bars
 * @author mfreire
 */
public class BarRenderer extends AbstractRenderer {


    @Override
    public void paint(Graphics g, Histogram histogram) {                        

        int h = histogram.getHeight();

        // setup for main drawing loop
        g.setColor(barColor);
        Rectangle2D rectangle = new Rectangle2D.Double();
        Graphics2D g2d = (Graphics2D)g;
//        RenderingHints hints = new RenderingHints(
//		RenderingHints.KEY_ANTIALIASING,
//		RenderingHints.VALUE_ANTIALIAS_ON);
//  	  g2d.setRenderingHints(hints);
        double[] b = histogram.getBars();
        int adjustedPixelsPerBin = (int)Math.min(maxPixelsPerBin,
                Math.max(1, pixelsPerBin-idealBinMarginInPixels));
        int preMargin = (int)Math.ceil((pixelsPerBin - adjustedPixelsPerBin)/2);

        // draw all those pesky bars
        for (int bin = 0; bin<numBins ; bin++) {
            double rh = b[bin] > 0 ? Math.max(1, (b[bin] * h) - 1) : 0;
            rectangle.setRect(pixelsPerBin * bin + preMargin, h - rh, adjustedPixelsPerBin, rh);
            g2d.fill(rectangle);
        }
    }    
}