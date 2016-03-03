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

import edu.umd.cs.hcil.manynets.hist.BarRenderer;
import edu.umd.cs.hcil.manynets.hist.Histogram;
import edu.umd.cs.hcil.manynets.hist.SelectedBarRenderer;
import edu.umd.cs.hcil.manynets.model.Ref;
import edu.umd.cs.hcil.manynets.model.Stat;
import edu.umd.cs.hcil.manynets.model.TableWrapper;
import edu.umd.cs.hcil.manynets.model.TableWrapper.Level;
import edu.umd.cs.hcil.manynets.ui.renderers.ColumnCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jdesktop.swingx.table.TableColumnModelExt;


/**
 * A sortable table panel displaying, for each graph, a row of metrics.
 *
 * @author Manuel Freire
 */
public class TablePanel extends JPanel {

    public static final float highlightHue = .3f; // .6 blue, .3 green

    protected JXTable jxt;
    protected JXTable dt;
    protected JPanel topPanel;
    protected TableWrapper tablew;

    public static final String TABLE_SORTING_PROPERTY = "table-sorting";
    public static final String CELL_CLICKED_PROPERTY = "cell-clicked";
    public static final String FINISHED_LOADING_PROPERTY = "finished-loading";

    public TablePanel() {
        setLayout(new BorderLayout());
    }

    /**
     * On double click of mouse shows details of the clicked row
     */
    public void showDetails(JXTable t, int row, int col){
        System.out.println("DoubleClick at row=" + row + ", col=" + col);
    }

    /**
     * On on right-click, returns (possibly null) JOptionMenu to pop up
     * on that location. Returning null means that no OptionMenu will be shown.
     */
    public JPopupMenu createPopupMenu(JXTable t, int row, int col) {
        System.out.println("RightClick at row=" + row + ", col=" + col);
        return null;
    }

    /**
     * Initializes the table
     */
    public void init(TableWrapper tw){
        tablew = tw;
        System.err.println("reinitializing TP with "
                + tw.getTable().getRowCount() + "x" + tw.getTable().getColumnCount());

        // remove previous components of panel
        removeAll();

        // instantiate tables (using all attribute cols), & associate via CCF
        dt = new JXTable();
        // FIXME - may want to update PGTM to check getTable at each moment
        jxt = new JXTable(new PrefuseGraphTableModel(tablew));
        CopycatColumnFactory ccf = new CopycatColumnFactory(jxt, dt);
        jxt.setColumnFactory(ccf);

        // setup colors. All histograms share the same renderer
        float higlightHue = .3f; // .6 blue, .3 green
        dt.setSelectionBackground(Color.getHSBColor(higlightHue, .1f, .9f));
        jxt.setSelectionBackground(Color.getHSBColor(higlightHue, .1f, .9f));
        Histogram hist = new Histogram(null, BarRenderer.class, SelectedBarRenderer.class);
        ((BarRenderer)hist.getRenderer()).setColor(Color.blue.darker());
        ((SelectedBarRenderer)hist.getSelectionRenderer()).setColor(
                Color.getHSBColor(TablePanel.highlightHue, 1f, 1f));
        hist = null;

        // configure main table, add to center pane
        MouseAdapter mouseHandler = new MouseHandler();
        jxt.setColumnControlVisible(true);
        jxt.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jxt.setSortable(true);
        jxt.setAutoResizeMode(JXTable.AUTO_RESIZE_ALL_COLUMNS);
        add(new JScrollPane(jxt), BorderLayout.CENTER);
        jxt.setBackground(Color.white);

        // configure distribution table, add to top pane
        dt.setRowHeight(40);
        dt.setColumnFactory(ccf);
        dt.setAutoResizeMode(JXTable.AUTO_RESIZE_ALL_COLUMNS);
        dt.setColumnControlVisible(false);
        topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        // since not displayed within scrollpane, add spacing via border
        JPanel intermediate = new JPanel(new BorderLayout());
        intermediate.setBorder(BorderFactory.createMatteBorder(
                2, 1,0, 0, jxt.getGridColor()));
        intermediate.add(dt, BorderLayout.CENTER);
        topPanel.add(intermediate, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);

        // configure contents of main table
        Object headers[] = new String[jxt.getColumnCount()];
        Object data[][] = new Object[1][jxt.getColumnCount()];
        for (int i=0; i<jxt.getColumnCount(); i++) {
            TableColumnExt oc = jxt.getColumnExt(i);
            Stat s = null;
            try {
                s = tablew.getStat(i);
            } catch (IndexOutOfBoundsException ioobe) {
                System.err.println("error loading col " + jxt.getColumnName(i)
                        + " at index " + i + ": " + ioobe);
                break;
            }
            oc.addPropertyChangeListener(ccf.getWidthDuplicator());
            oc.setToolTipText(s == null ? oc.getTitle() : s.getDescription());
            data[0][i] = headers[i] = oc.getTitle();
        }

        // configure contents of distribution table
        EmptyHeaderRenderer ehr = new EmptyHeaderRenderer();
        dt.setModel(new DefaultTableModel(data, headers));
        for (int i=0; i<jxt.getColumnCount(); i++) {
            TableColumnExt dc = dt.getColumnExt(i);
            dc.addPropertyChangeListener(ccf.getWidthDuplicator());
            dc.setHeaderRenderer(ehr);
            try {
                // associate all remaining renderers
                new ColumnManager(this, i, tablew.getStat(i));
            } catch (IndexOutOfBoundsException ioobe) {
                System.err.println("error loading col '" + jxt.getColumnName(i) + "' from '"
                       + tw.getColumnName(i) + "' at index " + i + ": " + ioobe);
                break;
            }
        }

        // pack and launch column copycat-behaviour
        jxt.packAll();
        dt.getColumnModel().addColumnModelListener(ccf.getModelDuplicator());
        jxt.getColumnModel().addColumnModelListener(ccf.getModelDuplicator());
        
        // listen to selection events to repaint distribution cells accordingly
        jxt.getSelectionModel().setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jxt.getSelectionModel().addListSelectionListener(
                new RowSelectionListener());

        // add mouse-listeners
        jxt.addMouseListener(mouseHandler);
        jxt.addMouseMotionListener(mouseHandler);
        dt.addMouseListener(mouseHandler);
        dt.addMouseMotionListener(mouseHandler);

        System.err.println("init called");
        firePropertyChange(FINISHED_LOADING_PROPERTY, null, true);
    }

