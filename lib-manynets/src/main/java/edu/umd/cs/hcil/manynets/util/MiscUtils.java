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

package edu.umd.cs.hcil.manynets.util;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ResourceBundle;

/**
 * Miscellaneous utilities
 *
 * @author mfreire
 */
public class MiscUtils {

    private static MemoryMXBean mbean = null;
    // default string bundle name (netbeans-hardcoded in many graphical forms...)
    private static final String defaultStringBundle = "strings";

    /**
     * Insert an internationalized string from the default resource bundle
     */
    public static String getString(String key) {
        return ResourceBundle.getBundle(defaultStringBundle).getString(key);
    }

    public static String getMemUsage() {

        if (mbean == null) {
            mbean = ManagementFactory.getMemoryMXBean();
        }

        mbean.gc();
        long used = mbean.getHeapMemoryUsage().getUsed();
        long max = mbean.getHeapMemoryUsage().getMax();
        NumberFormat f = DecimalFormat.getInstance();
        f.setMaximumFractionDigits(2);
        f.setMinimumFractionDigits(2);

        return f.format((double) used * 100.0 / max) + "% : " + (used / 1024) + "k / " + (max / 1024) + "k";
    }
}
