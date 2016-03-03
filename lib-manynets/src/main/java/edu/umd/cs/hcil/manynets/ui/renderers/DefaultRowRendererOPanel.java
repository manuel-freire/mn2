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
import edu.umd.cs.hcil.manynets.ui.renderers.AbstractRowRenderer.Sorting;
import edu.umd.cs.hcil.manynets.ui.ConfigPanel.ChoiceOption;
import java.util.Arrays;
import java.util.Collection;


/**
 *
 * @author Manuel Freire
 */
public class DefaultRowRendererOPanel extends AbstractRendererOPanel<AbstractRowRenderer> {

    /** Creates new form HistogramOptionsPanel */
    public DefaultRowRendererOPanel(AbstractRowRenderer renderer) {

        addOption(new ChoiceOption<AbstractRowRenderer>(
            "Sort by", "The sorting order to use",
            "sortAttribute", (Collection)Arrays.asList(Sorting.values())));
        endOptions();

        endSetupAndPrepareListener(renderer);
    }
}
