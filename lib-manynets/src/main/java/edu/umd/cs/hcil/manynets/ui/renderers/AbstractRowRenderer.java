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

import edu.umd.cs.hcil.manynets.dgram.DendrogramSorter;
import edu.umd.cs.hcil.manynets.model.Distribution;
import edu.umd.cs.hcil.manynets.ui.ColumnManager;
import edu.umd.cs.hcil.manynets.ui.TablePanel;
import edu.umd.cs.hcil.manynets.hist.DefaultHistogramModel;
import edu.umd.cs.hcil.manynets.hist.HistogramStats;
import edu.umd.cs.hcil.manynets.hist.HistogramStats.DistanceMetric;
import edu.umd.cs.hcil.manynets.hist.HistogramStats.HistogramMetric;
import edu.umd.cs.hcil.manynets.ui.ColumnManager.AbstractRendererOPanel;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

/**
 * Abstract renderer, makes developing stacked-row renderers easier
 *
 * @author manuel freire
 */
public abstract class AbstractRowRenderer extends JPanel
        implements ColumnCellRenderer, LabelRenderer.AxisLabeller,
        GraphicLabelRenderer.GraphicLabeller {

    private ArrayList<ColumnManager> otherColumns;

    public AbstractRowRenderer() {
        sortAttribute = Sorting.Column;
        metric = null;
    }

    void setOtherColumns(ArrayList<ColumnManager> otherColumns) {
        this.otherColumns = otherColumns;
        System.err.println("setting other columns " + otherColumns.get(0).getColumnName());
//        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * List of metrics (used to compare two histograms for similarity)
     */
    public enum Metric {

        Area("Area", "Normalized, Local comparison", new HistogramStats.AreaDistanceMetric()),
        KS("Kolmogorov-Smirnov", "Normalized, Local comparison", new HistogramStats.KSDistanceMetric()),
        Euclidean("Euclidean", "Global comparison", new HistogramStats.EuclideanDistanceMetric()),
        MDPA("Pair assignment", "Global comparison", new HistogramStats.MDPAMetric()),
        Value("Scalar abs. distance", "Scalar", new HistogramStats.AbsScalarDistanceMetric(), true),
        Kronecker("Scalar equality", "Scalar", new HistogramStats.KroneckerScalarDistanceMetric(), true);
        String name;
        DistanceMetric m;
        String description;
        boolean scalar;

        Metric(String name, String description, HistogramMetric m) {
            this(name, description, m, false);
        }

        Metric(String name, String description, DistanceMetric m, boolean scalar) {
            this.name = name;
            this.m = m;
            this.description = description;
            this.scalar = scalar;
        }

        public DistanceMetric getMetric() {
            return m;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * List of sorting methods
     */
    public enum Sorting {

        Column("external order", null),
        Min("minimum",
        new Scalarizer() {

            public double getScalar(DefaultHistogramModel d) {
                return d.getMin();
            }
        }),
        Max("maximum",
        new Scalarizer() {

            public double getScalar(DefaultHistogramModel d) {
                return d.getMax();
            }
        }),
        Avg("average",
        new Scalarizer() {

            public double getScalar(DefaultHistogramModel d) {
                return d.getAvg();
            }
        }),
        StdDev("standard deviation",
        new Scalarizer() {

            public double getScalar(DefaultHistogramModel d) {
                return d.getStandardDeviation();
            }
        }),
        Variance("variance",
        new Scalarizer() {

            public double getScalar(DefaultHistogramModel d) {
                return d.getStandardDeviation();
            }
        }),
        Kurtosis("kurtosis",
        new Scalarizer() {

            public double getScalar(DefaultHistogramModel d) {
                return d.getKurtosis();
            }
        }),
        Skewness("skewness",
        new Scalarizer() {

            public double getScalar(DefaultHistogramModel d) {
                return d.getSkewness();
            }
        }),
        Bimodality("bimodality",
        new Scalarizer() {

            public double getScalar(DefaultHistogramModel d) {
                return d.getBimodality();
            }
        }),
        Gap("gap", null),
        KS("Kolmogorov-Smirnov", null, true),
        Similarity("similarity to sel. row", null, true),
        Clusters("clusters", null, true),
        Multicluster("Multicolumn clusters", null, true);
        final String name;
        final Scalarizer scalarizer;
        final boolean simBased;

        Sorting(String name, Scalarizer scalarizer) {
            this(name, scalarizer, false);
        }

        Sorting(String name, Scalarizer scalarizer, boolean similarityBased) {
            this.name = name;
            this.scalarizer = scalarizer;
            this.simBased = similarityBased;
        }

        public double getScalar(DefaultHistogramModel d) {
            return scalarizer.getScalar(d);
        }
    }

    /**
     * Implemented by a sorting method, conflates a histogram into a single
     * number, which can then be sorted by. Not all sorting methods use one.
     */
    interface Scalarizer {

        double getScalar(DefaultHistogramModel d);
    }
    // min and max for the x-coord
    protected double max = Double.NEGATIVE_INFINITY;
    protected double min = Double.POSITIVE_INFINITY;
    protected ColumnManager cm;
    // indices[i] view position of model row i (i to y)
    protected int[] indices;
    // rindices[i] is the reverse mapping for indices (y to i)
    protected int[] rindices;
    protected Sorting sortAttribute;
    protected Metric metric;
    protected int height;
    protected int width;
    protected boolean dirty;
    protected boolean suspendSort;
    double sortableValues[];
    boolean hasValue = false;
    protected int similarityRow;
    // complex sorting is potentially slow; avoid doing it unnecessarily
    private boolean sortingRequired = true;
    protected AbstractRowRenderer sortingArr;
    Image oldImage;
    private int oldWidth, oldHeight;
    private String oldSortOrder;

    public boolean getSorted() {
        return hasValue;
    }

    @Override
    public void init(ColumnManager cm) {
        this.cm = cm;

        min = cm.getModel().getMin();
        max = cm.getModel().getMax();

        int totalRows = cm.getTable().getRowCount();
        sortableValues = new double[totalRows];
        indices = new int[totalRows];
        rindices = new int[totalRows];

        cm.getTable().addPropertyChangeListener(
                TablePanel.TABLE_SORTING_PROPERTY,
                new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (sortAttribute.equals(Sorting.Column)) {
                            dirty = true;
                        }
                    }
                });

        setBackground(Color.white);
    }

    /**
     * Generates graphic labels for this histogram
     */
    @Override
    public void setGraphicLabels(GraphicLabelRenderer glr) {
        glr.setColumn(cm);
    }

    /**
     * Generates axis labels for this histogram, as part of the AxisLabeller
     * interface
     */
    @Override
    public void setAxisLabels(LabelRenderer lr, boolean isVertical) {
        if (isVertical) {
            lr.clearAxisLabels();
            lr.addAxisLabel(0, "the " + cm.getTable().getRowCount() + " rows");
        } else {
            lr.generateHistogramLabels((DefaultHistogramModel) cm.getModel());
        }
    }

    /**
     * Clones the exact sorting used in another AbstractRowRenderer
     * From now on, changes to sorting order will be ignored
     * (once cloned, always cloned).
     * @param arr
     */
    public void cloneSorting(AbstractRowRenderer arr) {
        suspendSort = true;
        sortingArr = arr;
        dirty = true;
        if (cm != null) {
            System.err.println("sorting changed in col " + cm.getColumnName());
        }
        ((AbstractRendererOPanel) getSettingsPanel()).fireOptionsChanged();
    }

    protected abstract void paintImage(BufferedImage bi);

    protected Image generateImage(int width, int height) {
        String sortOrder = "" + cm.getTable().
                getSortOrder(cm.getColumnIndex());

        if ((width == oldWidth && height == oldHeight)
                && sortOrder.equalsIgnoreCase(oldSortOrder) && (!dirty)) {
            if (cm != null) {
                System.err.println("decided NOT to repaint col " + cm.getColumnName());
            }
            return oldImage;
        } else {
            if (cm != null) {
                System.err.println("REPAINTING col " + cm.getColumnName());
            }
        }

        if (suspendSort) {
            if (sortingArr.indices == null) {
                System.err.println("sorting because indices==null");
                sortingArr.sort();
            } else {
                System.err.println("reusing passed-in, pre-sorted indices");
            }
            indices = sortingArr.indices;
            rindices = sortingArr.rindices;
        } else {
            sort();
        }

        min = cm.getMin();
        max = cm.getMax();
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        paintImage(bi);

        dirty = false;
        oldWidth = width;
        oldHeight = height;
        oldSortOrder = sortOrder;
        return oldImage = bi;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        width = getWidth();
        height = getHeight();
        Image img = generateImage(width, height);
        g.drawImage(img, 0, 0, this);

        // now draw selection highlights, if any
        if (!cm.getTable().getSelectionModel().isSelectionEmpty()) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.GRAY);
            g2d.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, 0.5f));
            Rectangle2D rectangle = new Rectangle2D.Double();
            int totalRows = cm.getTable().getRowCount();
            double rowHeight = height * 1.0 / totalRows;
            int consecutiveForPixel = Math.max(1, (int) Math.round(1.0 / rowHeight));

            // for each pixel, render selections if any in range
            for (int i = 0; i < height; i++) {
                boolean selected = false;
                for (int j = 0; j < consecutiveForPixel; j++) {
                    int row = Math.min(totalRows - 1, (int) (i / rowHeight) + j);
                    selected |= cm.getTable().isRowSelected(imageRowToView(row));
                }

                if (!selected) {
                    rectangle.setRect(0, i, width, 1);
                    g2d.fill(rectangle);
                }
            }
        }
    }

    public int viewToImageRow(int viewRow) {
        return rindices[cm.getTable().convertRowIndexToModel(viewRow)];
    }

    public int imageRowToView(int imageRow) {
        return cm.getTable().convertRowIndexToView(indices[imageRow]);
    }

    public double[] getsortableValues() {
        return sortableValues;
    }

    public double viewRowSortingValue(int viewRow) {
        if (hasValue) {
//             System.err.println(getSortAttribute().getString() );
//            System.err.println(" sortable value= " + sortableValues[rindices[modelRow]]);
            return sortableValues[viewRow];
        }
        //if no value, return itself as the id
//        System.err.println(" id value " + rindices[modelRow]);
        return (double) viewRow;
    }

    public double modelRowSortingValue(int modelRow) {
        if (hasValue) {
//             System.err.println(getSortAttribute().getString() );
//            System.err.println(" sortable value= " + sortableValues[rindices[modelRow]]);
            return sortableValues[rindices[modelRow]];
        }
        //if no value, return itself as the id
//        System.err.println(" id value " + rindices[modelRow]);
        return (double) rindices[modelRow];
    }
    //
    // imageToModel : indices[imageRow]
    // modelToImage : rindices[modelRow]
    //

    int partition(double arr[], int left, int right) {
        int i = left, j = right;
        double tmp;

        double pivot = arr[(left + right) / 2];
        int tempIndex;

        while (i <= j) {
            while (arr[i] < pivot) {
                i++;
            }
            while (arr[j] > pivot) {
                j--;
            }
            if (i <= j) {
                tmp = arr[i];
                arr[i] = arr[j];
                arr[j] = tmp;

                tempIndex = indices[i];
                indices[i] = indices[j];
                indices[j] = tempIndex;

                i++;
                j--;
            }
        }
        return i;
    }

    void quickSort(double arr[], int left, int right) {

        int index = partition(arr, left, right);

        if (left < index - 1) {
            quickSort(arr, left, index - 1);
        }

        if (index < right) {
            quickSort(arr, index, right);
        }

    }

    @Override
    public void setBackground(Color c) {
        super.setBackground(c);
    }

    public Sorting getSortAttribute() {
        return sortAttribute;
    }

    public void setSortAttribute(Sorting sort) {
        if (sort != sortAttribute) {
            sortingRequired = true;
            sortAttribute = sort;
            dirty = true;
        }
    }

    public Metric getSortMetric() {
        return metric;
    }

    public void setSortMetric(Metric metric) {

        if (metric != this.metric) {
            sortingRequired = true;
            this.metric = metric;
            dirty = true;
        }
    }

    double xValue(double x) {
        return ((x - min) * (width) / (max - min));
    }

    int xValueInt(double x) {
        return (int) ((x - min) * (width) / (max - min));
    }

    /**
     * Sorts two arrays, indices and rindices, using the current sortAttribute
     * May or may not make use of an internal "sortableValues" column.
     */
    public void sort() {

        if (!sortingRequired) {
            return;
        }

        boolean needsValueSorting = true;

        int totalRows = cm.getTable().getRowCount();
        // column order
        for (int i = 0; i < totalRows; i++) {
            int vrow = cm.getTable().convertRowIndexToView(i);
            indices[vrow] = i;
        }
        boolean isDistribution = Distribution.class.isAssignableFrom(
                cm.getColumnClass());
        if (sortAttribute.scalarizer != null) {
            hasValue = true;
            if (isDistribution) {
//                System.err.println("Sorting by ===> " + sortAttribute);
                for (int i = 0; i < totalRows; i++) {
                    DefaultHistogramModel d = (DefaultHistogramModel) cm.getValue(i);
                    sortableValues[i] = d.isEmpty() ? Double.NEGATIVE_INFINITY
                            : sortAttribute.getScalar(d);
                    indices[i] = i;
//                    System.err.print(sortableValues[i] + " ");
                }
//                System.err.println("<== Sorted by " + sortAttribute);
            } else {
                if (cm.getColumnClass().equals(String.class)) {
                    DefaultHistogramModel m = (DefaultHistogramModel) cm.getModel();
                    for (int i = 0; i < totalRows; i++) {
                        sortableValues[i] = (int) m.getMappedValue(cm.getValue(indices[i]));
                        indices[i] = i;
                    }
                } else {
                    for (int i = 0; i < totalRows; i++) {
                        sortableValues[i] = Double.parseDouble("" + cm.getValue(i));
                        indices[i] = i;
                    }
                }
            }
        } else {
            hasValue = false;
            switch (sortAttribute) {
                case Column:
                    for (int i = 0; i < totalRows; i++) {
                        int vrow = cm.getTable().convertRowIndexToView(i);
                        indices[vrow] = i;
                    }
                    return;

                case Similarity:

                    if (metric == null) {
                        needsValueSorting = false;
                        break;
                    }
                    similarityRow = cm.getTable().getSelectedRow();
                    similarityRow = cm.getTable().convertRowIndexToModel(similarityRow);
                    if (similarityRow == -1) {
                        similarityRow = 0;
                    }
//                    System.err.println("row " + similarityRow + " is selected");
                    DefaultHistogramModel sel = (DefaultHistogramModel) cm.getValue(similarityRow);
                    for (int i = 0; i < totalRows; i++) {
                        DefaultHistogramModel d = (DefaultHistogramModel) cm.getValue(i);
                        // sortableValues[i] = d.getKSstat(sel);
//                        if(metric==null)
//                            metric=Metric.KS;
                        sortableValues[i] = d.isEmpty() ? Double.POSITIVE_INFINITY
                                : metric.getMetric().distance(d, sel);
                        indices[i] = i;
                    }
                    break;

                case KS:
                    ArrayList<DefaultHistogramModel> distributions = new ArrayList<DefaultHistogramModel>();
                    for (int i = 0; i < totalRows; i++) {
                        DefaultHistogramModel d = (DefaultHistogramModel) cm.getValue(i);
                        distributions.add(d);
                    }
                    indices = sortByNNTSP(distributions, (HistogramMetric) metric.getMetric());
                    needsValueSorting = false;
                    break;

                case Clusters:

                    if (metric == null) {
                        needsValueSorting = false;
                        break;
                    }
                    String strFile = cm.getColumnName() + "-" + cm.getTable().getRowCount() + "-cluster-" + metric + ".txt";
                    strFile = strFile.replaceAll("[ :?@+*()]", "_");
                    File f = new File(strFile);
//                    System.out.println(f + (f.exists()? " is  found " : " is missing "));
                    try {
                        if (f.exists()) {
//                            System.out.println(f.getName() + " exists ");
                            //read from that
                            BufferedReader br = new BufferedReader(new FileReader(f));
                            String strLine = "";
                            int lineNumber = 0;
                            while ((strLine = br.readLine()) != null) {
                                indices[lineNumber] = Integer.parseInt(strLine);
                                lineNumber++;
                            }
                        } else {
                            PrintWriter outputStream = new PrintWriter(new FileWriter(f));
                            ArrayList<DefaultHistogramModel> dists = new ArrayList<DefaultHistogramModel>();
                            for (int i = 0; i < totalRows; i++) {
                                DefaultHistogramModel d = (DefaultHistogramModel) cm.getValue(i);
                                dists.add(d);
                            }
                            DendrogramSorter ds = new DendrogramSorter();
                            indices = ds.sortByDendrogram(dists, (HistogramMetric) metric.getMetric());
                            for (int i = 0; i < indices.length; i++) {
                                outputStream.println(indices[i]);
                            }
                            outputStream.close();

                        }
                    } catch (IOException ex) {
                        Logger.getLogger(AbstractRowRenderer.class.getName()).log(Level.SEVERE, null, ex);
                        // calculate anyway
                        ArrayList<DefaultHistogramModel> dists = new ArrayList<DefaultHistogramModel>();
                        for (int i = 0; i < totalRows; i++) {
                            DefaultHistogramModel d = (DefaultHistogramModel) cm.getValue(i);
                            dists.add(d);
                        }
                        DendrogramSorter ds = new DendrogramSorter();
                        indices = ds.sortByDendrogram(dists, (HistogramMetric) metric.getMetric());
                    }
                    needsValueSorting = false;
                    break;

                case Multicluster:
                    String strFilem = "";
                    for (ColumnManager colm : otherColumns) {
                        strFilem += colm.getColumnName() + "-";
                    }
                    strFilem += cm.getTable().getRowCount() + "-multi-" + metric + ".txt";
                    strFilem = strFilem.replaceAll("[ :?@+*()]", "_");
                    File fm = new File(strFilem);
//                    System.out.println(f + (f.exists()? " is  found " : " is missing "));
                    try {
                        if (fm.exists()) {
//                            System.out.println(f.getName() + " exists ");
                            //read from that
                            BufferedReader br = new BufferedReader(new FileReader(fm));
                            String strLine = "";
                            int lineNumber = 0;
                            while ((strLine = br.readLine()) != null) {
                                indices[lineNumber] = Integer.parseInt(strLine);
                                lineNumber++;
                            }
                        } else {
                            PrintWriter outputStream = new PrintWriter(new FileWriter(fm));
//                            int totalColumns = otherColumns.size();
                            ArrayList<ArrayList> allDist =
                                    new ArrayList<ArrayList>();
                            ArrayList<DistanceMetric> allMetrics =
                                    new ArrayList<DistanceMetric>();
                            for (ColumnManager colm : otherColumns) {
                                ArrayList dists = new ArrayList();
                                if (Distribution.class.isAssignableFrom(
                                        colm.getColumnClass())) {
                                    for (int i = 0; i < totalRows; i++) {
                                        dists.add(colm.getValue(i));
                                    }
                                    allMetrics.add(metric.getMetric());
                                } else {
                                    // string or int
                                    if (colm.getColumnClass().equals(String.class)) {
                                        DefaultHistogramModel m = (DefaultHistogramModel) colm.getModel();

                                        for (int i = 0; i < totalRows; i++) {
                                            dists.add(m.getMappedValue(colm.getValue(indices[i])));
                                        }
                                        allMetrics.add(Metric.Kronecker.getMetric());
                                    } else {
                                        for (int i = 0; i < totalRows; i++) {
                                            dists.add(Double.parseDouble("" + colm.getValue(i)));
                                        }
                                        allMetrics.add(Metric.Value.getMetric());
                                    }
                                }
                                allDist.add(dists);
                            }
                            DendrogramSorter ds = new DendrogramSorter();
                            indices = ds.sortByDendrogramForMultiColumn(allDist, allMetrics);
                            for (int i = 0; i < indices.length; i++) {
                                outputStream.println(indices[i]);
                            }
                            outputStream.close();

                        }
                    } catch (IOException ex) {
                        Logger.getLogger(AbstractRowRenderer.class.getName()).log(Level.SEVERE, null, ex);
                        // calculate anyway
                        ArrayList<DefaultHistogramModel> dists = new ArrayList<DefaultHistogramModel>();
                        for (int i = 0; i < totalRows; i++) {
                            DefaultHistogramModel d = (DefaultHistogramModel) cm.getValue(i);
                            dists.add(d);
                        }
                        DendrogramSorter ds = new DendrogramSorter();
                        indices = ds.sortByDendrogram(dists, (HistogramMetric) metric.getMetric());
                    }
                    needsValueSorting = false;
                    break;

                default:
                    System.err.println("!=== Do not know how to sort " + sortAttribute);
                    Thread.dumpStack();
            } // end switch
        } // end else

        if (needsValueSorting) {
            quickSort(sortableValues, 0, sortableValues.length - 1);
        }

        sortingRequired = false;
        for (int i = 0; i < totalRows; i++) {
            rindices[indices[i]] = i;
        }
    }

    private class HistogramAndInteger {

        public DefaultHistogramModel h;
        public int i;

        public HistogramAndInteger(DefaultHistogramModel h, int i) {
            this.h = h;
            this.i = i;
        }
    }

    private int[] sortByNNTSP(ArrayList<DefaultHistogramModel> list, HistogramMetric m) {
        ArrayList<HistogramAndInteger> remaining = new ArrayList<HistogramAndInteger>();

        for (DefaultHistogramModel dhm : list) {
            remaining.add(new HistogramAndInteger(dhm, remaining.size()));
        }
        int[] idx = new int[list.size()];
        list.clear();
        HistogramAndInteger last = remaining.remove(0);
        idx[list.size()] = last.i;
        list.add(last.h);

        while (!remaining.isEmpty()) {
            double min = Double.MAX_VALUE;
            int i = 0;
            int tMin = 0;
            for (HistogramAndInteger hai : remaining) {
                double v = m.distance(hai.h, last.h);
                if (v < min) {
                    min = v;
                    tMin = i;
                }
                i++;
            }
            last = remaining.remove(tMin);
            idx[list.size()] = last.i;
            list.add(last.h);
        }
        return idx;
    }

    public String getYCaption() {
        String s = sortAttribute.toString();
        if (sortAttribute == Sorting.Column) {
            s = cm.getCurrentSorting();
        } else if (sortAttribute.simBased) {
            if (metric == null) {
                s = cm.getCurrentSorting();
            } else {
                s = metric.name + " " + sortAttribute.name;
                if (sortAttribute == Sorting.Similarity) {
                    s += " (" + similarityRow + ")";
                }
                s += "";
            }
        }

        return "rows, by " + s;
    }

    protected abstract String getToolTipText(int row, Point mousePosition);

    @Override
    public String getToolTipText(Point mousePosition) {
        int y = getRowForPoint(mousePosition, height);

        if (y > indices.length - 1 || y < 0) {
            System.err.println("Bad index: " + y);
            return null;
        }
        int row = indices[y];

        return getToolTipText(row, mousePosition);

    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        return this;
    }

    @Override
    public int getMaximumVerticalSize() {
        return ColumnManager.largeVerticalSize;
    }

    @Override
    public boolean updateHighlights() {
        return true;
    }

    private int getRowForPoint(Point p, Rectangle bounds) {
        return getRowForPoint(p, bounds.height);


    }

    public int getRowForPoint(Point p, int height) {
        float y = (float) p.getY();
        int row = Math.round(y * cm.getTable().getRowCount() / height);
        // System.err.println(" Searching for " + y + " = " + row);

        return Math.max(Math.min(row, indices.length - 1), 0);

    }

    /**
     * @param start
     * @param end
     * @param isAdjusting
     */
    @Override
    public void selectionDrag(JTable jt, Point start, Point end,
            Rectangle bounds, boolean isAdjusting) {

        int y0 = getRowForPoint(start, bounds);

        int y1 = getRowForPoint(end, bounds);

        int lo = Math.min(y0, y1);

        int hi = Math.max(y0, y1);

        ListSelectionModel sm = cm.getTable().getSelectionModel();
        sm.setValueIsAdjusting(true);
        sm.clearSelection();

        for (int i = lo; i
                <= hi; i++) {
            int tableRow = imageRowToView(i);
            sm.addSelectionInterval(tableRow, tableRow);
        }

        sm.setValueIsAdjusting(false);

    }
}
