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

import edu.umd.cs.hcil.manynets.model.Stat;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.table.AbstractTableModel;
import org.jdesktop.swingx.JXTable;

/**
 * A panel that allows a user to select and deselect stats
 *
 * @author Manuel Freire
 */
public class StatsSelectionPanel extends JXTable {

    public static final String ADD_STAT_PROPERTY = "AddStatProperty";
    public static final String REMOVE_STAT_PROPERTY = "RemoveStatProperty";

    private boolean isAdd;

    // keeps track of selected stats
    private Set<Stat> stats = new TreeSet<Stat>();

    public void addStat(Stat stat) {
        ((StatsTableModel)getModel()).addStat(stat);
    }
    public void removeStat(Stat stat) {
        ((StatsTableModel)getModel()).removeStat(stat);
    }

    /** Creates new form StatSelectionPanel */
    public StatsSelectionPanel(Collection<Stat> available, boolean startSelected,
            boolean isAdd) {
        super();

        setBackground(Color.white);

        setModel(new StatsTableModel(available, isAdd));
        if (startSelected) {
            stats.addAll(available);
        }

        setSortable(true);
        packAll();
        setAutoResizeMode(JXTable.AUTO_RESIZE_ALL_COLUMNS);
        setShowGrid(false);
        setColumnMargin(5);
        setRowMargin(4);
    }

    public boolean isSelected(Stat stat) {
        return stats.contains(stat);
    }
    public ArrayList<Stat> getStats() {
        return ((StatsTableModel)getModel()).statList;
    }

    private class StatsTableModel extends AbstractTableModel {
        private ArrayList<Stat> statList;
        private Set<Stat> inserted = new TreeSet<Stat>();
        private boolean toAdd;

        private String[] headers;
        public StatsTableModel(Collection<Stat> current, boolean isAdd) {
            // note that some node/edge stats may only be available after
            // the corresponding entity/rel stats have been computed

            toAdd = isAdd;

            System.err.println("currently have " + current.size());
            headers = new String[] {
                "Name", "Description", "Type", "Cost",
                "Source Level", "Target Level", toAdd ? "Add" : "Remove"
            };

            statList = new ArrayList<Stat>(current);
        }

        public void addStat(Stat stat) {
            int index = statList.indexOf(stat);
            if (index >= 0) {
                // avoid adding duplicates
                return;
            }
          

            inserted.add(stat);
            boolean wasEmpty = statList.isEmpty();
            statList.add(stat);
            if (wasEmpty) {
                fireTableDataChanged();
            } else {
                fireTableRowsInserted(statList.size()-1, statList.size()-1);
            }
        }
        
        public void removeStat(Stat stat) {
            int index = statList.indexOf(stat);
        
            if (index >= 0) {
                inserted.remove(stat);
                statList.remove(index);
                if (statList.isEmpty()) {
                    fireTableDataChanged();
                } else {
                    fireTableRowsDeleted(index, index);
                }
            }
        }

        public int getRowCount() {
            return statList.isEmpty() ? 1 : statList.size();
        }
        public int getColumnCount() {
            return headers.length;
        }

        @Override
        public String getColumnName(int col) {
//            if(col == headers.length-1){
//                if (isAdd) return "Add";
//                else return "Remove";
//            }
            return headers[col];
        }
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return statList.isEmpty() ? false : columnIndex == headers.length-1;
        }
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == headers.length-1) {
                Stat s = statList.get(rowIndex);
                if (stats.contains(s)) {
                    stats.remove(s);
                } else {
                    stats.add(s);
                }
            }
            fireTableRowsUpdated(rowIndex, rowIndex);
            firePropertyChange(isAdd ? ADD_STAT_PROPERTY :
                REMOVE_STAT_PROPERTY, this, null);
        }

//        private void select(Stat stat, boolean value) {
//
//            for (Stat s : statList) {
//                if (s.getProvider() == stat.getProvider()) {
//                    if (value) {
//                        stats.add(s);
//                    } else {
//                        stats.remove(s);
//                    }
//                }
//            }
//        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return (columnIndex == headers.length-1 && ! statList.isEmpty()) ?
                Boolean.class : String.class;
        }

        @Override
        public Object getValueAt(int row, int columnIndex) {

            if (statList.size() == 0) {
                return (columnIndex == 0) ?
                    "<html><i>(The table is empty)</i><html>" : "";
            }

            Stat s = statList.get(row);
            boolean available = s.getProvider() != null;
            switch (columnIndex) {
                case 0: return inserted.contains(s) ?
                    "<html><i><b>(*)</b></i> " + s.getName() + "<html>" :
                    s.getName();
                case 1: return s.getDescription();
                case 2: return s.getType().getSimpleName();
                case 3: return available ?
                    s.getProvider().getComplexity() :
                    "not available";
                case 4: return s.getSourceLevel();
                case 5: return s.getTargetLevel();
                case 6: return stats.contains(s);
            }
            return "???";
        }
    }
}
