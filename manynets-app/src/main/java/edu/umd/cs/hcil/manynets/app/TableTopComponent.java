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

package edu.umd.cs.hcil.manynets.app;

import edu.umd.cs.hcil.manynets.engines.ExpressionCalculator;
import edu.umd.cs.hcil.manynets.model.GraphExporter.Format;
import edu.umd.cs.hcil.manynets.model.GraphMerger;
import edu.umd.cs.hcil.manynets.model.PGraph;
import edu.umd.cs.hcil.manynets.model.Population;
import edu.umd.cs.hcil.manynets.model.Population.PopulationTableWrapper;
import edu.umd.cs.hcil.manynets.model.SchemaInstance.RelTableWrapper;
import edu.umd.cs.hcil.manynets.model.Stat;
import edu.umd.cs.hcil.manynets.model.TableWrapper;
import edu.umd.cs.hcil.manynets.model.TableWrapper.Level;
import edu.umd.cs.hcil.manynets.model.Transform;
import edu.umd.cs.hcil.manynets.model.TransformOptions;
import edu.umd.cs.hcil.manynets.transforms.CalculateStatsTransform.StatsOptions;
import edu.umd.cs.hcil.manynets.transforms.MergeTransform;
import edu.umd.cs.hcil.manynets.ui.ElementFilter;
import edu.umd.cs.hcil.manynets.ui.NetworkCreationPanel;
import edu.umd.cs.hcil.manynets.ui.PythonFilterDialog;
import edu.umd.cs.hcil.manynets.ui.PythonPanel;
import edu.umd.cs.hcil.manynets.ui.RelationshipBuilderUI;
import edu.umd.cs.hcil.manynets.ui.SplitDialog;
import edu.umd.cs.hcil.manynets.ui.StatsSelectionDialog;
import edu.umd.cs.hcil.manynets.ui.TablePanel;
import edu.umd.cs.hcil.manynets.util.FileUtils;
import edu.umd.cs.hcil.manynets.wrapped.NbPopulationTablePanel;
import edu.umd.cs.hcil.manynets.wrapped.NbTablePanel;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.FocusManager;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SingleSelectionModel;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.Cancellable;
import org.openide.windows.WindowManager;
import prefuse.data.Graph;
import prefuse.data.expression.AndPredicate;
import prefuse.data.expression.Predicate;
import prefuse.util.collections.IntIterator;

/**
 * Top component which displays something.
 *
 * In general, a table.
 */
@ConvertAsProperties(dtd = "-//edu.umd.cs.hcil.manynets.app//Table//EN",
autostore = false)
public final class TableTopComponent extends TopComponent {

    private String history = "";
    private TablePanel tp;
    private StatsOptions selectedStats = new StatsOptions();
    private static TableTopComponent instance;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "TableTopComponent";

    public void setTable(TablePanel tp, String name) {
        if (this.tp != null) {
            this.remove(this.tp);
        }
        this.tp = tp;
        add(tp);
        setDisplayName(name);
        setIcon(tp.getBaseWrapper().getIcon().getImage());

        history = name;
        setToolTipText(history);

        if( ! tp.getBaseWrapper().getLevel().equals(Level.Network) ){
             this.jbFilterNetElements.setVisible(false);
             this.jbViewMergeSelected.setVisible(false);
             this.jbViewSelected.setVisible(false);
             this.jbExportCSV.setVisible(false);
             this.jbOpenEdges.setVisible(false);
             this.jbOpenNodes.setVisible(false);
             this.jbSplitSelected.setVisible(false);
             this.jbMergeSelected.setVisible(false);
             this.jbCreateRel.setVisible(false);
             this.jbCreateNewNet.setVisible(false);
             this.jSeparator2.setVisible(false);
             this.jSeparator3.setVisible(false);
             this.jSeparator5.setVisible(false);
        }

        // assign focus and open the new tab 
        open();
        requestActive();

    }

    public TablePanel getTablePanel() {
        return tp;
    }

    /**
     * Return a list of actions for this topComponent
     */
    private Action[] actionArray;

//    @Override
//    public Action[] getActions() {
//        if (actionArray == null) {
//            actionArray = new Action[]{
//                new AbstractAction("Build 1.5 Ego nets") {
//
//                    public void actionPerformed(ActionEvent e) {
//                        Transform pt = new SplitNetworkTransform();
//                        Transformer t = new TopCompTransformer(TableTopComponent.this);
//                        t.applyIntoNewWindow(pt, new SplitNetworkTransform.EgoNetworkOptions(1));
//                    }
//                }
//            };
//        }
//        return actionArray;
//    }

