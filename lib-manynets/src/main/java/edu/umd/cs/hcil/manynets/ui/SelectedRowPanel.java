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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.swingx.JXTable;

/**
 *
 * @author Manuel Freire
 */
public class SelectedRowPanel extends JPanel implements PropertyChangeListener,
        ListSelectionListener {

    private ColumnSettingsPanel globalPp, selectedPp, lastPp;
    private TablePanel currentPanel;

    /** Creates new form SelectedRowPanel */
    public SelectedRowPanel() {
        initComponents();
        globalPp = new ColumnSettingsPanel();
        selectedPp = new ColumnSettingsPanel();
        lastPp = new ColumnSettingsPanel();
        jpPreview.add(globalPp);
        jpPreview.add(selectedPp);
        jpPreview.add(lastPp);
    }

    public void listenTo(TablePanel tp) {
        tp.getTable().getSelectionModel().addListSelectionListener(this);
        tp.addPropertyChangeListener(TablePanel.CELL_CLICKED_PROPERTY, this);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (currentPanel == null) {
            System.err.println("At " + this.getClass().getSimpleName() + ": " +
                    "expected a currentPanel to tie this selection event to");
            return;
        }
        generateSelectionLabel(currentPanel);
    }

    public static class CellClickedInfo {

        private TablePanel tp;
        private int row;
        private int col;

        public CellClickedInfo(TablePanel tp, int row, int col) {
            this.tp = tp;
            this.row = row;
            this.col = col;
        }

        public TablePanel getTablePanel() {
            return tp;
        }

        public int getRow() {
            return row;
        }

        public int getColumn() {
            return col;
        }

        public int getModelColumn() {
            return tp.getTable().convertColumnIndexToModel(col);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(TablePanel.CELL_CLICKED_PROPERTY)) {
            CellClickedInfo cci = (CellClickedInfo) evt.getNewValue();

            TablePanel tp = cci.getTablePanel();
            JXTable t = tp.getTable();
            JXTable ht = tp.getHeaderTable();

            int col = cci.getColumn();
            int row = cci.getRow();
            if (col < 0 || row < 0) {
                return;
            }

            // update the preview
            ColumnManager cm = ((ColumnManager) t.getCellRenderer(row, col));
            globalPp.init(tp.getColumnManager(col), ht, 0, col, cm.getOverviewRenderer());
            selectedPp.init(tp.getColumnManager(col), ht, 0, col, cm.getSelectionRenderer());
            lastPp.init(tp.getColumnManager(col), t, row, col, cm.getDataRenderer());

            currentPanel = tp;
            generateSelectionLabel(tp);
        }
    }

    public void generateSelectionLabel(TablePanel tp) {
        JXTable t = tp.getTable();

        StringBuilder html = new StringBuilder(
                "<html>No rows selected</html>");
        int nSelected = t.getSelectedRowCount();

        globalPp.repaint();
        if (nSelected == 0) {
            lastPp.setVisible(false);
            selectedPp.setVisible(false);
        } else if (nSelected == 1) {
            selectedPp.setVisible(false);
            lastPp.setVisible(true);
            lastPp.repaint();
        } else if (nSelected > 1) {
            lastPp.setVisible(false);
            selectedPp.setVisible(true);
            selectedPp.repaint();
        }
        jpPreview.validate();

        if (nSelected > 0) {
            int col = t.getSelectedColumn();
            int row = t.getSelectedRow();
            if (col < 0 || row < 0) {
                return;
            }

            // update the html
            html.replace(0, html.length(),
                    "<html>Selected <b>" + nSelected + "</b> row" +
                    (nSelected != 1 ? "s" : "") + " from " +
                    tp.getBaseWrapper().getName() + ":<br>");
            int k = 0;
            for (int i : t.getSelectedRows()) {

                // Avoid overwhelming when the selection is very large
                k++;
                if (nSelected > 30 && k > 20 && k < (nSelected - 10)) {
                    if (k == 21) {
                        html.append(" ... <br>");
                    }
                } else {
                    if ((nSelected % 5) == 0) {
                        html.append("<br>");
                    }
                    try {
                        html.append("" + tp.getId(i) + ", ");
                    } catch (Exception ex) {
                        // bad selection model, clear current selection
                        // t.getSelectionModel().clearSelection();
                        System.err.println("Not found: row " + i);
                        break;
                    }
                }
            }
            html.replace(html.length() - ", ".length(), html.length(), "");
            html.append("</ul></html>");
        } else {
            selectedPp.setVisible(false);
        }

        jepDescription.setContentType("text/html");
        jepDescription.setText(html.toString());
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

        jSplitPane1 = new javax.swing.JSplitPane();
        jpPreview = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jepDescription = new javax.swing.JEditorPane();

        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setDividerLocation(350);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jpPreview.setMinimumSize(new java.awt.Dimension(200, 100));
        jpPreview.setLayout(new javax.swing.BoxLayout(jpPreview, javax.swing.BoxLayout.Y_AXIS));
        jSplitPane1.setTopComponent(jpPreview);

        jepDescription.setEditable(false);
        jScrollPane1.setViewportView(jepDescription);

        jSplitPane1.setBottomComponent(jScrollPane1);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JEditorPane jepDescription;
    private javax.swing.JPanel jpPreview;
    // End of variables declaration//GEN-END:variables
}
