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
import edu.umd.cs.hcil.manynets.hist.Histogram;
import edu.umd.cs.hcil.manynets.hist.Histogram.HeightFunction;
import java.util.Collection;
import java.util.TreeMap;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 *
 * @author Manuel Freire
 */
public class HistogramOverviewOPanel extends AbstractRendererOPanel {

    private Histogram h;

    public HistogramOverviewOPanel(Histogram h) {
        initComponents();
        this.h = h;
        setComboOptions(jcbWidth, heights.keySet());
        setComboOptions(jcbHeight, heights.keySet());
    }

    private static TreeMap<String, HeightFunction> heights
            = new TreeMap<String, HeightFunction>();

    static {
        heights.put("Linear", new Histogram.LinearHeightFunction());
        heights.put("Square-root", new Histogram.SqrtHeightFunction());
        heights.put("Logarithmic", new Histogram.LogHeightFunction());
    }

    public void setComboOptions(JComboBox combo, Collection<String> options) {
        combo.removeAllItems();
        combo.setModel(new DefaultComboBoxModel((new Vector<String>(options))));
    }

    public HeightFunction getDeltaFunction() {
        return heights.get((String)jcbWidth.getSelectedItem());
    }

    public HeightFunction getHeightFunction() {        
        return heights.get((String)jcbHeight.getSelectedItem());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jcbHeight = new javax.swing.JComboBox();
        jcbWidth = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        jLabel3.setText("Bin width");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 2, 2);
        add(jLabel3, gridBagConstraints);

        jLabel2.setText("Bar height");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 2, 2);
        add(jLabel2, gridBagConstraints);

        jcbHeight.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Linear", "Square-root", "Logarithmic" }));
        jcbHeight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbHeightActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 7);
        add(jcbHeight, gridBagConstraints);

        jcbWidth.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Constant", "Quadratic", "Exponential" }));
        jcbWidth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbWidthActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 7);
        add(jcbWidth, gridBagConstraints);
    }

    private void jcbHeightActionPerformed(java.awt.event.ActionEvent evt) {
        if (jcbHeight.getSelectedItem() == null) return;
        h.setHeightFunction(getHeightFunction());
        fireOptionsChanged();
    }

    private void jcbWidthActionPerformed(java.awt.event.ActionEvent evt) {
        if (jcbWidth.getSelectedItem() == null) return;
        h.setDeltaFunction(getDeltaFunction());
        fireOptionsChanged();
    }


    // Variables declaration - do not modify
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JComboBox jcbHeight;
    private javax.swing.JComboBox jcbWidth;
    // End of variables declaration

}
