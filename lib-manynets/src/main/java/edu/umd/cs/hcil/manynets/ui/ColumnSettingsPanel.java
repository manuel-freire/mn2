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

import edu.umd.cs.hcil.manynets.model.Stat;
import edu.umd.cs.hcil.manynets.hist.Histogram;
import edu.umd.cs.hcil.manynets.ui.ColumnManager.AbstractRendererOPanel;
import edu.umd.cs.hcil.manynets.ui.renderers.AbstractRowRenderer;
import edu.umd.cs.hcil.manynets.ui.renderers.AbstractRowRenderer.Metric;
import edu.umd.cs.hcil.manynets.ui.renderers.ColumnCellRenderer;
import edu.umd.cs.hcil.manynets.ui.renderers.AbstractRowRenderer.Sorting;
import edu.umd.cs.hcil.manynets.ui.renderers.BoxplotRenderer;
import edu.umd.cs.hcil.manynets.ui.renderers.GridOverviewChooser;
import edu.umd.cs.hcil.manynets.ui.renderers.GridOverviewChooser.AxisChoice;
import edu.umd.cs.hcil.manynets.ui.renderers.GridOverviewChooser.ClassChoice;
import edu.umd.cs.hcil.manynets.ui.renderers.HistogramOverviewRenderer;
import edu.umd.cs.hcil.manynets.ui.renderers.LabelOverviewRenderer;
import edu.umd.cs.hcil.manynets.ui.renderers.HeatmapOverviewRenderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Manuel Freire
 */