    public ColumnManager getColumnManager(int modelIndex) {
        return (ColumnManager)
                ((TableColumnModelExt)jxt.getColumnModel())
                    .getColumnExt(modelIndex).getCellRenderer();
    }

    public JXTable getTable() {
        return jxt;
    }

    public JXTable getHeaderTable() {
        return dt;
    }

    public TableWrapper getBaseWrapper() {
        return tablew;
    }

    public Object getId(int viewRow) {
        return tablew.getId(jxt.convertRowIndexToModel(viewRow));
    }

    /**
     * Returns the view row-numbers corresponding to a list of labels (references)
     * @param labels
     * @return
     */
    public Collection<Integer> labelsToViewRows(Collection labels, int modelColumn) {

        Set<Integer> set = new TreeSet<Integer>();

        if (labels.isEmpty()) return set;

        for (Ref r : (Collection<Ref>)labels) {
            set.add(jxt.convertRowIndexToView(
                    labelToModelRow(r, modelColumn)));
        }

        return set;
    }

    /**
     * Finds the model row-number for a given label (a ref)
     * @return model row, or -1 if not found
     */
    public int labelToModelRow(Object label, int modelColumn) {
        Ref r = (Ref)label;
        Level wl = r.getTableWrapper().getLevel();

        TableWrapper tw = tablew;

        if (tw.getLevel().equals(Level.Node)) {
            Stat stat = tw.getStat(modelColumn);
            if (stat != null) {
                while ( ! wl.equals(tw.getLevel())) {
                    r = stat.getParentRef(r);
                    if (r == null) return -1;
                    wl = r.getTableWrapper().getLevel();
                }
            }
            return r.getRow();
        }
        
        while ( ! wl.equals(tw.getLevel())) {
            r = r.getParentRef();
            if (r == null) return -1;
            wl = r.getTableWrapper().getLevel();
        }
        return r.getRow();
    }

    /**
     * Returns a description for these references
     * @param references
     * @return
     */
    public String referencesToString(
            Collection<Object> references, int colIndex) {
        StringBuilder sb = new StringBuilder();
        for (Object o : references) {
            sb.append(o + "\n");
        }
        return sb.toString();
    }

//------------ Row selection listener, notifies col. manager of updates -----------

    private class RowSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if ( ! e.getValueIsAdjusting()) {
                int[] selected = jxt.getSelectedRows();
                for (int i=0; i<selected.length; i++) {
                    int r = jxt.convertRowIndexToModel(selected[i]);
                    selected[i] = r;
                }

                for (int i=0; i<jxt.getColumnCount(false); i++) {
                    getColumnManager(i).updateHighlights(selected);
                }
                jxt.repaint();
                dt.repaint();
//                System.err.println("updated highlights for " + selected.length + " rows");
            } else {
//                System.err.println("ignored adjusting value");
            }
        }
    }

