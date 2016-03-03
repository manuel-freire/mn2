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

package edu.umd.cs.hcil.manynets.wrapped;

import edu.umd.cs.hcil.manynets.model.Population;
import edu.umd.cs.hcil.manynets.app.DetailsTopComponent;
import edu.umd.cs.hcil.manynets.app.NetworkTopComponent;
import edu.umd.cs.hcil.manynets.app.TableTopComponent;
import edu.umd.cs.hcil.manynets.model.PGraph;
import edu.umd.cs.hcil.manynets.model.TableWrapper;
import edu.umd.cs.hcil.manynets.ui.PopulationTablePanel;
import javax.swing.JPopupMenu;
import org.jdesktop.swingx.JXTable;
import prefuse.data.Graph;

/**
 * A PopulationTablePanel that lives within a Netbeans TopComponent
 * @author Manuel Freire
 */
public class NbPopulationTablePanel extends PopulationTablePanel {

    public Population getPopulation() {
        return pop;
    }

    @Override
    public void setPopulation(Population gp) {
        super.setPopulation(gp);

        // register with detail-listener
        DetailsTopComponent dtc = DetailsTopComponent.findInstance();
        dtc.listenTo(this);
    }

    @Override
    public void createTableDialog(String title, String idField, TableWrapper tw) {
        NbTablePanel ntp = new NbTablePanel();
        ntp.init(tw);
        TableTopComponent ttc = new TableTopComponent();
        ttc.setTable(ntp, title);
        ttc.open();
    }

    @Override
    public JPopupMenu createPopupMenu(JXTable t, int row, int col) {
        return super.createPopupMenu(t, row, col);
    }

    @Override
    public void createNetworkDialog(String title, String labelField, Graph g) {
        NetworkTopComponent.displayNetwork(title, labelField, g);
    }

    @Override
    public void showDetails(JXTable t, int viewRow, int col) {
        int modelRow = jxt.convertRowIndexToModel(viewRow);
        String label = getLabel(modelRow);
        PGraph pg = pop.getGraph(tablew.getId(modelRow));

        NetworkTopComponent
                .displayNetwork(label, PGraph.labelField, pg.getGraph());
    }
}
