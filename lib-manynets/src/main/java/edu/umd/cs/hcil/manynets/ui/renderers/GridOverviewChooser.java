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

package edu.umd.cs.hcil.manynets.ui.renderers;

import edu.umd.cs.hcil.manynets.ui.ColumnManager;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Creates a grid view of renderers. Configurable to determine what
 * renderers to show where, and with what options.
 *
 * @author Manuel Freire
 */
public class GridOverviewChooser extends JPanel {

    private ColumnManager cm;

    private int cellRow, cellCol;
    private Object cellValue;

    public GridOverviewChooser(ColumnManager cm, 
            ArrayList<AxisChoice> xcs,
            int choiceRows, int choiceCols,
            int row, int col, Object value) {
        super();
        this.cm = cm;
        this.cellRow = row;
        this.cellCol = col;
        this.cellValue = value;

        setLayout(new GridLayout(choiceRows, choiceCols));
        ChoiceListener cl = new ChoiceListener();
        for (AxisChoice x : xcs) {
            ChoiceButton cb = new ChoiceButton(x);
            add(cb);
            cb.addActionListener(cl);
        }
    }

    private class ChoiceListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            System.err.println("Chosen: " +
                    ((ChoiceButton)e.getSource()).xc.getTooltipText());
        }
    }

    /**
     * A button that displays an option, and can be clicked to select an option
     */
    private class ChoiceButton extends JButton {
        public AxisChoice xc;
        public ColumnCellRenderer renderer;
        private Component child;
        public ChoiceButton(AxisChoice xc) {
            this.xc = xc;
            this.renderer = xc.getRenderer(cm);
            child = renderer.getTableCellRendererComponent(
                    cm.getTable(), cellValue, false, false, cellRow, cellCol);
            add(child);
            setToolTipText(xc.getTooltipText());
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            child.repaint();
        }
    }
    
    public interface AxisChoice {
        String getTooltipText();
        ColumnCellRenderer getRenderer(ColumnManager cm);
    }

    public static class ClassChoice implements AxisChoice {
        private Class rc;
        private Prop[] props;
        public ClassChoice(Class rc, Prop... props) {
            this.props = props;
            this.rc = rc;
        }
        @Override
        public ColumnCellRenderer getRenderer(ColumnManager cm) {
            ColumnCellRenderer renderer = null;
            LabelOverviewRenderer over = new LabelOverviewRenderer();
            try {
                renderer = ((Class<ColumnCellRenderer>)rc).newInstance();
                over.setInner(renderer);
                over.init(cm);
                for (Prop p : props) {
                    p.apply(renderer);
                }
            } catch (Exception e) {
                System.err.println("Error instantiating option renderer " + rc);
                e.printStackTrace();
            }
            return over;
        }
        
        @Override
        public String getTooltipText() {
            StringBuilder sb = new StringBuilder();
            sb.append("<html> " + rc.getSimpleName() + " <br> ");
            for (Prop p : props) {
                sb.append(p.getTooltipText() + " <br> ");
            }
            sb.append("</html>");
            return sb.toString();
        }

        public static class Prop {
            protected String propertyName;
            protected Object value;
            protected Class vClass;
            public Prop(String propName, Object value) {
                this(propName, value, value.getClass());
            }
            public Prop(String propName, Object value, Class vClass) {
                this.propertyName = propName;
                this.value = value;
                this.vClass = vClass;
            }
            public String getTooltipText() {
                return  "" + propertyName + " = " + 
                        (value instanceof Class ? ((Class)value).getSimpleName() :
                            value) ;
            }
            void apply(ColumnCellRenderer renderer) {
                try {
                    Method m = renderer.getClass().getMethod(propertyName, vClass);
                    m.invoke(renderer, value);
                } catch (Exception e) {
                    System.err.println("Error setting " +
                            propertyName + " to " +
                            value + " in " +
                            renderer + ":\n" + e);
                    e.printStackTrace();
                }
            }
        }
    }
}
