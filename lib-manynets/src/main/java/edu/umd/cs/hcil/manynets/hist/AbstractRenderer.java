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

import java.awt.Color;
import java.awt.Graphics;

/**
 * Abstract superclass for histogram renderers
 * @author Manuel Freire
 */
public abstract class AbstractRenderer implements HistogramRenderer {
    protected Color barColor = Color.blue.darker();

    protected static final int maxPixelsPerBin = 5;
    protected static final int idealBinMarginInPixels = 2;

    protected double range;
    protected double pixelWidth;
    protected double binWidth;
    protected double pixelsPerBin;
    protected int numBins;

    public void setColor(Color bar) {
        this.barColor = bar;
    }

    @Override
    public abstract void paint(Graphics g, Histogram histogram);

    @Override
    public int configure(HistogramModel m, int width) {
        range = m.getMax() - m.getMin();
        pixelWidth = range / width;
        binWidth = m.getMinBinSize();
        binWidth = Math.max(pixelWidth, binWidth);
        numBins = (int)Math.floor(range / binWidth) + 1;
        pixelsPerBin = Math.max(1, width / (double)numBins);

        return numBins;
    }
}
