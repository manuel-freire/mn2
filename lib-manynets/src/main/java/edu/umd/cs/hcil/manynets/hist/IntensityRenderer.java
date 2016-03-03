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


import edu.umd.cs.hcil.manynets.ColorPalette;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author mfreire
 */
public class IntensityRenderer extends AbstractRenderer {

    private String paletteName = "blue";
    private double eccentricity = 1;

    public void setColor(String paletteName) {
        this.paletteName = paletteName;
    }

    public void setEccentricity(double eccentricity) {
        this.eccentricity = eccentricity;
    }

    public void paint(Graphics g, Histogram histogram, double height) {

        ColorPalette cp = ColorPalette.getPalette(paletteName);

        Rectangle2D rectangle = new Rectangle2D.Double();
        Graphics2D g2d = (Graphics2D)g;
        double[] b = histogram.getBars();

        double x = 0;
        for (int bin = 0; bin<numBins ; bin++) {
            g2d.setColor(cp.map(transferFunction(b[bin], eccentricity)));
            rectangle.setRect(x, 0, pixelsPerBin+1, height);
            g2d.fill(rectangle);
            x += pixelsPerBin;
        }
    }

    @Override
    public void paint(Graphics g, Histogram histogram) {                        

        int h = histogram.getHeight();
        paint(g, histogram, h);
    }    
       
    /**
     * Larger eccentricities make low values more visible; Lower eccentricities
     * have the reverse effect.
     * @return
     */
    float transferFunction(double v, double e) {
        return 1 - (float) Math.pow(1 - v, e);
    }
}