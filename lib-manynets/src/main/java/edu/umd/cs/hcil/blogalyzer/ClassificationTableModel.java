/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umd.cs.hcil.blogalyzer;

import java.util.ArrayList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import prefuse.data.Table;
import prefuse.data.event.EventConstants;
import prefuse.data.event.TableListener;

/**
 *
 * @author Manuel Freire
 */
public class ClassificationTableModel implements TableModel, TableListener {

    private static final Class intArrayType = (new int[0]).getClass();
    private ClassificationTable ct;
    private ArrayList<TableModelListener> al = new ArrayList<TableModelListener>();

    public ClassificationTableModel(ClassificationTable ct) {
        this.ct = ct;
    }

    @Override
    public int getRowCount() {
        return ct.getTable().getRowCount();
    }

    @Override
    public int getColumnCount() {
        int r = ct.getStartAttribute() + ct.getGroupCount() +
                (ct.getTable().getColumnCount() - ct.getEndAttribute());
//        System.err.println(
//                " End at " + ct.getEndAttribute() +
//                " Total of " + ct.getTable().getColumnCount() +
//                " Counted to " + r);
        return r;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex < ct.getStartAttribute()) {
            return ct.getTable().getColumnName(columnIndex);
        } else if (columnIndex - ct.getStartAttribute() < ct.getGroups().size()) {
            columnIndex -= ct.getStartAttribute();
            return ct.getGroups().get(columnIndex).getPrefix();
        } else {
            columnIndex -= ct.getStartAttribute() + ct.getGroups().size();
            columnIndex += ct.getEndAttribute();
            return ct.getTable().getColumnName(columnIndex);
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex < ct.getStartAttribute()) {
            return ct.getTable().getColumnType(columnIndex);
        } else if (columnIndex - ct.getStartAttribute() < ct.getGroups().size()) {
            columnIndex -= ct.getStartAttribute();
            return intArrayType;
        } else {
            columnIndex -= ct.getStartAttribute() + ct.getGroups().size();
            columnIndex += ct.getEndAttribute();
            return ct.getTable().getColumnType(columnIndex);
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex < ct.getStartAttribute()) {
            return ct.getTable().get(rowIndex, columnIndex);
        }
        else if (columnIndex - ct.getStartAttribute() < ct.getGroups().size()) {
            columnIndex -= ct.getStartAttribute();
            ColumnGroup g = ct.getGroups().get(columnIndex);
            if (g.getAttNames().size() == 0) {
                Integer a = (Integer) ct.getTable().get(rowIndex, columnIndex);
                return a;
            }
            Object[] a;
            try {
                a = new Integer[g.getEnd() - g.getStart()];
//                System.err.println("Integer length= "+a.length);

                for (int i = 0; i < a.length; i++) {
                    a[i] = (Integer) ct.getTable().get(rowIndex, g.getStart() + i);
                }
            } catch (ClassCastException cce) {
                a = new Double[g.getEnd() - g.getStart()][];
//                System.err.println("Double length= "+a.length);
                for (int i = 0; i < a.length; i++) {
                    a[i] = (Double[]) ct.getTable().get(rowIndex, g.getStart() + i);
                }
            }
            return a;

        } else {
            columnIndex -= ct.getStartAttribute() + ct.getGroups().size();
            columnIndex += ct.getEndAttribute();
            return ct.getTable().get(rowIndex, columnIndex);
        }
    }

     public Object getValueAt(int rowIndex, int columnIndex, double offset){

         ColumnGroup g = ct.getGroups().get(columnIndex);
         int atts = g.getEnd()-g.getStart();
         Double[][] d = (Double[][])ct.getTable().get(rowIndex, columnIndex);

         return d[(int)(atts*offset)][0];
     }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        throw new UnsupportedOperationException();
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
                type = EventConstants.INSERT;
                break;
            case EventConstants.UPDATE:
                type = EventConstants.UPDATE;
                break;
            default:
                throw new IllegalArgumentException("Unknown ptype");
        }

        TableModelEvent e = new TableModelEvent(this, start, end, col, type);
        for (TableModelListener l : al) {
            l.tableChanged(e);
        }
    }
}
