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

package edu.umd.cs.hcil.manynets.ui;

import edu.umd.cs.hcil.manynets.util.MiscUtils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 * A small dialog that provides feedback on the progress of a task, and
 * allows the user to abort it in any moment. Once finished,
 * a callback is, uh, called.
 *
 * @author mfreire
 */
public class TaskProgressMonitor extends JDialog implements ActionListener {
    
    private javax.swing.JButton jbCancelProgress;
    private javax.swing.JLabel jlProgreso;
    private javax.swing.JProgressBar jpProgress;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator1;
    
    private Task task;

    private javax.swing.Timer t;
    private long startTime;
    private boolean isTestFinished = false;

    public interface Task {
        public void run();
        public void cancel();
        public double getProgress();
        public String getMessage();
        public void finished();
    }

    public TaskProgressMonitor(JComponent c, String title, Task task) {
        this.task = task;
        initComponents();
        setSize(400,300);
        setLocationRelativeTo(c);
        setTitle(title);
        setVisible(true);
        setAlwaysOnTop(true);
    }
   
    public void start() {
        Runner runner = new Runner();
        Thread testThread = new Thread(runner);
        testThread.start();
        startTime = System.currentTimeMillis();
        t = new javax.swing.Timer(1000, this); // miliseconds
        t.setRepeats(true);
        t.start();
    }
    
    private class Runner implements Runnable {
        private String message;
        public void run() {
            try {
                task.run();
                isTestFinished = true;
            } catch (RuntimeException e) {
                java.io.StringWriter sw = new java.io.StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                message = "<html><b>"+e.toString()+"</b><br>" +
                        "<pre>"+sw.toString()+"</pre></html>";
                e.printStackTrace();
                
                task.cancel();
                java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        JOptionPane.showMessageDialog(null, message,
                                "Error", JOptionPane.ERROR_MESSAGE);
                        dispose();
                    }
                });
            }
        }
    }
    
    public void actionPerformed(ActionEvent evt) {
        double p = task.getProgress();
        long elapsed = System.currentTimeMillis() - startTime;
        jpProgress.setValue((int)(p*100));
        double v = (elapsed/p - elapsed)/1000;        
        int s = (int)v;
        int minutes = s/60;
        int seconds = s%60;
        int es = ((int)elapsed / 1000) % 60;
        int em = ((int)elapsed / 1000) / 60;
        jlProgreso.setText("<html>"+
                "" + task.getMessage() + "<br>" +
                "Time elapsed " + em +" min, "+ es + " s<br>" +
                "Time remaining " + minutes+" min, "+ seconds + " s<br>" +
                "Memory  " + MiscUtils.getMemUsage() + "</html>");
        if (isTestFinished) {
            t.stop();
            dispose();
            System.err.println("Total time elapsed: "+elapsed+" ms");
            task.finished();
        }
    }
    
    public void cancel() {
        task.cancel();
        t.stop();
        dispose();
        return;
    }
    
    public void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        jpProgress = new javax.swing.JProgressBar();
        jPanel2 = new javax.swing.JPanel();
        jbCancelProgress = new javax.swing.JButton();
        jlProgreso = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        
        getContentPane().setLayout(new java.awt.GridBagLayout());
        
        setTitle("...");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(26, 7, 7, 7);
        getContentPane().add(jpProgress, gridBagConstraints);
        
        jbCancelProgress.setText("Cancel");
        jbCancelProgress.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancel();
            }
        });
        
        jPanel2.add(jbCancelProgress);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jPanel2, gridBagConstraints);
        
        jlProgreso.setText("Time remaining: unknown");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
        getContentPane().add(jlProgreso, gridBagConstraints);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jSeparator1, gridBagConstraints);
    }
}
