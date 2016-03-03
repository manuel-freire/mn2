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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * A ranking is a set of weighted objectives w/ their corresponding value
 * functions, which can be applied to a list of results in order to obtain
 * a ranked list. It implements java's default comparator interface, 
 * internally assigning floating-point values to each list item.
 * 
 * @author mfreire
 */
public class Ranking implements Comparator {

    private ArrayList<Objective> objectives = new ArrayList<Objective>();
    private ArrayList<Listener> listeners = new ArrayList<Listener>();   
    
    public ArrayList<Objective> getObjectives() {
        return objectives;
    }
    
    public void addRankingListener(Listener l) {
        listeners.add(l);
    }
    
    public void removeRankingListener(Listener l) {
        listeners.remove(l);
    }
    
    public void clearRankingListeners() {
        listeners.clear();
    }
    
    public int compare(Object o1, Object o2) {
        return Float.compare(score(o1), score(o2));
    }
    
    public void notifyListeners() {
        for (Listener l : listeners) {
            l.valuesChanged(this);
        }
    }    
    
    public float score(Object o1) {
        float total = 0;
        for (Objective o : objectives) {
            total += o.weigh(o1);
        }
        return total;        
    }
    
    public void normalize() {
        float total = 0;
        for (Objective o : objectives) {
            total += o.getWeight();
        }
        for (Objective o : objectives) {
            o.setWeight(o.getWeight() / total);
        }
    }
    
    public String explain(Object o1) {
        DecimalFormat df = new DecimalFormat("0.###");
        StringBuilder sb = new StringBuilder();
        float total = 0;
        boolean first = true;
        for (Objective o : objectives) {
            float v = o.weigh(o1);
            sb.append((first ? "" : " + ") + df.format(v));
            total += v;
            first = false;
        }
        return sb.toString() + " = " + df.format(total);
    }
    
    public interface Listener {
        public void valuesChanged(Ranking r);
    }
}
