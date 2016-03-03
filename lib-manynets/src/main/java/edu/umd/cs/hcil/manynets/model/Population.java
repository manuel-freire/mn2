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

package edu.umd.cs.hcil.manynets.model;

import edu.umd.cs.hcil.manynets.model.TableWrapper.Level;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import prefuse.data.Table;

/**
 * A set of networks and their attributes. Derives from a Transform.
 *
 * @author Manuel Freire
 */
public class Population {

    private Map<Integer, PGraph> graphs = new TreeMap<Integer, PGraph>();
    private Map<String, Integer> graphNamesToIds = new TreeMap<String, Integer>();
    private PopulationTableWrapper attributes;
    public static final String labelField = "Label";
    public static final String idField = "ID";
    private int nextGraphId = 0;
    private Population parent = null;

    public Table getAttributes() {
        return attributes.getTable();
    }

    public TableWrapper getWrappedAttributes() {
        return attributes;
    }

    /**
     * Initializes the schema for the graph attribute table attributes
     */
    public Population(String name) {
        attributes = new PopulationTableWrapper(name);
    }

    /**
     * Copies one population to another, but does not keep the previous data
     */
    public Population(String name, Population p) {
        this(name, p, false);
    }

    /**
     * Copies one pop-explorer to another, keeping the previous data
     * only if keepAttributes is true.
     */
    public Population(String name, Population p, boolean keepAttributes) {
        if (keepAttributes) {
            throw new UnsupportedOperationException("Not yet implemented");
        } else {
            attributes = new PopulationTableWrapper(name);
        }
        for (PGraph pg : p.getGraphs()) {
            addGraph(pg);
        }
    }

    public Collection<Integer> getGraphIds() {
        return graphs.keySet();
    }

    public Collection<PGraph> getGraphs() {
        return graphs.values();
    }

    /**
     * Add a new graph
     * @param pg
     */
    public void addGraph(PGraph pg) {
        if (getWrappedAttributes().detach()) {
            // population attributes have been cloned... this is a problem.
            // since the PGraph will be added to the clone, but not the original
            throw new IllegalArgumentException("FIXME: adding graphs to a " +
                    "filtered table of graphs is not currently allowed.");
        }
        Table at = getAttributes();

        if (graphNamesToIds.containsKey(pg.getName())) {
            throw new UnsupportedOperationException("\t WARNING: graph "
                    + pg.getName() + " already present with ID "
                    + graphNamesToIds.get(pg.getName()));
        }

        int id = nextGraphId;
        graphNamesToIds.put(pg.getName(), id);
        graphs.put(id, pg);
        int row = at.addRow();
        at.set(row, idField, id);
        at.set(row, labelField, pg.getName());
        System.err.println("\t added " + pg.getName() + " with nCols="
                + at.getColumnCount() + " and nNodes="
                + pg.getGraph().getNodeCount() + " and nEdges="
                + pg.getGraph().getEdgeCount()
                + " as ID " + id);

        pg.setRef((GraphRef) attributes.getRef(id));
        nextGraphId++;
    }

    public void removeGraph(PGraph pg) {
        int id = graphNamesToIds.remove(pg.getName());
        getAttributes().removeRow(attributes.getRow(id));
        graphs.remove(id);
    }

    public String getName() {
        return attributes.getName();
    }

    public PGraph getGraph(int id) {
        return graphs.get(id);
    }

    public int getGraphRow(int id) {
        return attributes.getRow(id);
    }

    public boolean isDirected() {
        if (graphs.size() == 0) {
            return false;
        }
        return graphs.values().iterator().next().getGraph().isDirected();
    }

    public class PopulationTableWrapper extends TableWrapper {

        public PopulationTableWrapper(String name) {
            super(name, new Table(), true, idField, Level.Edge.Network);
            addStat(new Stat(labelField,
                    "Label of network", String.class,
                    Level.Network, Level.Network));
        }

        public PopulationTableWrapper(PopulationTableWrapper tw) {
            super(tw);
        }        
        
        /**
         * copy from parent, should be subclasses for type-safety
         */
        @Override
        public PopulationTableWrapper copy() {
            return new PopulationTableWrapper(this);
        }

        public Population getPopulation() {
            return Population.this;
        }

        public PGraph getGraph(int id) {
            return Population.this.getGraph(id);
        }

        @Override
        public Ref createRef(int id) {
            return new GraphRef(this, id, Population.this.getGraph(id));
        }

        /**
         * Different graphs may have different or duplicate edges-stats or
         * node-stats. Entity or Relationships can fortunately only be
         * duplicate as a whole.
         * @return
         */
        @Override
        public ArrayList<TableWrapper> getChildWrappers() {
            ArrayList<TableWrapper> al = new ArrayList<TableWrapper>();
            if (!graphs.isEmpty()) {
                PGraph pg = graphs.values().iterator().next();
                al.addAll(pg.getNodeTable().getChildWrappers());
                al.addAll(pg.getEdgeTable().getChildWrappers());
                al.add(pg.getNodeTable());
                al.add(pg.getEdgeTable());
            }
            // FIXME: we are not looking further, but there could be other
            // interesting stats that we are missing because of alternate
            // sets of stats in the remaining tables of nodes and edges...
            return al;
        }
    }

    public static class GraphRef extends Ref {

        private PGraph pg;

        public GraphRef(PopulationTableWrapper ptw, int id, PGraph pg) {
            super(null, ptw, id);
            this.pg = pg;
        }

        @Override
        public Ref getParentRef() {
            return null;
        }

        public PGraph getPGraph() {
            return pg;
        }
    }
}
