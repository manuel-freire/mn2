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

package edu.umd.cs.hcil.manynets.transforms;

import edu.umd.cs.hcil.manynets.model.Stat;
import edu.umd.cs.hcil.manynets.model.TableWrapper;
import edu.umd.cs.hcil.manynets.model.TransformOptions;
import edu.umd.cs.hcil.manynets.model.TransformResults;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import org.jdom.Element;

/**
 * A transform that calculates a set of stats.
 * @author Manuel Freire
 */
public class CalculateStatsTransform extends AbstractTransform {

    @Override
    public void init(TableWrapper tw, TransformOptions options, TransformResults results) {
        super.init(tw, options, results);
    }

    @Override
    public synchronized void apply(int id, ProgressMonitor pm) {
        
        Collection<Stat> stats = ((StatsOptions)options).getStats();
        double delta = 1.0 / stats.size();
        double progress = 0;

        for (Stat s : stats) {
            pm.setProgress(id, "calculating stat " + s, progress);
            progress += delta;
            if (canceled) break;
            s.getProvider().calculate(tw, id);
        }
        
        pm.setProgress(id, "finished", 1);
    }

    /**
     * Options for stat calculation - the stat columns that should
     * be displayed later on.
     */
    public static class StatsOptions implements TransformOptions {

        private Set<Stat> stats = new TreeSet<Stat>();

        public StatsOptions(Collection<Stat> stats) {
            this.stats.addAll(stats);
        }

        public StatsOptions() {
        }
        
        public Set<Stat> getStats() {
            return stats;
        }

        @Override
        public void save(Element e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void load(Element e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getDescription() {
            return "" + stats.size() + " stats";
        }
    }

    /**
     * Results from a stat calculation
     */
    public static class StatsResults implements TransformResults {

        @Override
        public void save(Element e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void load(Element e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
