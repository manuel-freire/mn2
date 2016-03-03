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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import org.openide.awt.Actions;
import org.openide.awt.DropDownButtonFactory;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;

/**
 *
 * @author Manuel Freire
 */
public class PickDrawingLineAction extends CallableSystemAction {
    private static JButton button;
    private static ButtonGroup buttonGroup;
    private static JPopupMenu popup;
    private MyMenuItemListener menuItemListener;

    List handledCharts;

    public PickDrawingLineAction() {
        popup = new JPopupMenu();
        menuItemListener = new MyMenuItemListener();
        buttonGroup = new ButtonGroup();

        for (String x : new String[] {"Thing A", "Thing B"}) {
            JRadioButtonMenuItem item =
                new JRadioButtonMenuItem(x.toString());
            item.addActionListener(menuItemListener);
            buttonGroup.add(item);
            popup.add(item);
        }

        ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                "/edu/umd/cs/hcil/manynets/icons/add-column24.png"));
        button = DropDownButtonFactory.createDropDownButton(icon, popup);
        Actions.connect(button, (Action)this);
    }

    public String getName() {
        return "Pick Drawing Line";
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public Component getToolbarPresenter() {
        System.err.println("Queried for toolbar presenter!!!!!!!!");
        return button;
    }

    @Override
    public void performAction() {
        System.err.println("Default action selected!");
    }

    private class MyMenuItemListener implements ActionListener {
        public void actionPerformed(ActionEvent ev) {
            JMenuItem item = (JMenuItem)ev.getSource();
            String selectedStr = item.getText();
            System.err.println(selectedStr + " selected!");
        }
    }
}