//------------ Panel-wide mouse handler -----------

    /**
     * A mousehandler that provides
     *  - Hover: tooltips
     *  - Drag within complex viz: marquee selection
     *  - Double-click: detailed view
     */
    public class MouseHandler extends MouseAdapter {

        private int[] last = new int[] {-1, -1};
        private TableCellRenderer cr = null;

        public MouseHandler() {
        }

        @Override
        public void mouseMoved(MouseEvent me) {
            if (dragging) return;

            Point p = me.getPoint();
            JXTable t = (JXTable)me.getSource();
            int row = t.rowAtPoint(p);
            int col = t.columnAtPoint(p);
            if (row < 0 || col < 0) {
                t.setToolTipText(null);
                return;
            }

            Object v = t.getValueAt(row, col);
            cr = t.getCellRenderer(row, col);
            cr.getTableCellRendererComponent(t, v, true, true, row, col);

            Rectangle r = t.getCellRect(row, col, true);
            p = new Point(p.x - r.x, p.y - r.y);
            String text = ((ColumnManager)cr)
                    .getRealRenderer((JTable)me.getSource())
                    .getToolTipText(p);
            t.setToolTipText(text);
        }

        @Override
        public void mouseClicked(MouseEvent me) {
            final Point p = me.getPoint();
            final JXTable t = (JXTable)me.getSource();
            final int row = t.rowAtPoint(p);
            final int col = t.columnAtPoint(p);
            
            // double-click on a graph: if first col, try to open it
            if (me.getSource() == jxt && me.getClickCount() == 2) {
                if (me.getSource() == jxt) {
                    String label = "" + getId(row);
                    showDetails(t, row, col);
                }
            }

            // right-click: open options
            if (me.getClickCount() == 1 &&
                    me.getButton() == MouseEvent.BUTTON3) {
                JPopupMenu jpm = createPopupMenu(t, row, col);
                if (jpm != null) {
                    jpm.show(t, (int)p.getX(), (int)p.getY());
                }
            }

            // normal click: show details (in any suitable listener)
            if (me.getClickCount() == 1 &&
                    me.getButton() == MouseEvent.BUTTON1) {
                SelectedRowPanel.CellClickedInfo cci =
                        new SelectedRowPanel.CellClickedInfo(TablePanel.this, row, col);
                if (row != -1 && col != -1) {
                    firePropertyChange(CELL_CLICKED_PROPERTY, null, cci);
                }
            }
        }

        private Frame getParentFrame() {
            Component p = getParent();
            while ( ! (p instanceof Frame)) {
                p = p.getParent();
            }
            return (Frame)p;
        }

        private JXTable dragTable = null;
        private Point startPoint = null;
        private Rectangle dragRect = null;
        private boolean dragging = false;
        private int dragRow;
        private int dragCol;
        @Override
        public void mouseDragged(MouseEvent e) {
            Point p = e.getPoint();
            JXTable t = (JXTable)e.getSource();
            int row = t.rowAtPoint(p);
            int col = t.columnAtPoint(p);

            if (row < 0 || col < 0) return;

            if ( ! dragging) {
                System.err.println("Drag started at " + row + ", " + col);
                dragging = true;
                dragTable = t;
                dragRect = t.getCellRect(row, col, true);
                startPoint = new Point(p);
                startPoint.translate(-dragRect.x, -dragRect.y);
                dragRow = row;
                dragCol = col;
            } else {
                t = dragTable;
                Point endPoint = new Point(p);
                endPoint.translate(-dragRect.x, -dragRect.y);
                endPoint.x = Math.max(0, endPoint.x);
                endPoint.y = Math.max(0, endPoint.y);
                System.err.println("\t start " + startPoint + " end " + endPoint);
                int mi = t.getColumn(dragCol).getModelIndex();
                ColumnCellRenderer ccr = getColumnManager(mi).getRealRenderer(t);
                Object v = t.getValueAt(dragRow, dragCol);
                ccr.getTableCellRendererComponent(t, v, true, false, dragRow, dragCol);
                ccr.selectionDrag(jxt, startPoint, endPoint,
                        new Rectangle(0, 0, dragRect.width, dragRect.height),
                        true);
            }
        }
        @Override
        public void mouseReleased(MouseEvent e) {
            if (dragging) {
                Point p = e.getPoint();
                JXTable t = (JXTable)e.getSource();
                int row = t.rowAtPoint(p);
                int col = t.columnAtPoint(p);

                dragging = false;
                System.err.println("Drag ended at " + row + ", " + col);
            }
        }
    }

//------------ Displays an empty header -----------

    /**
     * Displays an empty header
     */
    private class EmptyHeaderRenderer extends JPanel
            implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            return this;
        }
    }
}