    public TableTopComponent() {

        initComponents();
        setName(NbBundle.getMessage(TableTopComponent.class, "CTL_TableTopComponent"));
        setToolTipText(NbBundle.getMessage(TableTopComponent.class, "HINT_TableTopComponent"));
           // assign focus
//        int tabPosition = getTabPosition();

//       requestFocusInWindow(false);

//       open();
//       requestActive();
//       setActivatedNodes(actionArray);

//        System.err.println("!!!!!!" + SystemAction.get(TestAction.class));
//        add(SystemAction.createToolbarPresenter(
//            new SystemAction[] {
//                SystemAction.get(PickDrawingLineAction.class),
//                SystemAction.get(PickDrawingLineAction.class),
//                SystemAction.get(TestAction.class),
//            }
//        ), java.awt.BorderLayout.PAGE_START);

//        Toolbar tb = new Toolbar();
//        tb.add(SystemAction.get(PickDrawingLineAction.class));
//        tb.add(SystemAction.get(TestAction.class));
//        add(tb, java.awt.BorderLayout.PAGE_END);
//
//        jtbTableActions.add(SystemAction.get(PickDrawingLineAction.class));
//        jtbTableActions.add(SystemAction.get(TestAction.class));
    }

    public void setFilter(Predicate p, boolean clearSelection) {
        TableWrapper tw = tp.getBaseWrapper();

        TableWrapper ctw = tw.copy();
        ctw.setName("Filtered " + tw.getName());
        ctw.setFilter(tw.isFiltered() ?
            new AndPredicate(tw.getFilter(), p) : p);
        ctw.detach();
        TableTopComponent ttc = new TableTopComponent();
        TablePanel ntp = new NbTablePanel();
        ntp.init(ctw);
        ttc.setTable(ntp, ctw.getName());
        ttc.open();

        if (clearSelection) {
            tp.getTable().getSelectionModel().clearSelection();
        }
    }


