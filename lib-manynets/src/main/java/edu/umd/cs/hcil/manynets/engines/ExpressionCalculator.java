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

import edu.umd.cs.hcil.manynets.model.Distribution;
import edu.umd.cs.hcil.manynets.model.PGraph;
import edu.umd.cs.hcil.manynets.model.Population.PopulationTableWrapper;
import edu.umd.cs.hcil.manynets.model.Ref;
import edu.umd.cs.hcil.manynets.model.Stat;
import edu.umd.cs.hcil.manynets.model.TableWrapper;
import edu.umd.cs.hcil.manynets.model.TableWrapper.Level;
import edu.umd.cs.hcil.manynets.hist.DefaultHistogramModel;
import edu.umd.cs.hcil.manynets.hist.DefaultHistogramModel.DataPoint;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.python.core.Py;
import org.python.core.PyFunction;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import prefuse.data.Tuple;
import prefuse.data.column.Column;

/**
 * A Calculator for python expressions; shares a common python interpreter for
 * all instances.
 *
 * @author Manuel Freire
 */
public class ExpressionCalculator extends AbstractCalculator {
    private String expression;
    private PyFunction function;
    private ArrayList<Argument> arguments = new ArrayList<Argument>();
    private static PythonInterpreter interpreter = new PythonInterpreter();

    private static String colRegex = "([A-Z][a-z]*)?\\[\\[([^]]+)]]";
    public static String getColExpression(String colName) {
        return "[[" + colName + "]]";
    }
    // Note: the 'name' is used to
    public enum ColPrefix {

        Default("", "Value", "Actual column value"),
        Min("Min", "Minimum", "Minimum of all values in the column"),
        Max("Max", "Maximum", "Maximum of all values in the column"),
        Avg("Avg", "Average", "Average of all values in the column"),
        Stdev("Stdev", "Standard Dev.", "Standard deviation of all values in the column"),
        Var("Var", "Variance", "Variance of all values in the column"),
        List("List", "List of values", "A list with all the values in the column"),

        Count("Count", "Count", "Count values in a distribution"),

        Nodes("Nodes", "List of node-values", "A list with all the values in the specified " +
                "column within the nodes in this graph", Level.Network),
        Edges("Edges", "List of edge-values", "A list with all the values in the specified " +
                "column within the edges in this graph", Level.Network);

        public final String prefix;
        public final String name;
        public final String description;
        public final Level tableLevel;
        ColPrefix(String prefix, String name, String description) {
            this(prefix, name, description, null);
        }
        ColPrefix(String prefix, String name, String description, Level tableLevel) {
            this.prefix = prefix;
            this.name = name;
            this.description = description;
            this.tableLevel = tableLevel;
        }
        @Override
        public String toString() { return name; }
        public boolean canUse(Level level) {
            return tableLevel == null || tableLevel == level;
        }
    }

    // incremented once for every precomputed function, for debugging purposes
    private static int functionId = 1;

