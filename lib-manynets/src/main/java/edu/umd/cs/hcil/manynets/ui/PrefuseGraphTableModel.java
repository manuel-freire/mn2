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

import edu.umd.cs.hcil.manynets.model.Dataset;
import edu.umd.cs.hcil.manynets.model.TableWrapper;
import java.util.ArrayList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import prefuse.data.Table;
import prefuse.data.event.EventConstants;
import prefuse.data.event.TableListener;
import prefuse.util.collections.IntIterator;

/**
 *
 * @author Manuel Freire
 */
class PrefuseGraphTableModel implements TableModel, TableListener {

    private TableWrapper tw;
    private ArrayList<TableModelListener> al = new ArrayList<TableModelListener>();

    public PrefuseGraphTableModel(TableWrapper tw) {
        this.tw = tw;
        tw.getTable().addTableListener(this);
    }

    @Override
    public int getRowCount() {
        return tw.getTable().getRowCount();
    }

    @Override
    public int getColumnCount() {
        return tw.getTable().getColumnCount();
    }

    @Override
    public String getColumnName(int columnIndex) {
        return tw.getColumnName(columnIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return tw.getTable().getColumnType(columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object o = null;
        try {
            return tw.getTable().get(rowIndex, columnIndex);
        } catch (Exception e) {
            System.err.println("!!! Bad model access: " +
                    "r " + rowIndex + " c " + columnIndex + " table " +
                    tw.getName() + "\n" +
                    Dataset.dumpTable(tw.getTable(), 3));
            e.printStackTrace();
        }
        return o;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        tw.getTable().set(rowIndex, columnIndex, aValue);
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        al.add(l);
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        al.remove(l);
    }

    /**
     * This is a prefuse-listener that rebroadcasts received events to all
     * registered swing-listeners.
     * @param t
     * @param start
     * @param end
     * @param pcol
     * @param ptype
     */
    @Override
    public void tableChanged(Table t, int start, int end, int pcol, int ptype) {

//        System.err.println("Table changed: " +
//                tw.getName() + " " + start + " " + end + " " + pcol + " " + ptype);

        // convert from prefuse-col to swing-col
        int col = pcol;
        if (pcol == EventConstants.ALL_COLUMNS) {
            col = TableModelEvent.ALL_COLUMNS;
        }

        // convert from prefuse-type to swing-type
        int type = ptype;
        switch (ptype) {
            case EventConstants.DELETE:
                type = TableModelEvent.DELETE;
                break;
            case EventConstants.INSERT: 
                type = TableModelEvent.INSERT;
                break;
            case EventConstants.UPDATE: type = TableModelEvent.UPDATE; break;
            default: throw new IllegalArgumentException("Unknown ptype");
        }

        TableModelEvent e = new TableModelEvent(this, start, end, col, type);
        for (TableModelListener l : al) {
            // Notification is broken: removing or adding cols does not work at all
            //System.err.println("Notifying " + l.getClass().getSimpleName());
            //l.tableChanged(e);
        }
    }
}