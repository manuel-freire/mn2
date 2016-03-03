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

/**
 * A HistogramRenderer is in charge of actually displaying a histogram on-screen.
 * It is a lightweight renderer: it stores no internal data.
 *
 * @author mfreire
 */
public interface HistogramRenderer {
    
    /**
     * Paints the given histogram
     */
    public void paint(Graphics g, Histogram h);

    /**
     * Will be called before building the cache to give the renderer
     * an opportunity to reconsider its desired number of levels.
     * Should return the desired number of levels
     * @param aThis
     */
    public int configure(HistogramModel m, int width);
}