    private void viewSelected(boolean unmerged) {
        TableWrapper tw = tp.getBaseWrapper();
        Population pop = ((PopulationTableWrapper)tw).getPopulation();

        int selected[] = tp.getTable().getSelectedRows();
        Graph g = null;
        PGraph sample = pop.getGraphs().iterator().next();
        String label = sample.getName();
        if (selected.length == 0) {
            ArrayList<Graph> al = new ArrayList<Graph>();
            int mr=0;
            for (IntIterator ii = tw.getTable().rows(); ii.hasNext(); /**/) {
                int i=ii.nextInt();
                mr = tp.getTable().convertRowIndexToModel(i);
                al.add(pop.getGraph(mr).getGraph());
            }
            label= pop.getGraph(mr).getName();
            label += " and " + al.size() + " others";
            g = GraphMerger.mergeGraphs(al, unmerged);
        } else if (selected.length == 1) {
            int mr = tp.getTable().convertRowIndexToModel(selected[0]);
            g = pop.getGraph(mr).getGraph();
            label= pop.getGraph(mr).getName();
        } else {
            ArrayList<Graph> al = new ArrayList<Graph>();
            int mr=0;
            for (int i=0; i<selected.length; i++) {
                mr = tp.getTable().convertRowIndexToModel(selected[i]);
                al.add(pop.getGraph(mr).getGraph());
            }
            label= pop.getGraph(mr).getName();
            label += " and " + al.size() + " others";
            g = GraphMerger.mergeGraphs(al, unmerged);
        }

        NetworkTopComponent.displayNetwork(label, PGraph.labelField, g);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jtbTableActions = new javax.swing.JToolBar();
        jbAddColumn = new javax.swing.JButton();
        jbAddColumnExpression = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jbFilter = new javax.swing.JButton();
        jbFilterKeep = new javax.swing.JButton();
        jbFilterRemove = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jbViewSelected = new javax.swing.JButton();
        jbViewMergeSelected = new javax.swing.JButton();
        jbExportCSV = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        jbOpenNodes = new javax.swing.JButton();
        jbOpenEdges = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        jbSplitSelected = new javax.swing.JButton();
        jbFilterNetElements = new javax.swing.JButton();
        jbMergeSelected = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        jbCreateNewNet = new javax.swing.JButton();
        jbCreateRel = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jtbTableActions.setRollover(true);

        jbAddColumn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/umd/cs/hcil/manynets/icons/add-column24.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jbAddColumn, org.openide.util.NbBundle.getMessage(TableTopComponent.class, "TableTopComponent.jbAddColumn.text")); // NOI18N
        jbAddColumn.setToolTipText(org.openide.util.NbBundle.getMessage(TableTopComponent.class, "TableTopComponent.jbAddColumn.toolTipText")); // NOI18N
        jbAddColumn.setFocusable(false);
        jbAddColumn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbAddColumn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbAddColumn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbAddColumnActionPerformed(evt);
            }
        });
        jtbTableActions.add(jbAddColumn);

        jbAddColumnExpression.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/umd/cs/hcil/manynets/icons/add-column-exp24.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jbAddColumnExpression, org.openide.util.NbBundle.getMessage(TableTopComponent.class, "TableTopComponent.jbAddColumnExpression.text")); // NOI18N
        jbAddColumnExpression.setToolTipText(org.openide.util.NbBundle.getMessage(TableTopComponent.class, "TableTopComponent.jbAddColumnExpression.toolTipText")); // NOI18N
        jbAddColumnExpression.setFocusable(false);
        jbAddColumnExpression.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbAddColumnExpression.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbAddColumnExpression.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbAddColumnExpressionActionPerformed(evt);
            }
        });
        jtbTableActions.add(jbAddColumnExpression);
        jtbTableActions.add(jSeparator1);

        jbFilter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/umd/cs/hcil/manynets/icons/filter-exp24.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jbFilter, org.openide.util.NbBundle.getMessage(TableTopComponent.class, "TableTopComponent.jbFilter.text")); // NOI18N
        jbFilter.setToolTipText(org.openide.util.NbBundle.getMessage(TableTopComponent.class, "TableTopComponent.jbFilter.toolTipText")); // NOI18N
        jbFilter.setFocusable(false);
        jbFilter.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbFilter.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbFilterActionPerformed(evt);
            }
        });
        jtbTableActions.add(jbFilter);

        jbFilterKeep.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/umd/cs/hcil/manynets/icons/filter-keep-selected24.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jbFilterKeep, org.openide.util.NbBundle.getMessage(TableTopComponent.class, "TableTopComponent.jbFilterKeep.text")); // NOI18N
        jbFilterKeep.setToolTipText(org.openide.util.NbBundle.getMessage(TableTopComponent.class, "TableTopComponent.jbFilterKeep.toolTipText")); // NOI18N
        jbFilterKeep.setFocusable(false);
        jbFilterKeep.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbFilterKeep.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbFilterKeep.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbFilterKeepActionPerformed(evt);
            }
        });
        jtbTableActions.add(jbFilterKeep);

        jbFilterRemove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/umd/cs/hcil/manynets/icons/filter-discard-selected24.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jbFilterRemove, org.openide.util.NbBundle.getMessage(TableTopComponent.class, "TableTopComponent.jbFilterRemove.text")); // NOI18N
        jbFilterRemove.setToolTipText(org.openide.util.NbBundle.getMessage(TableTopComponent.class, "TableTopComponent.jbFilterRemove.toolTipText")); // NOI18N
        jbFilterRemove.setFocusable(false);
        jbFilterRemove.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbFilterRemove.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbFilterRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbFilterRemoveActionPerformed(evt);
            }
        });
        jtbTableActions.add(jbFilterRemove);
        jtbTableActions.add(jSeparator2);

        jbViewSelected.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/umd/cs/hcil/manynets/icons/view-node-link.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jbViewSelected, org.openide.util.NbBundle.getMessage(TableTopComponent.class, "TableTopComponent.jbViewSelected.text")); // NOI18N
        jbViewSelected.setToolTipText(org.openide.util.NbBundle.getMessage(TableTopComponent.class, "TableTopComponent.jbViewSelected.toolTipText")); // NOI18N
        jbViewSelected.setFocusable(false);
        jbViewSelected.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbViewSelected.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbViewSelected.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbViewSelectedActionPerformed(evt);
            }
        });
        jtbTableActions.add(jbViewSelected);

        jbViewMergeSelected.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/umd/cs/hcil/manynets/icons/view-node-link24.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jbViewMergeSelected, org.openide.util.NbBundle.getMessage(TableTopComponent.class, "TableTopComponent.jbViewMergeSelected.text")); // NOI18N
        jbViewMergeSelected.setToolTipText(org.openide.util.NbBundle.getMessage(TableTopComponent.class, "TableTopComponent.jbViewMergeSelected.toolTipText")); // NOI18N
        jbViewMergeSelected.setFocusable(false);
        jbViewMergeSelected.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbViewMergeSelected.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbViewMergeSelected.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbViewMergeSelectedActionPerformed(evt);
            }
        });
        jtbTableActions.add(jbViewMergeSelected);

        jbExportCSV.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/umd/cs/hcil/manynets/icons/export-network24.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jbExportCSV, org.openide.util.NbBundle.getMessage(TableTopComponent.class, "TableTopComponent.jbExportCSV.text")); // NOI18N
        jbExportCSV.setToolTipText(org.openide.util.NbBundle.getMessage(TableTopComponent.class, "TableTopComponent.jbExportCSV.toolTipText")); // NOI18N
        jbExportCSV.setFocusable(false);
        jbExportCSV.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbExportCSV.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbExportCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbExportCSVActionPerformed(evt);
            }
        });
        jtbTableActions.add(jbExportCSV);
        jtbTableActions.add(jSeparator5);

        jbOpenNodes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/umd/cs/hcil/manynets/icons/open-node-table24.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jbOpenNodes, org.openide.util.NbBundle.getMessage(TableTopComponent.class, "TableTopComponent.jbOpenNodes.text")); // NOI18N
        jbOpenNodes.setToolTipText(org.openide.util.NbBundle.getMessage(TableTopComponent.class, "TableTopComponent.jbOpenNodes.toolTipText")); // NOI18N
        jbOpenNodes.setFocusable(false);
        jbOpenNodes.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbOpenNodes.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbOpenNodes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbOpenNodesActionPerformed(evt);
            }
        });
        jtbTableActions.add(jbOpenNodes);

        jbOpenEdges.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/umd/cs/hcil/manynets/icons/open-edge-table24.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jbOpenEdges, org.openide.util.NbBundle.getMessage(TableTopComponent.class, "TableTopComponent.jbOpenEdges.text")); // NOI18N
        jbOpenEdges.setToolTipText(org.openide.util.NbBundle.getMessage(TableTopComponent.class, "TableTopComponent.jbOpenEdges.toolTipText")); // NOI18N
        jbOpenEdges.setFocusable(false);
        jbOpenEdges.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbOpenEdges.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbOpenEdges.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbOpenEdgesActionPerformed(evt);
            }
        });
        jtbTableActions.add(jbOpenEdges);
        jtbTableActions.add(jSeparator3);

        jbSplitSelected.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/umd/cs/hcil/manynets/icons/split24.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jbSplitSelected, org.openide.util.NbBundle.getMessage(TableTopComponent.class, "TableTopComponent.jbSplitSelected.text")); // NOI18N
        jbSplitSelected.setToolTipText(org.openide.util.NbBundle.getMessage(TableTopComponent.class, "TableTopComponent.jbSplitSelected.toolTipText")); // NOI18N
        jbSplitSelected.setFocusable(false);
        jbSplitSelected.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbSplitSelected.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbSplitSelected.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbSplitSelectedActionPerformed(evt);
            }
        });
        jtbTableActions.add(jbSplitSelected);

        jbFilterNetElements.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/umd/cs/hcil/manynets/icons/filter-net-elements24.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jbFilterNetElements, org.openide.util.NbBundle.getMessage(TableTopComponent.class, "TableTopComponent.jbFilterNetElements.text")); // NOI18N
        jbFilterNetElements.setToolTipText(org.openide.util.NbBundle.getMessage(TableTopComponent.class, "TableTopComponent.jbFilterNetElements.toolTipText")); // NOI18N
        jbFilterNetElements.setFocusable(false);
        jbFilterNetElements.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbFilterNetElements.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbFilterNetElements.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbFilterNetElementsActionPerformed(evt);
            }
        });
        jtbTableActions.add(jbFilterNetElements);

        jbMergeSelected.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/umd/cs/hcil/manynets/icons/join24.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jbMergeSelected, org.openide.util.NbBundle.getMessage(TableTopComponent.class, "TableTopComponent.jbMergeSelected.text")); // NOI18N
        jbMergeSelected.setToolTipText(org.openide.util.NbBundle.getMessage(TableTopComponent.class, "TableTopComponent.jbMergeSelected.toolTipText")); // NOI18N
        jbMergeSelected.setFocusable(false);
        jbMergeSelected.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbMergeSelected.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbMergeSelected.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbMergeSelectedActionPerformed(evt);
            }
        });
        jtbTableActions.add(jbMergeSelected);
        jtbTableActions.add(jSeparator4);

        jbCreateNewNet.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/umd/cs/hcil/manynets/icons/create-new-network24.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jbCreateNewNet, org.openide.util.NbBundle.getMessage(TableTopComponent.class, "TableTopComponent.jbCreateNewNet.text")); // NOI18N
        jbCreateNewNet.setToolTipText(org.openide.util.NbBundle.getMessage(TableTopComponent.class, "TableTopComponent.jbCreateNewNet.toolTipText")); // NOI18N
        jbCreateNewNet.setFocusable(false);
        jbCreateNewNet.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbCreateNewNet.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbCreateNewNet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbCreateNewNetActionPerformed(evt);
            }
        });
        jtbTableActions.add(jbCreateNewNet);

        jbCreateRel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/umd/cs/hcil/manynets/icons/create-new-relationship24.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jbCreateRel, org.openide.util.NbBundle.getMessage(TableTopComponent.class, "TableTopComponent.jbCreateRel.text")); // NOI18N
        jbCreateRel.setToolTipText(org.openide.util.NbBundle.getMessage(TableTopComponent.class, "TableTopComponent.jbCreateRel.toolTipText")); // NOI18N
        jbCreateRel.setFocusable(false);
        jbCreateRel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbCreateRel.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbCreateRel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbCreateRelActionPerformed(evt);
            }
        });
        jtbTableActions.add(jbCreateRel);

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(TableTopComponent.class, "TableTopComponent.jButton1.text")); // NOI18N
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jtbTableActions.add(jButton1);

        add(jtbTableActions, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    private void jbAddColumnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbAddColumnActionPerformed

        StatsSelectionDialog ssd = new StatsSelectionDialog(tp);
        ssd.setVisible(true);
        System.err.println("accepted = " + ssd.isAccepted());

        if ( ! ssd.isAccepted()) return;

        // update the table
        TableWrapper tw = tp.getBaseWrapper();
        for (Stat s : ssd.getRemoved()) {
            System.err.println("removing stat " + s);
            tw.removeStat(s);
        }
                        
        if (ssd.getRemoved().isEmpty() && ssd.getAdded().isEmpty()) return;

        calculateStats(ssd.getAdded(), false);
    }//GEN-LAST:event_jbAddColumnActionPerformed

    public void calculateStats(Collection<Stat> stats, boolean force) {
        Thread t = new Thread(new StatRunner(
            stats, force, "Calculating stats...",new Runnable() {
            public void run() {
                // save previous selection
                TableWrapper tw = tp.getBaseWrapper();
                JTable jt = tp.getTable();
                int[] rows = jt.getSelectedRows();
                for (int i=0; i<rows.length; i++) {
                    rows[i] = tw.getId(i);
                }

                // update the tablepanel's model
                System.err.println("Updating model after recalculation...");
                tp.init(tw);

                // restore previous selection (even though sorting will be lost)
                jt.getSelectionModel().setValueIsAdjusting(true);
                for (int i=0; i<rows.length; i++) {
                    int sel = tw.getRow(rows[i]);
                    jt.getSelectionModel().addSelectionInterval(sel, sel);
                }
                jt.getSelectionModel().setValueIsAdjusting(false);
            }}));
        t.start();
    }

    private class StatRunner implements Runnable, Cancellable {
        private ProgressHandle p = null;
        private boolean canceled;
        private String name;
        private Runnable andThen;
        private Collection<Stat> stats;
        private Thread runner;
        private boolean force;
        public StatRunner(Collection<Stat> stats, boolean force, String name, Runnable andThen) {
            this.name = name;
            this.stats = stats;
            this.andThen = andThen;
            this.force = force;
        }
        public void run() {
            runner = Thread.currentThread();
            TableWrapper tw = tp.getBaseWrapper();

            if (force) {
                for (Stat s: stats) {
                    int c = tw.getStatCol(s);
                    if (s.getProvider() != null && c != -1)  {
                        System.err.println("Removed stale stat " + s);
                        tw.removeStat(s);
                    }
                }
            }

            p = ProgressHandleFactory.createHandle(name, this);
            int is=0, it=0; // counters for stat, row, and total rows
            int total=stats.size() * tw.getTable().getRowCount();
            p.start(total);
            outer: for (Stat s : stats) {
                is ++;
                if (s.getProvider() == null) continue;
                p.setDisplayName("calculating stat " + 
                        s + " (" + is + " of " + stats.size() + ")");
                int lastId = -1;
                for (IntIterator ri = tw.getTable().rows(); ri.hasNext(); /**/) {
                    if (canceled) break outer;
                    int row = ri.nextInt();
                    System.err.println("Calculating " + s + " for row " + row);
                    lastId = tw.getId(row);
                    it ++;
                    p.progress("row " + it + " of " + tw.getTable().getRowCount(), it);

                    // actually calculate
                    try {
                        s.getProvider().calculate(tw, lastId);
                    } catch (Exception e) {
                        System.err.println("Error calculating stat "
                                + s + " for " + lastId);
                        e.printStackTrace();
                        tw.removeStat(s);
                        continue outer;
                    }
                }
            }
            wrapup();
        }

        public void wrapup() {
            p.finish();
            EventQueue.invokeLater(new Runnable() {
              public void run() {
                andThen.run();
              }
            });
        }

        public boolean cancel() {
            canceled = true;
            runner.stop(); // BANZAII: only way to stop huge calculations...
            wrapup();
            return true;
        }
    }

    private void exportGraphs() {

        // Ask for the file format
        JComboBox jcb = new JComboBox(new DefaultComboBoxModel(
                Format.values()));
        JOptionPane jop = new JOptionPane(jcb,
        JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog jd = jop.createDialog(this, "Choose Export Format");
        jd.pack();
        jd.setResizable(true);
        jd.setVisible(true);

        if ( ! jop.getValue().equals(JOptionPane.OK_OPTION)) {
            return;
        }
        Format f = (Format)jcb.getSelectedItem();

        // gather those graphs
        ArrayList<PGraph> pgs = new ArrayList<PGraph>();
        TableWrapper tw = tp.getBaseWrapper();
        int selected[] = tp.getTable().getSelectedRows();
        for (int i=0; i<selected.length; i++) {
            int mr = tp.getTable().convertRowIndexToModel(selected[i]);
            pgs.add(((PopulationTableWrapper)tw).getGraph(tw.getId(mr)));
        }
        if (pgs.isEmpty()) {
            pgs.addAll(((PopulationTableWrapper)tw).getPopulation().getGraphs());
        }

        // show chooser dialog and save them somewhere
        try {
            File[] ds = FileUtils.chooseFile(
                    this, new File("/"), "Choose destination dir",
                    false, JFileChooser.DIRECTORIES_ONLY, false);
            if (ds == null) return;

            if ( ! f.getFormat().export(pgs, ds[0])) {
                System.err.println("bad luck... " + f.getFormat().getError());
            }                
        } catch (Exception e) {
            System.err.println("export failed");
        }
    }

    private void jbAddColumnExpressionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbAddColumnExpressionActionPerformed
        PythonPanel pp = new PythonPanel(tp.getBaseWrapper().getTable(),
                "new column");
        JOptionPane jop = new JOptionPane(pp,
                JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog jd = jop.createDialog(this, "New column expression");
        jd.setSize(600,300);
        jd.setResizable(true);
        jd.setVisible(true);

        if ((Integer)jop.getValue() != JOptionPane.CANCEL_OPTION) {
            Level l = tp.getBaseWrapper().getLevel();
            Stat s = new Stat(pp.getColName(), pp.getText(), pp.getColType(), l, l);
            new ExpressionCalculator(s, pp.getText(), "O(1)");
            ArrayList<Stat> al = new ArrayList<Stat>();
            al.add(s);
            calculateStats(al, false);
        }
    }//GEN-LAST:event_jbAddColumnExpressionActionPerformed

    private void jbFilterRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbFilterRemoveActionPerformed
        TableWrapper tw = tp.getBaseWrapper();
        int selected[] = tp.getTable().getSelectedRows();
        TreeSet<Integer> selectedIds = new TreeSet<Integer>();
        for (int i=0; i<selected.length; i++) {
            int mr = tp.getTable().convertRowIndexToModel(selected[i]);
            selectedIds.add(tw.getId(mr));
        }
        Predicate p = new TableWrapper.SetPredicate(
                selectedIds, tw.getIdField(), true);
        setFilter(p, true);
    }//GEN-LAST:event_jbFilterRemoveActionPerformed

    private void jbViewSelectedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbViewSelectedActionPerformed

        viewSelected(true);
    }//GEN-LAST:event_jbViewSelectedActionPerformed

    private void jbFilterKeepActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbFilterKeepActionPerformed
        TableWrapper tw = tp.getBaseWrapper();
        int selected[] = tp.getTable().getSelectedRows();
        TreeSet<Integer> selectedIds = new TreeSet<Integer>();
        for (int i=0; i<selected.length; i++) {
            int mr = tp.getTable().convertRowIndexToModel(selected[i]);
            selectedIds.add(tw.getId(mr));
        }
        
        Predicate p = new TableWrapper.SetPredicate(
                selectedIds, tw.getIdField(), false);
        setFilter(p, true);
    }//GEN-LAST:event_jbFilterKeepActionPerformed

    private void jbFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbFilterActionPerformed
        PythonFilterDialog pfd = new PythonFilterDialog(null, tp, 1, 
            false, false, null);
        pfd.setSize(600, 300);
        pfd.setLocation(300, 300);
        pfd.setTitle("Expression to filter on");
        pfd.setVisible(true);

        if ( ! pfd.wasCancelled()) {
            Level l = tp.getBaseWrapper().getLevel();
            Predicate p = new TableWrapper.ExpressionPredicate(
                    pfd.getText(0));
            setFilter(p, false);
        }
    }//GEN-LAST:event_jbFilterActionPerformed

    private void jbSplitSelectedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbSplitSelectedActionPerformed
        // TODO add your handling code here:

        TableWrapper tw = tp.getBaseWrapper();

        int selected[] = tp.getTable().getSelectedRows();
        final TreeSet<Integer> selectedIds = new TreeSet<Integer>();
        for (int i=0; i<selected.length; i++) {
            int mr = tp.getTable().convertRowIndexToModel(selected[i]);
            selectedIds.add(tw.getId(mr));
        }
        PGraph pg = selectedIds.isEmpty() ? null :
            ((PopulationTableWrapper)tw).getPopulation()
                .getGraph(selectedIds.first());

        SplitDialog sd = new SplitDialog(null, getTablePanel(), pg);
        sd.setLocationRelativeTo(this);
        sd.setVisible(true);
        if ( ! sd.isAccepted()) return;


        final Transform pt = sd.getTransform();
        if (selectedIds.size() > 0) {
            pt.restrictTo(selectedIds);
        }
        ProgressTransformer tr = new ProgressTransformer(
                this, tp.getBaseWrapper(), pt, pt.getOptions(),
                new Runnable() {
            public void run() {
                TableTopComponent next = new TableTopComponent();
                NbPopulationTablePanel nptp = new NbPopulationTablePanel();
                nptp.setPopulation(
                        ((PopulationTableWrapper)pt.getResultWrapper())
                        .getPopulation());
                next.setTable(nptp, pt.getOptions().getDescription());
                next.history = history + " > " +
                        pt.getOptions().getDescription();
                next.setToolTipText(history);
                next.open();

                System.err.println(Thread.currentThread().getName());
//                next.showStatsSelectionDialog();
                ArrayList<Stat> stats = tp.getBaseWrapper().getStats();
                // may want to remove calculator-less stats
                next.calculateStats(stats, false);
            }
        });
        tr.apply();

    }//GEN-LAST:event_jbSplitSelectedActionPerformed

    private void jbMergeSelectedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbMergeSelectedActionPerformed
        /**
         * Merge selected networks (or all networks, if none selected) into one.
         */

        TableWrapper tw = tp.getBaseWrapper();

        int selected[] = tp.getTable().getSelectedRows();
        final TreeSet<Integer> selectedIds = new TreeSet<Integer>();
        for (int i=0; i<selected.length; i++) {
            int mr = tp.getTable().convertRowIndexToModel(selected[i]);
            selectedIds.add(tw.getId(mr));
        }

        final Transform pt = new MergeTransform();
        TransformOptions o = null;
        if (selectedIds.size() > 0) {
            pt.restrictTo(selectedIds);
        }
        ProgressTransformer tr = new ProgressTransformer(
                this, tp.getBaseWrapper(), pt, o,new Runnable() {
            @Override
            public void run() {
                TableTopComponent next = new TableTopComponent();
                NbPopulationTablePanel nptp = new NbPopulationTablePanel();
                nptp.setPopulation(
                        ((PopulationTableWrapper)pt.getResultWrapper())
                        .getPopulation());
                next.setTable(nptp, pt.getOptions().getDescription());
                next.open();

                System.err.println(Thread.currentThread().getName());
//                next.showStatsSelectionDialog();
                ArrayList<Stat> stats = tp.getBaseWrapper().getStats();
                // may want to remove calculator-less stats
                next.calculateStats(stats, false);
            }
        });
        tr.apply();

    }//GEN-LAST:event_jbMergeSelectedActionPerformed

    private void jbCreateNewNetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbCreateNewNetActionPerformed
        final JDialog jd = new JDialog((Frame)null, "Create New Network");
        final NetworkCreationPanel ncp = new NetworkCreationPanel();
        ArrayList<TableWrapper> others = new ArrayList<TableWrapper>();
        ArrayList<String> names = new ArrayList<String>();

        TableTopComponent popttc = null;
        for (TopComponent tc : getRegistry().getOpened()) {
            if (tc instanceof TableTopComponent) {
                TableWrapper tw = ((TableTopComponent)tc)
                        .getTablePanel().getBaseWrapper();
                if (tw.getLevel().equals(Level.Relationship)) {
                    others.add(tw);
                    names.add(((RelTableWrapper)tw).getSchemaInstance().getName() +
                            ":" + tw.getName());
                } else if (tw.getLevel().equals(Level.Network)) {
                    popttc = (TableTopComponent)tc;
                    System.err.println("Found graph: " + tw.getName());
                } else {
                    System.err.println("Ignoring " + tw.getName() + " with level " + tw.getLevel());
                }
            }
        }
        final TableTopComponent pttc = popttc;

        ncp.setCandidateRelationships(others, names);
        ncp.jbAccept.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                PopulationTableWrapper ptw = (PopulationTableWrapper)pttc
                        .getTablePanel().getBaseWrapper();
                ArrayList<RelTableWrapper> rels = new ArrayList<RelTableWrapper>();
                if(ncp.getSelected()!=null){
                for (TableWrapper t : ncp.getSelected()) {
                    if (t instanceof RelTableWrapper) {
                        rels.add((RelTableWrapper)t);
                    } else {
                        System.err.println("Must think of way of adding a filtered table...");
                    }
                }
                PGraph pg = new PGraph(ncp.getNetName(), rels, ncp.elideSelected());
                ptw.getPopulation().addGraph(pg);
                ArrayList<Stat> stats = ptw.getStats();
                pttc.calculateStats(stats, true);
                jd.dispose();
                } else{
                    JOptionPane.showMessageDialog(null, "Please select relationships to create network.");
                }
            }
        });
        ncp.jbCancel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                jd.dispose();
            }
        });
        jd.add(ncp);
        jd.setSize(400, 300);
        jd.setLocationRelativeTo(this);
        jd.setVisible(true);
    }//GEN-LAST:event_jbCreateNewNetActionPerformed

    private void jbOpenEdgesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbOpenEdgesActionPerformed
        TableWrapper tw = tp.getBaseWrapper();
        int selected[] = tp.getTable().getSelectedRows();
        for (int i=0; i<selected.length; i++) {
            int mr = tp.getTable().convertRowIndexToModel(selected[i]);
            PGraph pg = ((PopulationTableWrapper)tw).getGraph(tw.getId(mr));
            ((NbPopulationTablePanel)tp).createTableDialog(
                    "Edge Table for " + pg.getName(), "ID", pg.getEdgeTable());
        }
    }//GEN-LAST:event_jbOpenEdgesActionPerformed

    private void jbOpenNodesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbOpenNodesActionPerformed
        TableWrapper tw = tp.getBaseWrapper();
        int selected[] = tp.getTable().getSelectedRows();
        for (int i=0; i<selected.length; i++) {
            int mr = tp.getTable().convertRowIndexToModel(selected[i]);
            PGraph pg = ((PopulationTableWrapper)tw).getGraph(tw.getId(mr));
            ((NbPopulationTablePanel)tp).createTableDialog(
                    // FIXME: removed for screenshots
                    // "Node Table for " +
                    pg.getName(), "ID", pg.getNodeTable());
        }
    }//GEN-LAST:event_jbOpenNodesActionPerformed

    private void jbViewMergeSelectedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbViewMergeSelectedActionPerformed
        viewSelected(false);
    }//GEN-LAST:event_jbViewMergeSelectedActionPerformed

    private void jbCreateRelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbCreateRelActionPerformed
        TableWrapper tw = tp.getBaseWrapper();

        int selected[] = tp.getTable().getSelectedRows();
        final TreeSet<Integer> selectedIds = new TreeSet<Integer>();
        for (int i=0; i<selected.length; i++) {
            int mr = tp.getTable().convertRowIndexToModel(selected[i]);
            selectedIds.add(tw.getId(mr));
        }
        PGraph pg = null;
        for (int i=0; i<selected.length; i++) {
            int mr = tp.getTable().convertRowIndexToModel(selected[i]);
            pg = ((PopulationTableWrapper)tw).getGraph(tw.getId(mr));
            break;
        }
        if (pg == null) {
            return;
        }
        
        RelationshipBuilderUI rb = new RelationshipBuilderUI(pg);
        JOptionPane jop = new JOptionPane(rb,
                JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog jd = jop.createDialog(this, "Relationship Builder");
        jd.pack();
        jd.setResizable(true);
        jd.setVisible(true);


        if (jop.getValue() != null &&
                (Integer)jop.getValue() != JOptionPane.CANCEL_OPTION) {
            TableTopComponent ttc = new TableTopComponent();
            TablePanel ntp = new TablePanel();
            RelTableWrapper rtw = rb.getRelationship();
            ntp.init(rtw);
            ttc.setTable(ntp, rtw.getName());
            ttc.open();
        }
    }//GEN-LAST:event_jbCreateRelActionPerformed

    private void jbExportCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbExportCSVActionPerformed
        exportGraphs();
    }//GEN-LAST:event_jbExportCSVActionPerformed

    private void jbFilterNetElementsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbFilterNetElementsActionPerformed

        TableWrapper tw = tp.getBaseWrapper();

        int selected[] = tp.getTable().getSelectedRows();
        final TreeSet<Integer> selectedIds = new TreeSet<Integer>();
        for (int i=0; i<selected.length; i++) {
            int mr = tp.getTable().convertRowIndexToModel(selected[i]);
            selectedIds.add(tw.getId(mr));
        }
        
        ElementFilter ef = new ElementFilter(
                ((PopulationTableWrapper)tw).getGraph(selectedIds.isEmpty() ?
                    ((PopulationTableWrapper)tw).getId(0) :
                    selectedIds.first()));
        JOptionPane jop = new JOptionPane(ef,
                JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog jd = jop.createDialog(this, "Filter elements");
        jd.pack();
        jd.setResizable(true);
        jd.setVisible(true);

        if (jop.getValue() != null &&
                (Integer)jop.getValue() != JOptionPane.CANCEL_OPTION) {
            System.err.println("expressions are: "
                    + ef.getNodeExp() + "/" + ef.isRemoveNodeMatches() + "//"
                    + ef.getEdgeExp() + "/" + ef.isRemoveEdgeMatches() + "//");
        } else {
            return;
        }

        final Transform pt = ef.getTransform();
        if (selectedIds.size() > 0) {
            pt.restrictTo(selectedIds);
        }
        ProgressTransformer tr = new ProgressTransformer(
                this, tp.getBaseWrapper(), pt, ef.getTransformOptions(),
                new Runnable() {
            public void run() {
                TableTopComponent next = new TableTopComponent();
                NbPopulationTablePanel nptp = new NbPopulationTablePanel();
                nptp.setPopulation(
                        ((PopulationTableWrapper)pt.getResultWrapper())
                        .getPopulation());
                next.setTable(nptp, pt.getOptions().getDescription());
                next.history = history + " > " +
                        pt.getOptions().getDescription();
                next.setToolTipText(history);
                next.open();

                System.err.println(Thread.currentThread().getName());
//                next.showStatsSelectionDialog();
                ArrayList<Stat> stats = tp.getBaseWrapper().getStats();
                // may want to remove calculator-less stats
                next.calculateStats(stats, false);
            }
        });
        tr.apply();

    }//GEN-LAST:event_jbFilterNetElementsActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        String out = JOptionPane.showInputDialog("Desired title for window",
                getDisplayName());
        if (out != null && out.length() > 0) {
            tp.getBaseWrapper().setName(out);
            setDisplayName(out);
        }
    }//GEN-LAST:event_jButton1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JButton jbAddColumn;
    private javax.swing.JButton jbAddColumnExpression;
    private javax.swing.JButton jbCreateNewNet;
    private javax.swing.JButton jbCreateRel;
    private javax.swing.JButton jbExportCSV;
    private javax.swing.JButton jbFilter;
    private javax.swing.JButton jbFilterKeep;
    private javax.swing.JButton jbFilterNetElements;
    private javax.swing.JButton jbFilterRemove;
    private javax.swing.JButton jbMergeSelected;
    private javax.swing.JButton jbOpenEdges;
    private javax.swing.JButton jbOpenNodes;
    private javax.swing.JButton jbSplitSelected;
    private javax.swing.JButton jbViewMergeSelected;
    private javax.swing.JButton jbViewSelected;
    private javax.swing.JToolBar jtbTableActions;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized TableTopComponent getDefault() {
        if (instance == null) {
            instance = new TableTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the TableTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized TableTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(TableTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof TableTopComponent) {
            return (TableTopComponent) win;
        }
        Logger.getLogger(TableTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID +
                "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        TableTopComponent singleton = TableTopComponent.getDefault();
        singleton.readPropertiesImpl(p);
        return singleton;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }
}
