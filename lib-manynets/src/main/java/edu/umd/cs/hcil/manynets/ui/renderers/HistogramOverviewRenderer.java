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
//import edu.umd.cs.hcil.manynets.ui.ColumnManager.ColumnCellRenderer;
import edu.umd.cs.hcil.manynets.hist.BarRenderer;
import edu.umd.cs.hcil.manynets.hist.DefaultHistogramModel;
import edu.umd.cs.hcil.manynets.hist.Histogram;
import edu.umd.cs.hcil.manynets.hist.NullRenderer;
import edu.umd.cs.hcil.manynets.hist.SelectedBarRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.text.DecimalFormat;
import java.util.Collection;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

/**
 *
 * @author Manuel Freire
 */
public class HistogramOverviewRenderer extends JPanel
        implements ColumnCellRenderer, LabelRenderer.AxisLabeller {

    private ColumnManager cm;
    private Histogram hist = null;
    private static DecimalFormat percentageFormatter = new DecimalFormat("0.00");
    private boolean selected = false;
    protected double max = Double.NEGATIVE_INFINITY;
    protected double min = Double.POSITIVE_INFINITY ;
 
    public int getPreferredHeight() {
        return 30;
    }

    @Override
    public void init(ColumnManager cm) {
        this.cm = cm;

        min = cm.getModel().getMin();
        max = cm.getModel().getMax();

        if (isSelectionView()) {
            hist = new Histogram(null, NullRenderer.class, SelectedBarRenderer.class);
        } else {
            hist = new Histogram(null, BarRenderer.class, SelectedBarRenderer.class);
        }
        DefaultHistogramModel m = (DefaultHistogramModel) cm.getModel();
        hist.setModel(m);
        hist.setSelectionModel(cm.getSelectionModel());
        hist.setLevels(Math.min(40, cm.getModel().getValueCount()));
        
        removeAll();
        add(hist, BorderLayout.CENTER);
        validate();

    }

    /** Creates new form HistogramOverviewRenderer */
    public HistogramOverviewRenderer() {
        setLayout(new BorderLayout());
    }

    @Override
    public void setBackground(Color c) {
        super.setBackground(c);
        if (hist != null) {
            hist.setBackground(c);
        }

    }
  
   @Override
    public String getToolTipText(Point p) {
        DefaultHistogramModel m = (DefaultHistogramModel) 
                (isSelectionView() ? cm.getSelectionModel() : cm.getModel());

        double f = hist.getValueForPoint(p, hist.getBounds(), isSelectionView());
        StringBuffer sb = new StringBuffer("<html><b>" + m.getValueLabel(f) + "</b> (" +
                f + ")<br>");
        Collection<Object> labels = m.getLabelsForValue(f);

        if (labels == null) return "";

        int nsel = 0;
        int total = labels.size();
        String percentage = "0";
        if (nsel > 0) {
            percentage = ((percentageFormatter.format(nsel * 100f / total)));
        } else {
            // System.err.println("none selected");
        }
        String tip= cm.getTooltipFor(labels);
        if(labels.size() == 0){
            tip= "count: " + m.getValueCount(f);
        }
        sb.append(tip);
        sb.append("</html>");
        return sb.toString();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        if (table != null && cm != null) {
            setBackground(
                    selected ? table.getSelectionBackground() 
                        : (((column % 2) == 0) ? cm.getEvenColor()
                            : cm.getOddColor()));
        }
        return this;
    }

    public void setDeltaFunction(Histogram.HeightFunction hf) {
        hist.setDeltaFunction(hf);
    }

    public void setHeightFunction(Histogram.HeightFunction hf) {
        hist.setHeightFunction(hf);
    }

    public JPanel getSettingsPanel() {
        return new HistogramOverviewOPanel(hist);
    }

    @Override
    public String getXCaption() {
        String xType = hist.getDeltaFunction().getClass().
                getSimpleName().toLowerCase().replaceAll("heightfunction", "");
        return "Values, " + xType;
    }

    @Override
    public String getYCaption() {
        String yType = hist.getHeightFunction().getClass().
                getSimpleName().toLowerCase().replaceAll("heightfunction", "");
        return "Counts, " + yType;
    }

    @Override
    public int getMaximumVerticalSize() {
        return ColumnManager.largeVerticalSize;
    }

    @Override
    public boolean updateHighlights() {

        hist.refresh(true);
        selected = false;
        return true;
    }

    private boolean isSelectionView() {
        return LabelOverviewRenderer.getInnerRenderer(cm.getSelectionRenderer())
                == this;
    }

    /**
     * @param start
     * @param end
     * @param isAdjusting
     */
    @Override
    public void selectionDrag(JTable t, Point start, Point end, Rectangle bounds, boolean isAdjusting) {

        if (isSelectionView()) {
//            System.err.println("Selection view - no selection drag triggered");
            return;
        }

        double x0 = hist.getValueForPoint(start, bounds);
        double x1 = hist.getValueForPoint(end, bounds);
        double lo = Math.min(x0, x1);
        double hi = Math.max(x0, x1);
//        hist.getModel().clearHighlights();
//        ((DefaultHistogramModel)hist.getModel()).setHighlightsBetween(lo, hi);
        hist.refresh();

        ListSelectionModel sm = t.getSelectionModel();
        sm.setValueIsAdjusting(true);
        sm.clearSelection();
        for (int i : cm.labelsToViewRows(
                hist.getModel().getLabelsBetween(lo, hi))) {
//            System.err.println("Found view rows for " + i + "!");
            sm.addSelectionInterval(i, i);
        }
        //selected = true;
        sm.setValueIsAdjusting(false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(null);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    @Override
    public boolean canHandle(ColumnManager cm, boolean overview) {
        return overview;
    }

    @Override
    public String getRendererName() {
        return "Histogram";
    }

    /**
     * Generates axis labels for this histogram, as part of the AxisLabeller
     * interface
     */
    @Override
    public void setAxisLabels(LabelRenderer lr, boolean isVertical) {
        if (isVertical) {
            lr.clearAxisLabels();
            lr.addAxisLabel(0, "0", false);
            lr.addAxisLabel(1, "" + hist.getMaxCount(), false);
        } else {
            lr.generateHistogramLabels((DefaultHistogramModel)cm.getModel()) ;
        }
    }

    @Override
    public ColumnCellRenderer copy() {
        HistogramOverviewRenderer hor = new HistogramOverviewRenderer();
        hor.init(cm);
        hor.hist.setDeltaFunction(hist.getDeltaFunction());
        hor.hist.setHeightFunction(hist.getHeightFunction());
        hor.hist.refresh(true);
        return hor;
    }
}
