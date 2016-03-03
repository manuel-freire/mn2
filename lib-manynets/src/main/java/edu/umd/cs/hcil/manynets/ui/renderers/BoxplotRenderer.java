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

import edu.umd.cs.hcil.manynets.ColorPalette;
import edu.umd.cs.hcil.manynets.model.Distribution;
import edu.umd.cs.hcil.manynets.ui.ColumnManager;
import edu.umd.cs.hcil.manynets.hist.DefaultHistogramModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

/**
 *
 * @author Awalin Shopan, Manuel Freire
 */
public class BoxplotRenderer extends AbstractRowRenderer {
    public BoxplotRenderer() {
        sortAttribute = sortAttribute.StdDev;
    }

    @Override
    protected void paintImage(BufferedImage bi) {

        Graphics2D g2d = bi.createGraphics();

        int totalRows = cm.getTable().getRowCount();

        ColorPalette b = ColorPalette.getPalette("blue");
        ColorPalette c = ColorPalette.getPalette("red");
        Color[] colors = new Color[]{
            b.map(0f), b.map(.4f), c.map(.4f), c.map(0f)};
        
        for (int i = 0; i < totalRows; i++) {
            DefaultHistogramModel d = (DefaultHistogramModel)cm.getValue(indices[i]);
            // i is the row number after sorting

            double avg = d.getAvg();
            double std = d.getStandardDeviation() ;

            double stdA = avg + std;
            double stdB = avg - std;

            g2d.setColor(colors[0]);
            g2d.drawLine( xValueInt( Math.max(min,d.getMin()) ) ,i,
                    xValueInt(Math.max(stdB,d.getMin()) ), i);

            g2d.setColor(colors[1]);
            g2d.drawLine ( xValueInt(Math.max(stdB,d.getMin())), i,
                    xValueInt(avg), i );

            g2d.setColor(colors[2]);
            g2d.drawLine(xValueInt(avg),i, xValueInt(Math.min(d.getMax(),stdA)) , i);

            g2d.setColor(colors[3]);
            g2d.drawLine( xValueInt(Math.min(d.getMax(),stdA)) , i,
                    xValueInt( d.getMax() ) , i);
        }
    }


    @Override
    protected String getToolTipText(int viewRow, Point mousePosition) {

        StringBuilder sb = new StringBuilder("<html>\n");
        sb.append("<b>ID</b> " + cm.getTable().getModel().getValueAt(viewRow, 0));
        DefaultHistogramModel d = (DefaultHistogramModel) cm.getValue(viewRow);
        sb.append("<br/>min " + d.getMin());
        sb.append("<br/>avg " + d.getAvg() + "<br/>");
        sb.append("max " + d.getMax());
        sb.append("</html>");
        return "" + sb.toString();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        return this;
    }

    @Override
    public JPanel getSettingsPanel() {
        return new DefaultRowRendererOPanel(this);
    }

    @Override
    public int getMaximumVerticalSize() {
        return -1;
    }

    @Override
    public String getXCaption() {
        return "min, -stdev, avg, stdev and max";
    }

   private int getRowForPoint(Point p, Rectangle bounds) {
        float y = (float)p.getY();
        int row = Math.round( y*cm.getTable().getRowCount()/ bounds.height);
    //    System.err.println( " Searching for " + y + " = " + row);
        return row;
    }

    /**
     * @param start
     * @param end
     * @param isAdjusting
     */
    public void selectionDrag(Point start, Point end, Rectangle bounds, boolean isAdjusting) {

        int y0 = getRowForPoint(start, bounds);
        int y1 = getRowForPoint(end, bounds);
        int lo = Math.min(y0, y1);
        int hi = Math.max(y0, y1);

        ListSelectionModel sm = cm.getTable().getSelectionModel();
        sm.setValueIsAdjusting(true);
        sm.clearSelection();

        for ( int i = lo ; i < hi ; i++ ) {
            int tableRow = cm.getTable().convertRowIndexToView(indices[i]);
            System.err.println(i + " th row is row # = " + tableRow);
            sm.addSelectionInterval(tableRow, tableRow);
        }
        sm.setValueIsAdjusting(false);
    }

    @Override
    public boolean canHandle(ColumnManager cm, boolean overview) {
        return overview &&
                Distribution.class.isAssignableFrom(cm.getColumnClass());
    }

    @Override
    public String getRendererName() {
        return "Boxplot";
    }

    @Override
    public ColumnCellRenderer copy() {
        BoxplotRenderer r = new BoxplotRenderer();
        r.dirty = true;
        r.metric = metric;
        r.sortAttribute = sortAttribute;
        r.suspendSort = suspendSort;

        r.init(cm);
        return r;
    }
}
