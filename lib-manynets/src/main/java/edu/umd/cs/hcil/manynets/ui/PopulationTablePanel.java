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

import edu.umd.cs.hcil.manynets.model.PGraph;
import edu.umd.cs.hcil.manynets.model.Population;
import edu.umd.cs.hcil.manynets.model.TableWrapper;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JPopupMenu;
import org.jdesktop.swingx.JXTable;
import prefuse.data.Graph;

/**
 * A TablePanel that displays populations of networks
 * @author awalin sopan
 */
public class PopulationTablePanel extends TablePanel {

    protected Population pop;

    public void setPopulation(Population pop) {
        this.pop = pop; 
        init(pop.getWrappedAttributes());
    }

    /**
     * Display TablePanel with the given table, within a dialog or window
     * @param title
     * @param t
     */
    public void createTableDialog(String title, String idField, TableWrapper tw) {
        JDialog jd = new JDialog((Frame)null, title);
        jd.setLayout(new BorderLayout());
        TablePanel tp = new TablePanel();
        // Fixme: ugly and probably unnecessary
        tp.init(tw);
        jd.add(tp);
        jd.validate();
        jd.setModal(false);
        jd.setSize(800,600);
        jd.setVisible(true);
    }

    /**
     * Display a Network, within a dialog or window
     */
    public void createNetworkDialog(String title, String labelField, Graph g) {
        // PopulationGui.openInSA(title, labelField, g);
    }

    public String getLabel(int modelRow) {
        return tablew.getTuple(tablew.getId(modelRow))
                .getString(Population.labelField);
    }

    @Override
    public JPopupMenu createPopupMenu(JXTable t, int viewRow, int viewCol) {
        Graph g = null;

        int modelRow = jxt.convertRowIndexToModel(viewRow);
        final String label = getLabel(modelRow);
        final PGraph pg = pop.getGraph(tablew.getId(modelRow));

        if (pg == null) {
            return null;
        }
        
        JPopupMenu popup = new JPopupMenu();
        popup.add(new AbstractAction("Open Edges") {

            @Override
            public void actionPerformed(ActionEvent e) {
                createTableDialog("Edge Table for " + label, "ID", pg.getEdgeTable());
            }
        });
        popup.add(new AbstractAction("Open Nodes") {

            @Override
            public void actionPerformed(ActionEvent e) {
                createTableDialog("Node Table for " + label, 
                        pg.getGraph().getNodeKeyField(), pg.getNodeTable());
            }
        });

        return popup;
    }

    @Override
    public void showDetails(JXTable t, int viewRow, int viewCol) {
        int modelRow = jxt.convertRowIndexToModel(viewRow);
        String label = getLabel(modelRow);
        PGraph pg = pop.getGraph(tablew.getId(modelRow));

        // FIXME - @ stuff is leftover from VAST'09
        if (label.contains("@")) {
            label = label.substring(label.indexOf('@'));
        }
        System.err.println("Graph is ... " + pg.getGraph() + " label: " + label);

        createNetworkDialog(label, pg.getNodeLabelField(), pg.getGraph());
    }
}
