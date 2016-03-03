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

import edu.umd.cs.hcil.manynets.engines.ExpressionCalculator;
import edu.umd.cs.hcil.manynets.model.Stat;
import edu.umd.cs.hcil.manynets.model.StatsFactory;
import edu.umd.cs.hcil.manynets.model.TableWrapper;
import edu.umd.cs.hcil.manynets.model.TableWrapper.Level;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

/**
 *
 * @author Manuel Freire
 */
public class StatsSelectionDialog extends javax.swing.JDialog {

    private TablePanel tablePanel;
    private Set<Stat> toRemove = new TreeSet<Stat>();
    private Set<Stat> toAdd = new TreeSet<Stat>();

    private StatsSelectionPanel currentStatsPanel;
    private StatsSelectionPanel availableStatsPanel;
    private JScrollPane availableStatsPanelScrollPane;
    private PythonPanel pythonPanel;
    private TableWrapper availableSource;

    private static class Option {
        String name;
        TableWrapper wrapper;
        public Option(TableWrapper wrapper) {
            this.wrapper = wrapper;
            this.name = (wrapper != null) ?
                    wrapper.getLevel() + " Table"
                    + " '" + wrapper.getName() + "'" :
                "User-defined Expression";
        }
        @Override
        public String toString() {
            return name;
        }
        public ImageIcon getIcon() {
            return wrapper != null ? wrapper.getIcon() : null;
        }
    }

    public Set<Stat> getRemoved() {
        return toRemove;
    }

    public Set<Stat> getAdded() {
        return toAdd;
    }

    private boolean accepted = false;

