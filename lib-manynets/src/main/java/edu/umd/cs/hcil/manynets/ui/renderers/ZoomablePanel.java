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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.JPanel;

/**
 * A zoomable JPanel. Catches mouse scrolling and translates it into zooming 
 * (x-axis, y-axis or both) + translation within the containing JScrollPane
 * to keep the center point always centered.
 *
 * @author mfreire
 */
public class ZoomablePanel extends JPanel implements MouseWheelListener {

    private double zoomFactor = 1.08;
    private boolean zoomXAxis = true;
    private boolean zoomYAxis = true;

    public ZoomablePanel(boolean zoomXAxis, boolean zoomYAxis) {
        this.zoomXAxis = zoomXAxis;
        this.zoomYAxis = zoomYAxis;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        updatePreferredSize(e.getWheelRotation(), e.getPoint());
    }

    private void updatePreferredSize(int n, Point p) {
        double dx = (double) n * zoomFactor;
        double dy = (double) n * zoomFactor;
        dx = (n > 0) ? 1 / dx : -dx;
        dy = (n > 0) ? 1 / dy : -dy;
        if (!zoomXAxis) {
            dx = 1;
        }
        if (!zoomYAxis) {
            dy = 1;
        }

        int w = (int) (getWidth() * dx);
        int h = (int) (getHeight() * dy);
        setPreferredSize(new Dimension(w, h));

        int offX = (int) (p.x * dx) - p.x;
        int offY = (int) (p.y * dy) - p.y;
        setLocation(getLocation().x - offX, getLocation().y - offY);

        getParent().doLayout();
    }
}
