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

package edu.umd.cs.hcil.manynets.vq;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * An extension to the default JTable implementation that renders a given
 * column with a ValueCellRenderer and supports ValueQuery sorting
 * and interaction on said column.
 *
 * @author mfreire
 */
public class ValueQueryTable extends JTable {

    private int valueCol;
    private ValueCellRenderer vcr;
    private Ranking ranking;
    private TableRowSorter<TableModel> sorter;
    
    public ValueQueryTable(Object[][] data, String[] columnNames, int valueCol) {
        super(data, columnNames);
        this.valueCol = valueCol;
        this.ranking = new Ranking();
        
        vcr = new ValueCellRenderer();
        vcr.setRanking(ranking);
        
        ValueCellMouseListener rml = new ValueCellMouseListener(this);
        addMouseMotionListener(rml);
        addMouseListener(rml);        
    
        sorter = new TableRowSorter<TableModel>(getModel());
        setRowSorter(sorter);
        sorter.setComparator(valueCol, ranking);

        ranking.addRankingListener(new Ranking.Listener() {
            public void valuesChanged(Ranking r) {
                getRowSorter().allRowsChanged();       
            }
        });
    }
    
    public Ranking getRanking() {
        return ranking;
    }
    
    public Objective getObjectiveAt(Point p) {        
        int row = rowAtPoint(p);
        int col = columnAtPoint(p);
        if (row != -1 && col == valueCol) {
            Object o = getValueAt(row, col);
            Rectangle r = getCellRect(row, col, true);
            int x = (int)(p.getX() - r.getX());
            return vcr.getObjective(o, x, (int)r.getWidth());
        }
        return null;
    }

    public void setValueCellRenderer(ValueCellRenderer vcr) {
        this.vcr = vcr;
    }
    
    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        return (column == valueCol) ? 
            vcr : 
            super.getCellRenderer(row, column);
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        Point p = e.getPoint();
        int row = rowAtPoint(p);
        int col = columnAtPoint(p);
        return (col != valueCol) ? 
            super.getToolTipText(e) : 
            ranking.explain(getValueAt(row, col));
    }        
}
