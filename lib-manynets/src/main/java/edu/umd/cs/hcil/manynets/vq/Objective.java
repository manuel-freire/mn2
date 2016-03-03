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

/**
 * An objective is something that can affect a sorting. Objectives can produce
 * values according to a value-function.
 * 
 * @author mfreire
 */
public class Objective {
    private ValueFunction vf;
    private float weight;
    
    public Objective() {
        this(new ValueFunction() {
            public float valueFor(Object o) {
                return 0;
            }
        });
    }
    
    public Objective(ValueFunction vf) {
        this(vf, 1);
    }
    
    public Objective(ValueFunction vf, float weight) {
        setValueFunction(vf);
        setWeight(weight);
    }
    
    public void setValueFunction(ValueFunction vf) {
        this.vf = vf;
    }
    
    public void setWeight(float weight) {
        this.weight = weight;
    }
    
    public float getWeight() {
        return weight;
    }
    
    public float weigh(Object o) {
        return vf.valueFor(o) * weight;
    }
}
