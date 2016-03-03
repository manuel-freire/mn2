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

import edu.umd.cs.hcil.manynets.app.TableTopComponent;
import edu.umd.cs.hcil.manynets.model.DatasetLoader;
import edu.umd.cs.hcil.manynets.model.Population;
import edu.umd.cs.hcil.manynets.model.TableWrapper;
import edu.umd.cs.hcil.manynets.ui.TablePanel;

/**
 * Overloads DatasetLoader to open each table in a Netbeans TableTopComponent
 * tab.
 * @author mfreire
 */
public class NbLoader extends DatasetLoader {

    @Override
    public void openTableComponent(TableWrapper tw, String name) {
        TableTopComponent ttc = new TableTopComponent();
        TablePanel tp = new NbTablePanel();
        tp.init(tw);
        ttc.setTable(tp, name);
        ttc.open();
    }

    @Override
    public void openTableComponent(Population pop, String name) {
        TableTopComponent prev = TableTopComponent.findInstance();
        TableTopComponent ttc = (prev.getTablePanel() == null) ?
            prev : new TableTopComponent();
        NbPopulationTablePanel tp = new NbPopulationTablePanel();
        tp.setPopulation(pop);
        ttc.setTable(tp, name);
        ttc.open();
    }
}
