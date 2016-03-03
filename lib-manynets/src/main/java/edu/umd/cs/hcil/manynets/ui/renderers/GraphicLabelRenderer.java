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
import edu.umd.cs.hcil.manynets.ui.TablePanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;

/**
 * Renders a label, horizontal or vertically.
 *
 * @author mfreire
 */
public class GraphicLabelRenderer extends JPanel {

    private LabelOverviewRenderer parent;
    private ColumnManager cm;
    private AbstractRowRenderer arr;

    public GraphicLabelRenderer(LabelOverviewRenderer parent) {
        this.parent = parent;
        setBackground( new Color(250, 250, 255));
        setFont(getFont().deriveFont(9f));
    }  
    
    public void setColumn(ColumnManager cm){
        this.cm=cm;
    }

    interface GraphicLabeller {

        public void setGraphicLabels(GraphicLabelRenderer glr);
    }

    @Override
    public void paint(Graphics g) {

        super.paint(g);
        ColumnCellRenderer ccr = parent.getInner();
        if ( ccr instanceof GraphicLabeller) {
//            System.err.print("Parent instance. Setting graphic labels ");
            ((GraphicLabeller) ccr).setGraphicLabels(this);
        }
        else {
//            System.err.println("parent not instance");
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        double width = getWidth();

        Rectangle2D rectangle = new Rectangle2D.Double();
       
        g2d.setColor(Color.lightGray);
        int totalRows = cm.getTable().getRowCount();
      
        if(ccr instanceof AbstractRowRenderer){
            arr=(AbstractRowRenderer)ccr;
        }
        else if( ccr instanceof MultiColumnOverviewRenderer ){
            arr = (AbstractRowRenderer)LabelOverviewRenderer.getInnerRenderer(parent);
        }
        else return;
        
        double max = totalRows-1;
        double min = 0;
        if (max==min){
            min = 1;
            max = 2;
        }
        double h = getHeight()*1.0/totalRows;

        /*
         * recap:
         * we want to show, for the sorting in THIS view, what was the position,
         * in the ORIGINAL column, of these rows.
         */

        Color light = Color.getHSBColor(TablePanel.highlightHue, 1f, 1f);
        Color dark = Color.getHSBColor(TablePanel.highlightHue, .5f, .5f);

        // first pass
        for (int i = 0; i < totalRows; i++) {
            
            int j = arr.imageRowToView(i);
//            int j = cm.getTable().convertRowIndexToView(i);

            double w = (width -2 ) * (j - min ) * (1.0/ (max-min));

            boolean selected = cm.getTable().isRowSelected(arr.imageRowToView(i));

            if (selected) {
                // System.err.println( " row "+ j +" is selected");
                rectangle.setRect( width-w, i*h, w, h);
                g2d.setColor(light);
                g2d.fill(rectangle);
                rectangle.setRect( 0, i*h, width-w, h);
                g2d.setColor(dark);
                g2d.fill(rectangle);
            } else {
                g2d.setColor(Color.lightGray);
                rectangle.setRect( width-w, i*h, w, h);
//            System.err.println("" + (i+1)*h + " of " + getHeight());
                g2d.fill(rectangle);
            }
        }

        g2d.setColor(Color.black);
        g2d.drawLine((int)width-1,0,(int)width-1, getHeight()-1 );
    }

    public String getToolTipText(Point p) {
        ColumnCellRenderer inner = 
                LabelOverviewRenderer.getInnerRenderer(parent);

        if ( ! (inner instanceof AbstractRowRenderer)) {
            return "";
        }
//        arr = (AbstractRowRenderer)inner;

//        System.err.println(" inside tooltip " );
        int row = arr.getRowForPoint(p , getHeight());
//        System.err.println(" tooltip row" + y);

        StringBuilder sb = new StringBuilder("<html>\n");
        sb.append("<b>ID</b> " + cm.getTable().getModel().getValueAt(row, 0));
        sb.append("<br/>"+arr.imageRowToView(row));
        sb.append("</html>");

        return "" + sb.toString();
    }

}
