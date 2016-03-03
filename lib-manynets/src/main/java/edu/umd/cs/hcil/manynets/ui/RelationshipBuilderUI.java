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
import edu.umd.cs.hcil.manynets.hist.DefaultHistogramModel;
import edu.umd.cs.hcil.manynets.hist.Histogram;
import edu.umd.cs.hcil.manynets.model.Dataset;
import edu.umd.cs.hcil.manynets.model.PGraph;
import edu.umd.cs.hcil.manynets.model.ReachabilityRelBuilder;
import edu.umd.cs.hcil.manynets.model.RelationshipBuilder;
import edu.umd.cs.hcil.manynets.model.RelationshipBuilder.ProgressableWorker;
import edu.umd.cs.hcil.manynets.model.RelationshipBuilder.RelPath;
import edu.umd.cs.hcil.manynets.model.Schema.Entity;
import edu.umd.cs.hcil.manynets.model.SchemaInstance.EntityTableWrapper;
import edu.umd.cs.hcil.manynets.model.SchemaInstance.RelTableWrapper;
import edu.umd.cs.hcil.manynets.model.SharedTargetRelBuilder;
import edu.umd.cs.hcil.manynets.model.SharedTargetRelBuilder.Relatedness;
import edu.umd.cs.hcil.manynets.model.Transform;
import edu.umd.cs.hcil.manynets.model.TransformOptions;
import edu.umd.cs.hcil.socialaction.SocialAction;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import prefuse.activity.ActivityManager;
import prefuse.data.Graph;

/**
 *
 * @author Manuel Freire
 */
public class RelationshipBuilderUI extends javax.swing.JPanel {

    private PGraph pg;
    private DefaultHistogramModel dhmCut;

    private Relatedness relatedness;

    private RelationshipBuilder builder;

    /** Creates new form RelationshipBuilder */
    public RelationshipBuilderUI(PGraph pg) {
        initComponents();
        jpCutSel.setVisible(false);
        
        this.pg = pg;
        
        // init source and target combo-boxes
        DefaultComboBoxModel em;
        em = new DefaultComboBoxModel();
        for (Entity e : pg.getSchema().getEntities().values()) {
            if (RelationshipBuilder.graphHasEnt(pg, e)) {
                em.addElement(e.getId());
            }
        }
        jcbSource.setModel(em);
        em = new DefaultComboBoxModel();
        for (Entity e : pg.getSchema().getEntities().values()) {
            if (RelationshipBuilder.graphHasEnt(pg, e)) {
                em.addElement(e.getId());
            }
        }
        jcbTarget.setModel(em);
        jcbPath.setModel(new DefaultComboBoxModel());

        updateStrategy();
    }


    private String oldStrategy;
    public void updateStrategy() {
        String strat = jcbStrategy.getSelectedItem().toString();
        if (strat.equals(oldStrategy)) {
            return;
        }
        oldStrategy = strat;
        System.err.println("strategy updated!");
        jpCutSel.setVisible(false);

        if (strat.contains("Co-occurrence")) {

            jlTarget.setVisible(false);
            jcbTarget.setVisible(false);
            relatedness = (strat.contains("%")) ?
                new SharedTargetRelBuilder.ProportionRelatedness() :
                new SharedTargetRelBuilder.SumRelatedness();
        } else {

            jlTarget.setVisible(true);
            jcbTarget.setVisible(true);
        }
        populatePath();
        repack();
    }

    private void repack() {
        Component root = SwingUtilities.getRoot(this);
        if (root instanceof JDialog) ((JDialog)root).pack();
    }

    private void populatePath() {
        System.err.println("updating path...");
        updateBuilder();
        DefaultComboBoxModel pm = new DefaultComboBoxModel();
        for (RelPath rp : builder.populatePath()) {
            System.err.println("  adding: " + rp);
            pm.addElement(rp);
        }
        jcbPath.setModel(pm);
        updateBuilder();
    }

    public void updateBuilder() {
        Entity se = pg.getSchema().getEnt("" + jcbSource.getSelectedItem());
        Entity te = pg.getSchema().getEnt("" + jcbTarget.getSelectedItem());
        RelPath p = (RelPath)jcbPath.getSelectedItem();

        if (jcbStrategy.getSelectedItem().toString().contains("Co-occurrence")) {
            // provide source-entity twice, as target entity "is not set" (not applicable)
            builder =
                new SharedTargetRelBuilder(pg, p, se, se);
            ((SharedTargetRelBuilder)builder).configure(relatedness, -1, -1);
            jbOpen.setVisible(true);
        } else {
            builder =
                new ReachabilityRelBuilder(pg, p, se, te);
            jbOpen.setVisible(false);
        }
    }

