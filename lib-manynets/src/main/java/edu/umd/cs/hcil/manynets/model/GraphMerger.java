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

import edu.umd.cs.hcil.manynets.Utils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Edge;
import prefuse.data.Table;


/**
 * A utility class that knows how to merge graphs. All methods should be static.
 * 
 * @author mfreire
 */
public class GraphMerger {

    /**
     * Merges several pgraphs and builds a new one. Assumes all graphs look like
     * the first one in terms of entities and relationships.
     * @param graphs
     * @param avoidOverlaps
     * @param name
     * @return
     */
    public static PGraph mergeGraphs(Collection<PGraph> graphs, boolean avoidOverlaps, String name) {
        if (graphs.isEmpty()) {
            throw new IllegalArgumentException("No graphs to merge");
        }

        ArrayList<Graph> gs = new ArrayList<Graph>(graphs.size());
        for (PGraph pg : graphs) {
            gs.add(pg.getGraph());
        }
        Graph merged = mergeGraphs(gs, avoidOverlaps);
        return new PGraph(name, graphs.iterator().next(), merged);
    }

    /**
     * Merge a lot of graphs into a single graph
     * @param graphs
     * @return
     */
    public static Graph mergeGraphs(Collection<Graph> graphs, boolean avoidOverlaps) {

        if (graphs.isEmpty()) {
            System.err.println("Requested merge of 0 graphs " +
                    "-- returning an empty graph");
            return new Graph();
        }

        Graph first = graphs.iterator().next();
        Table nodeTable = Utils.createTableWithCols(first.getNodeTable());
        Table edgeTable = Utils.createTableWithCols(first.getEdgeTable());
        if (avoidOverlaps) {
            doSplitMerge(nodeTable, edgeTable, graphs);
        } else {
            doRealMerge(nodeTable, edgeTable, graphs);
        }
        return new Graph(nodeTable, edgeTable, first.isDirected(),
                "ID", first.getEdgeSourceField(), first.getEdgeTargetField());
    }

    /**
     * Merges several graphs into one; edges that are complete duplicates will
     * also be removed -- but then again, all the edge fields are looked at, and
     * there should be few of those.
     *
     * @param nodeTable
     * @param edgeTable
     * @param graphs
     */
    private static void doRealMerge(Table nodeTable, Table edgeTable, Collection<Graph> graphs) {

        HashSet<Integer> usedNodeIds = new HashSet<Integer>();
        HashSet<String> usedEdges = new HashSet<String>();

        for (Graph g : graphs) {
            for (Iterator<Node> ni = g.nodes(); ni.hasNext(); /**/) {
                Node n = ni.next();
                if (usedNodeIds.add(n.getInt("ID"))) {
                    Utils.copyRow(g.getNodeTable(), nodeTable, n.getRow());
                }
            }

            for (Iterator<Edge> ei = g.edges(); ei.hasNext(); /**/) {
                Edge e = ei.next();
                StringBuilder sb = new StringBuilder();
                for (int j=0; j<e.getColumnCount(); j++) {
                    sb.append("_" + e.get(j));
                }
                if (usedEdges.add(sb.toString())) {
                    Utils.copyRow(g.getEdgeTable(), edgeTable, e.getRow());
                }
            }
        }
    }

    /**
     * Merges several graphs into one, but keeping each of them different.
     * Renames all vertices to make this possible.
     *
     * @param nodeTable
     * @param edgeTable
     * @param graphs
     */
    private static void doSplitMerge(Table nodeTable, Table edgeTable, Collection<Graph> graphs) {

        nodeTable.addColumn("oldID", Integer.TYPE);

        HashMap<Integer,Integer> usedIds = new HashMap<Integer,Integer>();
        int nextId = 0;
        int lastNodeRow = 0;
        int lastEdgeRow = 0;

        for (Graph g : graphs) {
            for (Iterator<Node> ni = g.nodes(); ni.hasNext(); /**/) {
                Node n = ni.next();
                usedIds.put(n.getInt("ID"), nextId);
                Utils.copyRow(g.getNodeTable(), nodeTable, n.getRow());
                nodeTable.set(lastNodeRow, "oldID", n.getInt("ID"));
                nodeTable.set(lastNodeRow, "ID", nextId);
                nextId ++;
                lastNodeRow ++;
            }

            for (Iterator<Edge> ei = g.edges(); ei.hasNext(); /**/) {
                Edge e = ei.next();
                Utils.copyRow(g.getEdgeTable(), edgeTable, e.getRow());
                edgeTable.set(lastEdgeRow, g.getEdgeSourceField(), usedIds.get(e.getSourceNode().getInt("ID")));
                edgeTable.set(lastEdgeRow, g.getEdgeTargetField(), usedIds.get(e.getTargetNode().getInt("ID")));
                lastEdgeRow ++;
            }
        }
    }
}
