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
import edu.umd.cs.hcil.manynets.ui.SelectedRowPanel.CellClickedInfo;
import edu.umd.cs.hcil.manynets.ui.TablePanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;

/**
 * This renderer provides X and Y labelling for an internal ColumnCellRenderer
 * @author mfreire
 */
public class LabelOverviewRenderer extends JPanel implements ColumnCellRenderer {

    private ColumnCellRenderer inner;
    private LabelRenderer xLabel;
    private LabelRenderer yLabel;
    private GraphicLabelRenderer gLabel;
    private LabelLayout layout;
    private ColumnManager cm;

    public LabelOverviewRenderer() {
        setBorder(BorderFactory.createLineBorder(Color.white, 1));
    }

    /**
     * Must be called prior to init() or any other method (eg.: canHandle)
     */
    public LabelOverviewRenderer setInner(ColumnCellRenderer inner) {
        this.inner = inner;
        setLayout(layout = new LabelLayout());
        this.xLabel = new LabelRenderer(this, false);
        this.yLabel = new LabelRenderer(this, true);
        this.gLabel = new GraphicLabelRenderer(this);
        add(xLabel);
        if ((inner instanceof AbstractRowRenderer) || (inner instanceof MultiColumnOverviewRenderer)) {
//            System.err.println(" added ");
            add(gLabel);
        }
        add(yLabel);
        add((JComponent) inner);
        return this;
    }

    /**
     * Allows CCRs that may or may not be wrapped in a LabelOverviewRenderer
     * to be unwrapped
     * @param ccr
     * @return
     */
    public static ColumnCellRenderer getInnerRenderer(ColumnCellRenderer ccr) {
        if (ccr instanceof LabelOverviewRenderer) {
            ccr = ((LabelOverviewRenderer) ccr).getInner();
            if (ccr instanceof MultiColumnOverviewRenderer) {
                ccr = ((MultiColumnOverviewRenderer) ccr).getSubrenderers().get(0);
            }
        }
        return ccr;
    }

    public ColumnCellRenderer getInner() {
        return inner;
    }
    
//
//    public void setBounds(int x, int y, int w, int h) {
////        System.err.println(" " + x + " " + y + " " + w + " " + h);
//        super.setBounds(x, y, w, h);
//    }

    //---- The specialized layout used by this renderer
    public class LabelLayout implements LayoutManager {

        private int lotsOfPixels = 1000000;
        private int height;
        private int width;
        private int vOffset;
        private int hOffset;
        private Dimension preferredDim = null;
        private Dimension minimumDim = new Dimension(50, 50);

        @Override
        public void addLayoutComponent(String string, Component cmpnt) {
            // nothing to do here: already have refences in LOR
        }

        @Override
        public void removeLayoutComponent(Component cmpnt) {
            // nothing will ever be removed
        }


        @Override
        public Dimension preferredLayoutSize(Container cntnr) {
            if (preferredDim == null) {
                preferredDim = new Dimension(lotsOfPixels, inner.getMaximumVerticalSize());
            } else {
                preferredDim.setSize(lotsOfPixels, inner.getMaximumVerticalSize());
            }

            return preferredDim;
        }

        @Override
        public Dimension minimumLayoutSize(Container cntnr) {
            return minimumDim;
        }

        /**
         * The main method here - assigns areas to the labels (which may
         * be completely hidden) and the visualization itself
         * @param cntnr
         */
        @Override
        public void layoutContainer(Container cntnr) {
            width = getWidth() - 2; //cntnr.getWidth()-2;
            height = getHeight() - 2; //cntnr.getHeight()-2;
            vOffset = 0;
            hOffset = 0;

            System.err.println("updating layout...");

            // decision logic to set vOffset and hOffset to good values
            if (width < 30 || height < 30) {
                hOffset = vOffset = 1;
            } else if (width < 60 || height < 60) {
                hOffset = 1;
                vOffset = 12 + 1;
            } else {
                hOffset = vOffset = 24 + 1;
            }
            if (inner.getXCaption().length() == 0) {
                vOffset = 1;
            }
            if (inner.getYCaption().length() == 0) {
                hOffset = 1;
            }

            yLabel.setBounds(
                    1, 1, hOffset, height - vOffset);

            if ((inner instanceof AbstractRowRenderer) || (inner instanceof MultiColumnOverviewRenderer)) {
//                if(cm.getTable().getRowCount()>1){ // NPE while detaching window, otherwise ok.
                gLabel.setBounds(hOffset, 1, hOffset, height - vOffset - 1);
                hOffset *= 2;
//                }else{
//                    remove(gLabel);
//                }
            }
            xLabel.setBounds(
                    hOffset, height - vOffset, width - hOffset, vOffset);
//            System.err.println("Relayout: " + width + "x" + height + " => " + hOffset + "x" + vOffset);

            ((JComponent) inner).setBounds(
                    hOffset, 1, width - hOffset, height - vOffset - 1);
//            System.err.println("Relayout: " + width + "x" + height + " => " +
//                    (width - hOffset) + "x" + (height - vOffset - 1));
        }
    }

