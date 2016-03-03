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

import edu.umd.cs.hcil.manynets.hist.DefaultHistogramModel;
import edu.umd.cs.hcil.manynets.model.RelationshipBuilder.RelPath.Walker;
import edu.umd.cs.hcil.manynets.model.Schema.Entity;
import edu.umd.cs.hcil.manynets.model.Schema.Relationship;
import edu.umd.cs.hcil.manynets.model.SchemaInstance.EntityTableWrapper;
import edu.umd.cs.hcil.manynets.model.SchemaInstance.RelTableWrapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.SwingWorker;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;

/**
 * Interface to be implemented by relationship builders (methods to derive
 * new relationships from a graph)
 *
 * @author Manuel Freire
 */
public abstract class RelationshipBuilder {

    protected PGraph pg;
    protected RelPath pathToFollow;
    protected Entity sourceEntity;
    protected Entity targetEntity;
    protected boolean cancelled;
    protected double progress;

    public RelationshipBuilder(PGraph pg, RelPath pathToFollow, Entity sourceEntity, Entity targetEntity) {
        this.pg = pg;
        this.pathToFollow = pathToFollow;
        this.sourceEntity = sourceEntity;
        this.targetEntity = targetEntity;
    }

    public boolean graphHasEnt(Entity e) {        
        return graphHasEnt(pg, e);
    }


    public boolean graphHasRel(Relationship r) {
        for (RelTableWrapper rtw : pg.getEdgeTables()) {
            if (rtw.getRelationship().equals(r)) {
                return true;
            }
        }
        return false;
    }

    public static boolean graphHasEnt(PGraph pg, Entity e) {
        for (EntityTableWrapper etw : pg.getNodeTables()) {
            if (etw.getEntity().equals(e)) {
                return true;
            }
        }
        return false;
    }

    public RelTableWrapper build(String relName) {
        System.err.println("building rel " + relName);

        // create or replace
        pg.getSchema().addRelationship(relName,
                "ID", sourceEntity.getId(), "ID1", targetEntity.getId(), "ID2", true);
        Relationship nrel = pg.getSchema().getRel(relName);

        SchemaInstance si = null;
        for (EntityTableWrapper etw : pg.getNodeTables()) {
            if (etw.getEntity().equals(sourceEntity)) {
                si = etw.getSchemaInstance();
                break;
            }
        }

        // populate table
        Table t = new Table(0, 4);
        t.addColumn("ID", Integer.TYPE);
        t.addColumn("ID1", Integer.TYPE);
        t.addColumn("ID2", Integer.TYPE);
        t.addColumn("Weight", Integer.TYPE);
        fillRelTable(t);

        // build wrapper
        RelTableWrapper rtw = new RelTableWrapper(si, nrel, t, true);
        si.getRelationships().add(rtw);
        si.crossReference();
        return rtw;
    }

    public DefaultHistogramModel explore(ProgressableWorker sw) {
        Graph sg = pg.getGraph();
        DefaultHistogramModel dhm = new DefaultHistogramModel();
        Walker w = pathToFollow.walker();
        w.next();

        System.err.println("about to consider " + sg.getNodeCount()
                + " nodes, src=" + sourceEntity.getId());
        // load all the nodes that have the specified value in the splitfield
        double delta = 1.0 / sg.getNodeCount();
        progress = 0;
        for (Iterator ni = sg.nodes(); ni.hasNext() && !sw.isCancelled(); /**/) {
            Node n = (Node) ni.next();
            int id = PGraph.getId(n);
            if (!pg.getEnt(id).equals(sourceEntity)) {
                // System.err.println("discarded " + n.getString(PGraph.labelField));
                continue;
            } else {
                System.err.println("CONSIDERED " + n.getString(PGraph.labelField));
            }
            try {
                explore(n, w, dhm, sw);
            } catch (Exception e) {
                e.printStackTrace();
            }
            progress += delta;
            sw.updateProgress((int) (progress * 100));
        }
        return dhm;
    }

