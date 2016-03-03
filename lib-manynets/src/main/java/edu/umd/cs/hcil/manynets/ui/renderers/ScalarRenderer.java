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

import edu.umd.cs.hcil.manynets.model.Distribution;
import edu.umd.cs.hcil.manynets.ui.ColumnManager;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;


/**
 * Renders a strings and numbers in scientific notation, where warranted
 * @author Manuel Freire
 */
public class ScalarRenderer implements ColumnCellRenderer {
    private boolean showBar = true;
    private boolean showValue = true;
    private Color barColor = Color.blue.darker();
    private Object lastValue;
    private ColumnManager cm;

    private JLabel label = new JLabel();
    private JPanel jp = new JPanel() {
        @Override
        public void paint(Graphics g) {
            super.paint(g); paintOverlay((Graphics2D)g, getWidth(), getHeight());
        }
    };

    @Override
    public void init(ColumnManager cm) {
        this.cm = cm;
    }

    public void setShowBar(boolean showBar) {
        this.showBar = showBar;
    }

    public void setShowValue(boolean showValue) {
        this.showValue = showValue;
    }

    public void setBarColor(Color barColor) {
        this.barColor = barColor;
    }

    public Color getBarColor() {
        return barColor;
    }

    public ScalarRenderer() {
        jp.setLayout(new BorderLayout());
        jp.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jp.add(label, BorderLayout.EAST);
        label.setOpaque(false);
    }

    public String getText(Object value) {
        if (value instanceof Double) {
            return cm.formatDouble((Double)value);
        }
        else return "" + value;
    }

    public void paintOverlay(Graphics2D g, int width, int height) {
        if (showBar) {
            Double v = null;
            try  {
                v = Double.parseDouble("" + lastValue);
            } catch (NumberFormatException nfe) {
                showBar = false;
                return;
            }
            double w = (width-2) * (v - cm.getMin()) * (1.0 /
                    (cm.getMax() - cm.getMin()));
//                System.err.println(" v " + v + " m " + cm.getMin() + " M " + cm.getMax() + " : w = " +w);
            g.setColor(barColor);
            g.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, 0.2f));
            g.fillRect(1, 1, (int)w, height-2);
        }
    }

    @Override
    public void selectionDrag(JTable dest, Point start, Point end, Rectangle bounds, boolean isAdjusting) {
    }

    @Override
    public String getXCaption() {
        return "";
    }

    @Override
    public String getYCaption() {
        return "";
    }

    @Override
    public Component getTableCellRendererComponent(JTable table,
            final Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {

        lastValue = value;

        label.setIcon(cm.getIcon(row));
        label.setHorizontalTextPosition(SwingConstants.LEADING);
        label.setText(getText(value));
        label.setFont(table.getFont());
        label.setVisible(showValue);
        jp.setBackground(isSelected ? table.getSelectionBackground() :
            (((column%2)==0) ? cm.getEvenColor() : cm.getOddColor()));
        return jp;
    }

    @Override
    public boolean updateHighlights() {
        return false;
    }

    @Override
    public String getToolTipText(Point p) {
        if (lastValue == null) {
            return "";
        } else {
            return "" + lastValue + " (" + lastValue.getClass().getSimpleName() + ")";
        }
    }

    @Override
    public JPanel getSettingsPanel() {
        return new ScalarOPanel(this);
    }

    @Override
    public int getMaximumVerticalSize() {
        return 16;
    }

    @Override
    public boolean canHandle(ColumnManager cm, boolean overview) {
        return (! overview) &&
                ( ! Distribution.class.isAssignableFrom(cm.getColumnClass()));
    }

    @Override
    public String getRendererName() {
        return "Scalar";
    }

    @Override
    public ColumnCellRenderer copy() {
        ScalarRenderer r = new ScalarRenderer();
        r.init(cm);
        r.barColor = barColor;
        r.showBar = showBar;
        r.showValue = showValue;
        return r;
    }
}