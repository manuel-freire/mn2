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

import edu.umd.cs.hcil.manynets.model.TableWrapper;
import edu.umd.cs.hcil.manynets.model.Transform;
import edu.umd.cs.hcil.manynets.model.Transform.ProgressMonitor;
import edu.umd.cs.hcil.manynets.model.TransformOptions;
import java.awt.EventQueue;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Cancellable;
import org.openide.windows.TopComponent;
import prefuse.util.collections.IntIterator;

/**
 * A transformer that does not show any interface at all. Useful for testing.
 *
 * @author Manuel Freire
 */
public class ProgressTransformer implements ProgressMonitor, Runnable, Cancellable {

    private TopComponent tc = null;

    private TableWrapper tw;
    private Transform pt;
    private TransformOptions to;
    private Runnable whenFinished;

    private ProgressHandle ph = null;
    private Thread runner;
    private int total = 10000;
    private int closed = 0;
    private double closeDelta;

    public ProgressTransformer(TopComponent tc, TableWrapper tw, Transform pt, TransformOptions to, Runnable whenFinished) {
        this.tc = tc;
        this.tw = tw;
        this.pt = pt;
        this.to = to;
        this.whenFinished = whenFinished;
    }

    public void apply() {
        if (pt.getOptions() != to) {
            pt.init(tw, to, null);
        }
        (new Thread(this)).start();
    }

    @Override
    public void run() {
        runner = Thread.currentThread();        
        ph = ProgressHandleFactory.createHandle(pt.getOptions().getDescription(), this);
        ph.start(total);

        int toProcess = 0;
        IntIterator rit = tw.getTable().rows();
        while (rit.hasNext()) {
            int id = tw.getId(rit.nextInt());
            if (pt.shouldProcess(id)) toProcess ++;
        }
        closeDelta = 1.0/toProcess;

        System.err.println("Transform started (" + toProcess + " elements)...");

        rit = tw.getTable().rows();
        while (rit.hasNext()) {
            int id = tw.getId(rit.nextInt());
            if ( ! pt.shouldProcess(id)) {
                System.err.println("Ignoring id " + id);
                continue;
            }

            try {
                pt.apply(id, this);
            } catch (Exception e) {
                System.err.println("Could not finish transform: exception at id " + id);
                e.printStackTrace();
                break;
            }
        }
        wrapup();
        System.err.println("Transform ended");
    }

    public boolean cancel() {
        pt.cancel();
        ph.progress("Stopping");
        final Thread rt = runner;
        Thread killThread = new Thread(new Runnable() {
            public void run() {
                try { Thread.sleep(1000); } catch (Exception e) {
                if ( ! rt.isAlive()) rt.stop();
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
        ph.finish();
        EventQueue.invokeLater(new Runnable() {
          public void run() {
            whenFinished.run();
          }
        });
    }

    @Override
    public void setProgress(int id, String message, double progress) {
        String progressMessage = "" + id + " " + message;
        int current = (int)(closeDelta * closed * total);
        if (progress == 1) {
            closed ++;
            current += closeDelta * total;
        } else {
            current += progress * closeDelta * total;
        }
        System.err.println("Progress: " + progressMessage + " -- " + current);
        ph.progress(progressMessage, current);
    }
}
