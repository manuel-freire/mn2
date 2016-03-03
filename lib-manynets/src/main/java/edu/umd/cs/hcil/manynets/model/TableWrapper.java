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

import edu.umd.cs.hcil.manynets.Utils;
import edu.umd.cs.hcil.manynets.Utils.Duplicator;
import edu.umd.cs.hcil.manynets.engines.ExpressionCalculator;
import edu.umd.cs.hcil.manynets.hist.DefaultHistogramModel;
import edu.umd.cs.hcil.manynets.hist.DefaultHistogramModel.ValueMapper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.ImageIcon;
import prefuse.data.CascadedTable;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.expression.AbstractPredicate;
import prefuse.data.expression.BooleanLiteral;
import prefuse.data.expression.Predicate;
import prefuse.util.collections.IntIterator;

/**
 * A Table that is very cheap to create (delegating all data to an internal
 * version) but, on the first call to 'detach', creates a whole new copy.
 * Each row is identified by an ID field. This field can be used to quickly
 * retrieve rows, as it is guaranteed to contain an index.
 * If it does not exist when a 'detached' TableWrapper is initialized, it is
 * created automatically.
 * Also contains a description of the columns, to be used when presenting the
 * table graphically.
 *
 * Users of this class should always access the table via "getTable()", keeping
 * no references to the actual result. And call "detach()" whenever a data-
 * changing operation is performed.
 *
 * WARNING: implementation assumes that no two TableWrappers pointing to the
 * same table with different idFields will be used.
 *
 * @author Manuel Freire
 */
public class TableWrapper {

    /**
     * The level of this tableWrapper
     */
    public enum Level {
        Entity,       // belongs to data entities (eg, 'user age')
        Relationship, // belongs to data relationship (eg, 'user/user rating)
        Node,         // within a node in a network view (eg, 'in-degree')
        Edge,         // within an edge in a network view (eg, 'is-bridge')
        Network,        // at the whole-graph level (eg, 'component count')
        
        None          // can be used instead of null where no level makes sense
    };

    private String name;
    private String idField;
    
    private Table raw;              // non-filtered; can add rows & cols; default
    private CascadedTable filtered; // filtered; can, but should not, add cols (as invisible to parent)
    
    private Level level;
    private ImageIcon icon;

    private boolean detached;
    
    protected TreeMap<Integer, Ref> refs =
            new TreeMap<Integer, Ref>();
    protected TreeMap<String, ValueMapper> valueMappers =
            new TreeMap<String, ValueMapper>();

    protected ArrayList<Stat> cols = new ArrayList<Stat>();
    protected TreeMap<Stat, Integer> stats = new TreeMap<Stat, Integer>();

    /**
     * Shallow copy, good for filtering; the copy itself appears unfiltered.
     * @param parent
     */
    public TableWrapper(TableWrapper parent) {
        this.name = parent.getName();
        this.raw = parent.raw;
        this.filtered = null;
        this.detached = false;
        this.idField = parent.getIdField();
        this.level = parent.getLevel();
        this.valueMappers = (TreeMap<String, ValueMapper>)valueMappers.clone();        
        this.cols = new ArrayList<Stat>(parent.cols);
        for (Stat s : cols) {
            stats.put(s, parent.stats.get(s));
        }
        
//        System.err.println("created tw, with " + filtered.getRowCount() + "x" + filtered.getColumnCount());
//        for (int i=0; i<cols.size(); i++) {
//            System.err.println(raw.getColumnName(i));
//        }
//        Thread.dumpStack();
    }

    /**
     * Standard constructor
     * @param name
     * @param t
     * @param detached
     * @param idField
     * @param level
     */
    public TableWrapper(String name, Table t, boolean detached, String idField, Level level) {
        this.name = name;
        this.raw = t;
        this.filtered = null;
        this.detached = detached;
        this.idField = idField;
        this.level = level;

        if (t.getColumnNumber(idField) == -1) {
            if (detached) {
                addIdColumn(t, idField);
            } else {
                throw new IllegalArgumentException("TableWrapper " + name
                        + " missing ID column " + idField);
            }
        }
        // add stats for all current columns
        for (int i=0; i<t.getColumnCount(); i++) {
            Stat stat = new Stat(t.getColumnName(i),
                t.getColumnName(i) + " of " + getLevel() + " " + getName(),
                t.getColumnType(i), getLevel(), getLevel());
            if (t.getColumnName(i).equals(TableWrapper.this.idField)) {
                stat.setIcon(getIcon());
            }
            cols.add(stat);
            stats.put(stat, i);
        }

        // make sure idField has an index
        t.index(idField);
    }

