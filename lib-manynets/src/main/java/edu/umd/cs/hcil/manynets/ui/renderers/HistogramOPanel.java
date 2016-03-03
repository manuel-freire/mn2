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
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 *
 * @author Manuel Freire
 */
public class HistogramOPanel extends AbstractRendererOPanel 
        implements PropertyChangeListener {
    
    private HistogramRenderer renderer;

    /** Creates new form HistogramOptionsPanel */
    public HistogramOPanel(HistogramRenderer renderer) {
        this.renderer = renderer;
        initComponents();
        HistogramOverviewOPanel hoop =
                new HistogramOverviewOPanel(renderer.getHist());
        hoop.addPropertyChangeListener(renderOptionsProperty, this);
        add(hoop, BorderLayout.NORTH);
    }

    public boolean isRangeGlobal() {
        return ((String)jcbRange.getSelectedItem()).startsWith("global");
    }

    public boolean isHeightGlobal() {
        return ((String)jcbHeight.getSelectedItem()).startsWith("global");
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
        jcbRange = new javax.swing.JComboBox();
        jcbHeight = new javax.swing.JComboBox();

        setLayout(new java.awt.BorderLayout());

        jpOptions.setLayout(new java.awt.GridBagLayout());

        jLabel2.setText("Range");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 2, 2);
        jpOptions.add(jLabel2, gridBagConstraints);

        jLabel3.setText("Height");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 2, 2);
        jpOptions.add(jLabel3, gridBagConstraints);

        jcbRange.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "local: min, max of each cell", "global: min, max of column" }));
        jcbRange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbRangeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 7);
        jpOptions.add(jcbRange, gridBagConstraints);

        jcbHeight.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "local: max value in cell", "global: max value in colum", " " }));
        jcbHeight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbHeightActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 7);
        jpOptions.add(jcbHeight, gridBagConstraints);

        add(jpOptions, java.awt.BorderLayout.CENTER);
    }

    private void jcbHeightActionPerformed(java.awt.event.ActionEvent evt) {
        renderer.setGlobalHeight(isHeightGlobal());
        fireOptionsChanged();
    }

    private void jcbRangeActionPerformed(java.awt.event.ActionEvent evt) {
        renderer.setGlobalRange(isRangeGlobal());
        fireOptionsChanged();
    }


    // Variables declaration - do not modify
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JComboBox jcbHeight;
    private javax.swing.JComboBox jcbRange;
    private javax.swing.JPanel jpOptions;
    // End of variables declaration

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(renderOptionsProperty)) {
            fireOptionsChanged();
        }
    }
}