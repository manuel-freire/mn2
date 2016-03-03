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
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Listens to interaction on a ValueCellRenderer within a JTable cell
 * 
 * @author mfreire
 */
public class QueryPaneMouseListener implements MouseListener, MouseMotionListener {

    private boolean dragging = false;
    private Objective objective = null;
    private double initialWeight;
    private Point start = null;
    private Ranking ranking;
    private QueryPane.ValueLabel vl;
            
    public QueryPaneMouseListener(Ranking ranking, QueryPane.ValueLabel vl) {
        this.ranking = ranking;
        this.vl = vl;
    }
       
    public void mouseDragged(MouseEvent e) {
        if (dragging) {
            Point p = e.getPoint();
            double n = (start.getY() - p.getY())/50.0;        
//            System.err.println("\t" + n + " on " + objective);
            objective.setWeight((float) Math.max(Math.min(initialWeight * (1  + n), 1), 0));
            ranking.normalize();
            ranking.notifyListeners();
        }
    }                             
    
    public void mouseClicked(MouseEvent e) {
        if ( ! dragging) {
            vl.startEdit();
        }
    }

    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();        
                       
        objective = vl.getObjectiveAt(p);
        // System.err.println("mouse moved: "+p+" "+i+", "+j);
        if (objective != null) {
            start = p;
            dragging = true;
            initialWeight = objective.getWeight();            
            System.err.println("Drag started");
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (dragging) {
            mouseDragged(e);
            start = null;
            dragging = false;
            System.err.println("Drag ended");
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