    private void fillRelTable(Table rt) {
        Walker w = pathToFollow.walker();
        w.next();

        Graph sg = pg.getGraph();
        // load all the nodes that have the specified value in the splitfield
        double delta = 1.0 / sg.getNodeCount();
        for (Iterator ni = sg.nodes(); ni.hasNext() && !cancelled; /**/) {
            Node n = (Node) ni.next();
            int id = n.getInt(PGraph.idField);
            if (!pg.getEnt(id).equals(sourceEntity)) {
                continue;
            }
            process(id, n, w, rt);
            progress += delta;
        }
        finishProcess(rt);
    }

    /**
     * Process a node that may result in an addition to the relTable
     * @param n
     * @param w
     * @param rt
     */
    protected abstract void process(int sid, Node n, Walker w, Table rt);

    /**
     * Finish a processing run
     */
    protected void finishProcess(Table rt) {
        // by default, do nothing
    }

    /**
     * Find possible paths for this relationship
     * @param sourceEntity
     * @param targetEntity
     * @return
     */
    public abstract ArrayList<RelPath> populatePath();

    /**
     * Generate a histogram with the edges that this builder will generate
     * @param pg
     * @return
     */
    protected abstract void explore(Node n, Walker w, DefaultHistogramModel dhm, SwingWorker sw);

    /**
     * A SwingWorker with support for setting progress
     */
    public static abstract class ProgressableWorker extends SwingWorker {
        /** the default setProgress is final and protected; we need it public */
        public void updateProgress(int p) {
            super.setProgress(p);
        }
    }

    /**
     * A path through several relationships in an ER graph
     */
    public static class RelPath {

        private boolean forward;
        private ArrayList<Relationship> path = new ArrayList<Relationship>();

        public RelPath(boolean forward, Collection<Relationship> rs) {
            this.forward = forward;
            for (Relationship r : rs) {
                path.add(r);
            }
        }

        public RelPath(boolean forward, Relationship... rs) {
            this.forward = forward;
            for (Relationship r : rs) {
                path.add(r);
            }
        }

        public Walker walker() {
            return new Walker();
        }

        public class Walker implements Iterator<Relationship> {

            int step;
            private Entity source;
            private Entity target;
            private Relationship rel;

            private Walker() {
                target = forward
                        ? path.get(0).getSource() : path.get(0).getTarget();
                step = 0;
            }

            public Walker copy() {
                Walker w = new Walker();
                w.step = step;
                w.source = source;
                w.target = target;
                w.rel = rel;
                return w;
            }

            @Override
            public boolean hasNext() {
                return step < path.size();
            }

            @Override
            public Relationship next() {
                source = target;
                rel = path.get(step++);
                target = rel.getTarget() != source
                        ? rel.getTarget() : rel.getSource();
                return rel;
            }

            /**
             * tries to walk through an edge, assuming correct source node type.
             * Returns false if bad edge type, or bad destination type.
             * @param e
             * @return
             */
            public boolean canWalk(Node sn, Edge e, PGraph pg) {
                int eid = PGraph.getId(e);
                int tid = PGraph.getId(e.getAdjacentNode(sn));
                return pg.getRel(eid).equals(rel) && pg.getEnt(tid).equals(target);
            }

            public Relationship current() {
                return rel;
            }

            public Entity currentSource() {
                return source;
            }

            public Entity currentTarget() {
                return target;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }

        @Override
        public String toString() {
            if (path.isEmpty()) {
                return "<no path>";
            }

            StringBuffer sb = new StringBuffer();
            Walker w = walker();
            while (w.hasNext()) {
                w.next();
                sb.append(w.current().getId() + " to " + w.currentTarget().getId() + ">");
            }
            return (forward ? "outgoing: " : "incoming: ")
                    + sb.substring(0, sb.lastIndexOf(">"));
        }

        public ArrayList<Relationship> getPath() {
            return path;
        }

        public boolean isForward() {
            return forward;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof RelPath)) {
                return false;
            }
            return toString().equals(o.toString());
        }

        @Override
        public int hashCode() {
            return toString().hashCode();
        }
    }
}
