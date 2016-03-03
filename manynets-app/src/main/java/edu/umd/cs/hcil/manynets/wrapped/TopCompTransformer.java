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
import edu.umd.cs.hcil.manynets.app.TableTopComponent;
import edu.umd.cs.hcil.manynets.model.TableWrapper;
import edu.umd.cs.hcil.manynets.model.Transform;
import edu.umd.cs.hcil.manynets.model.TransformOptions;
import edu.umd.cs.hcil.manynets.ui.TablePanel;

/**
 * A ManyNets transformer that outputs new windows into TopComponents.
 * @author Manuel Freire
 */
public class TopCompTransformer {

    private TableTopComponent ttc;
    private Population gp;
    
    public TopCompTransformer(TableTopComponent ttc) {
        this.ttc = ttc;
        if (ttc.getTablePanel() != null) {
            gp = ((NbPopulationTablePanel)ttc.getTablePanel()).getPopulation();
        }
        if (gp == null) {
            gp = new Population("Transformed");
        }
    }

    public TableWrapper getWrapper() {
        return gp.getWrappedAttributes();
    }

    public TablePanel getTablePanel() {
        return ttc.getTablePanel();
    }

    public void applyIntoNewWindow(final Transform pt,
        TransformOptions ops) {

        System.err.println("Applying into new window...");
//        apply(pt, ops, new Runnable() {
//
//            @Override
//            public void run() {
//                throw new UnsupportedOperationException("NYI");
////                TableTopComponent next = new TableTopComponent();
////                NbPopulationTablePanel nptp = new NbPopulationTablePanel();
////                nptp.setPopulation(pt.getResultPopulation());
////                next.setTable(nptp, pt.getOptions().getDescription());
////                System.err.println(Thread.currentThread().getName());
//////                next.showStatsSelectionDialog();
////                next.recalculate(next.getTablePanel());
//
//            }
//        });
    }

    public void applyIntoSameWindow(final Transform pt,
        final TransformOptions ops) {
        
//        apply(pt, ops, new Runnable() {
//
//            @Override
//            public void run() {
//                throw new UnsupportedOperationException("NYI");
////                ((PopulationTablePanel)ttc.getTablePanel())
////                        .setPopulation(pt.getResultPopulation());
//            }
//        });
    }
}
