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
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * All renderers used here must subclass this interface. This allows them
 * to be pre-configured to column guidelines, and guarantees that they
 * can provide tooltips on-demand.
 */
public interface ColumnCellRenderer extends TableCellRenderer {

    /**
     * copies this CCR - same cm, same options.
     * @return
     */
    public ColumnCellRenderer copy();

    /**
     * Returns 'true' if the renderer can handle the intended data; this
     * will be called on an un-initialized renderer used only for this
     * purpose (that is, without 'init()' being called first.
     */
    public boolean canHandle(ColumnManager cm, boolean overview);

    /**
     * Initialize with a given ColumnManager. Because renderers should have a
     * zero-arg constructor for instantiation purposes.
     * @param cm
     */
    public void init(ColumnManager cm);

    /**
     * Returns a settings panel for this renderer. To fire or listen to events
     * on these settings register something on the render-options-property
     * defined in the AbstractRendererOPanel.
     */
    public JPanel getSettingsPanel();

    /**
     * To be called after getTableCellRendererComponent, with 'p'
     * relative to this cell's Rectangle
     */
    public String getToolTipText(Point p);

    /**
     * Returns the name of the renderer
     */
    public String getRendererName();

    /**
     * Returns a  textual description of the information found on the X axis
     */
    public String getXCaption();

    /**
     * Returns a  textual description of the information found on the Y axis
     */
    public String getYCaption();

    /**
     * Allows the renderer to configure side labels; will only be called if
     * enough space is present and getXCaption returns non-null
     */
    //public void configureXLabels();

    /**
     * Allows the renderer to configure side labels; will only be called if
     * enough space is present and getYCaption returns non-null
     */
    //public void configureYLabels();

    /**
     * Returns the maximum vertical space to be assigned to this renderer
     * Use ColumnManager.MAX_VERTICAL_RENDERING_SPACE for lots of vertical
     * space
     */
    public int getMaximumVerticalSize();

    /**
     * Update highlights in the renderer (assuming the renderer actually
     * supports highlights) due to a change in the current selection.
     * Return 'true' if the cell should be refreshed after this.
     */
    public boolean updateHighlights();

    /**
     * Drag gesture within a single visualization: if supported, update
     * selected cells in the specified table
     */
    public void selectionDrag(JTable dest, Point start, Point end,
            Rectangle bounds, boolean isAdjusting);
}
