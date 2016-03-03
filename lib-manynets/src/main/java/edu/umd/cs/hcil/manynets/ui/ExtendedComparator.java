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

package edu.umd.cs.hcil.manynets.ui;

import java.util.Arrays;
import java.util.Comparator;

/**
 * A comparator that specializes in strings that mix numbers and text.
 * Other objects are compared using their in-built comparators, or lacking those,
 * not compared at all.
 *
 * @author Manuel Freire
 */
public class ExtendedComparator implements Comparator {
    
    public int compare(Object o1, Object o2) {
        if (o1 instanceof String && o2 instanceof String) {
            return compareStrings((String)o1, (String)o2);
        } else if (o1 instanceof Comparable) {
            if (o2 == null) return 1;
            return ((Comparable)o1).compareTo(o2);
        } else {
            return 0; // cannot compare
        }
    }

    /**
     * Compares two strings by separating number-parsts from string-parts
     * @param s1
     * @param s2
     * @return
     */
    public int compareStrings(String s1, String s2) {
        int p1 = 0, p2 = 0;
        int l1 = s1.length(), l2 = s2.length();
        while (p1 < l1 && p2 < l2) {
            int v1 = s1.charAt(p1++);
            int v2 = s2.charAt(p2++);
            if (Character.isDigit((char)v1)) {
                v1 *= 100;
                while (p1 < l1 && Character.isDigit(s1.charAt(p1))) {
                    v1 = v1*10 + s1.charAt(p1++);
                }
            }
            if (Character.isDigit((char)v2)) {
                v2 *= 100;
                while (p2 < l2 && Character.isDigit(s2.charAt(p2))) {
                    v2 = v2*10 + s2.charAt(p2++);
                }
            }
            if (v1 != v2) {
                return v1 - v2;
            }
        }
        if (l1 != l2) {
            return l1 - l2;
        }
        return 0;
    }

    private static void printArray(String[] a) {
        System.err.println("++++++++++++");
        for (String s : a) System.err.println(s);
    }

    public static void main(String[] args) {
        ExtendedComparator ec = new ExtendedComparator();
        String[] set = new String[] {
          "prueba", "pruebita", "pruebaza", "prue123ba", "prue10001ba", "prue10002ba", "prue9ba"
        };
        Arrays.sort(set, ec);
        printArray(set);
        Arrays.sort(set);
        printArray(set);
    }
}
