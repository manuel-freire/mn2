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

import edu.umd.cs.hcil.manynets.Utils;
import edu.umd.cs.hcil.manynets.model.Distribution;
import edu.umd.cs.hcil.manynets.model.PGraph;
import edu.umd.cs.hcil.manynets.transforms.SplitNetworkTransform.SplitOptions;
import edu.umd.cs.hcil.manynets.hist.DefaultHistogramModel;
import java.util.HashMap;
import java.util.Iterator;
import org.jdom.Element;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;

/**
 *
 * @author Manuel Freire
 */
public class ModalSplitOptions implements SplitOptions {

    private String nodeSplitField;
    private String nodeSplitValue;

    public ModalSplitOptions(String nodeSplitField, String nodeSplitValue) {
        this.nodeSplitField = nodeSplitField;
        this.nodeSplitValue = nodeSplitValue;
    }

    @Override
    /**
     * Guaranteed to return true exactly once per graph
     */
    public boolean isSeedNode(PGraph g, Node n) {
        return n.getRow() == g.getGraph().getNode(0).getRow();
    }

    public void addModalHeaders(Table nodeTable, Table edgeTable) {
        nodeTable.addColumn("RATINGS-IN", Distribution.class);
        edgeTable.addColumn("WEIGHT", Integer.TYPE);
    }

    private static class CNode {
        public Node node;
        public int weight;
        public CNode(Node n) {
            this.node = n;
            this.weight = 1;
        }
        public void increment() {
            weight ++;
        }
    }

    public void addModalEdges(Node n, Graph sg, Table nodeTable, Table edgeTable) {
        HashMap<Integer, CNode> friends = new HashMap<Integer, CNode>();
        DefaultHistogramModel d = new DefaultHistogramModel();
        for (Iterator ei = n.edges(); ei.hasNext(); /**/) {
            Edge e = (Edge)ei.next();
            Node sn = e.getAdjacentNode(n);
            d.addValue((float)e.getDouble("RATING"), "" + sn.getInt("ID"));
            for (Iterator sei = sn.edges(); sei.hasNext(); /**/) {
                Edge se = (Edge)sei.next();
                Node tn = se.getAdjacentNode(sn);

                if (n.getRow() < tn.getRow()) {
                    if ( ! friends.containsKey(tn.getRow())) {
                        friends.put(tn.getRow(), new CNode(tn));
                    } else {
                        friends.get(tn.getRow()).increment();
                    }
                }
            }
        }

        String srcField = sg.getEdgeSourceField();
        String tgtField = sg.getEdgeTargetField();
        for (CNode cn : friends.values()) {
            int r = edgeTable.addRow();
            edgeTable.set(r, srcField, n.getInt("ID"));
            edgeTable.set(r, tgtField, cn.node.getInt("ID"));
            edgeTable.set(r, "WEIGHT", cn.weight);
        }
        nodeTable.set(nodeTable.getRowCount()-1, "RATINGS-IN", d);
    }

    @Override
    public PGraph buildGraph(PGraph source, Node seed) {
        Graph sg = source.getGraph();
        Table nodeTable = Utils.createTableWithCols(sg.getNodeTable());
        Table edgeTable = new Table();
        edgeTable.addColumn(sg.getEdgeSourceField(), 
                sg.getEdgeTable().getColumnType(sg.getEdgeSourceField()));
        edgeTable.addColumn(sg.getEdgeTargetField(),
                sg.getEdgeTable().getColumnType(sg.getEdgeTargetField()));
        addModalHeaders(nodeTable, edgeTable);
        
        // load all the nodes that have the specified value in the splitfield
        for (Iterator ni = sg.nodes(); ni.hasNext(); /**/) {
            Node n = (Node)ni.next();
            if (n.getString(nodeSplitField).equals(nodeSplitValue)) {
                Utils.copyRow(sg.getNodeTable(), nodeTable, n.getRow());
                addModalEdges(n, sg, nodeTable, edgeTable);
            }
        }

        // FIXME: only builds them undirected....
        Graph g = new Graph(nodeTable, edgeTable, false,
                PGraph.idField,  // nodefield
                sg.getEdgeSourceField(),  // edgefield1
                sg.getEdgeTargetField()); // edgefield2
        return null;
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
        return "modal split: only " + nodeSplitValue;
    }
}
