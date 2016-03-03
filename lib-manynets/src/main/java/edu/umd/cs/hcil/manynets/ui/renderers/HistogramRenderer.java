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

import edu.umd.cs.hcil.manynets.model.Distribution;
import edu.umd.cs.hcil.manynets.ui.ColumnManager;
import edu.umd.cs.hcil.manynets.hist.BarRenderer;
import edu.umd.cs.hcil.manynets.hist.DefaultHistogramModel;
import edu.umd.cs.hcil.manynets.hist.Histogram;
import edu.umd.cs.hcil.manynets.hist.HistogramModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTable;

/**
 * Displays a 'distribution cell', a cell whose value is a Distribution
 * of values, as a histogram
 * @author Manuel Freire
 */
public class HistogramRenderer extends JPanel implements ColumnCellRenderer {

    protected Histogram hist = new Histogram(null, BarRenderer.class, null);
    protected ColumnManager cm;

    protected int maxCount = -1; // if not -1, should be the max of all cells.
    protected boolean globalRange = true;

    public HistogramRenderer() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        add(hist, BorderLayout.CENTER);
    }

    @Override
    public void init(ColumnManager cm) {
        this.cm = cm;
    }

    public Histogram getHist() {
        return hist;
    }

    public void setGlobalRange(boolean globalRange) {
        this.globalRange = globalRange;
        if (globalRange) {
            hist.setRange(cm.getMin(), cm.getMax());
            hist.setReferenceModel(cm.getModel());
        } else {
            // FIXME
            hist.setReferenceModel(null);
        }
        hist.refresh();
    }

    public void setGlobalHeight(boolean globalHeight) {
        if (globalHeight) {
            maxCount = cm.getMaxHistogramCount(hist);
        } else {
            maxCount = -1;
        }
        hist.refresh();
    }

    @Override
    public String getToolTipText(Point p) {
        //System.err.println("my bounds are " + hist.getBounds() + " req point " + p);

        DefaultHistogramModel m = (DefaultHistogramModel)hist.getModel();
        if (m.isEmpty()) {
            return "(empty)";
        }
        //m.dump();
        //System.err.println("hist bounds are " + hist.getBounds());
        double f = hist.getValueForPoint(p, hist.getBounds());
        //System.err.println("value for point is " + f);

        StringBuffer sb = new StringBuffer("<html><b>" + m.getValueLabel(f) + "</b><br>");
        Collection<Object> labels = hist.getModel().getLabelsForValue(f);
        String tip = cm.getTooltipFor(labels);
        if (labels.size() == 0){
            tip= "count: " + m.getValueCount(f);
        }
        sb.append(tip);
        sb.append("</html>");
        return sb.toString();
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
    public void selectionDrag(JTable dest, Point start, Point end, Rectangle bounds, boolean isAdjusting) {
    }

    @Override
    public Component getTableCellRendererComponent(JTable table,
            Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        HistogramModel m = (value == null) ?
            new DefaultHistogramModel() : (HistogramModel)value;
        hist.setModel(m);
        if (globalRange) {
            hist.setRange(cm.getMin(), cm.getMax());
            hist.setReferenceModel(cm.getModel());
        }
        hist.setForceMaxCount(maxCount);

        hist.setBackground(isSelected ? table.getSelectionBackground() :
            (column%2)==0 ? cm.getEvenColor() : cm.getOddColor());
        return this;
    }

    @Override
    public boolean updateHighlights() {
        return false;
    }

    @Override
    public JPanel getSettingsPanel() {
        return new HistogramOPanel(this);
    }

    @Override
    public int getMaximumVerticalSize() {
        return ColumnManager.largeVerticalSize;
    }

    @Override
    public boolean canHandle(ColumnManager cm, boolean overview) {
        return ! overview &&
                Distribution.class.isAssignableFrom(cm.getColumnClass());
    }

    @Override
    public String getRendererName() {
        return "Histogram";
    }

    @Override
    public ColumnCellRenderer copy() {
        HistogramRenderer hr = new HistogramRenderer();
        hr.init(cm);
        hr.maxCount = maxCount;
        hr.globalRange = globalRange;
        hr.hist.setRenderer(
                hist.getRenderer() != null ? hist.getRenderer().getClass() : null,
                hist.getSelectionRenderer() != null ? hist.getSelectionRenderer().getClass() : null);
        hr.hist.setDeltaFunction(hist.getDeltaFunction());
        hr.hist.setHeightFunction(hist.getHeightFunction());
        hr.hist.refresh(true);
        return hr;
    }
}