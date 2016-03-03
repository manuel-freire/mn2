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

package edu.umd.cs.hcil.manynets.model;

import edu.umd.cs.hcil.manynets.model.Transform.ProgressMonitor;
import edu.umd.cs.hcil.manynets.ui.TaskProgressMonitor;
import edu.umd.cs.hcil.manynets.ui.TaskProgressMonitor.Task;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import prefuse.util.collections.IntIterator;

/**
 * Implements a progress-monitorizeable task to actually carry out a transform
 * Can multi-thread the transform among multiple cores, if available.
 * FIXME: currently Prefuse seems to be thread-challenged, so we have disabled this
 */
public class TransformTask implements Task, ProgressMonitor {

    private TableWrapper tw;
    private Transform pt;
    private TransformOptions to;

    private Runnable whenFinished;
    private boolean running = false;
    private int total;

    private TaskProgressMonitor tpm;

    public TransformTask(TableWrapper tw, Transform pt, TransformOptions to, Runnable whenFinished) {
        this.tw = tw;
        this.pt = pt;
        this.to = to;
        this.whenFinished = whenFinished;
    }

    public void apply(TableWrapper tw, Transform pt, TransformOptions to,
            JComponent jc, Runnable whenFinished) {

        TransformTask tt = new TransformTask(tw, pt, pt.getOptions(), whenFinished);
        System.err.println("Ready to go: " + to.getDescription());
        pt.init(tw, to, null);
        System.err.println("Ready to go1: " + to.getDescription());
        tpm = new TaskProgressMonitor(jc,
                "Applying: " + pt.getOptions().getDescription(), tt);
        System.err.println("Ready to go2: " + to.getDescription());
        tt.setTpm(tpm);
        System.err.println("Ready to go3: " + to.getDescription());
        tpm.start();
    }

    public void setTpm(TaskProgressMonitor tpm) {
        this.tpm = tpm;
    }

    @Override
    public void run() {
        // Not running in UI thread: must *not* call UI code
        running = true;
        int n = tw.getTable().getRowCount();
        total = n;
        System.err.println("Transform started (" + n + " elements)...");

        // each processor gets a thread with a fair share
        // last processors may get 1 less than others
        int procs = 1; // Runtime.getRuntime().availableProcessors();
        Thread threads[] = new Thread[procs];
        int delta = (int) Math.ceil(n / (double) procs);
        IntIterator rit = tw.getTable().rows();
        for (int i = 0; i < procs; i++) {
            ArrayList<Integer> ids = new ArrayList<Integer>();
            for (int j = 0; j < delta && rit.hasNext(); j++) {
                ids.add(tw.getId(rit.nextInt()));
            }
            threads[i] = new Thread(new Runner(i, ids));
            threads[i].start();
        }
        for (int i = 0; i < procs; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException ex) {
                System.err.println("Interrupted while joining thread " + i);
                i--;
            }
        }
        System.err.println("Transform ended");
    }

    @Override
    public void finished() {
        whenFinished.run();
    }

    @Override
    public void cancel() {
        running = false;
    }

    @Override
    public void setProgress(int id, String message, double progress) {
        progressMessage = "" + id + " " + message;
        currentProgress = progress;
        if (progress == 1.0) {
            totalProgress += 1;
        }
    }

    private String progressMessage;
    private double currentProgress;
    private double totalProgress;

    @Override
    public double getProgress() {
        return (totalProgress + currentProgress) / total;
    }

    @Override
    public String getMessage() {
        return progressMessage;
    }

    private class Runner implements Runnable {

        private int id;
        private ArrayList<Integer> ids;

        public Runner(int id, ArrayList<Integer> gids) {
            this.id = id;
            this.ids = gids;
        }

        public void run() {
            System.err.println("thread " + id + " has " + ids.size() + " graphs ...");
            try {
                for (int i : ids) {
                    if (!running) {
                        return;
                    }
                    System.err.println("" + id + ": " + i + " started...");
                    pt.apply(i, TransformTask.this);
                    System.err.println("" + id + ": " + i + " FINISHED");
                }
            } catch (Exception e) {
                if (tpm != null) {
                    java.awt.EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            tpm.cancel();
                        }
                    });
                }
                JOptionPane.showMessageDialog((JFrame)null,
                        "Failed: " + e.getMessage(),
                        "Error applying transform", JOptionPane.WARNING_MESSAGE);
                System.err.println("thread " + id + " ERRORed and ended");
                e.printStackTrace();
            }
            System.err.println("thread " + id + " is OVER");
        }
    }
}
