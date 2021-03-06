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

import edu.umd.cs.hcil.manynets.ui.ColumnManager.AbstractRendererOPanel;
import javax.swing.DefaultComboBoxModel;

/**
 *
 * @author Manuel Freire
 */
public class ScatterplotOverviewOPanel extends AbstractRendererOPanel {

    private ScatterplotOverviewRenderer renderer;

    /** Creates new form HistogramOptionsPanel */
    public ScatterplotOverviewOPanel(ScatterplotOverviewRenderer renderer, 
            String[] columns) {
        this.renderer = renderer;
        initComponents();
        jcbOtherColumn.setModel(new DefaultComboBoxModel(columns));
    }

    public String getOtherColumn() {
        return (String)jcbOtherColumn.getSelectedItem();
    }

    public String getYScaling() {
        return (String)jcbYScaling.getSelectedItem();
    }

    public String getXScaling() {
        return (String)jcbXScaling.getSelectedItem();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jpOptions = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jcbOtherColumn = new javax.swing.JComboBox();
        jcbYScaling = new javax.swing.JComboBox();
        jcbXScaling = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        jpOptions.setLayout(new java.awt.GridBagLayout());

        jLabel2.setText("Y Column");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 2, 2);
        jpOptions.add(jLabel2, gridBagConstraints);

        jLabel3.setText("Y Scaling");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 2, 2);
        jpOptions.add(jLabel3, gridBagConstraints);

        jcbOtherColumn.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "local: min, max of each cell", "global: min, max of all cells in column", " " }));
        jcbOtherColumn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbOtherColumnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 7);
        jpOptions.add(jcbOtherColumn, gridBagConstraints);

        jcbYScaling.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Linear", "Logarithmic", " " }));
        jcbYScaling.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbYScalingActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 7);
        jpOptions.add(jcbYScaling, gridBagConstraints);

        jcbXScaling.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Linear", "Logarithmic", " " }));
        jcbXScaling.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbXScalingActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 2, 2, 7);
        jpOptions.add(jcbXScaling, gridBagConstraints);

        jLabel4.setText("X Scaling");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 2, 2);
        jpOptions.add(jLabel4, gridBagConstraints);

        add(jpOptions, java.awt.BorderLayout.CENTER);
    }

    private void jcbYScalingActionPerformed(java.awt.event.ActionEvent evt) {
        //renderer.setGlobalHeight(isHeightGlobal());
        fireOptionsChanged();
    }

    private void jcbOtherColumnActionPerformed(java.awt.event.ActionEvent evt) {
        renderer.setOtherColumn(getOtherColumn());
        fireOptionsChanged();
    }

    private void jcbXScalingActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        fireOptionsChanged();
    }


    // Variables declaration - do not modify
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JComboBox jcbOtherColumn;
    private javax.swing.JComboBox jcbXScaling;
    private javax.swing.JComboBox jcbYScaling;
    private javax.swing.JPanel jpOptions;
    // End of variables declaration

}