    public ExpressionCalculator(Stat stat, String expression, String complexity) {
        super(new Stat[] { stat }, complexity);
        this.expression = expression;
        try {
            function = compile();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public PyFunction compile() throws Exception {

        /*
         * Example:
         * Column['Node count'] == 3.0 and Column['Edge-node ratio'] >= 1.0
         *
         * becomes:
         * >>> def function1(arg1, arg2):
         * ...     arg1 == 3.0 and arg2 >= 1.0
         * ...
         * >>> function1
         * and the result of evaluating the last statement (function1) is
         * saved for later reuse.
         *
         * arguments for this function, given a Tuple 't', would be
         *    arg1 = new PyDouble(t.get('Node count'));
         *    arg2 = new PyDouble(t.get('Edge-node ratio'));
         */

        expression = expression.trim();
        String returningExpression = expression;

        // require a one-liner or a 'return' statement somewhere
        if ( ! returningExpression.contains("return ")) {
            if (returningExpression.contains("\n")) {
                throw new IllegalArgumentException(
                        "Multi-line expressions " +
                        "must have at least one 'return' statement");
            } else {
                returningExpression = "return " + returningExpression;
            }
        }

        // find the arguments (column names) and generate function code
        Matcher m = Pattern.compile(colRegex).matcher(returningExpression);
        StringBuffer sb = new StringBuffer();
        StringBuilder argumentList = new StringBuilder("(");
        while (m.find()) {
            Argument arg = new Argument(
                    arguments.size()+1, m.group(1), m.group(2));
            arguments.add(arg);
            m.appendReplacement(sb, arg.argName);
            argumentList.append(arg.argName + ",");
            System.err.println(arg);
        }
        m.appendTail(sb);
        // empty arguments are possible (!)
        if (argumentList.length() == 1) {
            argumentList.append(",");
        }
        argumentList.setCharAt(argumentList.length()-1, ')');

        String functionName = "function" + (functionId ++);
        String functionDef = "def " + functionName + argumentList + ":\n\t"
                + sb.toString().replaceAll("\n", "\n\t")
                + "\n\t";

        System.err.println(functionDef);
        interpreter.exec(functionDef);
        return (PyFunction)interpreter.get(functionName);
    }
    
    public PyObject evalArgs(Object ...values) {
        PyObject[] args = new PyObject[arguments.size()];
        int i=0;
        for (Object v : values) {
            args[i++] = Py.java2py(v);
        }
        return function.__call__(args);
    }

    public PyObject evalTuple(Tuple t, TableWrapper tw) {
        PyObject[] args = new PyObject[arguments.size()];
        int i=0;
        for (Argument arg : arguments) {
            args[i++] = arg.instantiate(t, tw);
        }
        return function.__call__(args);
    }

    @Override
    public void calculate(TableWrapper tw, int id) {
        checkAddStat(provides[0], tw);
        Stat s = provides[0];
        Object o = null;
        if (Distribution.class.isAssignableFrom(s.getType())) {
            PyList po = (PyList)evalTuple(tw.getTuple(id), tw);
            DefaultHistogramModel dhm = new DefaultHistogramModel();
            Ref ref = tw.getRef(id);
            for (Iterator<PyObject> li = po.iterator(); li.hasNext(); /**/) {
                dhm.addValue(Py.tojava(li.next(), Double.TYPE), ref);
            }
            o = dhm;
        } else {
            o = Py.tojava(evalTuple(tw.getTuple(id), tw), s.getType());
        }
        tw.getTable().set(tw.getRow(id), s.getName(), o);
    }

    /**
     * An argument for the generated python-expression
     */
    private static class Argument {
        public String argName;
        public String special; // null, or Min, Max, Med, Var, Dev
        public String colName;
        public Argument(int index, String special, String colName) {
            this.argName = "arg"+index;
            this.special = special;
            this.colName = colName;
        }
        public PyObject instantiate(Tuple t, TableWrapper tw) {
            int ci = t.getColumnIndex(colName);
//            if ()
            return special != null ?
                replaceComplex(special, t, ci, tw) :
                replace(t.get(ci));
        }
        @Override
        public String toString() {
            return argName + (special == null ? " value" : " " + special)
                    + " of '" + colName + "'";
        }
    }

    /**
     * Create a python-object replacement for a Java object.
     * If the object is a
     * - string, return it in quotes
     * - number, return it as-is
     * - distribution of either, return [ r1 r2 ... rn ]
     * @param o
     * @return
     */
    private static PyObject replace(Object o) {
        if (o instanceof DefaultHistogramModel) {
            DefaultHistogramModel d = (DefaultHistogramModel)o;
            PyList pl = new PyList();
            for (Entry<Double,DataPoint> e : d.getData().entrySet()) {
                // FIXME: use a better Python mapping for distributions
                if (d.isNominal()) {
                    for (int i=0; i<e.getValue().count; i++) {
                        System.err.println("appended " + d.getValueLabel(e.getKey()));
                        pl.append(Py.java2py(d.getValueLabel(e.getKey())));
                    }
                } else {
                    for (int i=0; i<e.getValue().count; i++) {
                        pl.append(Py.java2py(e.getKey()));
                    }
                }
            }
            return pl;
        } else {
            return Py.java2py(o);
        }
    }

    private static PyObject columnAsList(Column col) {
        PyList v = new PyList();
        for (int i=0; i<col.getRowCount(); i++) {
            v.__add__(Py.java2py(col.get(i)));
        }
        return v;
    }

    private static PyObject replaceComplex(String key, Tuple t, int colIndex, TableWrapper tw) {
        ColPrefix prefix = ColPrefix.valueOf(key);

        if (key.equals("Dist")) {
            return columnAsList(t.getTable().getColumn(colIndex));
        } else if (key.equals("Node")) {
            PGraph pg = ((PopulationTableWrapper)tw).getGraph(t.getInt(tw.getIdField()));
            return columnAsList(pg.getNodeTable().getTable().getColumn(colIndex));
        } else if (key.equals("Edge")) {
            // would like to access all edges in t, known to be a graph
            PGraph pg = ((PopulationTableWrapper)tw).getGraph(t.getInt(tw.getIdField()));
            return columnAsList(pg.getEdgeTable().getTable().getColumn(colIndex));
        }

        DefaultHistogramModel d =  (DefaultHistogramModel)t.get(colIndex);
        double v = Float.NaN;
        switch (prefix) {
            case Count: v = d.count(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY); break;
            case Max: v = d.getMax(); break;
            case Min: v = d.getMin(); break;
            case Avg: v = d.getAvg(); break;
            case Var: v = d.getVariance(); break;
            case Stdev: v = d.getStandardDeviation(); break;
        }
        return Py.java2py(v);
    }

    public String getExpression() {
        return expression;
    }
}