    //---- Mandated by ColumnCellRenderer interface
    @Override
    public void init(ColumnManager cm) {
        inner.init(cm);
        this.cm = cm;

        cm.getTablePanel().addPropertyChangeListener(
                TablePanel.CELL_CLICKED_PROPERTY, new BorderListener());

        setBackground(Color.white);
    }

    private class BorderListener implements PropertyChangeListener {

        private boolean selected = false;

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            int selCol = ((CellClickedInfo) evt.getNewValue()).getModelColumn();
            boolean selChange = (selCol == cm.getColumnIndex()) != selected;
            if (selChange) { // check for model-view transform
//                System.err.println("border At col " + cm.getColumnIndex() + " detected click on " + selCol);
                selected = !selected;
                setBorder(BorderFactory.createLineBorder(selected ? Color.blue : Color.white, 1));
                cm.getTablePanel().getHeaderTable().repaint();
            }
        }
    }

    @Override
    public boolean canHandle(ColumnManager cm, boolean overview) {
        return inner.canHandle(cm, overview);
    }

    @Override
    public JPanel getSettingsPanel() {
        return inner.getSettingsPanel();
    }

    @Override
    public String getToolTipText(Point p) {

        // FIXME: if point is on labelling area,
        // may want to provide more details, or simply not pass it onwards...

        Point q = new Point(p.x - layout.hOffset, p.y);
        if ((q.x < 0) && ((inner instanceof AbstractRowRenderer)
                || (inner instanceof MultiColumnOverviewRenderer))) {
            if (!this.isAncestorOf(gLabel)) {
                return "";
            } else {
                return gLabel.getToolTipText(p);
            }
        }
        return inner.getToolTipText(q);
    }

    @Override
    public String getRendererName() {
        return inner.getRendererName();
    }

    @Override
    public String getXCaption() {
        return inner.getXCaption();
    }

    @Override
    public String getYCaption() {
        return inner.getYCaption();
    }

    @Override
    public int getMaximumVerticalSize() {
        return inner.getMaximumVerticalSize();
    }

    @Override
    public boolean updateHighlights() {
        return inner.updateHighlights();
    }

    @Override
    public void selectionDrag(JTable dest, Point start, Point end, Rectangle bounds, boolean isAdjusting) {
        bounds = new Rectangle(0, 0,
                layout.width - layout.hOffset, layout.height - layout.vOffset);
        Point s = new Point(start.x - layout.hOffset, start.y);
        Point e = new Point(end.x - layout.hOffset, end.y);
        inner.selectionDrag(dest, s, e, bounds, isAdjusting);
    }

    @Override
    public Component getTableCellRendererComponent(JTable jt, Object v, boolean bln, boolean bln1, int i, int i1) {
        try {
            inner.getTableCellRendererComponent(jt, v, bln, bln1, i, i1);
        } catch (Exception e) {
            System.err.println("Unable to load inner renderer of class " + inner.getClass().getSimpleName()
                    + " row " + i + " col " + i1 + " of table " + jt.getRowCount() + "x" + jt.getColumnCount());
            System.err.println("v= " + v);
        }
        return this;
    }

    @Override
    public ColumnCellRenderer copy() {
        LabelOverviewRenderer r = new LabelOverviewRenderer();
        ColumnCellRenderer ir = inner.copy();
        r.setInner(ir);
        return r;
    }
}
