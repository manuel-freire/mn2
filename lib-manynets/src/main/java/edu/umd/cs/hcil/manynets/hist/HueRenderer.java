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

import java.awt.Font;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 * Note that this component is lightweight indeed, and quickly switching the
 * model and repainting is as good as adding the component itself.
 *
 * @author mfreire
 */
public class HueRenderer extends JPanel implements HistogramRenderer {
        
    private float MAX_FONT = 12;    
    private static Color[] colorLUT = null;
    
    /**
     * A fast hue-color lookup, avoiding creation of new colors;
     * hue must be between 0 and 1,
     * sat can be either 'false' (half-saturation) or 'true' (full).
     */
    private static Color getColor(float hue, boolean sat) {
        int size = 1000;
        
        if (colorLUT == null) {
            colorLUT = new Color[size+2];
            for (int i=0; i<(size/2)+1; i++) {
                float f = i*1f/(size/2);
                float h = 1- ( .25f + .7f * f ) ;
                colorLUT[2*i]   = Color.getHSBColor(h, 1, 1);
                colorLUT[2*i+1] = Color.getHSBColor(h, 0.5f, 1);
            }
        }
        
        int i = (int)(hue*(size/2));
        return sat ? colorLUT[2*i] : colorLUT[2*i+1];
    }
    
    public void paint(Graphics g, Histogram histogram) {                        

        int h = histogram.getHeight();
        int w = histogram.getWidth();
                
        Font smallFont = g.getFont().deriveFont(Math.min(MAX_FONT, (float)h));
      
        float base = 1.0f/histogram.getLevels();
        
        // first & only pass
        for (int i=0; i<w; i++) {
            // do not draw 'empty' bars
            if (histogram.getBars()[i] == 0) continue;            
            
            float x = i*1.0f/w;    
            g.setColor(getColor((float)histogram.getBars()[i],
                    histogram.getModel().count(x, x+1.0f/w) > 0));
            
            g.drawLine(i, 0, i, h);
        }  
                
        // highlights (if any)
//        if (histogram.getModel().getHighlights() != null) {
//            g.setColor(Color.GREEN.darker());
//            for (double high : histogram.getModel().getHighlights()) {
//                g.drawLine((int)(high*w), 0, (int)(high*w), h);
//            }
//        }
        
        // text (if any)
        if (histogram.getText() != null) {
            Font f = g.getFont();
            g.setColor(Color.black);
            g.setFont(smallFont);
            g.drawString(histogram.getText(), 0, h-g.getFontMetrics().getMaxDescent());
            g.setFont(f);
        }
    }       

    public int configure(HistogramModel m, int width) {
        return width;
    }
}