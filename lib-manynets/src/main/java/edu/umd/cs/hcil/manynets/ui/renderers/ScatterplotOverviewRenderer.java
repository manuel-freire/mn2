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

import edu.umd.cs.hcil.manynets.ui.ColumnManager;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;

/**
 *
 * @author Manuel Freire
 */
public class ScatterplotOverviewRenderer extends JPanel implements ColumnCellRenderer {

    private ColumnManager cm;

    private int xCol = -1;
    private int yCol = -1;
    private Color squareColor = Color.blue.darker();
    private Color highlightColor = Color.green;

    private P[] cache;
    private double xlo, xhi, ylo, yhi;
    private int lastWidth, lastHeight;

    public void init(ColumnManager cm) {
        this.cm = cm;
        xCol = cm.getColumnIndex();
        yCol = findInitialYColumn(cm);
        
        buildCache();
//
//        cm.getTablePanel().addPropertyChangeListener(
//                TablePanel.CELL_CLICKED_PROPERTY, new BorderListener());
//        setBackground(Color.white);
    }

    private NumberFormat smallFormatter = new DecimalFormat("0.000E0");
    private NumberFormat largeFormatter = new DecimalFormat("0.###");

    public String formatDouble(double v) {
        String s = (v != 0 && (v <= 0.1 && v >= -0.1)) ?
            smallFormatter.format(v) : largeFormatter.format(v);
        return s.replaceAll("E", "");
    }

    public void setOtherColumn(String name) {
        TableModel tm = cm.getTable().getModel();
        for (int i=0; i<tm.getColumnCount(); i++) {
            if (tm.getColumnName(i).equals(name)) {
                this.yCol = i;
                buildCache();
                return;
            }
        }
        throw new IllegalArgumentException("Column " + name + " not found");
    }

    @Override
    public JPanel getSettingsPanel() {
        TableModel tm = cm.getTable().getModel();
        String badCol = cm.getColumnName();
        String[] columns = new String[tm.getColumnCount() -1];
        for (int i=0, j=0; i<tm.getColumnCount(); i++) {
            if ( ! tm.getColumnName(i).equals(badCol)) {
                columns[j++] = tm.getColumnName(i);
            }
        }
        return new ScatterplotOverviewOPanel(this, columns);
    }

    @Override
    public String getToolTipText(Point p) {
        float x = p.x / (float)lastWidth;
        float y = 1 - p.y / (float)lastHeight;
        StringBuilder sb = new StringBuilder("<html>\n");
        for (P np : getNearestPoints(x, y)) {
            sb.append(np.toString() + "<br>\n");
        }
        sb.append("</html>");
        return "" + sb.toString();
    }

    public class P {
        Point2D.Double p;
        int row;
        public P(double x, double y, int row) {
            this.p = new Point2D.Double(x, y);
            this.row = row;
        }
        @Override
        public String toString() {
            TableModel tm = cm.getTable().getModel();
            return "ID " +  cm.getModelRowId(row) + " at " +
                    tm.getValueAt(row, xCol) + ", " + tm.getValueAt(row, yCol);
        }
    }

    @Override
    public String getYCaption() {
        return cm.getTable().getModel().getColumnName(yCol);
    }

    @Override
    public String getXCaption() {
        return cm.getTable().getModel().getColumnName(cm.getColumnIndex());
    }

    public ArrayList<P> getNearestPoints(float x, float y) {
        ArrayList<P> al = new ArrayList<P>();
        double minDist = Float.POSITIVE_INFINITY;
        for (P p : cache) {
            double d = p.p.distanceSq(x, y);
            if (d < minDist) {
                minDist = d;
                al.clear();
            }
            if (d == minDist) {
                al.add(p);
            }
        }
        return al;
    }

    public ArrayList<P> getPointsIn(Rectangle2D r) {
        //TODO
        // x axis is the selected column. y axis can be different 
        return null;
    }

