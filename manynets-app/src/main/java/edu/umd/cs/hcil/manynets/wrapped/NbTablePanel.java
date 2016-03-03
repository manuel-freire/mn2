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

import edu.umd.cs.hcil.manynets.app.DetailsTopComponent;
import edu.umd.cs.hcil.manynets.model.TableWrapper;
import edu.umd.cs.hcil.manynets.ui.TablePanel;
import javax.swing.JPopupMenu;
import org.jdesktop.swingx.JXTable;

/**
 * A TablePanel that adds Netbeans-Platform specific code to a TablePanel
 * @author Manuel Freire
 */
public class NbTablePanel extends TablePanel {

    @Override
    public void init(TableWrapper tw) {
        super.init(tw);

        // register with detail-listener
        DetailsTopComponent dtc = DetailsTopComponent.findInstance();
        dtc.listenTo(this);
    }

    @Override
    public JPopupMenu createPopupMenu(JXTable t, int row, int col) {
        return super.createPopupMenu(t, row, col);
    }
}