    private SwingWorker currentWorker;
    public void generate() {
        updateBuilder();

        if (currentWorker != null) {
            System.err.println("cancelling... ");
            currentWorker.cancel(false);
            return;
        }

        System.err.println("Graph is " + pg.getName());
        for (EntityTableWrapper etw : pg.getNodeTables()) {
            System.err.println("\t" + etw.getName() + ": " + etw.getTable().getRowCount() + " rows");
        }
        currentWorker = new ProgressableWorker() {
            @Override
            protected void done() {
                System.err.println("Done! at " + getProgress());
                try {
                    dhmCut = (DefaultHistogramModel)get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                jbOpen.setText("Generate");
                currentWorker = null;
                generated();
            }
            @Override
            protected Object doInBackground() throws Exception {
                try {
                    return builder.explore(this);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            }
        };
        
        currentWorker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (currentWorker != null) {
                    jbOpen.setText("" + currentWorker.getProgress()
                            + "% done, click to cancel");
                }
            }
        });
        currentWorker.execute();
    }    

    public void generated() {
        dhmCut.dump();
        jpCutSel.setVisible(true);
        Histogram h = new Histogram(dhmCut, BarRenderer.class, null);
        h.setPreferredSize(new Dimension(getWidth(), 150));
        jpHistogram.removeAll();
        jpHistogram.add(h);
        int min = (int)dhmCut.getMin();
        int max = (int)dhmCut.getMax();
        jlMin.setText("Min: " + min + " (" + dhmCut.count(min, max+1) + " edges)");
        jlMax.setText("Max: " + max + " (" + dhmCut.count(max, max+1) + " edges)");
        jsHigh.setValue(max);
        jsHigh.setMinimum(min);
        jsHigh.setMaximum(max);
        jsLow.setMinimum(min);
        jsLow.setMaximum(max);
        jsLow.setValue(min);
        
        jsHigh.setVisible(min != max);
        jsLow.setVisible(min != max);
        jpHistogram.setVisible(min != max);
        repack();
    }

    public RelTableWrapper getRelationship() {
        updateBuilder();

        if (builder instanceof SharedTargetRelBuilder) {
            ((SharedTargetRelBuilder)builder).configure(relatedness,
                    getLowCutpoint(), getHighCutpoint());
        }

        RelTableWrapper rtw = builder.build(jtfRelName.getText());
        System.err.println(Dataset.dumpTable(rtw.getTable(), 5));
        return rtw;
    }

    public Transform getTransform() {
        JFrame jf = new JFrame("The new tablePanel");
        jf.setSize(800, 600);
        TablePanel tp = new TablePanel();
        tp.init(getRelationship());
        jf.add(tp);
        jf.setVisible(true);
        return null;
    }
    
    public TransformOptions getTransformOptions() {
        return null;
    }

    public int getLowCutpoint() {
        int v = Math.min(jsLow.getValue(), jsHigh.getValue());
        return v;
    }

    public int getHighCutpoint() {
        int v = Math.max(jsLow.getValue(), jsHigh.getValue());
        return v;
    }

    private void openInSA(String title, String labelField, Graph g) {
        JDialog jd = new JDialog(
                (JDialog)SwingUtilities.getAncestorOfClass(JDialog.class, this), title, false);
        final SocialAction sa = (SocialAction)SocialAction.demo2(g, labelField);

        sa.setCurvedEdges(true);
        sa.setArrowRendered(true);
        sa.setEdgeSize(2);
        sa.nodeAttributesButton.doClick();
        sa.getNodeAttributes1D().setSelectedRanking("Type");

        jd.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ActivityManager.stopThread();
            }
        });

        jd.add(sa);
        jd.pack();
        jd.setSize(800, 600);
        jd.setVisible(true);
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

        jPanel5 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jtfRelName = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jpPath = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jlTarget = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jcbSource = new javax.swing.JComboBox();
        jcbTarget = new javax.swing.JComboBox();
        jcbPath = new javax.swing.JComboBox();
        jbOpen = new javax.swing.JButton();
        jbOpenInSA = new javax.swing.JButton();
        jbOpenGERInSA = new javax.swing.JButton();
        jpCutSel = new javax.swing.JPanel();
        jsHigh = new javax.swing.JSlider();
        jsLow = new javax.swing.JSlider();
        jlMax = new javax.swing.JLabel();
        jpHistogram = new javax.swing.JPanel();
        jlMin = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jlCurrent = new javax.swing.JLabel();
        jpStrat = new javax.swing.JPanel();
        jcbStrategy = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        jPanel5.setLayout(new java.awt.GridBagLayout());

        jLabel5.setText("Relationship name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        jPanel5.add(jLabel5, gridBagConstraints);

        jtfRelName.setColumns(24);
        jPanel5.add(jtfRelName, new java.awt.GridBagConstraints());

        jPanel3.setLayout(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel5.add(jPanel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(jPanel5, gridBagConstraints);

        jpPath.setBorder(javax.swing.BorderFactory.createTitledBorder("Select path"));
        jpPath.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Source entity");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jpPath.add(jLabel1, gridBagConstraints);

        jlTarget.setText("Target entity");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jpPath.add(jlTarget, gridBagConstraints);

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel3.setText("Path to follow");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jpPath.add(jLabel3, gridBagConstraints);

        jcbSource.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Film", "Item 2", "Item 3", "Item 4" }));
        jcbSource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbSourceActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        jpPath.add(jcbSource, gridBagConstraints);

        jcbTarget.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Film", "Item 2", "Item 3", "Item 4" }));
        jcbTarget.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbTargetActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        jpPath.add(jcbTarget, gridBagConstraints);

        jcbPath.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "User-Film", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        jpPath.add(jcbPath, gridBagConstraints);

        jbOpen.setText("Generate"); // NOI18N
        jbOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbOpenActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jpPath.add(jbOpen, gridBagConstraints);

        jbOpenInSA.setText("View Full Schema"); // NOI18N
        jbOpenInSA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbOpenInSAActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jpPath.add(jbOpenInSA, gridBagConstraints);

        jbOpenGERInSA.setText("View Graph Schema"); // NOI18N
        jbOpenGERInSA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbOpenGERInSAActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        jpPath.add(jbOpenGERInSA, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(jpPath, gridBagConstraints);

        jpCutSel.setBorder(javax.swing.BorderFactory.createTitledBorder("Choose cut-point"));
        jpCutSel.setLayout(new java.awt.GridBagLayout());

        jsHigh.setPaintLabels(true);
        jsHigh.setValue(0);
        jsHigh.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jsHighStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        jpCutSel.add(jsHigh, gridBagConstraints);

        jsLow.setPaintLabels(true);
        jsLow.setValue(100);
        jsLow.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jsLowStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        jpCutSel.add(jsLow, gridBagConstraints);

        jlMax.setText("Max: ?");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        jpCutSel.add(jlMax, gridBagConstraints);

        jpHistogram.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jpCutSel.add(jpHistogram, gridBagConstraints);

        jlMin.setText("Min: 1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        jpCutSel.add(jlMin, gridBagConstraints);

        jlCurrent.setText("Selected:"); // NOI18N
        jPanel4.add(jlCurrent);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        jpCutSel.add(jPanel4, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(jpCutSel, gridBagConstraints);

        jpStrat.setBorder(javax.swing.BorderFactory.createTitledBorder("Select strategy"));
        jpStrat.setLayout(new java.awt.GridBagLayout());

        jcbStrategy.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Co-occurrence: two entities share cut-point or more total destinations", "Co-occurrence: two entities share cut-point or more % of destinations", "Reachability: the source can reach the target by following the path" }));
        jcbStrategy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbStrategyActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        jpStrat.add(jcbStrategy, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(jpStrat, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jbOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbOpenActionPerformed
        // TODO add your handling code here:
        generate();
    }//GEN-LAST:event_jbOpenActionPerformed

    private void jsLowStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jsLowStateChanged
        int l = getLowCutpoint();
        int h = getHighCutpoint();
        jlCurrent.setText("" + l + "-" + h + ": " + dhmCut.count(l, h+0.5) + " edges");
    }//GEN-LAST:event_jsLowStateChanged

    private void jcbTargetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcbTargetActionPerformed
        populatePath();
    }//GEN-LAST:event_jcbTargetActionPerformed

    private void jcbSourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcbSourceActionPerformed
        populatePath();
    }//GEN-LAST:event_jcbSourceActionPerformed

    private void jcbStrategyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcbStrategyActionPerformed
        updateStrategy();
    }//GEN-LAST:event_jcbStrategyActionPerformed

    private void jsHighStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jsHighStateChanged
        int l = getLowCutpoint();
        int h = getHighCutpoint();
        jlCurrent.setText("" + l + "-" + h + ": " + dhmCut.count(l, h+0.5) + " edges");
    }//GEN-LAST:event_jsHighStateChanged

    private void jbOpenInSAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbOpenInSAActionPerformed
        // TODO add your handling code here:
        openInSA("Schema of the full dataset", "Label", pg.getSchema().toBimodalGraph());
    }//GEN-LAST:event_jbOpenInSAActionPerformed

    private void jbOpenGERInSAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbOpenGERInSAActionPerformed
        // TODO add your handling code here:
        openInSA("Schema of this network", "Label", pg.getSchema().toBimodalGraph(pg));
    }//GEN-LAST:event_jbOpenGERInSAActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JButton jbOpen;
    private javax.swing.JButton jbOpenGERInSA;
    private javax.swing.JButton jbOpenInSA;
    private javax.swing.JComboBox jcbPath;
    private javax.swing.JComboBox jcbSource;
    private javax.swing.JComboBox jcbStrategy;
    private javax.swing.JComboBox jcbTarget;
    private javax.swing.JLabel jlCurrent;
    private javax.swing.JLabel jlMax;
    private javax.swing.JLabel jlMin;
    private javax.swing.JLabel jlTarget;
    private javax.swing.JPanel jpCutSel;
    private javax.swing.JPanel jpHistogram;
    private javax.swing.JPanel jpPath;
    private javax.swing.JPanel jpStrat;
    private javax.swing.JSlider jsHigh;
    private javax.swing.JSlider jsLow;
    private javax.swing.JTextField jtfRelName;
    // End of variables declaration//GEN-END:variables

}
