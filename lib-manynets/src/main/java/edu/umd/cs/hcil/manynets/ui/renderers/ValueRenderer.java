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

package edu.umd.cs.hcil.manynets.ui.renderers;

import edu.umd.cs.hcil.manynets.transforms.AddValueSortOptions.ComplexSortObject;
import edu.umd.cs.hcil.manynets.ui.ColumnManager;
import edu.umd.cs.hcil.manynets.vq.Objective;
import edu.umd.cs.hcil.manynets.vq.Ranking;
import edu.umd.cs.hcil.manynets.vq.ValueCellRenderer;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.JTable;

/**
 *
 * @author Manuel Freire
 */
public class ValueRenderer implements ColumnCellRenderer {

    private ColumnManager cm;
    private ComplexSortObject cso;
    private ValueCellRenderer vcr;

    @Override
    public void init(ColumnManager cm) {
        this.cm = cm;
        this.vcr = new ValueCellRenderer() {
            @Override
            public String getStringFor(Object o) {
                return "";
            }
        };
    }

    @Override
    public JPanel getSettingsPanel() {
        return new JPanel();
    }

    @Override
    public String getToolTipText(Point p) {
        return "" + cso.ranking.explain(cso);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table,
            final Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        cso = (ComplexSortObject)value;
        vcr.setRanking(cso.ranking);
        return vcr.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
    }

    @Override
    public int getMaximumVerticalSize() {
        return 16;
    }

    @Override
    public boolean updateHighlights() {
        return false;
    }
    
    public String getXCaption() {
        return "";
    }

    public String getYCaption() {
        return "";
    }

    public void selectionDrag(JTable dest, Point start, Point end, Rectangle bounds, boolean isAdjusting) {
        System.err.println("Would adjust weight right about " + start + " by " + (start.x - end.x) +
                " within " + bounds);
        int n = (start.x - end.x);
        Ranking r = cso.ranking;
        Objective o = vcr.getObjective(cso, start.x, bounds.width);
        o.setWeight((float) o.getWeight() * (1 + 0.01f * n));
        r.normalize();
        r.notifyListeners();
        // FIXME should also re-sort table...
        dest.repaint();
    }

    @Override
    public boolean canHandle(ColumnManager cm, boolean overview) {
        return ( ! overview) &&
                ComplexSortObject.class.isAssignableFrom(cm.getColumnClass());
    }

    @Override
    public String getRendererName() {
        return "Stacked bar";
    }

    @Override
    public ColumnCellRenderer copy() {
        throw new UnsupportedOperationException();
    }
}
