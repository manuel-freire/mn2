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
import prefuse.util.collections.IntIterator;

/**
 * A transformer that does not show any interface at all. Useful for testing.
 *
 * @author Manuel Freire
 */
public class HeadlessTransformer implements ProgressMonitor, Runnable {
    private TableWrapper tw;
    private Transform pt;
    private TransformOptions to;

    private Runnable whenFinished;
    private int total;


    public HeadlessTransformer(TableWrapper tw, Transform pt, TransformOptions to, Runnable whenFinished) {
        this.tw = tw;
        this.pt = pt;
        this.to = to;
        this.whenFinished = whenFinished;
    }

    public void apply() {
        pt.init(tw, to, null);
        run();
    }

    @Override
    public void run() {
        int n = tw.getTable().getRowCount();
        total = n;
        System.err.println("Transform started (" + n + " elements)...");

        IntIterator rit = tw.getTable().rows();
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

        System.err.println("Transform ended");
        whenFinished.run();
    }

    @Override
    public void setProgress(int id, String message, double progress) {
        String progressMessage = "" + id + " " + message;
        currentProgress = progress;
        if (progress == 1.0) {
            totalProgress += 1;
        }
        System.err.println(progressMessage + " " +
                (totalProgress + currentProgress) / total);
    }

    private double currentProgress;
    private double totalProgress;
}
