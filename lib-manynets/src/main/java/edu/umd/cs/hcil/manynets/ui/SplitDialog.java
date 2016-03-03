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
import edu.umd.cs.hcil.manynets.model.PGraph;
import edu.umd.cs.hcil.manynets.model.Population.PopulationTableWrapper;
import edu.umd.cs.hcil.manynets.model.Stat;
import edu.umd.cs.hcil.manynets.model.TableWrapper;
import edu.umd.cs.hcil.manynets.model.TableWrapper.Level;
import edu.umd.cs.hcil.manynets.model.Transform;
import edu.umd.cs.hcil.manynets.model.TransformOptions;
import edu.umd.cs.hcil.manynets.transforms.FilterComponentsTransform;
import edu.umd.cs.hcil.manynets.transforms.MultiSplitNetworkTransform;
import edu.umd.cs.hcil.manynets.transforms.SplitComponentNetworkTransform;
import edu.umd.cs.hcil.manynets.transforms.SplitComponentNetworkTransform.SplitComponentOptions;
import edu.umd.cs.hcil.manynets.transforms.SplitNetworkTransform;
import edu.umd.cs.hcil.manynets.transforms.EgoNetworkOptions;
import edu.umd.cs.hcil.manynets.transforms.FilterNetsTransform;
import java.util.Calendar;
import java.util.Date;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import org.jdom.Element;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;

/**
 * Different types of network splits
 *
 * @author mfreire
 */
public class SplitDialog extends javax.swing.JDialog {

    private boolean accepted = false;

    private PGraph pg;
    private PythonPanel pp;
    private TableWrapper tw;
    private TablePanel tp;

    public enum DateUnit {
        SECOND("Second", Calendar.SECOND),
        MINUTE("Minute", Calendar.MINUTE),
        HOUR("Hour", Calendar.HOUR),
        MDAY("Day-of-month", Calendar.DAY_OF_MONTH),
        WDAY("Day-of-week", Calendar.DAY_OF_WEEK),
        YDAY("Day-of-year", Calendar.DAY_OF_YEAR),
        WEEK("Week", Calendar.WEEK_OF_YEAR),
        MONTH("Month", Calendar.MONTH),
        YEAR("Year", Calendar.YEAR);
        private String name;
        private int calendarField;
        DateUnit(String name, int calendarField) {
            this.name = name;
            this.calendarField = calendarField;
        }
        @Override
        public String toString() { return name; }
        public int getValue(Calendar c, Date d) {
            c.setTime(d);
            return c.get(calendarField);
        }
    }

    /** Creates new form SplitDialog */
    public SplitDialog(java.awt.Frame parent, TablePanel tp, PGraph pg) {
        super(parent, true);
        initComponents();
        this.pg = pg;
        this.tp = tp;
        tw = tp.getBaseWrapper();

        setTitle("Split into subnetworks");

        // init for Ego: enable node filtering
        DefaultComboBoxModel m = new DefaultComboBoxModel();
        if (pg == null) {
            pg = ((PopulationTableWrapper)tw).getPopulation()
                .getGraphs().iterator().next();
        }
        Table t = pg.getNodeTable().getTable();
        pp = new PythonPanel(t, null);
        jpNodeFilter.add(pp);
        jpNodeFilter.validate();
        jpNodeFilter.setVisible(false);

        // init for Clusters: add a group to force single selection
        ButtonGroup cbg = new ButtonGroup();
        cbg.add(jrbAllClusters);
        cbg.add(jrbLargest);
        cbg.add(jrbNotLargest);
        cbg.add(jrbNotSize1);

        // init for Motifs: add a group to force single selection
        ButtonGroup mbg = new ButtonGroup();
        mbg.add(jrbPairs);
        mbg.add(jrbTriplets);

        // init for features: use the buttonGroups to force single selections
        jbgNodesOrEdges.add(jrbOnEdges);
        jbgNodesOrEdges.add(jrbOnNodes);
        jbgTemporalFeature.add(jrbSimpleDateUnits);
        jbgTemporalFeature.add(jrbUserSpecifiedDates);
        // and enable nodes (selected by default)
        jrbOnNodesActionPerformed(null);
        // and set a default time unit - days
        DefaultComboBoxModel dcm = (DefaultComboBoxModel)jcbTimeUnit.getModel();
        dcm.removeAllElements();
        for (DateUnit u : DateUnit.values()) {
            dcm.addElement(u);
        }
        jcbTimeUnit.setSelectedItem(DateUnit.YDAY);
    }

