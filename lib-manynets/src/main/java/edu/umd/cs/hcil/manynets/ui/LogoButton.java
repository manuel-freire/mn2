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

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 *
 * @author Manuel Freire
 */
public class LogoButton extends JLabel implements MouseListener {

    private String uri;
    private ImageIcon icon;

    public LogoButton(String logoFileName, String uri) {
        this.icon = new ImageIcon(LogoButton.class.getResource(logoFileName));
        this.uri = uri;
        setOpaque(true);
        setBackground(Color.white);
        setIcon(icon);
        setPreferredSize(new Dimension(
                icon.getIconWidth()+2, icon.getIconHeight()+2));
        setBorder(BorderFactory.createLineBorder(Color.white, 2));
        addMouseListener(this);
    }

    public void mouseClicked(MouseEvent e) {
        try {
            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new java.net.URI(uri));
                return;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "See " + uri);
        }
    }
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {
        setBorder(BorderFactory.createLineBorder(Color.blue, 2));
    }
    public void mouseExited(MouseEvent e) {
        setBorder(BorderFactory.createLineBorder(Color.white, 2));
    }
}