   private void buildCache() {
        // Find limits
        TableModel tm = cm.getTable().getModel();
        xlo = cm.getModel().getMin();
        xhi = cm.getModel().getMax();
        
        ylo = Float.POSITIVE_INFINITY;
        yhi = Float.NEGATIVE_INFINITY;
        
        for (int row=0; row<tm.getRowCount(); row++) {
            float v = Float.valueOf("" + tm.getValueAt(row, yCol));
            ylo = Math.min(ylo, v);
            yhi = Math.max(yhi, v);
        }

        cache = new P[tm.getRowCount()];
        for (int row=0; row<tm.getRowCount(); row++) {
            double x = Double.valueOf("" + tm.getValueAt(row, xCol));
            double y = Double.valueOf("" + tm.getValueAt(row, yCol));
            x = (x - xlo) / (xhi - xlo);
            y = (y - ylo) / (yhi - ylo);
            cache[row] = new P(x, y, row);
        }
    }

    public void paint(Graphics g) {
        super.paint(g);

        int w = getWidth();
        int h = getHeight();

        BufferedImage bi = new BufferedImage(w, h,
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = bi.createGraphics();

        Rectangle2D s = new Rectangle2D.Float();
        g2d.setColor(squareColor);
        g2d.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, 0.4f));
        for (P p : cache) {
            int tr = cm.getTable().convertRowIndexToView(p.row);
            if (cm.getTable().isRowSelected(tr)) {
                continue;
            }
            s.setRect(p.p.x * (w - 3) + 1, (1 - p.p.y) * (h - 3) + 1, 2, 2);
            g2d.draw(s);
        }

