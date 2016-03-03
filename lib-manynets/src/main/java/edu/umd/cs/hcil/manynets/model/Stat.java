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

import edu.umd.cs.hcil.manynets.model.TableWrapper.Level;
import javax.swing.ImageIcon;

/**
 * A "statistic" or "column" of a ManyNets table. There are different types of
 * statistics, depending on their level and their origin.
 *
 * @author Manuel Freire
 */
public class Stat implements Comparable<Stat> {

    // stat name
    protected String name;
    // longer description (used, eg, as tooltip)
    protected String description;
    // data-type of cells of this column
    protected Class type;
    // level of the stat (TableWrapper.Level of the target table)
    protected Level targetLevel;
    // level of the stat (TableWrapper.Level of the source data)
    protected Level sourceLevel;
    // calculator to be used if recalculation triggered
    protected StatCalculator provider = null;
    // a fixed icon, if applicable
    protected ImageIcon icon = null;

    // possible presentations; by default, only one
    protected Presentation[] presentations = new Presentation[] {
        new Presentation("default")
    };

    public Stat(String name, String description, Class type, 
            Level targetLevel, Level sourceLevel) {
//        System.err.println("Initialized stat " + name + " level " + level);
        this.name = name;
        this.description = description;
        this.type = type;
        this.targetLevel = targetLevel;
        this.sourceLevel = sourceLevel;
    }

    /**
     * Certain aggregation stats (most notably incoming/ougtoing edges for
     * vertices) need to redefine this.
     * @param r
     * @return
     */
    public Ref getParentRef(Ref r) {
        return r.getParentRef();
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }

    public class Presentation {
        private String name;
        public Presentation(String name) {
            this.name = name;
        }
        public Ref present(Ref r) {
            return r;
        }
        public String getName() {
            return name;
        }
    }

    public ImageIcon getIcon(Ref r) {
        return icon;
    }

    public Presentation[] getPresentations() {
        return presentations;
    }

    public Level getTargetLevel() {
        return targetLevel;
    }

    public Level getSourceLevel() {
        return sourceLevel;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Class getType() {
        return type;
    }

    public StatCalculator getProvider() {
        return provider;
    }

    public void setProvider(StatCalculator p) {
        this.provider = p;
    }

    public String getComplexity() {
        return provider == null ? "" : provider.getComplexity();
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Stat)
                && compareTo((Stat)o) == 0;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (name != null ? name.hashCode() : 0);
        hash = 97 * hash + (type != null ? type.hashCode() : 0);
        hash = 97 * hash + targetLevel.hashCode();
        return hash;
    }

    @Override
    public int compareTo(Stat o) {
        int c = 0;
        // change the order of these around to alter sort priority
        if (c == 0) c = compareComplexity(getComplexity(), o.getComplexity());
        if (c == 0) c = targetLevel.compareTo(o.targetLevel);
        if (c == 0) c = sourceLevel.compareTo(o.sourceLevel);
        if (c == 0) c = name.compareTo(o.getName());
        if (c == 0) c = description.compareTo(o.getDescription());
        return -c;
    }

    private static int compareComplexity(String a, String b) {
        return complexity(a) - complexity(b);
    }

    private static int complexity(String s) {
        int n = 0;
        if (s == null || s.length() == 0) {
            return Integer.MAX_VALUE;
        }

        s = s.substring(s.indexOf("(")+1, s.lastIndexOf(")"));
        
        if (s.equals("1")) n = 1;
        else if (s.equals("V")) n = 100;
        else if (s.equals("E")) n = 1000;
        else if (s.equals("E + V")) n = 1100;
        else if (s.equals("V^2")) n = 100 * 100;
        else if (s.equals("V^2 + VE")) n = 100 * 1000;
        else if (s.equals("V^3")) n = 100 * 100 * 100;
        else {
            System.err.println("Non-hardcoded complexity found: " + s);
            n = Integer.MAX_VALUE;
        }

        return n;
    }

    @Override
    public String toString() {
        return name + " (" + description + ") "
                + sourceLevel + " -> " + targetLevel + " " +
                (provider == null ? "(no provider)" : provider.getClass().getSimpleName());
    }

    public boolean isExpensive() {
        return getProvider() == null ?
            false : complexity(getComplexity()) >= 10000;
    }
}
