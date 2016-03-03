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

package edu.umd.cs.hcil.manynets.model;

import java.util.HashMap;

/**
 * A simple treemap that supports efficient "getKey(value)".
 * Notice that there must not be any duplicates of either keys or values
 * for this to work...
 * @author Manuel Freire
 */
public class DoubleMap<A,B> extends HashMap<A,B> {
    private HashMap<B,A> reverse;

    public DoubleMap() {
        this.reverse = new HashMap<B,A>();
    }

    @Override
    public B put(A key, B value) {
        reverse.put(value, key);
        return super.put(key, value);
    }

    @Override
    public B remove(Object key) {
        reverse.remove(get((A)key));
        return super.remove((A)key);
    }

    @Override
    public boolean containsValue(Object value) {
        return reverse.containsKey((B)value);
    }

    @Override
    public void clear() {
        reverse.clear();
        super.clear();
    }

    public A getKey(B value) {
        return reverse.get(value);
    }
}
