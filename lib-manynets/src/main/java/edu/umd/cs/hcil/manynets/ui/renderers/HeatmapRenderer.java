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

import edu.umd.cs.hcil.manynets.ui.ColumnManager;
import edu.umd.cs.hcil.manynets.hist.IntensityRenderer;

/**
 * Displays a 'distribution cell', a cell whose value is a Distribution
 * of values, as a histogram
 * @author Manuel Freire
 */
public class HeatmapRenderer extends HistogramRenderer {

    public HeatmapRenderer() {
        super();
        hist.setRenderer(new IntensityRenderer());
    }

    @Override
    public void init(ColumnManager cm) {
        super.init(cm);
        setGlobalHeight(false);
    }

    @Override
    public String getRendererName() {
        return "Color-encoded hist.";
    }
}