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
import edu.umd.cs.hcil.manynets.ui.ColumnManager.AbstractRendererOPanel;
import edu.umd.cs.hcil.manynets.ui.renderers.AbstractRowRenderer.Metric;
import edu.umd.cs.hcil.manynets.ui.renderers.AbstractRowRenderer.Sorting;
import edu.umd.cs.hcil.manynets.ui.renderers.HeatmapOverviewRenderer.IntensityEmphasis;
import edu.umd.cs.hcil.manynets.ui.renderers.HeatmapOverviewRenderer.IntensityType;
import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.Collection;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Manuel Freire
 */
public class MultiColumnOPanel extends AbstractRendererOPanel {

    private MultiColumnOverviewRenderer renderer;

    /** Creates new form HistogramOptionsPanel */
    public MultiColumnOPanel(MultiColumnOverviewRenderer renderer) {

        this.renderer = renderer;

        // sorting order and metrics (metrics only active for certain sortings)
        addOption(new ChoiceOption<MultiColumnOverviewRenderer>(
            "Sort by", "The sorting order to use (works on 1st col, except for multicluster sort)",
            "sortAttribute", (Collection)Arrays.asList(Sorting.values())));
        beginInner(new InnerOption<MultiColumnOverviewRenderer, Object>(
                "Metric", "<html>Configure the metric used to compare histograms for<br>"
                + " similarity and clustering purposes</html>") {
            @Override
            public boolean isValid(MultiColumnOverviewRenderer target) {
                return target.getSortAttribute().simBased;
            }
        })
        .addInner(new ChoiceOption<MultiColumnOverviewRenderer>(
            "Compare by", "The sorting order to use",
            "sortMetric", (Collection)Arrays.asList(Metric.values())))
        .endInner();

        // color intensity
        beginInner(new InnerOption("Color intensity", "Configure how color intensity is assigned to row values"))
        .addInner(new ChoiceOption<AbstractRowRenderer>(
            "Maximum", "<html>Scale intensities relative to <i>global</i> (all rows) maximum"
            + " <br>or to <i>each</i> rows' maximum value",
            "intensityType", (Collection)Arrays.asList(IntensityType.values())))
        .addInner(new ChoiceOption<AbstractRowRenderer>(
            "Emphasis", "<html>Choose range of values to emphasize <br>"
            + " by giving them a larger share of the color palette",
            "intensity", (Collection)Arrays.asList(IntensityEmphasis.values())))
        .endInner();

        // columns
        JPanel jpColumns = new JPanel(new BorderLayout());
        jtfOtherColumns = new JTextField("1,3,5");
        jtfOtherColumns.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtfOtherColumnsActionPerformed(evt);
            }
        });
        JButton jbEditColumns = new JButton("Edit...");
        jbEditColumns.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbEditColumnsActionPerformed(evt);
            }
        });
        jpColumns.add(jtfOtherColumns, BorderLayout.CENTER);
        jpColumns.add(jbEditColumns, BorderLayout.EAST);
        addConfigLine("Columns",
            "Columns to show in this overview; use the button to change",
            jpColumns);

        // no more options after this
        endOptions();

        endSetupAndPrepareListener(renderer);
    }

    @Override
    public void initialize() {
        jtfOtherColumns.setText(renderer.getColumnString());
        super.initialize();
    }

    private JTextField jtfOtherColumns;

    private void jtfOtherColumnsActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:

        renderer.setColumnString(jtfOtherColumns.getText());
        fireOptionsChanged();
    }

    private void jbEditColumnsActionPerformed(java.awt.event.ActionEvent evt) {
        MultiColumnChooser mcc = new MultiColumnChooser();
        mcc.init(renderer.getColumns().iterator().next().getTablePanel(), renderer);
        mcc.setVisible(true);
        if (mcc.isAccepted()) {
            renderer.setColumns(mcc.getSelection());
            jtfOtherColumns.setText(renderer.getColumnString());
            fireOptionsChanged();
        }
    }
}
