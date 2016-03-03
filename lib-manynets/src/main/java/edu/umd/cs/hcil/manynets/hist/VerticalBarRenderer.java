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
 *
 * @author mfreire
 */
public class VerticalBarRenderer implements HistogramRenderer {
    
    private int hatHeight = 5;
    private int hatWidth = 4;
        
     public void paint(Graphics g, Histogram histogram) {                        

        int h = histogram.getHeight();
        int w = histogram.getWidth();
        
//        g.setColor(Color.gray);
//        g.fillRect(0,0,w, (int)(histogram.getCurrent()*h));

        // first pass: accurate as hell
        g.setColor(Color.ORANGE);
        for (int i=0; i<h; i++) {
            // do not draw empty bars
            if (histogram.getBars()[i] == 0) continue;
            
            float x = i*1.0f/h;
            int rw =(int)(histogram.getBars()[i] * (w-hatWidth));
            g.drawLine(w-rw, i, w, i);
        }
        
        // second pass: sampling hats
        g.setColor(Color.RED);        
        
        float step = 1f/histogram.getLevels();      
        for (float x=0; x<1f; x+=step) {           
            int i = (int)(x*h);
            
            // do not draw empty hats; and only draw them where due
            if (histogram.getBars()[i] == 0) continue;
            int rw =(int)(histogram.getBars()[i] * (w-hatWidth));            
            g.fillRect(w-rw-hatWidth, i-(hatHeight/2), hatWidth, hatHeight);                  
            g.drawLine(w-rw, i, w, i);
        }
        
        // text (if any)
        if (histogram.getText() != null) {
            g.setColor(Color.black);
            g.drawString(histogram.getText(), 0, h-g.getFontMetrics().getMaxDescent());        
        }
    }
     
    @Override
    public int configure(HistogramModel m, int width) {
        return width;
    }
}