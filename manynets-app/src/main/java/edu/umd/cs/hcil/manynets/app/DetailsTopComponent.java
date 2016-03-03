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

import edu.umd.cs.hcil.manynets.model.NodeRef;
import edu.umd.cs.hcil.manynets.model.PGraph;
import edu.umd.cs.hcil.manynets.model.PGraph.NodeTableWrapper;
import edu.umd.cs.hcil.manynets.model.Ref;
import edu.umd.cs.hcil.manynets.ui.SelectedRowPanel;
import edu.umd.cs.hcil.manynets.ui.SelectedRowPanel.CellClickedInfo;
import edu.umd.cs.hcil.manynets.ui.TablePanel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.TreeMap;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
//import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;

/**
 * Displays a currently selected cell. More than one can exist
 */
@ConvertAsProperties(
    dtd="-//edu.umd.cs.hcil.manynets.app//Details//EN",
    autostore=false
)
public final class DetailsTopComponent extends TopComponent
        implements PropertyChangeListener {

    private TablePanel last;
    private SelectedRowPanel srp;
    private static TreeMap<String, TablePanel> panelMap =
            new TreeMap<String, TablePanel>();

    private static DetailsTopComponent instance;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";

    private static final String PREFERRED_ID = "DetailsTopComponent";

    public DetailsTopComponent() {
        initComponents();
        srp = new SelectedRowPanel();
        add(srp);
        setName(NbBundle.getMessage(DetailsTopComponent.class, "CTL_DetailsTopComponent"));
        setToolTipText(NbBundle.getMessage(DetailsTopComponent.class, "HINT_DetailsTopComponent"));
//        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
    }

    public void listenTo(TablePanel tp) {
        last = tp;
        srp.listenTo(tp);
        tp.addPropertyChangeListener(TablePanel.CELL_CLICKED_PROPERTY, 
                (PropertyChangeListener)this);
        String name = tp.getBaseWrapper().getName();
        if ( ! panelMap.containsKey(name)) {
            panelMap.put(name, tp);
            jcbDestination.setModel(new DefaultComboBoxModel());
            for (String s : panelMap.keySet()) {
                ((DefaultComboBoxModel)jcbDestination.getModel()).addElement(s);
            }
        }
    }

    public static void transfer(TablePanel from, TablePanel to) {
        ListSelectionModel lsm = to.getTable().getSelectionModel();
        lsm.clearSelection();
        lsm.setValueIsAdjusting(true);
        int rows[] = from.getTable().getSelectedRows();
        System.err.println("Transferring " + rows.length + " rows");
        NodeTableWrapper snt = (NodeTableWrapper)from.getBaseWrapper();
        NodeTableWrapper dnt = (NodeTableWrapper)to.getBaseWrapper();
        PGraph dpg = dnt.getPGraph();
        for (int r : rows) {
            int sr = -1, dr = -1;
            try {
                sr = from.getTable().convertRowIndexToModel(r);
                NodeRef sn = (NodeRef)snt.getRef(snt.getId(sr));
                Ref ser = sn.getEntityRef();
                int dnid = dpg.getNodeId(ser);
                dr = dnt.getRow(dnid);

                if (dr >= 0) {
                    lsm.addSelectionInterval(dr, dr);
                }
            } catch (Exception e) {
                System.err.println("Error transfering view row " + r + " (" +
                        sr + " " + dr + ")");
                e.printStackTrace();
            }
        }
        lsm.setValueIsAdjusting(false);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(TablePanel.CELL_CLICKED_PROPERTY)) {
            last = ((CellClickedInfo)evt.getNewValue()).getTablePanel();
        }
    }

    public class TransferHandler implements ListSelectionListener {
        private TablePanel from, to;
        private TransferHandler(TablePanel from, TablePanel to) {
            this.from = from; this.to = to;
        }
        public void valueChanged(ListSelectionEvent e) {
            if ( ! e.getValueIsAdjusting()) {
                transfer(from, to);
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jpPaste = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jcbDestination = new javax.swing.JComboBox();
        jbConfirm = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DetailsTopComponent.class, "DetailsTopComponent.jLabel1.text")); // NOI18N
        jpPaste.add(jLabel1);

        jpPaste.add(jcbDestination);

        org.openide.awt.Mnemonics.setLocalizedText(jbConfirm, org.openide.util.NbBundle.getMessage(DetailsTopComponent.class, "DetailsTopComponent.jbConfirm.text")); // NOI18N
        jbConfirm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbConfirmActionPerformed(evt);
            }
        });
        jpPaste.add(jbConfirm);

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    private void jbConfirmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbConfirmActionPerformed
        int[] selViews = last.getTable().getSelectedRows();
        TablePanel dest = panelMap.get((String)jcbDestination.getSelectedItem());
        if (selViews != null && selViews.length > 0 && dest != last) {
            System.err.println("Will tranfer from " + last.getBaseWrapper().getName() + " to " +
                    dest.getBaseWrapper().getName());
            TransferHandler th = new TransferHandler(last, dest);
            transfer(last, dest);
            last.getTable().getSelectionModel().addListSelectionListener(th);
        }
    }//GEN-LAST:event_jbConfirmActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton jbConfirm;
    private javax.swing.JComboBox jcbDestination;
    private javax.swing.JPanel jpPaste;
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized DetailsTopComponent getDefault() {
        if (instance == null) {
            instance = new DetailsTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the DetailsTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized DetailsTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(DetailsTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof DetailsTopComponent) {
            return (DetailsTopComponent) win;
        }
        Logger.getLogger(DetailsTopComponent.class.getName()).warning(
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
        DetailsTopComponent singleton = DetailsTopComponent.getDefault();
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
