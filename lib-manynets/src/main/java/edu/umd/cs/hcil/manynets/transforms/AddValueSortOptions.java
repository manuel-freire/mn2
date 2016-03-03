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

import edu.umd.cs.hcil.manynets.engines.ExpressionCalculator;
import edu.umd.cs.hcil.manynets.model.Population;
import edu.umd.cs.hcil.manynets.ui.PythonFilterDialog;
import edu.umd.cs.hcil.manynets.vq.Objective;
import edu.umd.cs.hcil.manynets.vq.Ranking;
import edu.umd.cs.hcil.manynets.vq.ValueFunction;
import java.util.Collection;
import java.util.Iterator;
import org.jdom.Element;
import prefuse.data.Table;
import prefuse.data.Tuple;

/**
 * Adds a "ValueSort" column
 * 
 * @author Manuel Freire
 */
public class AddValueSortOptions {
    private String colName;
    private PythonFilterDialog pfd;
    private Ranking r;

    public String getColName() {
        return colName;
    }

    public AddValueSortOptions(String colName, Collection<ExpressionCalculator> ecs) {
        this.colName = colName;
        r = new Ranking();
        float w = 1.0f / ecs.size();
        for (ExpressionCalculator ec : ecs) {
            r.getObjectives().add(new
                    Objective(new LinearEvalValueFunction(ec), w));
        }
    }

    public static class LinearEvalValueFunction implements ValueFunction<ComplexSortObject> {
        private ExpressionCalculator ec;
        private float min = Float.POSITIVE_INFINITY;;
        private float max = Float.NEGATIVE_INFINITY;;
        boolean initialized = false;
        public LinearEvalValueFunction(ExpressionCalculator ec) {
            this.ec = ec;
        }

        public float valueFor(ComplexSortObject o) {
            if ( ! initialized) {
                Iterator ti = o.t.tuples();
                while (ti.hasNext()) {
                    // FIXME - null??
                    float f = (float)ec.evalTuple((Tuple)ti.next(), null).asDouble();
                    if (f > max) max = f;
                    if (f < min) min = f;
                }
                initialized = true;
            }
            float f = (float)ec.evalTuple(o.t.getTuple(o.row), null).asDouble();
            return max == min ? 1 : ((f - min) / (max - min));
        }
    }

    public Class getColType() {
        return ComplexSortObject.class;
    }

    public void save(Element e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void load(Element e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getDescription() {
        return "Not supported yet.";
    }

    public Object getValue(Population pop, int id) {
        int row = pop.getGraphRow(id);
        return new ComplexSortObject(pop.getAttributes(), row, r);
    }

    /**
     * Data cells will hold one of these each. It is ugly to add cell-renderers
     * everywhere.
     */
    public static class ComplexSortObject 
            implements Comparable<ComplexSortObject>, Ranking.Listener {
        public Table t;
        public int row;
        public Ranking ranking;

        private float cachedValue = Float.NaN;

        public ComplexSortObject(Table t, int row, Ranking r) {
            this.t = t;
            this.row = row;
            this.ranking = r;
            r.addRankingListener(this);
        }
        
        public float getValue() {
            if (Float.isNaN(cachedValue)) {
                cachedValue = ranking.score(this);
            }
            return cachedValue;
        }

        public int compareTo(ComplexSortObject o) {
            return Float.compare(o.getValue(), getValue());
        }

        public void valuesChanged(Ranking r) {
            cachedValue = Float.NaN;
        }
    }
}
