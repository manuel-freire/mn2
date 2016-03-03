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

package edu.umd.cs.hcil.manynets.app.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.Timer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * This class monitors memory use. If less than a configurable % or absolute size
 * maximum VM memory is free, a popup dialog will be shown, directing the user
 * to steps to increase VM size.
 *
 * @author mfreire
 */
public class LowMemoryPopup {

    private static MemoryMXBean mbean = null;
    private long lowBoundInBytes;
    private double lowBoundAsPercent;
    private Timer timer;

    public LowMemoryPopup(long lowBoundInBytes, double lowBoundAsPercent) {
        this.lowBoundInBytes = lowBoundInBytes;
        this.lowBoundAsPercent = lowBoundAsPercent;
        mbean = ManagementFactory.getMemoryMXBean();
    }

    private long getFreeMemory() {
        return mbean.getHeapMemoryUsage().getMax() -
                mbean.getHeapMemoryUsage().getUsed();
    }

    private long getTotalMemory() {
        return mbean.getHeapMemoryUsage().getMax();
    }

    private String getFormattedMessage() {
        NumberFormat f = DecimalFormat.getInstance();
        f.setMaximumFractionDigits(2);
        f.setMinimumFractionDigits(2);
        long used = mbean.getHeapMemoryUsage().getUsed();
        long max = getFreeMemory();

        return f.format((double) used * 100.0 / max) + "% : " + (used / 1024) + "k / " + (max / 1024) + "k";
    }

    private void showMessage() {
        // tries to GC a few times, to hopefully add a little extra wiggling space
        System.gc();
        System.gc();
        System.gc();

        NotifyDescriptor.Message nd = new NotifyDescriptor.Message(
            "ManyNets is running out of memory. You are now using " + getFormattedMessage() + "\n" +
            "To increase the memory available to ManyNets, read the README.txt file included with your" +
            "distribution (this message is only displayed once per application restart).");
        nd.setOptionType(NotifyDescriptor.WARNING_MESSAGE);
        nd.setTitle("Low memory warning");
        DialogDisplayer.getDefault().notify(nd);
    }

    public synchronized void startTimer(int delayInMillis) {
        if (timer != null) {
            timer.stop();
        }

        timer = new Timer(delayInMillis, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
//                System.err.println("" + getFreeMemory() + " vs " +  lowBoundInBytes);
//                System.err.println("" + ((getFreeMemory() * 100.0 / getTotalMemory())) + " vs " + lowBoundAsPercent);


                if (getFreeMemory() < lowBoundInBytes ||
                        ((getFreeMemory() * 100.0 / getTotalMemory())) < lowBoundAsPercent) {
                    timer.stop();
                    showMessage();
                }
            }
        });
        timer.setRepeats(true);
        timer.start();
    }
}
