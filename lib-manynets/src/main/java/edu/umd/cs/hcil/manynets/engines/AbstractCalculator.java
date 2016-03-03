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

package edu.umd.cs.hcil.manynets.engines;

import edu.umd.cs.hcil.manynets.model.Stat;
import edu.umd.cs.hcil.manynets.model.StatCalculator;
import edu.umd.cs.hcil.manynets.model.TableWrapper;
import java.util.Collection;

/**
 * Default calculator. Should be subclassed to do any actual calculating.
 * @author Manuel Freire
 */
public abstract class AbstractCalculator implements StatCalculator {

    protected Stat[] provides;
    protected String complexity;
    protected boolean canceled = false;

    public AbstractCalculator(Stat[] provides, String complexity) {
        this.complexity = complexity;
        this.provides = provides;
        for (Stat stat : provides) {
            stat.setProvider(this);
        }
    }

    @Override
    public void cancel() {
        canceled = true;
    }

    public void addStatsTo(Collection<Stat> stats) {
        for (Stat stat : provides) {
            stats.add(stat);
        }
    }

    @Override
    public String getComplexity() {
        return complexity;
    }

    protected boolean checkAddStat(Stat stat, TableWrapper tw) {
        if (tw.getStatCol(stat) < 0) {
            return tw.addStat(stat);
        } else {
            return false;
        }
    }
}
