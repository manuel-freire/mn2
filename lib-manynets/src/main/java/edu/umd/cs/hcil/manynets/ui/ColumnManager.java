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

import edu.umd.cs.hcil.manynets.hist.DefaultHistogramModel;
import edu.umd.cs.hcil.manynets.hist.DefaultHistogramModel.DataPoint;
import edu.umd.cs.hcil.manynets.hist.Histogram;
import edu.umd.cs.hcil.manynets.hist.HistogramModel;
import edu.umd.cs.hcil.manynets.model.Distribution;
import edu.umd.cs.hcil.manynets.model.DistributionFactory;
import edu.umd.cs.hcil.manynets.model.Ref;
import edu.umd.cs.hcil.manynets.model.Stat;
import edu.umd.cs.hcil.manynets.model.Stat.Presentation;
import edu.umd.cs.hcil.manynets.model.TableWrapper;
import edu.umd.cs.hcil.manynets.transforms.AddValueSortOptions.ComplexSortObject;
import edu.umd.cs.hcil.manynets.ui.renderers.BoxplotRenderer;
import edu.umd.cs.hcil.manynets.ui.renderers.ColumnCellRenderer;
import edu.umd.cs.hcil.manynets.ui.renderers.HeatmapOverviewRenderer;
import edu.umd.cs.hcil.manynets.ui.renderers.HeatmapRenderer;
import edu.umd.cs.hcil.manynets.ui.renderers.HistogramOverviewRenderer;
import edu.umd.cs.hcil.manynets.ui.renderers.HistogramRenderer;
import edu.umd.cs.hcil.manynets.ui.renderers.LabelOverviewRenderer;
import edu.umd.cs.hcil.manynets.ui.renderers.MultiColumnOverviewRenderer;
import edu.umd.cs.hcil.manynets.ui.renderers.ScalarOverviewRenderer;
import edu.umd.cs.hcil.manynets.ui.renderers.ScalarRenderer;
import edu.umd.cs.hcil.manynets.ui.renderers.ScatterplotOverviewRenderer;
import edu.umd.cs.hcil.manynets.ui.renderers.ValueRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.SortOrder;
import org.jdesktop.swingx.table.ColumnHeaderRenderer;
import org.jdesktop.swingx.table.TableColumnExt;
import prefuse.data.Tuple;



/**
 * Manages a single column, suppying renderers on demand to a JXTable and its
 * associated header-JXTable
 *
 * There is one renderer for the overview, and another for the data. Both are
 * only used by this column, so they can accept column-wide configuration and
 * keep it regardless of what other columns are up to.
 *
 * A static, central repository of renderers is kept here. Renderers
 * should be queried before use to make sure that they can handle the intended
 * data to be rendered.
 *
 * @author Manuel Freire
 */
public class ColumnManager implements TableCellRenderer, TableModelListener {

    public static int largeVerticalSize = 1000000;

    private static Color evenColor = Color.getHSBColor(0.2f, 0f, 1f);
    private static Color evenColorH = Color.getHSBColor(0.2f, 0f, 0.98f);

    private static Color oddColor = Color.getHSBColor(0.6f, 0.05f, 1f);
    private static Color oddColorH = Color.getHSBColor(0.6f, 0.05f, 0.98f);

    private NumberFormat smallFormatter = new DecimalFormat("0.000E0");
    private NumberFormat largeFormatter = new DecimalFormat("0.###");

    private TablePanel tablePanel;
    private JXTable table;
    private TableModel tm;
    private TableColumnExt tc;
    private TableColumnExt htc;

    private HistogramModel model = null;
    private DefaultHistogramModel selectionModel = null;

    private double min = Double.NaN;
    private double max = Double.NaN;

    private ColumnCellRenderer overviewRenderer;
    private ColumnCellRenderer selectionRenderer;
    private ColumnCellRenderer dataRenderer;
    private ArrayList<ColumnCellRenderer> detachedRenderers = new
            ArrayList<ColumnCellRenderer>();

    private ColumnSettingsPanel csp;

    private Stat stat;
    private Presentation presentation;

    private Map<Integer, ArrayList<Entry<Double, Object>>> rowsToRefs =  new
            TreeMap<Integer, ArrayList<Entry<Double, Object>>>();

    private static ArrayList<ColumnCellRenderer> allRenderers;

