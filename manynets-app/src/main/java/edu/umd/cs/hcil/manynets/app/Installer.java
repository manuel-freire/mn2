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

import edu.umd.cs.hcil.manynets.app.ui.LowMemoryPopup;
import edu.umd.cs.hcil.manynets.model.DatasetLoader;
import edu.umd.cs.hcil.manynets.util.FileUtils;
import edu.umd.cs.hcil.manynets.wrapped.NbLoader;
import java.io.File;
import java.util.StringTokenizer;
import org.openide.ErrorManager;
import org.openide.modules.ModuleInstall;

/**
 * Provides bootstrapping for the ManyNets App module. Not strictly necessary
 * unless quicker loading is desired.
 */
public class Installer extends ModuleInstall {

    @Override
    public void restored() {

        // avoid certain noisy log messages
        System.err.println("ManyNets initializing ...");
        System.getProperties().setProperty("log4j.configuration",
                "edu/umd/cs/hcil/manynets/app/log4j.xml");

        // start the low-memory-warning
        LowMemoryPopup lmp = new LowMemoryPopup(20 * 1024 * 1024, 3);
        lmp.startTimer(1000);

        /*
         * try to find a file named "demo.txt" in the local directory. If found,
         * read the demo location from the first line in that file.
         */
        final String datasetName = locateDemo(new File("demo.txt"));

        if (datasetName != null)
            java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {                                               
                System.err.println("Launching demo: " + datasetName);
                try {                    
                    File f = new File(datasetName);
                    if ( ! f.canRead()) {
                        System.err.println("Cannot read demo file: not there?");
                        return;
                    }

                    DatasetLoader dl = new NbLoader();
                    dl.prepareLoad(DatasetLoader.fileToDescriptor(f, false),
                            // false, false, true, false, true, null);
                            true, true, true, false, true, null);
                    ProgressLoader pl = new ProgressLoader(new TableTopComponent(),
                            dl, "Loading " + datasetName);
                    pl.start();
                } catch (Exception e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        });
    }

    /**
     * Returns the first line of demoConfigFile that is not empty, ignoring
     * anything after a "#" comment character and leading and trailing
     * whitespace.
     * @param demoConfigFile
     * @return
     */
    private String locateDemo(File demoConfigFile) {
        if ( ! demoConfigFile.exists() || ! demoConfigFile.canRead()) {
            return null;
        }
        try {
            String s = FileUtils.readFileToString(demoConfigFile);
            StringTokenizer st = new StringTokenizer(s, "\n");
            while (st.hasMoreElements()) {
                String l = st.nextToken().replaceFirst("[#].*", "").trim();
                if (l.length() > 0) {
                    return l;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
