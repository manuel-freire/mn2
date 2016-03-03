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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
//import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.event.TableColumnModelExtListener;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;
import javax.swing.table.TableColumnModel;
import org.jdesktop.swingx.table.TableColumnModelExt;

/**
 * Keeps two JXTables in sync
 * @author Manuel Freire
 */
public class CopycatColumnFactory extends ColumnFactory {

    private JXTable a, b;
    private ColWidthDuplicator cwd = new ColWidthDuplicator();
    private ColModelDuplicator cmd = new ColModelDuplicator();
    public CopycatColumnFactory(JXTable a, JXTable b) {
        this.a = a;
        this.b = b;
    }

    public ColWidthDuplicator getWidthDuplicator() {
        return cwd;
    }

    public ColModelDuplicator getModelDuplicator() {
        return cmd;
    }

    @Override
    public void packColumn(JXTable table, TableColumnExt columnExt, int margin, int max) {
        JXTable to = table == a ? b : a;
        TableColumnExt co = to.getColumnExt(columnExt.getIdentifier());
        if (co == null) {
            System.err.println("!!! no column with identifier '" + columnExt.getIdentifier() + "' found");
        }
        if (table == a) {
            super.packColumn(table, columnExt, margin, max);
            int w = columnExt.getPreferredWidth();
            co.setPreferredWidth(w);
        } else {
            int w = co.getPreferredWidth();
            columnExt.setPreferredWidth(w);
        }
    }

    public class ColWidthDuplicator implements PropertyChangeListener {
        boolean active = false;
        public void propertyChange(PropertyChangeEvent evt) {
//                System.err.println(evt.getPropertyName() + " " +
//                        evt.getOldValue() + " -> " + evt.getNewValue());
            if ( ! active && evt.getPropertyName().equals("width")) {
                active = true;
                int w = (Integer)evt.getNewValue();
                TableColumnExt other = getOtherCol(evt.getSource());
                if (other != null) other.setPreferredWidth(w);
                active = false;
            }
            if ( ! active && evt.getPropertyName().equals("visible")) {
                active = true;
                TableColumnExt other = getOtherCol(evt.getSource());
                if (other != null) other.setVisible((Boolean)evt.getNewValue());
                active = false;
            }
        }
    }

    public TableColumnModel getOtherModel(Object srcColModel) {
        JXTable t = (a.getColumnModel() == srcColModel) ? b : a;
        return t.getColumnModel();
    }

    public TableColumnExt getOtherCol(Object srcCol) {
        Object id = ((TableColumnExt)srcCol).getIdentifier();
        JXTable t = (a.getColumnExt(id) == srcCol) ? b : a;
        try {
            return (TableColumnExt)t.getColumnExt(id);
        } catch (Exception e) {
            return null;
        }
    }

    public class ColModelDuplicator implements TableColumnModelExtListener {
        boolean active = false;
        boolean avoidNextMove = false;

        private void printEvent(TableColumnModelEvent e, String type) {
            System.err.println("[" + type + "] " + e.getFromIndex()
                    + " -> " + e.getToIndex());
            showColumns("A:", (TableColumnModelExt)a.getColumnModel());
            showColumns("B:", (TableColumnModelExt)b.getColumnModel());
        }

        private void showColumns(String title, TableColumnModelExt cme) {
            System.err.println(title);
            Iterator<TableColumn> itc = cme.getColumns(true).iterator();
            for (int i=0; i<cme.getColumnCount(true); i++) {
                TableColumnExt tce = (TableColumnExt)itc.next();
                System.err.println("\t" + i + " " + tce.getIdentifier()
                        + " " + tce.isVisible());
            }
            System.err.println(title + " (visible)");
            for (int i=0; i<cme.getColumnCount(); i++) {
                TableColumnExt tce = (TableColumnExt)cme.getColumn(i);
                System.err.println("\t" + i + " " + tce.getIdentifier());
            }
        }
        public void columnMoved(TableColumnModelEvent e) {
            if ( ! active) {
                active = true;
                if (avoidNextMove) {
                    avoidNextMove = false;
                } else {
                    printEvent(e, "Moved");
                    if (e.getFromIndex() != e.getToIndex()) {
                        getOtherModel(e.getSource()).moveColumn(
                            e.getFromIndex(), e.getToIndex());
                    }
                    printEvent(e, "End-Moved");
                }
                active = false;
            }
        }
        public void columnSelectionChanged(ListSelectionEvent e) {
            System.err.println("Selected col-row " + e.getFirstIndex());
        }
        public void columnAdded(TableColumnModelEvent e) {
            if ( ! active) {
                active = true;
                /*
                 * Making a column visible is translated as adding it to the
                 * end, and then moving it to its true position. Add is called
                 * before move; and that particular move should be ignored.
                 */
//                printEvent(e, "Added");
                TableColumnModelExt own = (TableColumnModelExt)e.getSource();
                TableColumnExt local = own.getColumnExt(e.getToIndex());
                TableColumnExt other = getOtherCol(local);
                other.setVisible(true);
                avoidNextMove = true;
                active = false;
            }
        }
        public void columnRemoved(TableColumnModelEvent e) {
            if ( ! active) {
                active = true;
//                printEvent(e, "Removed");
                active = false;
            }
        }

        public void columnMarginChanged(ChangeEvent e) {}

        public void columnPropertyChange(PropertyChangeEvent event) {}
    }
}