    /** Creates new form StatSelectionPanel */
    public StatsSelectionDialog(TablePanel parent) {
        super((JFrame)null, "Select statistics to use", true);
        initComponents();
        jcbColSource.setRenderer(new OptionsRenderer());

        this.tablePanel = parent;
        TableWrapper tw = tablePanel.getBaseWrapper();
        currentStatsPanel = new StatsSelectionPanel(
                tw.getStats(), false, false);
        currentStatsPanel.addPropertyChangeListener(
                StatsSelectionPanel.REMOVE_STAT_PROPERTY, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                removeStats();
            }
        });
        jpCurrent.add(new JScrollPane(currentStatsPanel),
                BorderLayout.CENTER);

        DefaultComboBoxModel model = new DefaultComboBoxModel();
        jcbColSource.setModel(model);
        model.addElement(new Option(tw));
        for (TableWrapper ctw : tw.getChildWrappers()) {
            model.addElement(new Option(ctw));
        }
        model.addElement(new Option(null));

        if (tw.getLevel().equals(Level.Relationship) ||
                tw.getLevel().equals(Level.Entity)) {
            setAvailableSource(null);
        } else {
            setAvailableSource(tw);
        }
        jbAddExpCol.setVisible(false);

        setSize(700, 500);
        setLocationRelativeTo(parent);
    }

    public boolean isAccepted() {
        return accepted;
    }

    /**
     * renders options with icons
     */
    private static class OptionsRenderer extends JLabel
                           implements ListCellRenderer {
        public OptionsRenderer() {
            setHorizontalAlignment(LEFT);
        }
        @Override
        public Component getListCellRendererComponent(
               JList list, Object value, int index,
               boolean isSelected, boolean cellHasFocus) {
            if (cellHasFocus) {
                setBorder(BorderFactory.createLineBorder(Color.blue));
            } else {
                setBorder(BorderFactory.createEmptyBorder());
            }
            setIcon(((Option)value).getIcon());
            setText(((Option)value).toString());
            return this;
        }
    }

    public void setAvailableSource(TableWrapper tw) {
        if (availableStatsPanel != null) {
            jpToAdd.remove(availableStatsPanelScrollPane);
        }
        if (pythonPanel != null) {
            jpToAdd.remove(pythonPanel);
        }
        if (tw != null) {

            TableWrapper dest = tablePanel.getBaseWrapper();

            Set<Stat> as = StatsFactory.availableStats(tw, dest);
            as.removeAll(toAdd);
            as.removeAll(tw.getStats());
            availableStatsPanel = new StatsSelectionPanel(as, false, true);
            availableStatsPanel.addPropertyChangeListener(
                        StatsSelectionPanel.REMOVE_STAT_PROPERTY, new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        addStats();
                    }
            });
            availableStatsPanelScrollPane = new JScrollPane(availableStatsPanel);
            fixAvailable();
            jpToAdd.add(availableStatsPanelScrollPane,
                    BorderLayout.CENTER);
            jbAddExpCol.setVisible(false);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("kk:mm:ss");
            String colName = "Added at " + sdf.format(new Date());
            pythonPanel = new PythonPanel(
                tablePanel.getBaseWrapper().getTable(),
                colName);
            pythonPanel.setBorder(
                BorderFactory.createTitledBorder("User-defined column (Python)"));
            jpToAdd.add(pythonPanel, BorderLayout.CENTER);
            jbAddExpCol.setVisible(true);
        }
        availableSource = tw;
        for (int i=0; i<jcbColSource.getItemCount(); i++) {
            Option o = (Option)jcbColSource.getModel().getElementAt(i);
            if (o.wrapper == tw) {
                jcbColSource.setSelectedIndex(i);
                jcbColSource.repaint();
                break;
            }
        }
        jpToAdd.validate();
    }

    public void addExpressionCol() {
        TableWrapper dest = tablePanel.getBaseWrapper();

        // create and add a new Python stat, and refresh the Python view
        Stat stat = new Stat(pythonPanel.getColName(), pythonPanel.getText(),
                pythonPanel.getColType(), dest.getLevel(), dest.getLevel());
        if (pythonPanel.getColName().isEmpty()
                || pythonPanel.getText().isEmpty()) {
            // ignore obvious typos
            return;
        }
        try {
            new ExpressionCalculator(stat, pythonPanel.getText(), "O(1)");
            toAdd.add(stat);
            currentStatsPanel.addStat(stat);
            setAvailableSource(null);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(pythonPanel,
                    "<html>" + e.toString().replaceAll("\n", "<br>") +
                    "</html>", "Error compiling expression",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    public void addStats() {
        // find which stats have been selected, remove them from view
        for (Stat stat : availableStatsPanel.getStats()) {
            if (availableStatsPanel.isSelected(stat)) {
                toAdd.add(stat);
                toRemove.remove(stat);
            }
        }

        for (Stat stat : toAdd) {
            currentStatsPanel.addStat(stat);
            availableStatsPanel.removeStat(stat);
        }
    }

    public void removeStats() {
        for (Stat stat : currentStatsPanel.getStats()) {
            if (currentStatsPanel.isSelected(stat)) {
                toAdd.remove(stat);
                toRemove.add(stat);
            }
        }
        for (Stat stat : toRemove) {
            currentStatsPanel.removeStat(stat);
        }
        fixAvailable();
    }
    
    public void fixAvailable() {
        if (availableSource == null) return;

        Set<Stat> as = StatsFactory.availableStats(availableSource,
                tablePanel.getBaseWrapper());
        as.retainAll(toRemove);
        for (Stat stat : as) {
            System.err.println("retaining " + as);
            availableStatsPanel.addStat(stat);
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

        jpButtons = new javax.swing.JPanel();
        jbAccept = new javax.swing.JButton();
        jbCancel = new javax.swing.JButton();
        jpCurrent = new javax.swing.JPanel();
        jpToAdd = new javax.swing.JPanel();
        jpSource = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jcbColSource = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        jbAddExpCol = new javax.swing.JButton();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        jbAccept.setText("Confirm Additions/Removals");
        jbAccept.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbAcceptActionPerformed(evt);
            }
        });
        jpButtons.add(jbAccept);

        jbCancel.setText("Cancel");
        jbCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbCancelActionPerformed(evt);
            }
        });
        jpButtons.add(jbCancel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jpButtons, gridBagConstraints);

        jpCurrent.setBorder(javax.swing.BorderFactory.createTitledBorder("Current Columns"));
        jpCurrent.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        getContentPane().add(jpCurrent, gridBagConstraints);

        jpToAdd.setBorder(javax.swing.BorderFactory.createTitledBorder("Available Columns"));
        jpToAdd.setLayout(new java.awt.BorderLayout());

        jpSource.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("From");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 4);
        jpSource.add(jLabel1, gridBagConstraints);

        jcbColSource.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jcbColSource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbColSourceActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 4);
        jpSource.add(jcbColSource, gridBagConstraints);

        jPanel1.setLayout(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jpSource.add(jPanel1, gridBagConstraints);

        jpToAdd.add(jpSource, java.awt.BorderLayout.NORTH);

        jbAddExpCol.setText("Add expression column");
        jbAddExpCol.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbAddExpColActionPerformed(evt);
            }
        });
        jpToAdd.add(jbAddExpCol, java.awt.BorderLayout.PAGE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        getContentPane().add(jpToAdd, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jbAcceptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbAcceptActionPerformed
        accepted = true;
        this.dispose();
    }//GEN-LAST:event_jbAcceptActionPerformed

    private void jcbColSourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcbColSourceActionPerformed
        Option next = (Option)jcbColSource.getSelectedItem();
        if (next.wrapper != availableSource) {
            setAvailableSource(next.wrapper);
        }
        jcbColSource.repaint();
    }//GEN-LAST:event_jcbColSourceActionPerformed

    private void jbCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbCancelActionPerformed
        this.dispose();
    }//GEN-LAST:event_jbCancelActionPerformed

    private void jbAddExpColActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbAddExpColActionPerformed
        // TODO add your handling code here:
        addExpressionCol();
    }//GEN-LAST:event_jbAddExpColActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton jbAccept;
    private javax.swing.JButton jbAddExpCol;
    private javax.swing.JButton jbCancel;
    private javax.swing.JComboBox jcbColSource;
    private javax.swing.JPanel jpButtons;
    private javax.swing.JPanel jpCurrent;
    private javax.swing.JPanel jpSource;
    private javax.swing.JPanel jpToAdd;
    // End of variables declaration//GEN-END:variables

}
