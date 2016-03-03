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

package edu.umd.cs.hcil.manynets.actions;

import edu.umd.cs.hcil.manynets.app.ProgressLoader;
import edu.umd.cs.hcil.manynets.app.TableTopComponent;
import edu.umd.cs.hcil.manynets.app.ui.OpenDatasetDialog;
import edu.umd.cs.hcil.manynets.model.DatasetLoader;
import edu.umd.cs.hcil.manynets.wrapped.NbLoader;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.ErrorManager;

public final class OpenFileAction implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        OpenDatasetDialog ofd = new OpenDatasetDialog((Frame)null, true);
        ofd.setLocationRelativeTo(null);
        ofd.setVisible(true);
        if ( ! ofd.wasAccepted()) {
            return;
        }

        try {
            DatasetLoader dl = new NbLoader();
            dl.prepareLoad(ofd.getDescriptor(), ofd.openEntities(), ofd.openRels(),
                    ofd.graphPerRel(), ofd.graphAllRels(),
                    ofd.defaultStats(), ofd.getInstances());
            ProgressLoader pl = new ProgressLoader(new TableTopComponent(),
                    dl, "Loading " + ofd.getDescriptor().getName());
            pl.start();
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
}
