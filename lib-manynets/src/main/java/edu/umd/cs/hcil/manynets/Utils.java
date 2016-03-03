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

package edu.umd.cs.hcil.manynets;

import prefuse.data.Table;
import prefuse.util.collections.IntIterator;
import prefuse.data.Graph;
import prefuse.data.Node;
import java.util.Iterator;

/**
 *
 * @author Manuel Freire
 */
public class Utils {

    /**
     * Copy a row from one table to another. Both must have the same
     * number of columns, and the source row must exist (prefuse Tables
     * are allowed to skip rows for efficient deletes & inserts).
     * @param src
     * @param dest
     * @param row
     * @return the row-number in the new table of the newly-copied row
     */
    public static int copyRow(Table src, Table dest, int row) {
        int nr = dest.addRow();
        for (int i=0; i<src.getColumnCount(); i++) {
            dest.set(nr, i, src.get(row, i));
        }
        return nr;
    }

    /**
     * Create an empty table with the columns from another table
     * @param src
     * @return
     */
    public static Table createTableWithCols(Table src) {
        Table t = new Table(0, 0);
        for (int i=0; i<src.getColumnCount(); i++) {
            t.addColumn(src.getColumnName(i), src.getColumnType(i));
        }
        return t;
    }

    /**
     * Print a Prefuse table to STDERR, optionally stopping after n rows.
     * Use -1 for "all the rows in the table"
     * @param t
     */
    public static void printTable(Table t, int maxRowsToPrint) {
        System.err.println("Printing table - "+t.getColumnCount()+"x"+t.getRowCount());
        System.err.print("0 ");
        for (int j = 0; j < t.getColumnCount(); j++) {
            System.err.print("\t" + t.getColumnName(j));
        }
        System.err.println();
        for (int i = 0; i < t.getRowCount(); i++) {
            if ((maxRowsToPrint -- ) == 0) break;
            System.err.print("" + i + " ");
            for (int j = 0; j < t.getColumnCount(); j++) {
                System.err.print("\t" + t.get(i, j));
            }
            System.err.println();
        }
    }

    /**
     * Find a row with a given ID in an ID field; expects the ID to be unique
     *
     * Notice that Prefuse is adamant on types: you cannot query an 'int' index with,
     * say, a 'long'.
     *
     * @param idField
     * @param id
     * @param t
     * @return
     */
    public static int getRowFor(String idField, int id, Table t) {
        return t.getIndex(idField).get(id);
    }

    /**
     * Find a row with a given ID in an ID field; expects the ID to be unique
     * @param idField
     * @param id
     * @param t
     * @return
     */
    public static int getRowFor(String idField, String id, Table t) {
        return t.getIndex(idField).get(id);
    }


    /**
     * Create a copy from a table. The source table may have missing rows;
     * the destination table will not have them.
     * @param src
     * @return
     */
    public static Table copyTable(Table src) {
        Table t = new Table(src.getRowCount(), 0);
        for (int i=0; i<src.getColumnCount(); i++) {
            t.addColumn(src.getColumnName(i), src.getColumnType(i));
        }
        IntIterator ii = src.rows();
        for (int k=0; ii.hasNext(); k++) {
            int i = ii.nextInt();
            for (int j=0; j<src.getColumnCount(); j++) {
                t.set(k, j, src.get(i, j));
            }
        }
        return t;
    }

    public static Graph copyGraph(Graph g) {
        return new Graph(
            copyTable(g.getNodeTable()),
            copyTable(g.getEdgeTable()),
            g.isDirected(), g.getNodeKeyField(),
            g.getEdgeSourceField(), g.getEdgeTargetField());
    }

    /**
     * Create a deep copy from a table, using a duplicator.
     * The source table may have missing rows, but the result will not have them.
     * @param src
     * @return
     */
    public static Table deepCopyTable(Table src, Duplicator d) {
        Table t = new Table(src.getRowCount(), 0);
        for (int i=0; i<src.getColumnCount(); i++) {
            t.addColumn(src.getColumnName(i), src.getColumnType(i));
        }
        IntIterator it = src.rows();
        for (int k=0; it.hasNext(); k++) {
            int i = it.nextInt();
            for (int j=0; j<src.getColumnCount(); j++) {
                t.set(k, j, d.duplicate(src.get(i, j)));
            }
        }
        return t;
    }

    public interface Duplicator {
        public Object duplicate(Object o);
    }
    
    public static boolean edgeExists(Graph g, Node n1, Node n2) {

	int r2 = n2.getRow();
        Iterator neighbours = g.neighbors(n1);
        while (neighbours.hasNext()) {
            if (((Node) neighbours.next()).getRow() == r2) {
                return true;
            }
        }
        return false;
    } 
}
