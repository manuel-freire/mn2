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

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author mfreire
 */
public abstract class QueryPane extends JPanel {
    private JLabel label;
    private JTextField textField = new JTextField();
    private CardLayout cl = new CardLayout();
    protected Ranking ranking;
    protected String data = "";
    
    public QueryPane(Ranking r, String s) {
        this.ranking = r;
        setLayout(cl);
        label = new ValueLabel();
        label.setFocusable(true);
        add(label, "label");
        add(textField, "editor");
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.err.println("lost!");
                update(textField.getText());
                cl.show(QueryPane.this, "label");                
            }
        });
        update(s);
    }
    
    public void  update(String s) {
        String updated = parse(s);
        if (updated != null) {
            data = updated;
            textField.setText(updated);
            colorize();
            ranking.notifyListeners();
        }
    }
    
    public abstract String parse(String s);
    
    protected void colorize() {
        StringBuffer sb = new StringBuffer("<html>");
        int i = 0;
        for (String s : data.trim().split(" +")) {
            Color c = ValueCellRenderer.colors[i++];
            sb.append("<span color='#"+htmlColor(c)+"'>"+s+"</span>&nbsp");
        }        
        label.setText(sb + "</html>");
        // System.err.println(data + " => " + label.getText());
    }        
    
    private String htmlColor(Color c) {
        byte[] hex = "0123456789abcdef".getBytes();
        byte[] r = new byte[6];
        r[0] = hex[(c.getRed() & 0xf0) >> 4];
        r[1] = hex[(c.getRed() & 0x0f)];
        r[2] = hex[(c.getGreen() & 0xf0) >> 4];
        r[3] = hex[(c.getGreen() & 0x0f)];
        r[4] = hex[(c.getBlue() & 0xf0) >> 4];
        r[5] = hex[(c.getBlue() & 0x0f)];
        return new String(r);
    }
    
    public class ValueLabel extends JLabel {
        public ValueLabel() {
            super();
            ranking.addRankingListener(new Ranking.Listener() {
                public void valuesChanged(Ranking r) {
                    repaint();
                }
            });
            QueryPaneMouseListener l = new QueryPaneMouseListener(ranking, this);
            addMouseListener(l);
            addMouseMotionListener(l);
        }
        public void paint(Graphics g) {
            super.paint(g);
            int w = getWidth();
            int h = 2;
            double start = 0;
            for (int i=0; i<ranking.getObjectives().size(); i++) {
                Objective o = ranking.getObjectives().get(i);
                double dw = w * o.getWeight();
                g.setColor(ValueCellRenderer.colors[i]);
                g.fillRect((int)start, 0, (int)dw, h);
                start += dw;
            }
        }
        public void startEdit() {
            System.err.println("gained!");
            cl.show(QueryPane.this, "editor");           
        }
        public Objective getObjectiveAt(Point p) {
            int w = getWidth();
            double x = p.getX();
            double start = 0;
            for (int i=0; i<ranking.getObjectives().size(); i++) {
                Objective o = ranking.getObjectives().get(i);
                double dw = w * o.getWeight();
                if (start <= x && x <= start + dw) {
                    return o;
                }
                start += dw;
            }
            return null;
        }
    }
}
