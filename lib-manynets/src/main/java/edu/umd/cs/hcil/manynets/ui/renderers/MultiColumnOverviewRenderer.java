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

import edu.umd.cs.hcil.manynets.hist.DefaultHistogramModel;
import edu.umd.cs.hcil.manynets.model.Distribution;
import edu.umd.cs.hcil.manynets.ui.ColumnManager;
import edu.umd.cs.hcil.manynets.ui.renderers.AbstractRowRenderer.Metric;
import edu.umd.cs.hcil.manynets.ui.renderers.AbstractRowRenderer.Sorting;
import edu.umd.cs.hcil.manynets.ui.renderers.HeatmapOverviewRenderer.ColorScheme;
import edu.umd.cs.hcil.manynets.ui.renderers.HeatmapOverviewRenderer.IntensityEmphasis;
import edu.umd.cs.hcil.manynets.ui.renderers.HeatmapOverviewRenderer.IntensityType;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTable;

/**
 * Once configured, can create an overview of several columns at once, driven
 * by a master column (at position 0)
 *
 * @author Manuel Freire
 */
public class MultiColumnOverviewRenderer extends JPanel
        implements ColumnCellRenderer, LabelRenderer.AxisLabeller,
        GraphicLabelRenderer.GraphicLabeller {

    private ArrayList<ColumnManager> columns;
    private ArrayList<AbstractRowRenderer> subRenderers;
    private ColumnManager cm;
    
    public MultiColumnOverviewRenderer() {
        super();
        columns = new ArrayList<ColumnManager>();
        subRenderers = new ArrayList<AbstractRowRenderer>();
    }

    private void addColumn(ColumnManager cm, AbstractRowRenderer arr) {
        columns.add(cm);
        if (arr == null) {
            try {
                arr = Distribution.class.isAssignableFrom(cm.getColumnClass()) ?
                    new HeatmapOverviewRenderer() : new ScalarOverviewRenderer();
            } catch (Exception e) {
                System.err.println("Unable to instantiate default subrenderer");
                e.printStackTrace();
            }
        }
        if (arr instanceof HeatmapOverviewRenderer) {
            boolean odd = ((columns.size() % 2) == 0);
            ((HeatmapOverviewRenderer) arr).setColorScheme(
                    odd ? ColorScheme.whiteBlue2 : ColorScheme.whiteRed2);
            if (subRenderers.size() > 0) {
                arr.cloneSorting(subRenderers.get(0));
            }
        }
        subRenderers.add(arr);
        arr.init(cm);
        add(arr);
        revalidate();
    }

    @Override
    public void init(ColumnManager cm) {
        this.cm = cm;
        columns.clear();
        subRenderers.clear();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        removeAll();
        addColumn(cm, Distribution.class.isAssignableFrom(cm.getColumnClass()) ?
            new HeatmapOverviewRenderer() : new ScalarOverviewRenderer());
    }

    public ArrayList<AbstractRowRenderer> getSubrenderers() {
        return subRenderers;
    }

    public ArrayList<ColumnManager> getColumns() {
        return columns;
    }

    public void setColumns(int[] cols) {
        columns.clear();
        subRenderers.clear();
        removeAll();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        for (int i : cols) {
            addColumn(cm.getTablePanel().getColumnManager(i), null);
        }
        setSortAttribute(subRenderers.get(0).getSortAttribute());
    }

    public void setColumnString(String colString) {
        int[] cols = null;
        try {
            String[] split = colString.split("[, ]+");
            cols = new int[split.length];
            int i = 0;
            for (String s : split) {
                cols[i++] = Integer.parseInt(s);
            }
        } catch (Exception e) {
            System.err.println("bad col selection");
        }
        setColumns(cols);
    }

    public String getColumnString() {
        StringBuilder sb = new StringBuilder();
        for (ColumnManager c : columns) {
            sb.append("").append(c.getColumnIndex()).append(",");
        }
        return sb.substring(0, sb.lastIndexOf(",")).toString();
    }

    @Override
    public JPanel getSettingsPanel() {
        return new MultiColumnOPanel(this);
    }

    public void setSortAttribute(AbstractRowRenderer.Sorting sortAttribute) {

        // the first column is sorted by this
        AbstractRowRenderer arr0 = subRenderers.get(0);
        arr0.setSortAttribute(sortAttribute);

        if(sortAttribute.equals(Sorting.Multicluster)){
            arr0.setOtherColumns(columns);
        }
        arr0.sort();

        // the rest are just copied over
        for (AbstractRowRenderer arr : subRenderers) {            
            if (arr == arr0) {
                continue;
            }
            arr.cloneSorting(arr0);
            arr.repaint();
        }
        repaint();
    }



    public void setSortMetric(AbstractRowRenderer.Metric metric) {

//        System.err.println(" Metric is " + metric);

        // the first column is sorted by this
        AbstractRowRenderer arr0 = subRenderers.get(0);
        arr0.setSortMetric(metric);

        // the rest are just copied over
        for (AbstractRowRenderer arr : subRenderers) {
            if (arr == arr0) {
                continue;
            }
            arr.cloneSorting(arr0);
        }
        repaint();
    }

    @Override
    public String getToolTipText(Point p) {
        int delta = getWidth()/subRenderers.size();
        int tipColIndex = 0;
        while (p.x - delta > 0) {
            p.x -= delta;
            tipColIndex ++;
        }
        // avoid off-by-one errors, due to integer division
        tipColIndex = Math.min(subRenderers.size()-1, tipColIndex);
        // add an indication of the column we're in
        String tt = subRenderers.get(tipColIndex).getToolTipText(p);
        tt = tt.replaceFirst("<html>",
                "<html><b>Col " + columns.get(tipColIndex).getColumnName() + "</b><hr>");
        return tt;
    }

    @Override
    public int getMaximumVerticalSize() {
        return subRenderers.get(0).getMaximumVerticalSize();
    }

    @Override
    public boolean updateHighlights() {
        boolean updated = false;
        for (AbstractRowRenderer arr : subRenderers) {
            updated |= arr.updateHighlights();
        }
        return updated;
    }

    @Override
    public void selectionDrag(JTable dest, Point start, Point end, Rectangle bounds, boolean isAdjusting) {
        for (AbstractRowRenderer arr : subRenderers) {
            arr.selectionDrag(dest, start, end, bounds, isAdjusting);
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        for (AbstractRowRenderer arr : subRenderers) {
            arr.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);
        }
        return this;
    }

    public Metric getSortMetric() {
        return subRenderers.get(0).getSortMetric();
    }

    public String getOtherColumns() {
        StringBuilder sb = new StringBuilder();
        for (AbstractRowRenderer arr : subRenderers) {
            sb.append(arr.cm.getColumnIndex()).append(", ");
        }
        if (sb.length() > 0) sb.setLength(sb.lastIndexOf(","));
        return "[" + sb + "]";
    }

    public Sorting getSortAttribute() {
        return subRenderers.get(0).getSortAttribute();
    }

    public IntensityType getIntensityType() {
        for (AbstractRowRenderer arr : subRenderers) {
            if (arr instanceof HeatmapOverviewRenderer) {
                return ((HeatmapOverviewRenderer)arr).getIntensityType();
            }
        }
        return IntensityType.global;
    }

    public void setIntensityType(IntensityType type) {
        for (AbstractRowRenderer arr : subRenderers) {
            if (arr instanceof HeatmapOverviewRenderer) {
                ((HeatmapOverviewRenderer)arr).setIntensityType(type);
            }
        }
    }

    public IntensityEmphasis getIntensity() {
        for (AbstractRowRenderer arr : subRenderers) {
            if (arr instanceof HeatmapOverviewRenderer) {
                return ((HeatmapOverviewRenderer)arr).getIntensity();
            }
        }
        return IntensityEmphasis.emphNormal;
    }

    public void setIntensity(IntensityEmphasis emph) {
        for (AbstractRowRenderer arr : subRenderers) {
            if (arr instanceof HeatmapOverviewRenderer) {
                ((HeatmapOverviewRenderer)arr).setIntensity(emph);
            }
        }
    }

    @Override
    public boolean canHandle(ColumnManager cm, boolean overview) {
        return new HeatmapOverviewRenderer().canHandle(cm, overview) ||
                new ScalarOverviewRenderer().canHandle(cm, overview);
    }

    @Override
    public String getRendererName() {
        return "MultiColumn";
    }

    @Override
    public String getXCaption() {
        StringBuilder sb = new StringBuilder("Comparing: ");
        for (ColumnManager m : columns) {
            sb.append(" ").append(m.getColumnName());
        }
        return sb.toString();
    }

    @Override
    public String getYCaption() {
        return subRenderers.get(0).getYCaption();
    }

    @Override
    public void setAxisLabels(LabelRenderer lr, boolean isVertical) {
        if (isVertical) {
            lr.clearAxisLabels();
            lr.addAxisLabel(0, "" + cm.getTable().getRowCount() + " rows");
        } else {
            // lr.generateHistogramLabels((DefaultHistogramModel)cm.getModel()) ;
            double delta = 1.0/columns.size();
            double start = 0;
            double end;
            for (ColumnManager m : columns) {
                lr.addAxisLabel2(start, m.getColumnName());
                end = start + delta;
                if (end + delta > 1) {
                    end = 1;
                }
                DefaultHistogramModel dhm = (DefaultHistogramModel)m.getModel();
                lr.generateHistogramLabels(dhm, start, end, start == 0);
                start += delta;
            }
        }
    }

    @Override
    public ColumnCellRenderer copy() {
        MultiColumnOverviewRenderer r = new MultiColumnOverviewRenderer();
        r.init(cm);

        r.removeAll();
        r.columns = new ArrayList<ColumnManager>();
        r.subRenderers = new ArrayList<AbstractRowRenderer>();
        for (int i = 0; i < columns.size(); i++) {
            AbstractRowRenderer arr = (AbstractRowRenderer) subRenderers.get(i).copy();
            r.addColumn(columns.get(i), arr);
        }
        r.setSortMetric(getSortMetric());
        r.setSortAttribute(getSortAttribute());
        r.setIntensityType(getIntensityType());
        r.setIntensity(getIntensity());
        return r;
    }

     /**
     * Generates graphic labels for this histogram
     */
    @Override
    public void setGraphicLabels(GraphicLabelRenderer glr) {
        glr.setColumn( cm ) ;
    }
}
