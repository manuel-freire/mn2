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

import edu.umd.cs.hcil.manynets.ui.ColumnManager.AbstractRendererOPanel;
import edu.umd.cs.hcil.manynets.ui.renderers.AbstractRowRenderer.Metric;
import edu.umd.cs.hcil.manynets.ui.renderers.AbstractRowRenderer.Sorting;
import edu.umd.cs.hcil.manynets.ui.renderers.HeatmapOverviewRenderer.ColorScheme;
import edu.umd.cs.hcil.manynets.ui.renderers.HeatmapOverviewRenderer.IntensityEmphasis;
import edu.umd.cs.hcil.manynets.ui.renderers.HeatmapOverviewRenderer.IntensityType;
import edu.umd.cs.hcil.manynets.ui.renderers.HeatmapOverviewRenderer.RowAggregator;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author manuel, sopan
 */
public class HeatmapOPanel extends AbstractRendererOPanel {

    /** Creates new form HeatmapOPanel */
    public HeatmapOPanel(HeatmapOverviewRenderer renderer) {

        // sorting order and metrics (metrics only active for certain sortings)
        addOption(new ChoiceOption<AbstractRowRenderer>(
            "Sort by", "The sorting order to use",
            "sortAttribute", (Collection)Arrays.asList(Sorting.values())));
        beginInner(new InnerOption<AbstractRowRenderer, Object>(
                "Metric", "<html>Configure the metric used to compare histograms for<br>"
                + " similarity and clustering purposes</html>") {
            @Override
            public boolean isValid(AbstractRowRenderer target) {
                return target.getSortAttribute().simBased;
            }
        })
        .addInner(new ChoiceOption<AbstractRowRenderer>(
            "Compare by", "The sorting order to use",
            "sortMetric", (Collection)Arrays.asList(Metric.values())))
        .endInner();
        
        // row aggregation
        addOption(new ChoiceOption<AbstractRowRenderer>(
            "Row aggregation", "Values to show when there are many rows crammed into a single line of pixels",
            "rowAggregator", (Collection)Arrays.asList(RowAggregator.values())));

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

        // and color selection
        addOption(new ChoiceOption<AbstractRowRenderer>(
            "Colors", "Color palette to use for row values",
            "colorScheme", (Collection)Arrays.asList(ColorScheme.values())));

        // no more options after this
        endOptions();

        endSetupAndPrepareListener(renderer);
    }
}
