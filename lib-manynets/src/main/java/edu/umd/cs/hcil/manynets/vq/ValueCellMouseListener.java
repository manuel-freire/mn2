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

package edu.umd.cs.hcil.manynets.vq;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Listens to interaction on a ValueCellRenderer within a JTable cell
 * 
 * @author mfreire
 */
public class ValueCellMouseListener implements MouseListener, MouseMotionListener {

    private ValueQueryTable table;

    private boolean dragging = false;
    private int dragRow = 0;
    private Objective objective = null;
    private double initialWeight;
    private Point start = null;
            
    public ValueCellMouseListener(ValueQueryTable table) {
        this.table = table;
    }
       
    public void mouseDragged(MouseEvent e) {
        if (dragging) {
            table.setRowSelectionInterval(dragRow, dragRow);
            Point p = e.getPoint();
            double n = (start.getY() - p.getY())/50.0;        
//            System.err.println("\t" + n + " on " + objective);
            objective.setWeight((float) Math.max(Math.min(initialWeight * (1  + n), 1), 0));
            table.getRanking().normalize();
            table.repaint();
        }
    }                             
    
    public void mouseClicked(MouseEvent e) {
        // nothing;
    }

    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();
        int i = table.rowAtPoint(p);
        objective = table.getObjectiveAt(p);
        // System.err.println("mouse moved: "+p+" "+i+", "+j);
        if (objective != null) {
            start = p;
            dragging = true;
            dragRow = i;
            initialWeight = objective.getWeight();
            System.err.println("Drag started");
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (dragging) {
            start = null;
            dragging = false;
            System.err.println("Drag ended");
            table.getRanking().normalize();           
            table.getRanking().notifyListeners();
        }
    }

    public void mouseEntered(MouseEvent e) {
        //;
    }

    public void mouseExited(MouseEvent e) {
        //;
    }

    public void mouseMoved(MouseEvent e) {
        //;
    }
}