    private void updateFeatureSplitColumns(boolean edges) {
        DefaultComboBoxModel dcm = (DefaultComboBoxModel)jcbSelectedFeature.getModel();
        dcm.removeAllElements();
        TableWrapper w = edges ? pg.getEdgeTable() : pg.getNodeTable();
        for (int i=0; i<w.getTable().getColumnCount(); i++) {
            dcm.addElement(w.getTable().getColumnName(i));
        }
    }    

    public boolean isAccepted() {
        return accepted;
    }

    /**
     * Only valid if the dialog was accepted, this returns the fully-configured
     * transform that should be applied to carry out the chosen split
     * @return
     */
    public Transform getTransform() {
        Transform t = null;
        switch (jTabbedPane1.getSelectedIndex()) {
            case 0: {
                // ego network
                t = new SplitNetworkTransform();
                int radius = (Integer)jsRadius.getValue();
                boolean strict = ! jcbPlusDotFive.isSelected();
                boolean egoless = jcbEgoless.isSelected();
                ExpressionCalculator filter = null;
                if (jcbFilterSeeds.isSelected()) {
                    Level l = tp.getBaseWrapper().getLevel();
                    filter = new ExpressionCalculator(
                        new Stat("made-up-stat", "", Boolean.class, l, l),
                        pp.getText(), "O(1)");
                }
                t.init(tw, new EgoNetworkOptions(radius, strict, egoless, filter), null);
                break;
            }
            case 1: {
                // clusters/components
                t = new FilterComponentsTransform();
                TransformOptions to = null;
                if (jrbAllClusters.isSelected()) {
                    t = new SplitComponentNetworkTransform();
                    to = new SplitComponentOptions();
                } else if (jrbLargest.isSelected()) {
                    to = new FilterComponentsTransform.OnlyLargestOption();
                } else {
                    to = new FilterComponentsTransform.AllExceptLargestOption();
                }
                t.init(tw, to, null);
                break;
            }
            case 2: {
                // motifs
                t = new MultiSplitNetworkTransform();
                TransformOptions to = null;
                if (jrbPairs.isSelected()) {
                    to = new MultiSplitNetworkTransform.PieceMealOptions(2);
                } else {
                    to = new MultiSplitNetworkTransform.PieceMealOptions(3);
                }
                t.init(tw, to, null);
                break;
            }
            case 3:  {
                // features
                t = new FilterNetsTransform();
                TransformOptions to = new FilterNetsTransform.NetFilterOptions(){
                    @Override
                    public boolean accepts(Graph g, Edge e) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public boolean accepts(Graph g, Node n) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void save(Element e) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void load(Element e) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public String getDescription() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                };
                break;
            }
            default: // ???
                throw new IllegalArgumentException(
                        "No transform defined for this JTP index");
        }
        return t;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jbgNodesOrEdges = new javax.swing.ButtonGroup();
        jbgTemporalFeature = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jsRadius = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        jcbPlusDotFive = new javax.swing.JCheckBox();
        jcbEgoless = new javax.swing.JCheckBox();
        jcbFilterSeeds = new javax.swing.JCheckBox();
        jpNodeFilter = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jcbClusteringAlgorithm = new javax.swing.JComboBox();
        jrbAllClusters = new javax.swing.JRadioButton();
        jrbLargest = new javax.swing.JRadioButton();
        jrbNotLargest = new javax.swing.JRadioButton();
        jrbNotSize1 = new javax.swing.JRadioButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jrbPairs = new javax.swing.JRadioButton();
        jrbTriplets = new javax.swing.JRadioButton();
        jPanel5 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jcbSelectedFeature = new javax.swing.JComboBox();
        jrbOnNodes = new javax.swing.JRadioButton();
        jrbOnEdges = new javax.swing.JRadioButton();
        jPanel6 = new javax.swing.JPanel();
        jcbRemoveOrphans = new javax.swing.JCheckBox();
        jPanel7 = new javax.swing.JPanel();
        jcbTimeUnit = new javax.swing.JComboBox();
        jrbSimpleDateUnits = new javax.swing.JRadioButton();
        jrbUserSpecifiedDates = new javax.swing.JRadioButton();
        jLabel8 = new javax.swing.JLabel();
        jtfOffset = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jtfIncrement = new javax.swing.JTextField();
        jcbTimeZone = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        jbApply = new javax.swing.JButton();
        jbCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("<html>For each seed node, create a network that contains its neighbors up to a given radius</html>");

        jsRadius.setModel(new javax.swing.SpinnerNumberModel(1, 1, 10, 1));

        jLabel2.setText("Radius");

        jcbPlusDotFive.setSelected(true);
        jcbPlusDotFive.setText("Include all inter-neighbor connections (\"+.5\")");

        jcbEgoless.setText("Exclude the seed node itself (\"egoless\")");

        jcbFilterSeeds.setText("Use a node filter to select seed nodes");
        jcbFilterSeeds.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbFilterSeedsActionPerformed(evt);
            }
        });

        jpNodeFilter.setBorder(javax.swing.BorderFactory.createTitledBorder("Node filter (operates on node attributes)"));
        jpNodeFilter.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 501, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jsRadius, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jcbFilterSeeds)
                    .addComponent(jcbEgoless)
                    .addComponent(jcbPlusDotFive))
                .addContainerGap())
            .addComponent(jpNodeFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 525, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jsRadius, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jcbPlusDotFive)
                .addGap(5, 5, 5)
                .addComponent(jcbEgoless)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jcbFilterSeeds)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jpNodeFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("By Ego", jPanel2);

        jLabel3.setText("<html>Subdivide into clusters or connected components</html>");

        jLabel4.setText("Algorithm");

        jcbClusteringAlgorithm.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Connected components" }));

        jrbAllClusters.setSelected(true);
        jrbAllClusters.setText("Split into all clusters");

        jrbLargest.setText("Retain only largest cluster");

        jrbNotLargest.setText("Remove largest cluster");

        jrbNotSize1.setText("Remove clusters with only 1 node");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jrbNotSize1)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 501, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jcbClusteringAlgorithm, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jrbAllClusters)
                    .addComponent(jrbLargest)
                    .addComponent(jrbNotLargest))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jcbClusteringAlgorithm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jrbAllClusters)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jrbLargest)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jrbNotLargest)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jrbNotSize1)
                .addContainerGap(89, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("By Clusters", jPanel3);

        jLabel5.setText("<html>Split into all instances of a given connectivity pattern; patterns may overlap</html>");

        jrbPairs.setSelected(true);
        jrbPairs.setText("Pairs");

        jrbTriplets.setText("Triplets");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 501, Short.MAX_VALUE)
                    .addComponent(jrbPairs)
                    .addComponent(jrbTriplets))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jrbPairs)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jrbTriplets)
                .addContainerGap(166, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("By Motif", jPanel4);

        jLabel6.setText("<html>Build all networks with particular values or value-ranges for a given feature</html>");

        jLabel7.setText("Feature from");

        jcbSelectedFeature.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jcbSelectedFeature.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbSelectedFeatureActionPerformed(evt);
            }
        });

        jrbOnNodes.setSelected(true);
        jrbOnNodes.setText("nodes");
        jrbOnNodes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jrbOnNodesActionPerformed(evt);
            }
        });

        jrbOnEdges.setText("edges");
        jrbOnEdges.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jrbOnEdgesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 34, Short.MAX_VALUE)
        );

        jcbRemoveOrphans.setText("If unconnected 'orphan' nodes result, remove them");

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Temporal Feature"));

        jcbTimeUnit.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jrbSimpleDateUnits.setText("Use simple unit");
        jrbSimpleDateUnits.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jrbSimpleDateUnitsActionPerformed(evt);
            }
        });

        jrbUserSpecifiedDates.setText("Roll your own");
        jrbUserSpecifiedDates.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jrbUserSpecifiedDatesActionPerformed(evt);
            }
        });

        jLabel8.setText("Offset");

        jtfOffset.setText("0");
        jtfOffset.setPreferredSize(new java.awt.Dimension(100, 27));
        jtfOffset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtfOffsetActionPerformed(evt);
            }
        });

        jLabel9.setText("Increment");

        jtfIncrement.setText("0");
        jtfIncrement.setPreferredSize(new java.awt.Dimension(100, 27));

        jcbTimeZone.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jrbSimpleDateUnits)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jcbTimeUnit, 0, 201, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jcbTimeZone, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jrbUserSpecifiedDates)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 39, Short.MAX_VALUE)
                        .addComponent(jLabel8)
                        .addGap(2, 2, 2)
                        .addComponent(jtfOffset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel9)
                        .addGap(4, 4, 4)
                        .addComponent(jtfIncrement, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jcbTimeUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jrbSimpleDateUnits)
                    .addComponent(jcbTimeZone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jrbUserSpecifiedDates)
                    .addComponent(jLabel8)
                    .addComponent(jtfOffset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(jtfIncrement, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(31, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 501, Short.MAX_VALUE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jcbRemoveOrphans)
                                .addGap(144, 144, 144))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jrbOnNodes)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jrbOnEdges)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jcbSelectedFeature, 0, 256, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(jcbSelectedFeature, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jrbOnNodes)
                            .addComponent(jrbOnEdges))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jcbRemoveOrphans)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("By Feature", jPanel5);

        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jbApply.setText("Apply");
        jbApply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbApplyActionPerformed(evt);
            }
        });
        jPanel1.add(jbApply, new java.awt.GridBagConstraints());

        jbCancel.setText("Cancel");
        jbCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbCancelActionPerformed(evt);
            }
        });
        jPanel1.add(jbCancel, new java.awt.GridBagConstraints());

        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jbApplyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbApplyActionPerformed
        accepted = true;
        dispose();
}//GEN-LAST:event_jbApplyActionPerformed

    private void jbCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbCancelActionPerformed
        dispose();
}//GEN-LAST:event_jbCancelActionPerformed

    private void jcbFilterSeedsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcbFilterSeedsActionPerformed
        jpNodeFilter.setVisible(jcbFilterSeeds.isSelected());
    }//GEN-LAST:event_jcbFilterSeedsActionPerformed

    private void jtfOffsetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtfOffsetActionPerformed
        
    }//GEN-LAST:event_jtfOffsetActionPerformed

    private void jcbSelectedFeatureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcbSelectedFeatureActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jcbSelectedFeatureActionPerformed

    private void jrbOnNodesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jrbOnNodesActionPerformed
        updateFeatureSplitColumns(false);
    }//GEN-LAST:event_jrbOnNodesActionPerformed

    private void jrbOnEdgesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jrbOnEdgesActionPerformed
        updateFeatureSplitColumns(true);
    }//GEN-LAST:event_jrbOnEdgesActionPerformed

    private void jrbSimpleDateUnitsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jrbSimpleDateUnitsActionPerformed

    }//GEN-LAST:event_jrbSimpleDateUnitsActionPerformed

    private void jrbUserSpecifiedDatesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jrbUserSpecifiedDatesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jrbUserSpecifiedDatesActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton jbApply;
    private javax.swing.JButton jbCancel;
    private javax.swing.ButtonGroup jbgNodesOrEdges;
    private javax.swing.ButtonGroup jbgTemporalFeature;
    private javax.swing.JComboBox jcbClusteringAlgorithm;
    private javax.swing.JCheckBox jcbEgoless;
    private javax.swing.JCheckBox jcbFilterSeeds;
    private javax.swing.JCheckBox jcbPlusDotFive;
    private javax.swing.JCheckBox jcbRemoveOrphans;
    private javax.swing.JComboBox jcbSelectedFeature;
    private javax.swing.JComboBox jcbTimeUnit;
    private javax.swing.JComboBox jcbTimeZone;
    private javax.swing.JPanel jpNodeFilter;
    private javax.swing.JRadioButton jrbAllClusters;
    private javax.swing.JRadioButton jrbLargest;
    private javax.swing.JRadioButton jrbNotLargest;
    private javax.swing.JRadioButton jrbNotSize1;
    private javax.swing.JRadioButton jrbOnEdges;
    private javax.swing.JRadioButton jrbOnNodes;
    private javax.swing.JRadioButton jrbPairs;
    private javax.swing.JRadioButton jrbSimpleDateUnits;
    private javax.swing.JRadioButton jrbTriplets;
    private javax.swing.JRadioButton jrbUserSpecifiedDates;
    private javax.swing.JSpinner jsRadius;
    private javax.swing.JTextField jtfIncrement;
    private javax.swing.JTextField jtfOffset;
    // End of variables declaration//GEN-END:variables

}
