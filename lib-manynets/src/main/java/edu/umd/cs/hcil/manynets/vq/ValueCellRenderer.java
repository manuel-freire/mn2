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

package edu.umd.cs.hcil.manynets.vq;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author mfreire
 */
public class ValueCellRenderer extends JLabel implements TableCellRenderer {

    private Ranking r;
    private Object value;
    private boolean selected;
    private JTable table;
    
    public static Color[] colors = new Color[] {
        Color.red, Color.green.darker(), Color.ORANGE, Color.GRAY
    };
    public static Color[] lightColors = null;

    static {
        lightColors = new Color[colors.length];
        int i=0;
        float hsb[] = new float[3];
        for (Color c : colors) {
            Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsb);
            lightColors[i] = Color.getHSBColor(hsb[0], 0.1f, 1f);
            i++;
        }
    }

    public Ranking getRanking() {
        return r;
    }

    public void setRanking(Ranking r) {
        this.r = r;
    }
    
    public String getStringFor(Object o) {
        return o.toString();
    }
    
    public Component getTableCellRendererComponent(JTable table, 
            Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        this.table = table;
        this.value = value; 
        this.selected = isSelected;
        setText(getStringFor(value)); 
        return this;
    }   
       
    public Objective getObjective(Object value, int x, int w) {
        System.err.println("x = "+x+" w = "+w+" value="+value);
        double start = 0;
        for (int i=0; i<r.getObjectives().size(); i++) {
            Objective o = r.getObjectives().get(i);
            double dw = w * o.weigh(value);
            if (start <= x && x <= (start + dw)) {
                return o;
            }
            start += dw;
            System.err.println("start = " + start);
        }
        System.err.println("objective not found: reached "+start+" but no match for "+x);
        return null;
    }
    
    public void paint(Graphics g) {        
//        g.setColor(selected ? 
//            table.getSelectionBackground() : 
//            table.getBackground());
//        g.fillRect(0, 0, getWidth(), getHeight());
        int w = getWidth();
        int h = getHeight();
        double start = 0;
        for (int i=0; i<r.getObjectives().size(); i++) {
            Objective o = r.getObjectives().get(i);
            double dw = w * o.weigh(value);
            g.setColor(lightColors[i]);
            g.fillRect((int)start, 0, (int)dw, h);
            g.setColor(colors[i]);
            g.fillRect((int)start, 0, (int)dw, 1);
            start += dw;
        }
        super.paint(g);
    }
}