    static {
        allRenderers = new ArrayList<ColumnCellRenderer>();
        allRenderers.add(new HistogramOverviewRenderer());
        allRenderers.add(new ScatterplotOverviewRenderer());
        allRenderers.add(new HeatmapOverviewRenderer());
        allRenderers.add(new BoxplotRenderer());
        allRenderers.add(new MultiColumnOverviewRenderer());
        allRenderers.add(new ScalarOverviewRenderer());
        allRenderers.add(new HistogramRenderer());
        allRenderers.add(new ScalarRenderer());
        allRenderers.add(new ValueRenderer());
        allRenderers.add(new HeatmapRenderer());
    }

    public ArrayList<ColumnCellRenderer> getAvailableRenderers(boolean overview) {
        ArrayList<ColumnCellRenderer> good = new ArrayList<ColumnCellRenderer>();
        for (ColumnCellRenderer ccr : allRenderers) {
            if (ccr.canHandle(this, overview)) {
                good.add(ccr);
            }
        }
        return good;
    }

    /**
     * Instantiate and register this columnManager
     * @param tc
     * @param htc
     */
    public ColumnManager(TablePanel panel, int viewCol, Stat stat) {

        this.tablePanel = panel;
        this.table = panel.getTable();
        this.tm = table.getModel();
        this.tc = table.getColumnExt(viewCol);
        this.htc = panel.getHeaderTable().getColumnExt(viewCol);
        this.stat = stat;

        this.presentation = stat.getPresentations()[stat.getPresentations().length-1];
        
        csp = new ColumnSettingsPanel();
        update();

        Class dataClass = tm.getColumnClass(tc.getModelIndex());
        boolean distColumn = Distribution.class.isAssignableFrom(dataClass);

        tc.setHeaderRenderer(new WrappedHeaderRenderer(
                table.getGridColor(), panel.getHeaderTable()));
        tc.setComparator(new ExtendedComparator());
        tc.setCellRenderer(this);
        tc.setEditable( ! distColumn);
        tm.addTableModelListener(this);

        htc.setCellRenderer(this);
        htc.setResizable(false);
        htc.setEditable(false);        

        // FIXME: for testing only
        overviewRenderer = new LabelOverviewRenderer()
                .setInner(new HistogramOverviewRenderer());
        overviewRenderer.init(this);

        selectionRenderer = new LabelOverviewRenderer()
                .setInner(new HistogramOverviewRenderer());
        selectionRenderer.init(this);

        dataRenderer = distColumn ?
            new HistogramRenderer() :
                (ComplexSortObject.class.isAssignableFrom(dataClass) ?
                    new ValueRenderer() :
                    new ScalarRenderer());
        dataRenderer.init(this);
    }

    /**
     * Update col. summaries after a value has changed
     * @param e
     */
    @Override
    public void tableChanged(TableModelEvent e) {
        int col = e.getColumn();
        if (col == tc.getModelIndex() || col == TableModelEvent.ALL_COLUMNS) {
            update();
            overviewRenderer.init(this);
            System.err.println("Updated! " + getMax() + " to " + getMin());
        }
    }

    /**
     * Common number format for column
     * @param v
     * @return
     */
    public String formatDouble(double v) {
        return (v != 0 && (v <= 0.1 && v >= -0.1)) ?
            smallFormatter.format(v) : largeFormatter.format(v);
    }

    public int getColumnIndex() {
        return tc.getModelIndex();
    }

    public String getTooltipFor(Collection<Object> labels) {
        StringBuilder sb = new StringBuilder();
        if (labels == null) {
            return "";
        }

        sb.append("<table>");
        int maxRows = 10;
        for (Object lo : labels) {
            if (--maxRows < 0) {
                sb.append("<tr><td><b> ... </b>" +
                    "<tr><td> Total: " + labels.size() + " rows");
                break;
            }
            if (lo instanceof Ref) {
                try {
                    Tuple t = presentation.present((Ref)lo).getTuple();
                    sb.append("<tr>");
                    for (int i=0; i<t.getColumnCount(); i++) {
                        sb.append("<td>" + t.get(i));
                    }
                } catch (Exception e) {
                    sb.append("(!)");
                }
            } else {
                sb.append("" + lo);
            }
        }
        if (maxRows >= 0) {
            sb.append("<tr><td> Total: " + labels.size() + " rows");
        }
        sb.append("</table>");
        return sb.toString();
    }

    /**
     * Return the current distribution / histogramModel
     * @return
     */
    public HistogramModel getModel() {
        return model;
    }

    public Class getColumnClass() {
        return table.getModel().getColumnClass(tc.getModelIndex());
    }

    public HistogramModel getSelectionModel() {
        return selectionModel;
    }

    public JXTable getTable() {
        return table;
    }

    public String getColumnName() {
        return table.getModel().getColumnName(tc.getModelIndex());
    }

