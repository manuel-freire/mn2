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
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.SwingWorker;
import prefuse.data.Edge;
import prefuse.data.Node;
import prefuse.data.Table;

/**
 *
 * @author Manuel Freire
 */
public class SharedTargetRelBuilder extends RelationshipBuilder {

    private int low;
    private int high;
    private Relatedness rf;

    public SharedTargetRelBuilder(PGraph pg, RelPath pathToFollow, Entity sourceEntity, Entity targetEntity){
        super(pg, pathToFollow, sourceEntity, targetEntity);
    }

    public void configure(Relatedness rf, int low, int high) {
        this.rf = rf;
        this.low = low;
        this.high = high;
    }

    @Override
    public ArrayList<RelPath> populatePath() {

        ArrayList<RelPath> paths = new ArrayList<RelPath>();
//
//        for (Entity e : pg.getSchema().getEntities().values()) {
//            System.err.println(e.getId() + " " + e.getIncoming().size() + " " + e.getOutgoing().size());
//        }

        for (Relationship r : sourceEntity.getIncoming()) {
           if ( ! graphHasRel(r)) continue;
           paths.add(new RelPath(false, r));
        }
        for (Relationship r : sourceEntity.getOutgoing()) {
           if ( ! graphHasRel(r)) continue;
           paths.add(new RelPath(true, r));
        }
        return paths;
    }

    @Override
    protected void process(int sid, Node n, Walker w, Table rt) {
        int soid = pg.getEntityRef(PGraph.getId(n)).getId();

        HashMap<Integer, CNode> friends = new HashMap<Integer, CNode>();
        for (Iterator ei = n.edges(); ei.hasNext() && !cancelled; /**/) {
            Edge e = (Edge) ei.next();
            if ( ! w.canWalk(n, e, pg)) {
                continue;
            }
            Node sn = e.getAdjacentNode(n);
            for (Iterator sei = sn.edges(); sei.hasNext(); /**/) {
                Edge se = (Edge) sei.next();
                Node tn = se.getAdjacentNode(sn);
                if (n.getRow() < tn.getRow()) {
                    if (!friends.containsKey(tn.getRow())) {
                        friends.put(tn.getRow(), new CNode(tn));
                    } else {
                        friends.get(tn.getRow()).increment(se);
                    }
                }
            }
        }
        for (CNode cn : friends.values()) {
            int v = rf.calculate(n, cn.node, cn.weight);
            if (v < low || v > high) {
                continue;
            }
            int r = rt.addRow();
            int toid = pg.getEntityRef(PGraph.getId(cn.node)).getId();
            rt.set(r, "ID", r);
            rt.set(r, "ID1", soid);
            rt.set(r, "ID2", toid);
            rt.set(r, "Weight", cn.weight);
        }
    }

    @Override
    protected void explore(Node n, Walker w, DefaultHistogramModel dhm, SwingWorker sw) {
        if (n.getDegree() > 0) {
//            System.err.println("HEY: " + n.getDegree() + " "
//                    + n.getString(PGraph.labelField) + " " + n.getRow());
        }
        HashMap<Integer, CNode> friends = new HashMap<Integer, CNode>();
        for (Iterator ei = n.edges(); ei.hasNext() && ! sw.isCancelled(); /**/) {
            Edge e = (Edge) ei.next();
            Node t = e.getAdjacentNode(n);
            int eid = PGraph.getId(e);
            int tid = PGraph.getId(t);
            if ( ! w.canWalk(n, e, pg)) {
//                System.err.println("discarded due to bad pathrel "
//                        + "(" + pg.getRel(eid).getId() + " instead of " + w.current().getId() + ")"
//                        + " or bad nodetype "
//                        + "(" + pg.getEnt(tid).getId() + " instead of " + w.currentTarget().getId() + ")");
                continue;
            }
//            System.err.println("\t " + PGraph.getLabel(t) + " " + t.getRow());

            for (Iterator sei = t.edges(); sei.hasNext(); /**/) {
                Edge se = (Edge) sei.next();
                Node st = se.getAdjacentNode(t);
//                System.err.println("\t\t " + PGraph.getLabel(st) + " " + st.getRow());

                if (n.getRow() < st.getRow()) {
                    if ( ! friends.containsKey(st.getRow())) {
                        // found a new friend
//                        System.err.println("nice!");
                        friends.put(st.getRow(), new CNode(st));
                    } else {
//                        System.err.println("wow!");
                        friends.get(st.getRow()).increment(se);
                    }
                } else {
//                    System.err.println("\t\t Bletch! "
//                            + PGraph.getLabel(n) + " " + n.getRow()
//                            //+ getLabel(nn) + " " + nn.getRow()
//                            + " == " + st.getRow());
                }
            }
        }
        for (CNode cn : friends.values()) {
            dhm.addValue(rf.calculate(n, cn.node, cn.weight), null);
        }
    }


    /**
     * Used within 'generate'
     */
    public static class CNode {

        public Node node;
        public int weight;

        public CNode(Node n) {
            this.node = n;
            this.weight = 1;
        }

        public void increment(Edge e) {
            weight++;
        }
    }

    /**
     * Used to codify the relationship between two that share 'common' neighbors
     */
    public interface Relatedness {

        public int calculate(Node a, Node b, int common);
    }

    public static class SumRelatedness implements Relatedness {

        @Override
        public int calculate(Node a, Node b, int common) {
            return common;
        }
    }

    public static class ProportionRelatedness implements Relatedness {

        @Override
        public int calculate(Node a, Node b, int common) {
            return common * 200 / (a.getDegree() + b.getDegree());
        }
    }
}