        g2d.setColor(highlightColor);
        g2d.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, 0.9f));
        for (P p : cache) {
            int tr = cm.getTable().convertRowIndexToView(p.row);
            if (!cm.getTable().isRowSelected(tr)) {
                continue;
            }
            s.setRect(p.p.x * (w - 3) + 1, (1 - p.p.y) * (h - 3) + 1, 2, 2);
            g2d.draw(s);
        }
        lastWidth = getWidth();
        lastHeight = getHeight();

        g.drawImage(bi, 0, 0, w - 1, h - 1, this);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table,
            final Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
       setBackground((column % 2) ==0 ? cm.getEvenColor() : cm.getOddColor());
       return this;
    }

    @Override
    public int getMaximumVerticalSize() {
        return ColumnManager.largeVerticalSize;
    }

    @Override
    public boolean updateHighlights() {
        return false;
    }

    public void selectionDrag(JTable dest, Point start, Point end, Rectangle bounds, boolean isAdjusting) {

        int totalRows=cm.getTable().getRowCount();
        //table rows are alphabetically sorted, so this will not work.

        float x0 = (float) start.getX()/lastWidth;
        int rowi = Math.round( x0 * totalRows  );
      //  System.err.println("scatter plot: Searching for " + x0 + " = " + rowi);
        rowi=Math.max(Math.min(rowi, totalRows-1), 0);

        float x1 = (float) end.getX()/lastWidth;
        int rowf = Math.round( x1 * totalRows );
     //   System.err.println("scatter plot:  Searching for " + x1 + " = " + rowf);
        rowf=Math.max(Math.min(rowf, totalRows-1), 0);


        double y0 = (float) start.getY()/lastHeight;
        y0 = y0*(yhi-ylo)+ylo;
        //System.err.println("scatter plot: Searching for y0 " + y0 );

        double y1 = (float) end.getY()/lastHeight;
        y1 = y1*(yhi-ylo)+ylo;
        //System.err.println("scatter plot:  Searching for y1 " + y1 );

        double loy= Math.min(y1, y0);
        double hiy= Math.max(y1, y0);

        ListSelectionModel sm = cm.getTable().getSelectionModel();
        sm.setValueIsAdjusting(true);
        sm.clearSelection();

        int lox=Math.min(rowf, rowi);
        int hix=Math.max(rowf, rowi);

        for (int i = lox; i <= hix; i++) {
//          int tableRow = cache[i].row;
            
            if (cm.getTable().getColumnClass(yCol).toString().equalsIgnoreCase(Integer.class.toString() )) {
                Integer value = (Integer) cm.getTable().getValueAt(i, yCol);
                System.err.println(value);
                if (value >= loy && value <= hiy) {
                     System.err.println("val" + value);
                    sm.addSelectionInterval(i, i);
                }
            } else if (cm.getTable().getColumnClass(yCol).toString().equalsIgnoreCase( Double.class.toString() )) {
                Double value = (Double) cm.getTable().getValueAt(i, yCol);
                 System.err.println(value);
                if (value >= loy && value <= hiy) {
                     System.err.println(value);
                    sm.addSelectionInterval(i, i);
                }
            }
        }

        sm.setValueIsAdjusting(false);
    }

 
     /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jpMain = new javax.swing.JPanel();
        jlMin = new javax.swing.JLabel();
        jlMax = new javax.swing.JLabel();
        jlSeparator = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());


        jMinCount = new javax.swing.JLabel();
        jMaxCount = new javax.swing.JLabel();

        jMinCount.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jMinCount.setFont(jMinCount.getFont().deriveFont(jMinCount.getFont().getSize()-3f));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        add(jMinCount, gridBagConstraints);
        jMinCount.setVisible(false);

        jMaxCount.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        jMaxCount.setFont(jMaxCount.getFont().deriveFont(jMaxCount.getFont().getSize()-3f));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(jMaxCount, gridBagConstraints);
        jMaxCount.setVisible(false);

        

        jpMain.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
        add(jpMain, gridBagConstraints);

        jlMin.setFont(jlMin.getFont().deriveFont(jlMin.getFont().getSize() - 3f));
        jlMin.setText("jLabel1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 0, 0);
        add(jlMin, gridBagConstraints);

        jlMax.setFont(jlMax.getFont().deriveFont(jlMax.getFont().getSize() - 3f));
        jlMax.setText("jLabel3");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        add(jlMax, gridBagConstraints);

        jlSeparator.setFont(jlSeparator.getFont().deriveFont(jlSeparator.getFont().getSize() - 3f));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jlSeparator, gridBagConstraints);
    }// </editor-fold>
    // Variables declaration - do not modify
    private javax.swing.JLabel jlMax;
    private javax.swing.JLabel jlMin;
    private javax.swing.JLabel jlSeparator;
    private javax.swing.JPanel jpMain;
    private javax.swing.JLabel jMaxCount;
    private javax.swing.JLabel jMinCount;
    // End of variables declaration

    /**
     * Can be called before 'init'; looks for a candidate column (ie.: not the 
     * same as this one, and with integer or double contents) to use as a y-column
     */
    private int findInitialYColumn(ColumnManager cm) {
        int current = cm.getColumnIndex();
        for (int i=0; i<cm.getTable().getColumnCount(); i++) {
//            if (i == current) continue;
            Class cc = cm.getTable().getColumnClass(i);
            if (Integer.TYPE.isAssignableFrom(cc) || Double.TYPE.isAssignableFrom(cc)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean canHandle(ColumnManager cm, boolean overview) {
        return true;
//        return overview && (
//                Integer.TYPE.isAssignableFrom(cm.getColumnClass()) ||
//                Double.TYPE.isAssignableFrom(cm.getColumnClass())) &&
//                    ( findInitialYColumn(cm) != -1);
    }

    @Override
    public String getRendererName() {
        return "Scatterplot";
    }

    @Override
    public ColumnCellRenderer copy() {
        ScatterplotOverviewRenderer r = new ScatterplotOverviewRenderer();
        r.init(cm);
        r.yCol = yCol;
        buildCache();
        return r;
    }
}
