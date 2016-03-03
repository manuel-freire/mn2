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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import javax.swing.SwingWorker;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;

/**
 * Constructs a relationship between two nodes if they are reachable following
 * a given path (specified by edge types) through a multi-modal network.
 * @author Manuel Freire
 */
public class ReachabilityRelBuilder extends RelationshipBuilder {

    public ReachabilityRelBuilder(PGraph pg, RelPath pathToFollow, Entity sourceEntity, Entity targetEntity){
        super(pg, pathToFollow, sourceEntity, targetEntity);
    }

    private HashMap<String, Integer> foundEdges = new HashMap<String, Integer>();

    @Override
    public ArrayList<RelPath> populatePath() {
        Graph erGraph = pg.getSchema().toGraph(pg);

        Node source = null;
        Node target = null;
        for (Iterator ni = erGraph.nodes(); ni.hasNext(); /**/) {
            Node n = (Node)ni.next();
            Entity ent = (Entity)n.get("Entity");
            if (ent.equals(sourceEntity)) source = n;
            if (ent.equals(targetEntity)) target = n;
        }
        if (source == null || target == null) {
            System.err.println(
                    "source (=" + source + " for " + sourceEntity.getId() + ") " +
                    "source (=" + target + " for " + targetEntity.getId() + ") " +
                    "not found when populating paths");
            return new ArrayList<RelPath>();
        }
        ArrayList<RelPath> paths = dfSearch(erGraph, source, source, target,
                new HashSet<Edge>(), new HashSet<Edge>(),
                new ArrayList<Edge>(), new ArrayList<RelPath>());
        return paths;
    }


    @Override
    protected void finishProcess(Table rt) {
        for (Map.Entry<String, Integer> e : foundEdges.entrySet()) {
            int pos = e.getKey().indexOf("_");
            int soid = Integer.parseInt(e.getKey().substring(0, pos));
            int toid = Integer.parseInt(e.getKey().substring(pos+1));
            int r = rt.addRow();
            rt.set(r, "ID", r);
            rt.set(r, "ID1", soid);
            rt.set(r, "ID2", toid);
            rt.set(r, "Weight", e.getValue());
        }
        foundEdges.clear();
    }

    /**
     * The walker will already be at the first valid position, so that n
     * is guaranteed to be a source-node for this walker's current state.
     */
    @Override
    protected void process(int sid, Node n, Walker w, Table rt) {
        int soid = pg.getEntityRef(sid).getId();

        for (Iterator ei = n.edges(); ei.hasNext(); /**/) {
            Edge e = (Edge)ei.next();
            Node t = e.getAdjacentNode(n);
            int eid = PGraph.getId(e);
            int tid = PGraph.getId(t);

            if ( ! w.canWalk(n, e, pg)) {
//                System.err.println("discarded due to bad pathrel " +
//                    "(" + pg.getRel(eid).getId() + " instead of " + w.current().getId() + ")" +
//                    " or bad nodetype " +
//                    "(" + pg.getEnt(tid).getId() + " instead of " + w.currentTarget().getId() + ")");
                continue;
            }

            // check termination or dive deeper
            if ( ! w.hasNext()) {
                // ok, termination reached. Avoid self-edges, count repetitions
                if (tid != sid) {
                    int toid = pg.getEntityRef(tid).getId();

                    String key = "" + soid + "_" + toid;
                    Integer count = null;
                    if ((count = foundEdges.get(key)) == null) {
                        foundEdges.put(key, 1);
                    } else {
                        foundEdges.put(key, count + 1);
                    }
                }
            } else {
                Walker nw = w.copy();
                nw.next();
                process(sid, t, nw, rt);
            }
        }
    }

    @Override
    protected void explore(Node n, Walker w, DefaultHistogramModel dhm, SwingWorker sw) {
       throw new UnsupportedOperationException("Not supported yet.");
    }    

    private static String show(Node n) {
        return ((Entity)n.get("Entity")).getId();
    }
    private static String show(Edge e) {
        return ((Relationship)e.get("Relationship")).getId();
    }
    private static String show(Collection<Edge> es) {
        StringBuilder sb = new StringBuilder("[");
        for (Edge e : es) sb.append(show(e) + ",");
        sb.setCharAt(sb.length()-1, ']');
        return sb.toString();
    }
    private ArrayList<RelPath> dfSearch(Graph g, Node start, Node current, Node target,
            HashSet<Edge> visitedOut, HashSet<Edge> visitedIn,
            ArrayList<Edge> currentEdges, ArrayList<RelPath> paths) {
//        System.err.println("current " + show(current) + " " + show(target) + " v (" +
//                show(visitedOut) + " out / " + show(visitedIn) + " in) c (" + show(currentEdges) + ")" +
//                " e " + paths.size());
        if (current.equals(target) && ! currentEdges.isEmpty()) {
            ArrayList<Relationship> rels = new ArrayList<Relationship>();
            for (Edge e : currentEdges) {
                rels.add((Relationship)e.get("Relationship"));
            }
            paths.add(new RelPath(start.equals(currentEdges.get(0).getSourceNode()), rels));
        }

        // this is NOT an 'else' - a->a and a->b->b->a are both legal
        for (Iterator ei = current.outEdges(); ei.hasNext(); /**/) {
            Edge e = (Edge)ei.next();
            Node other = e.getAdjacentNode(current);
//            System.err.println("   considering " + show(e) + " to " + show(other));
            if ( ! visitedOut.contains(e)) {
                visitedOut.add(e);

                ArrayList<Edge> es = new ArrayList<Edge>(currentEdges);
                es.add(e);
                dfSearch(g, start, other, target, visitedOut, visitedIn, es, paths);

                visitedOut.remove(e);
//                System.err.println("   >>");
            } else {
//                System.err.println("   dupe.");
            }
        }
        for (Iterator ei = current.inEdges(); ei.hasNext(); /**/) {
            Edge e = (Edge)ei.next();
            Node other = e.getAdjacentNode(current);
//            System.err.println("   considering " + show(e) + " to " + show(other));
            if ( ! visitedIn.contains(e)) {
                visitedIn.add(e);

                ArrayList<Edge> es = new ArrayList<Edge>(currentEdges);
                es.add(e);
                dfSearch(g, start, other, target, visitedOut, visitedIn, es, paths);

                visitedIn.remove(e);
//                System.err.println("   >>");
            } else {
//                System.err.println("   dupe.");
            }
        }
        return paths;
    }
}
