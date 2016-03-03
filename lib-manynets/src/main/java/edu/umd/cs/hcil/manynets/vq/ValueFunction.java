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
 * A value-function can be invoked from an objective to produce a numerical
 * value for a given object.
 * 
 * @author mfreire
 */
public interface ValueFunction<T> {
    public float valueFor(T o);
       
    /**
     * Highest score is 1, lowest is 0, others are linearly interpolated 
     * between high and low.
     * 
     * @param T
     */
    public abstract static class LinearValueFunction<T> implements ValueFunction<T> {
        private float l;
        private float w;

        public LinearValueFunction(float low, float high) {
            l = low;
            w = high - l;
        }        
        
        public float valueFor(T o) {
            float x = getVal(o);
            return Math.max(Math.min((x - l) / w, 1), 0);
        }

        public abstract float getVal(T o);
    }
    
    /**
     * Highest score is 'best', form is bell-shaped, with 97% within 2 standard
     * deviations.
     * 
     * @param T
     */
    public abstract static class BellValueFunction<T> implements ValueFunction<T> {
        private float b;
        private double a = 1;
        private double c2;        
        
        public BellValueFunction(float best, float stdDev) {            
            b = best;
            c2 = stdDev * stdDev;            
        }

        public float valueFor(T o) {
            double x = getVal(o);            
            return (float)(a * Math.exp( - (x - b)*(x - b)/(2 * c2)));
        }

        public abstract float getVal(T o);
    }
    
    /**
     * Simple all-or-nothing function
     */
    public abstract static class StepValueFunction<T> implements ValueFunction<T> {
        protected Object pattern;
        public StepValueFunction(Object pattern) {
            this.pattern = pattern;
        }
    }
}