    public Object getValue(int modelRow) {
        return table.getModel().getValueAt(modelRow, tc.getModelIndex());
    }

    public Object getModelRowId(int modelRow) {
        TableWrapper tw = tablePanel.getBaseWrapper();
        return tw.getTable().get(modelRow, tw.getIdField());
    }

    public Object getViewRowId(int viewRow) {
        return tablePanel.getId(viewRow);
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public Color getEvenColor() {
        return evenColor;
    }

    public Color getOddColor() {
        return oddColor;
    }

    public ImageIcon getIcon(int viewRow) {
        TableWrapper tw = tablePanel.getBaseWrapper();
        int modelRow = table.convertRowIndexToModel(viewRow);
        Ref r = tw.getRef(tw.getId(modelRow));
        return r == null ? null : stat.getIcon(r);
    }

    /**
     * Read in column data, and update the range and observations
     */
    public void update() {
//        System.err.println("Updating column " + tc.getModelIndex());
        model = null;
        TableWrapper tw = tablePanel.getBaseWrapper();
        try {
            
            model = DistributionFactory.build(tw, 
                    tc.getModelIndex());

            selectionModel = new DefaultHistogramModel();

            // build quick row-id to ref mapping
            for (Entry<Double,DataPoint> me : ((DefaultHistogramModel)model).points()) {
                for (Object o : me.getValue().labels) {
                    int i = tablePanel.labelToModelRow(o, tc.getModelIndex());
                    if (i == -1) {
                        // refs without a model row should be impossible...
                        System.err.println("Ref without row in col "
                                + tm.getColumnName(tc.getModelIndex()) + " = "
                                +((Ref)o).getTableWrapper().getName() +
                                "::" + ((Ref)o).getId());
                        continue;
                    }
                    ArrayList al = rowsToRefs.get(i);
                    if (al == null) {
                        al = new ArrayList();
                        rowsToRefs.put(i, al);
                    }
                    al.add(new SimpleEntry((Double)me.getKey(), o));
                }
            }
        } catch (Exception e) {
            System.err.println("Problem with col " + tc.getModelIndex() + ": " +
                    tm.getColumnName(tc.getModelIndex()) + " in " +
                    tm.getRowCount() + " x " + tm.getColumnCount());
            e.printStackTrace();
        }
        min = model.getMin();
        max = model.getMax();
    }

    /**
     * Returns the name of the currently-used sorting
     * @return
     */
    public String getCurrentSorting() {
        TableColumn stc = table.getSortedColumn();
        return (stc == null) ? "initial order" :
            table.getModel().getColumnName(stc.getModelIndex());       
    }

    private static class SimpleEntry implements Entry<Double,Object> {
        private Double key;
        private Object value;
        public SimpleEntry(Double key, Object value) {
            this.key = key; this.value = value;
        }
        @Override
        public Double getKey() { return key; }
        @Override
        public Object getValue() { return value; }
        @Override
        public Object setValue(Object arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    /**
     * Update any highlights in this column. Return 'true' if a re-render is
     * in order.
     */
    public boolean updateHighlights(int[] modelRows) {
        selectionModel.clear();
        for (int i : modelRows) {
            ArrayList<Entry<Double,Object>> es = rowsToRefs.get(i);
            if (es == null) {
                // some rows have no refs at all... (think empty distributions)
                continue;
            }
            for (Entry<Double,Object> e : es) {
                selectionModel.addValue(e.getKey(), e.getValue());
            }
        }

        boolean dirty = overviewRenderer.updateHighlights();
        dirty |= dataRenderer.updateHighlights();
        selectionRenderer.updateHighlights();
        if ( ! detachedRenderers.isEmpty()) {
            System.err.println("notifying " + detachedRenderers.size() + " detached renderers");
            for (ColumnCellRenderer r : detachedRenderers) {
                r.updateHighlights();
            }
        }

        return dirty;
    }

    /**
     * Return view rows that correspond to a set of labels
     * @param labels
     * @return
     */
    public Collection<Integer> labelsToViewRows(Collection labels) {
        return tablePanel.labelsToViewRows(labels, tc.getModelIndex());
    }

    /**
     * Return a suitable renderer for this table
     * @return
     */
    @Override
    public Component getTableCellRendererComponent(JTable t,
            Object v, boolean sel, boolean focus, int r, int c) {
        
        return getRealRenderer(t)
                .getTableCellRendererComponent(t, v, sel, focus, r, c);
    }

    /**
     * Return the real renderer for a given table
     */
    public ColumnCellRenderer getRealRenderer(JTable t) {
        return (t.getModel() == tm) ?
            dataRenderer : overviewRenderer;
    }

    /**
     * @return the selectionRenderer
     */
    public ColumnCellRenderer getSelectionRenderer() {
        return selectionRenderer;
    }

    public void setSelectionRenderer(ColumnCellRenderer selectionRenderer) {
        this.selectionRenderer = selectionRenderer;
    }

    /**
     * @return the overviewRenderer
     */
    public ColumnCellRenderer getOverviewRenderer() {
        return overviewRenderer;
    }


    public TablePanel getTablePanel() {
        return tablePanel;
    }

    /**
     * @param overviewRenderer the overviewRenderer to set
     */
    public void setOverviewRenderer(ColumnCellRenderer overviewRenderer) {
        this.overviewRenderer = overviewRenderer;
    }

    /**
     * @return the dataRenderer
     */
    public ColumnCellRenderer getDataRenderer() {
        return dataRenderer;
    }

    /**
     * @param dataRenderer the dataRenderer to set
     */
    public void setDataRenderer(ColumnCellRenderer dataRenderer) {
        this.dataRenderer = dataRenderer;
    }

    public void addDetachedRenderer(ColumnCellRenderer ccr) {
        detachedRenderers.add(ccr);
    }

    public void removeDetachedRenderer(ColumnCellRenderer ccr) {
        Thread.dumpStack();
        detachedRenderers.remove(ccr);
    }

    /**
     * Finds the highest value in all histograms, were they to be rendered
     * with a given, sample histogram; important settings: max, min, axis
     * scaling.
     * @return
     */
    public int getMaxHistogramCount(Histogram h) {
        int maxCount = 0;
        for (int i=0; i<tm.getRowCount(); i++) {
            Object v = tm.getValueAt(i, tc.getModelIndex());
            int n = (v instanceof HistogramModel) ?
                h.calculateMaxCount((HistogramModel)v) : 0;
            maxCount = Math.max(maxCount, n);
        }
        return maxCount;
    }

//------------ Interfaces for renderers -----------

    /**
     * Utilities for SettingsPanels for different renderers
     */
    public static class AbstractRendererOPanel<R extends ColumnCellRenderer> extends ConfigPanel<R> {
        public static final String renderOptionsProperty =
                "render-options-property";

        public void fireOptionsChanged() {
            firePropertyChange(renderOptionsProperty, this, null);
            System.err.println("options fired!");
        }

        public void endSetupAndPrepareListener(R renderer) {
            setTarget(renderer);
            initialize();
            addConfigListener(new ConfigListener() {
                @Override
                public void configChanged(boolean isConfigValid) {
                    if (isConfigValid) fireOptionsChanged();
                }
            });
        }
    }


//------------ A simple header renderer -----------

    /**
     * Used by WrappedHeaderRenderer
     */
    static private int sortOrderToInt(SortOrder so) {
        return so.isSorted() ? (so.isAscending() ? 1 : 2) : 0;
    }

    /**
     * Renders a header cell as a (possibly wrapping) label
     * Also repaints all headers after each sorting event.
     */
    public class WrappedHeaderRenderer extends JLabel
            implements TableCellRenderer {

        private SortOrder prevOrder = SortOrder.UNSORTED;
        private JTable headerTable;

        public WrappedHeaderRenderer(Color gridColor, JTable headerTable) {
            this.headerTable = headerTable;
            setFont(getFont().deriveFont(Font.PLAIN));
            setOpaque(true);
            setBorder(BorderFactory.createMatteBorder(0, 0, 2, 1, gridColor));
        }

        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            setBackground((column%2)==0 ? evenColorH : oddColorH);
            String s = "" + value;
            s = s.replaceAll("[\n]+", "<br>");
            s = s.replaceAll("[:]", " ");
            s = "<table><tr><td>"
                    + s + "</td></tr></table>";
            setText("<html>" + s + "</html>");

            SortOrder so = ((JXTable)table).getSortOrder(column);
            if ( ! so.equals(prevOrder)) {
                table.firePropertyChange(TablePanel.TABLE_SORTING_PROPERTY,
                        sortOrderToInt(so), sortOrderToInt(prevOrder));
                headerTable.repaint();
                prevOrder = so;
            }

            if (so.equals(SortOrder.UNSORTED)) {
                setIcon(null);
            } else if (so.equals(SortOrder.ASCENDING)) {
                setIcon(UIManager.getIcon(ColumnHeaderRenderer.DOWN_ICON_KEY));
            } else if (so.equals(SortOrder.DESCENDING)) {
                setIcon(UIManager.getIcon(ColumnHeaderRenderer.UP_ICON_KEY));
            }

            return this;
        }
    }
}