    public Tuple getTuple(int id) {
        return raw.getTuple(Utils.getRowFor(idField, id, raw));
    }

    /**
     * copy from parent, should be subclasses for type-safety
     */
    public TableWrapper copy() {
        return new TableWrapper(this);
    }

    /**
     * sets the name of the TW
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns 'true' if the table is being filtered, false otherwise
     */
    public boolean isFiltered() {
        return filtered != null;
    }

    /**
     * Clears filtering on this table
     */
    public void clearFilters() {
        setFilter(BooleanLiteral.TRUE);
    }

    /**
     * Set the filter for this table. Doing this on a node or edge table can lead to 
     * interesting results if the node or edge table is being used in a graph...
     * @param p
     */
    public void setFilter(Predicate p) {
        if (p.equals(BooleanLiteral.TRUE)) {
            filtered = null;
        } else {
            filtered = new CascadedTable(raw, p);
            filtered.index(idField);
        }
    }

    private static String td(String s, Table t) {
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<t.getColumnCount(); i++) {
            sb.append(t.getColumnName(i) + " " + t.getColumnNumber(t.getColumnName(i)) + " ");
        }
        return s + ": " + t.getColumnCount() + "x" + t.getRowCount() + ": " + sb;
    }

    /**
     * Retrieve the current predicate. By default, a true predicate is returned
     * @return
     */
    public Predicate getFilter() {
        return (filtered == null) ?
            BooleanLiteral.TRUE :
            filtered.getRowFilter();
    }

    /**
     * Used when listing alternate sources for stats (by aggregation of
     * children)
     * @return
     */
    public ArrayList<TableWrapper> getChildWrappers() {
        return new ArrayList<TableWrapper>();
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;        
        int i=0;
        for (Stat stat : cols) {
            if (raw.getColumnName(i++).equals(TableWrapper.this.idField)) {
                stat.setIcon(getIcon());
            }
        }
    }

    public ImageIcon getIcon() {
        if (icon == null) {
            ClassLoader cl = getClass().getClassLoader();
            String prefix = "edu/umd/cs/hcil/manynets/ui/icons/";
            String iconFile = null;
            switch(level) {
                case Edge: iconFile = prefix + "edge.png"; break;
                case Entity: iconFile = prefix + "entity.png"; break;
                case Node: iconFile = prefix + "node.png"; break;
                case Relationship: iconFile = prefix + "relationship.png"; break;
                case Network: iconFile = prefix + "graph.png"; break;
            }
            icon = new ImageIcon(cl.getResource(iconFile));
        }
        return icon;
    }

    public Level getLevel() {
        return level;
    }

    public ValueMapper getValueMapper(String field) {
        return valueMappers.get(field);
    }

    public void setValueMapper(String field, ValueMapper vm) {
        valueMappers.put(field, vm);
    }

    /**
     * Get descriptions of the columns of the wrapped table
     * @return
     */
    public ArrayList<Stat> getStats() {
        return new ArrayList<Stat>(cols);
    }

    /**
     * Add a stat-column to this table. This should be the only way to add stats,
     * as it updates all necessary records.
     */
    public boolean addStat(Stat stat) {
        return addStat(stat, false);
    }

    /**
     * Add a stat-column to this table. This should be the only way to add stats,
     * as it updates all necessary records.
     */
    public boolean addStat(Stat stat, boolean overwrite) {
        int col = raw.getColumnNumber(stat.getName());
        if (col >= 0 && ! overwrite) {
            return false;
        }
        
        if (col < 0) {

            // detach if before adding a new column
            detach();

            cols.add(stat);
            raw.addColumn(stat.getName(), stat.getType());
            stats.put(stat, cols.size()-1);
        } else {
            Stat prev = cols.get(col);
            cols.set(col, stat);
            stats.remove(prev);
            stats.put(stat, col);
        }
        return true;
    }

    /**
     * Returns the column-number of a stat (if present), or -1 if not found
     */
    public int getStatCol(Stat stat) {
        Integer col = stats.get(stat);
        return col == null ? -1 : col;
    }

    /**
     * Calculate a stat, the low-level way. It is more elegant to use monitors
     * to follow progress, and stat calculation can fail for a variety of
     * reasons, including taking too long and annoying the user...
     */
    public void calculateStat(Stat stat) {
        IntIterator ri = getTable().rows();
        while (ri.hasNext()) {
            int id = getId(ri.nextInt());
            stat.getProvider().calculate(this, id);
        }
    }

    /**
     * Remove a stat-column, by column index
     * @param col
     */
    public boolean removeStat(int col) {
        if (col < 0 || col >= cols.size()) {
            return false;
        } else {

            // detach before removing a column
            detach();

            Stat stat = cols.remove(col);            
            raw.removeColumn(stat.getName());
            stats.remove(stat);
            for (Entry<Stat,Integer> se : stats.entrySet()) {
                if (se.getValue() >= col) {
                    se.setValue(se.getValue() - 1);
                }
            }
            return true;
        }
    }

    /**
     * Remove a stat-column from this table, by stat
     */
    public boolean removeStat(Stat stat) {
        int i = getStatCol(stat);
        if (i == -1) {
            return false;
        } else {
            return removeStat(i);
        }
    }

    /**
     * Get the stat used to generate/describe a given column. Stats should be
     * one-to-one with columns
     * @return
     */
    public Stat getStat(int col) {
        return cols.get(col);
    }

    public int getRow(int id) {
        return Utils.getRowFor(idField, id, getTable());
    }

    public int getId(int row) {
        try {
            return getTable().getInt(row, idField);
        } catch (Exception e) {
            System.err.println("at " + getName() + " while accessing row "
                    + row + " of " + getTable().getRowCount());
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * createRef, called by getRef
     * @param id
     * @return
     */
    protected Ref createRef(int id) {
        return new Ref(null, this, id);
    }

    public Ref getRef(int id) {
        Ref ref = refs.get(id);
        if (ref == null) {
            ref = createRef(id);
            refs.put(id, ref);
        }
        return ref;
    }

    private static void addIdColumn(Table t, String idField) {
        t.addColumn(idField, Integer.TYPE);
        int idColumn = t.getColumnNumber(idField);
        int lastId = 1;
        for (IntIterator ri = t.rows(); ri.hasNext(); /**/) {
            int i = ri.nextInt();
            t.set(i, idColumn, lastId ++);
        }
    }

    /**
     * Should return the tables' true column names, but is allowed
     * to cheat on foreign-key names and IDs, so these become visible
     * to users (which can access the real stuff through the actual table)
     * @param columnIndex
     * @return
     */
    public String getColumnName(int columnIndex) {
        String n = raw.getColumnName(columnIndex);
        if (columnIndex == raw.getColumnNumber(getIdField())) {
            n += " (key)";
        }
        return n;
    }

    public String getName() {
        return name;
    }

    public String getIdField() {
        return idField;
    }

    /**
     * Creates a deep copy of this table. Copy() just returns shallow copies, ideal
     * for filtering. Deep copies should be used when true changes (beyond visibility)
     * are to be made.
     * @return
     */
    public boolean detach() {
        if ( ! detached) {

            System.err.println("Detaching TW " + getName() + " of level " + getLevel());

            raw = Utils.deepCopyTable(getTable(), new SimpleDuplicator());
            filtered = null;
            raw.index(idField);

            detached = true;
            return detached;
        } else {
            return false;
        }
    }
    
    public Table getTable() {
        return filtered != null ? filtered : raw;
    }

    public static class SimpleDuplicator implements Duplicator {
        @Override
        public Object duplicate(Object o) {
            if (o instanceof DefaultHistogramModel) {
                DefaultHistogramModel om = (DefaultHistogramModel)o;
                DefaultHistogramModel nm = new DefaultHistogramModel();
                nm.union(om);
                o = nm;
            }
            return o;
        }
    }

    public static class SetPredicate extends AbstractPredicate {
        private Set<Integer> list;
        private boolean blackList;
        private String idField;
        public SetPredicate(Set<Integer> list, String idField, boolean blackList) {
            this.list = list;
            this.idField = idField;
            this.blackList = blackList;
        }

        @Override
        public boolean getBoolean(Tuple t) {
            int id = -1;
            try {
                id = t.getInt(idField);
            } catch (Exception e) {
                for (int i=0; i<t.getColumnCount(); i++) {
                    System.err.println("!!!" + t.getColumnName(i) + " " + t.get(i));
                }
            }

            // blacklist and not-found, or not-blacklist and found
            return blackList ^ list.contains(id);
        }
    }

    public static class ExpressionPredicate extends AbstractPredicate {
        private ExpressionCalculator expression;
        private static int counter = 0;
        
        public ExpressionPredicate(ExpressionCalculator expression) {
            this.expression = expression;
        }

        public ExpressionPredicate(String pythonText) {
            this(new ExpressionCalculator(new Stat(
                        "__" + (++counter) + "__", "temporary stat #" + counter,
                        Boolean.class, Level.None, Level.None),
                    pythonText, "O(1)"));
        }

        @Override
        public boolean getBoolean(Tuple t) {
            return (Boolean)expression.evalTuple(t, null).__tojava__(Boolean.class);
        }
    }
}