public class ColumnSettingsPanel extends javax.swing.JPanel
        implements PropertyChangeListener {

    private static int screenshotCounter = 1;
    private static final String detachProperty = "DETACH_PROPERTY";

    private ColumnManager cm;
    private ColumnCellRenderer ccr;
    private JPanel rsp;
    private PreviewPanel pp = new PreviewPanel();
    private JTable table;
    private Object value;
    private int row;
    private int col;
    private boolean initNotFinished = false;
    private ArrayList<ColumnCellRenderer> availableRenderers;
    private boolean overview;
    private boolean detached = false;

    /** Creates new form ColumnSettingsPanel */
    public ColumnSettingsPanel() {
        initComponents();
        jpPreview.removeAll();
        jpPreview.add(pp, BorderLayout.CENTER);

        addDetachListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                JDialog jd = new JDialog();
                jd.addWindowStateListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        System.err.println("unregistering detached overview");
                        cm.removeDetachedRenderer(ccr);
                    }
                });
                jd.setModal(false);
                jd.setTitle(cm.getTablePanel().getBaseWrapper().getName()+": overview of: "
                        + cm.getColumnName().replaceAll(".*:", ""));
                jd.add((JComponent)evt.getNewValue());
                jd.setSize(getWidth(), getHeight());
                jd.setVisible(true);
            }
        });

        jpType.setVisible(false);
        validate();
    }

    public void init(ColumnManager cm, JTable t, int row, int col, ColumnCellRenderer ccr) {
        init(cm, t, row, col, ccr, ccr == cm.getOverviewRenderer());
    }

    /**
     * Initialize this panel for display.
     */
    public void init(ColumnManager cm, JTable t, int row, int col, ColumnCellRenderer ccr, boolean overview) {
        this.cm = cm;
        this.ccr = ccr;
        table = t;

        this.overview = overview;

        availableRenderers = cm.getAvailableRenderers(overview);

        this.value = overview ? cm.getModel() : t.getValueAt(row, col);
        if (value == null) {
            try {
                value = t.getColumnClass(col).newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "Error trying to instantiate value for col " + col, e);
            }
        }

        this.row = row;
        this.col = col;
        pp.init(table, value, row, col, ccr);

        rsp = ccr.getSettingsPanel();
        rsp.addPropertyChangeListener(
                AbstractRendererOPanel.renderOptionsProperty, this);

        // deactivates listener while list gets recomputed
        initNotFinished = true;

        // small border to visually separate options, when shown
        jpOptions.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(2, 2, 2, 2),
                BorderFactory.createLineBorder(t.getGridColor())));

        jpOptions.removeAll();
        jpOptions.add(rsp);
        System.err.println("Settings for type " + value.getClass().getSimpleName());

        jcbType.removeAllItems();
        for (ColumnCellRenderer r : availableRenderers) {
            jcbType.addItem(r.getRendererName());
        }
        jcbType.setSelectedItem(ccr.getRendererName());

        // hide controls unless requested by pressing button in PreviewPanel
        jpType.setVisible(false);
        jpOptions.setVisible(false);
        validate();

        initNotFinished = false;
    }

    /**
     * Detach callback
     */
    public void addDetachListener(PropertyChangeListener pl) {
        addPropertyChangeListener(detachProperty, pl);
    }

    public void removeDetachListener(PropertyChangeListener pl) {
        removePropertyChangeListener(detachProperty, pl);
    }

    /**
     * Detaches this panel: triggers detachProperty event launching with
     * newValue an exact copy of this renderer, but one that ignores values
     * from the actual TablePanel
     */
    public void detach() {
        ColumnSettingsPanel d = new ColumnSettingsPanel();
        d.detached = true;
        d.init(cm, table, row, col, ccr.copy(), overview);
        cm.addDetachedRenderer(ccr);
        firePropertyChange(detachProperty, null, d);
    }

    /**
     * Shows or hides the row settings panel
     */
    public void toggleRSPanel() {
        jpType.setVisible( ! jpType.isVisible());
        jpOptions.setVisible( ! jpOptions.isVisible());
    }

    /**
     * Visual choosing
     */
    public void toggleGridPanel() {
        JDialog jd = new JDialog();
        jd.setLayout(new BorderLayout());
        jd.setTitle("Choose overview for column " + cm.getColumnName());
        ArrayList<AxisChoice> xc = new ArrayList<AxisChoice>();

        xc.add(new ClassChoice(HistogramOverviewRenderer.class,
                new ClassChoice.Prop("setHeightFunction",
                new Histogram.LinearHeightFunction(),
                Histogram.HeightFunction.class)));
        xc.add(new ClassChoice(HistogramOverviewRenderer.class,
                new ClassChoice.Prop("setHeightFunction",
                new Histogram.SqrtHeightFunction(),
                Histogram.HeightFunction.class)));
        xc.add(new ClassChoice(HistogramOverviewRenderer.class,
                new ClassChoice.Prop("setHeightFunction",
                new Histogram.LogHeightFunction(),
                Histogram.HeightFunction.class)));

        xc.add(new ClassChoice(BoxplotRenderer.class,
                new ClassChoice.Prop("setSortAttribute", Sorting.Column)));
        xc.add(new ClassChoice(BoxplotRenderer.class,
                new ClassChoice.Prop("setSortAttribute", Sorting.Avg)));
        xc.add(new ClassChoice(HeatmapOverviewRenderer.class,
                new ClassChoice.Prop("setMetric", Metric.KS),
                new ClassChoice.Prop("setGlobal", true, Boolean.TYPE),
                new ClassChoice.Prop("setSortAttribute", Sorting.Clusters)
                ));
        xc.add(new ClassChoice(HeatmapOverviewRenderer.class,
                new ClassChoice.Prop("setSortAttribute", Sorting.Column)));
        xc.add(new ClassChoice(HeatmapOverviewRenderer.class,
                new ClassChoice.Prop("setSortAttribute", Sorting.Avg)));
        xc.add(new ClassChoice(HeatmapOverviewRenderer.class,
                new ClassChoice.Prop("setMetric", Metric.Euclidean),
                new ClassChoice.Prop("setGlobal", false, Boolean.TYPE),
                new ClassChoice.Prop("setSortAttribute", Sorting.Clusters)
                ));

        jd.add(new GridOverviewChooser(cm,
                xc, 3, 3, row, col, value));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(new JButton("Cancel"));
        jd.add(buttonPanel, BorderLayout.SOUTH);
        jd.setSize(600, 600);
        jd.setVisible(true);
    }

    /**
     * Adds the current sorting used in the visible AbstractRowRenderer as a 
     * column to the currently-visible table.
     */
    private void addSortColumn() {
        // sort
        AbstractRowRenderer arr = (AbstractRowRenderer)
                LabelOverviewRenderer.getInnerRenderer(ccr);
        TablePanel tp = cm.getTablePanel();

        Stat stat = new Stat(cm.getColumnName() + " " + arr.getYCaption(),
                "Sorting used in " + cm.getColumnName() + " " + arr.getYCaption(), Double.TYPE,
                tp.getBaseWrapper().getLevel(), tp.getBaseWrapper().getLevel());
        tp.getBaseWrapper().addStat(stat);
        int statCol = tp.getBaseWrapper().getStatCol(stat);
        
        for( int iRow = 0 ; iRow < cm.getTable().getRowCount(); iRow++){
            double v = arr.modelRowSortingValue(iRow);
            tp.getBaseWrapper().getTable().set(iRow, statCol, v);
        }

        tp.init(tp.getBaseWrapper());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        System.err.println("Property changed! " + evt);
        if (evt.getPropertyName().equals(
                AbstractRendererOPanel.renderOptionsProperty)) {
            pp.innerTable.repaint();
            table.repaint(); // fixme: there are other ways..
        }
    }

    /**
     * A preview panel, using the exact same renderer used for the tablecells.
     * A special 'selections only' preview panel can also be created
     * (see constructor)
     */
    public class PreviewPanel extends JPanel implements
            MouseMotionListener, MouseListener, ListSelectionListener {

        private JTable innerTable = new JTable(new AbstractTableModel() {

            public int getRowCount() {
                return 1;
            }

            public int getColumnCount() {
                return 1;
            }

            public Object getValueAt(int rowIndex, int columnIndex) {
                return value;
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        });
        private ColumnCellRenderer ccr;
        private JTable table;
        private Object value;
        private int row, col;
        private JLabel label = new JLabel("test");
        private JToggleButton jbDetach;
        private JToggleButton jbSorter;
        private JToggleButton jbGrid;
        private JToggleButton jbSettings;
        private JPanel jpSouthButtons = new JPanel(new GridBagLayout());
        private JPanel jpSouth = new JPanel(new BorderLayout());

        public PreviewPanel() {
            setLayout(new BorderLayout());
            jpSouth.add(label, BorderLayout.CENTER);

            innerTable.setBorder(BorderFactory.createEtchedBorder());
            innerTable.setBackground(label.getBackground());
            innerTable.addMouseMotionListener(this);
            innerTable.addMouseListener(this);
            innerTable.getSelectionModel().addListSelectionListener(this);
            add(jpSouth, BorderLayout.SOUTH);
            add(innerTable, BorderLayout.CENTER);
            
            // FIXME: hacky way of saving screenshots of overviews
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    System.err.println("Generating screenshot...");
                    
                    int h = innerTable.getHeight();
                    BufferedImage bi = new BufferedImage(getWidth(), h, BufferedImage.TYPE_INT_ARGB);
                    ((JComponent)ccr).setBounds(0, 0, bi.getWidth(), bi.getHeight());
                    ((JComponent)ccr).doLayout();
                    ((JComponent)ccr).paint(bi.getGraphics());

                    String fName = ("" + (screenshotCounter++) + "_" +
                            ccr.getRendererName() + "_" +
                            ccr.getXCaption() + "_" +
                            ccr.getYCaption() + ".png").replaceAll("[ ,@:-]", "_");
                    File f = new File(fName);
                    try {
                        ImageIO.write(bi, "png", f);
                        System.err.println("Screenshot saved to " + f.getName());
                    } catch (Exception ex) {
                        System.err.println("Error saving screenshot to " + f.getName());
                    }
                }
            });

            ClassLoader cl = getClass().getClassLoader();
            String iconBase = "edu/umd/cs/hcil/manynets/icons/";

            /* buttons and toggle-buttons may be represented differently; to
             * avoid uglyness, we use only toggle-buttons, but make some of them
             * behave identically to normal-buttons
             */
            jbDetach = new JToggleButton("<html><small>Detach</small><html>");
            jbDetach.setHorizontalAlignment(JButton.LEFT);
            jbDetach.setIcon(new ImageIcon(cl.getResource(iconBase
                    + "overview-detach24.png")));
            jbDetach.setToolTipText("Detach this overview from the table");
            jbDetach.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    detach();
                    jbDetach.setSelected(false);
                }
            });
            jbSettings = new JToggleButton("<html><small>Settings</small><html>");
            jbSettings.setIcon(new ImageIcon(cl.getResource(iconBase
                    + "overview-settings-dialog24.png")));
            jbSettings.setToolTipText("Configure this overview");
            jbSettings.setHorizontalAlignment(JButton.LEFT);
            jbSettings.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    toggleRSPanel();
                }
            });
            jbGrid = new JToggleButton("<html><small>Chooser</small><html>");
            jbGrid.setIcon(new ImageIcon(cl.getResource(iconBase
                    + "overview-settings-grid24.png")));
            jbGrid.setToolTipText("Choose an overview from a graphical menu");
            jbGrid.setHorizontalAlignment(JButton.LEFT);
            jbGrid.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    toggleGridPanel();
                }
            });
            jbGrid.setVisible(false);
            jbSorter = new JToggleButton("<html><small>Add sort col.</small><html>");
            jbSorter.setIcon(new ImageIcon(cl.getResource(iconBase
                    + "add-column-sort-overview24.png")));
            jbSorter.setToolTipText("Add a column with this overview's sorting to the table");
            jbSorter.setHorizontalAlignment(JButton.LEFT);
            jbSorter.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    addSortColumn();
                    jbSorter.setSelected(false);
                }
            });

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.weightx = c.weighty = 1;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.gridwidth = c.gridheight = 1;
            c.gridx = c.gridy = 0;
            jpSouthButtons.add(jbSettings, c);
            c.gridx = 1;
            jpSouthButtons.add(jbDetach, c);
            c.gridx = 0;
            c.gridy = 1;
            jpSouthButtons.add(jbSorter, c);
            c.gridx = 1;
            jpSouthButtons.add(jbGrid, c);
            jpSouth.add(jpSouthButtons, BorderLayout.EAST);

            setVisible(false);
        }

        public void init(JTable table, Object value, int row, int col,
                ColumnCellRenderer ccr) {
            this.table = table;
            this.value = value;
            this.row = row;
            this.col = col;
            this.ccr = ccr;


            int buttonHeight = jbSettings.getPreferredSize().height;
            int maxHeight = ccr.getMaximumVerticalSize() + buttonHeight;
            super.setPreferredSize(new Dimension(
                    getParent() == null ? 100 : getParent().getWidth(),
                    Math.min(maxHeight, 150)));
            innerTable.setValueAt(value, 0, 0);
            innerTable.getColumnModel().getColumn(0).setCellRenderer(ccr);

            if (table != cm.getTable()) {
                if (overview) {
                    label.setText(table.getColumnName(col) + ": all rows");
                } else {
                    label.setText(table.getColumnName(col) + ": selected rows");
                }
            } else {
                label.setText(table.getColumnName(col) + ": row "
                        + table.getValueAt(row, 0));
            }
            label.setText("<html><table><tr><td>" + label.getText() + "</td></tr></html>");

            ColumnCellRenderer inner = LabelOverviewRenderer.getInnerRenderer(ccr);
            jbSorter.setVisible(inner instanceof AbstractRowRenderer);
            if (inner instanceof AbstractRowRenderer) {
                System.err.println("Inner sorting was " + ((AbstractRowRenderer)inner).getSortAttribute());
            }
            jbGrid.getModel().setPressed(false);
            jbSettings.getModel().setPressed(jpOptions.isVisible());

            setVisible(true);
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        public void setBounds(int x, int y, int w, int h) {
            super.setBounds(x, y, w, h);
            if (ccr == null || w == 0 || h == 0) {
                return;
            }

            int maxHeight = ccr.getMaximumVerticalSize() == -1
                    ? Integer.MAX_VALUE / 2 : ccr.getMaximumVerticalSize();
            int available = h - (int)jpSouth.getPreferredSize().getHeight();
            int rh = Math.min(Math.max(1, available - 1),
                    maxHeight);
//            System.err.println("Setting inner table bounds from " +
//                    x + ", " + y + ", " + w + ", " + h + " --> " +
//                    (x+1) + ", " + (y+1) + ", " + (w-2) + ", " + rh +
//                    " : " + rh + " bh= " + (int)jpSouth.getHeight());

            innerTable.setBounds(x + 3, y + 3, w - 6, rh);
            innerTable.setRowHeight(rh);
        }
        private Point startPoint = null;
        private Rectangle dragRect = null;
        private boolean dragging = false;

        @Override
        public void mouseDragged(MouseEvent e) {
            Point p = e.getPoint();

            if (!dragging) {
                System.err.println("Drag started (detail view)");
                dragging = true;
                dragRect = innerTable.getCellRect(0, 0, true);
                startPoint = new Point(p);
                startPoint.translate(-dragRect.x, -dragRect.y);
            } else {
                Point endPoint = new Point(p);
                endPoint.translate(-dragRect.x, -dragRect.y);
                endPoint.x = Math.max(0, endPoint.x);
                endPoint.y = Math.max(0, endPoint.y);
                System.err.println("\t start " + startPoint + " end " + endPoint);
                JTable dt = cm.getTable();
                Object v = innerTable.getValueAt(0, 0);
                ccr.getTableCellRendererComponent(innerTable, v, true, false, 0, 0).setBounds(dragRect);
                ccr.selectionDrag(dt, startPoint, endPoint,
                        new Rectangle(0, 0, dragRect.width, dragRect.height),
                        true);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (dragging) {
                dragging = false;
                System.err.println("Drag ended (detail view): " +
                        "\n\tstart: " + startPoint +
                        "\n\trect:   " + dragRect);
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (ccr == null) {
                return;
            }
            ccr.getTableCellRendererComponent(
                    table, value, false, false, row, col);
            innerTable.setToolTipText(ccr.getToolTipText(e.getPoint()));
        }

        /**
         * Prevent any selections in the innerTable
         * @param e
         */
        @Override
        public void valueChanged(ListSelectionEvent e) {
            innerTable.clearSelection();
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jpPreview = new javax.swing.JPanel();
        jpOptions = new javax.swing.JPanel();
        jpType = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jcbType = new javax.swing.JComboBox();
        jpStretcher = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        jpPreview.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jpPreview, gridBagConstraints);

        jpOptions.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jpOptions, gridBagConstraints);

        jpType.setLayout(new java.awt.GridBagLayout());

        jLabel2.setText("Type");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        jpType.add(jLabel2, gridBagConstraints);

        jcbType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Histogram", "Box plot", "Min", "Max", "Average", "Median", "" }));
        jcbType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbTypeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 6);
        jpType.add(jcbType, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jpType, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weightx = 1.0E-4;
        add(jpStretcher, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jcbTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcbTypeActionPerformed

        ColumnCellRenderer next = jcbType.getSelectedIndex() == -1 ? null
                : availableRenderers.get(jcbType.getSelectedIndex());

        if (next == null
                || next.getClass().equals(ccr.getClass())
                || initNotFinished) {
            return;
        }

        ColumnCellRenderer nextRenderer = null;
        try {
            nextRenderer = next.getClass().newInstance();
        } catch (Exception e) {
            System.err.println("Exception instancing " + next.getClass());
            e.printStackTrace();
            return;
        }

        jpOptions.removeAll();
        if ( ! detached) {
            if (overview) {
                nextRenderer = new LabelOverviewRenderer().setInner(nextRenderer);
                cm.setOverviewRenderer(nextRenderer);
            } else if (ccr == cm.getSelectionRenderer()) {
                nextRenderer = new LabelOverviewRenderer().setInner(nextRenderer);
                cm.setSelectionRenderer(nextRenderer);
            } else {
                cm.setDataRenderer(nextRenderer);
            }
        } else {
            if (overview) {
                nextRenderer = new LabelOverviewRenderer().setInner(nextRenderer);
                // unattach and reattach
                cm.removeDetachedRenderer(ccr);
                cm.addDetachedRenderer(nextRenderer);
            }
        }
        nextRenderer.init(cm);
        ccr = nextRenderer;
        // FIXME
        pp.init(table, value, row, col, ccr);
        rsp = ccr.getSettingsPanel();
        rsp.addPropertyChangeListener(
                AbstractRendererOPanel.renderOptionsProperty, this);
        jpOptions.add(rsp);
        validate();

        table.repaint();
    }//GEN-LAST:event_jcbTypeActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel2;
    private javax.swing.JComboBox jcbType;
    private javax.swing.JPanel jpOptions;
    private javax.swing.JPanel jpPreview;
    private javax.swing.JPanel jpStretcher;
    private javax.swing.JPanel jpType;
    // End of variables declaration//GEN-END:variables
}
