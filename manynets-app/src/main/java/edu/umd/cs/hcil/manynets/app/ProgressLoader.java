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

import edu.umd.cs.hcil.manynets.model.Dataset.DatasetListener;
import edu.umd.cs.hcil.manynets.model.DatasetLoader;
import edu.umd.cs.hcil.manynets.model.DatasetLoader.StatCalculationMonitor;
import java.awt.EventQueue;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Cancellable;
import org.openide.windows.TopComponent;

/**
 *
 * @author Manuel Freire
 */
public class ProgressLoader implements Runnable, Cancellable {

    private Runnable whenFinished;

    private ProgressHandle ph = null;
    private Thread runner;
    private boolean cancelled;
    private final TopComponent tc;
    private String description;
    private StringBuilder log = new StringBuilder();

    private DatasetLoader loader;

    public ProgressLoader(TopComponent tc, final DatasetLoader loader, String description) {
        this.tc = tc;
        this.description = description;
        this.loader = loader;
        whenFinished = new Runnable() {
            @Override
            public void run() {
                loader.doDisplay();
            }
        };
    }

    public void start() {
        (new Thread(this)).start();
    }

    public String append(String text) {
        log.append(text + "\n");
        return text;
    }

    private class ProgressDatasetListener implements DatasetListener {
        public void notifySchemaOk() {
            ph.setDisplayName(append("Schema read ok"));
        }

        public void notifyInstanceCount(int n) {
            ph.setDisplayName(append("Instances = " + n));
        }

        public void notifyInstanceStart(String in, int nt) {
            ph.setDisplayName(append("Instance " + in + " has " + nt + " tables"));
        }

        public void notifyInstanceProgress(String in, String en, int ni) {
            ph.setDisplayName(append("Instance " + in + " table "
                    + en + " (" + ni + ")"));
        }
    }

    private class StatProgressListener implements StatCalculationMonitor {
        private String lastStat = "";

        @Override
        public void notify(String statName, int row) {
            if ( ! statName.equals(lastStat)) {
                lastStat = statName;
                append(statName);
            }
            ph.setDisplayName(statName + " row " + row);
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }
    }

    @Override
    public void run() {
        runner = Thread.currentThread();
        ph = ProgressHandleFactory.createHandle(
                description, this);
        ph.start();
        try {
            loader.setStatMonitor(new StatProgressListener());
            loader.doLoad(new ProgressDatasetListener());
        } catch (Exception e) {
            JTextArea jta = new JTextArea(10, 80);
            jta.setText("The dataset could not be loaded:\n" +
                    e.getMessage() + "\n" +
                    log.toString());
            System.err.println("Error loading dataset\n" + log.toString());
            e.printStackTrace();
            jta.setEditable(false);
            JOptionPane.showMessageDialog(tc, jta, 
                "Error loading dataset", JOptionPane.ERROR_MESSAGE);
        }

        wrapup();
        System.err.println("Transform ended");
    }

    public boolean cancel() {
        cancelled = true;
        ph.setDisplayName(append("Stopping"));
        final Thread rt = runner;
        Thread killThread = new Thread(new Runnable() {
            public void run() {
                try { Thread.sleep(1000); } catch (Exception e) {
                if (rt.isAlive()) rt.stop();
            }
        }});
        killThread.start();
        try {
            killThread.join();
        } catch (Exception e) {
            System.err.println("Interrupted while waiting: " + e);
            e.printStackTrace();
        }
        return true;
    }

    public void wrapup() {
        ph.setDisplayName(append("Showing tables..."));
        ph.finish();
        EventQueue.invokeLater(whenFinished);
    }
}
