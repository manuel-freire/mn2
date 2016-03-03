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

import edu.umd.cs.hcil.manynets.hist.DefaultHistogramModel;
import edu.umd.cs.hcil.manynets.model.Distribution;
import edu.umd.cs.hcil.manynets.model.PGraph;
import edu.umd.cs.hcil.manynets.model.Population.PopulationTableWrapper;
import edu.umd.cs.hcil.manynets.model.Stat;
import edu.umd.cs.hcil.manynets.model.TableWrapper;
import edu.umd.cs.hcil.manynets.model.TableWrapper.Level;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.buffer.UnboundedFifoBuffer;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;

/**
 * From Jung, adapting for ManyNets
 *
 * Computes betweenness centrality for each vertex and edge in the graph. The result is that each vertex and edge has a
 * UserData element of type Double whose key is 'centrality.RelativeBetweennessCentrality' Note: Many social network
 * researchers like to normalize the betweenness values by dividing the values by (n-1)(n-2)/2. The values given here
 * are unnormalized.
 * <p>
 * <p>
 * A simple example of usage is: <br>
 * RelativeBetweennessCentrality ranker = new RelativeBetweennessCentrality(someGraph); <br>
 * ranker.evaluate(); <br>
 * ranker.printRankings();
 * <p>
 * Running time is: O(n^2 + nm).
 * 
 * @see "Ulrik Brandes: A Faster Algorithm for Betweenness Centrality. Journal of Mathematical Sociology 25(2):163-177,
 *      2001."
 *      
 * Modified from JUNG v1.7.4 (edu.uci.ics.jung.algorithms.importance.BetweennessCentrality) to work with Prefuse.
 * 
 * @author Scott White
 * @author Adam Perer
 */
public class BetweennessCentralityCalculator extends AbstractCalculator {


    public static Stat betweennessCentralityG = new Stat("Betweenness Centrality",
           "Portion of shortest paths that pass through each node",
           Distribution.class, Level.Network, Level.Network);
    public static Stat betweennessCentralityN = new Stat("Betweenness Centrality",
           "Portion of shortest paths that pass through each node",
           Double.TYPE, Level.Node, Level.Network);

    public BetweennessCentralityCalculator(boolean forceUndirected) {
        super(new Stat[]{betweennessCentralityG, betweennessCentralityG},
                "O(V^2 + VE)");
        this.forceUndirected = forceUndirected;
    }
    double sigma[];
    double d[];
    double Cb[];
    double Cb_e[];
    double delta[];
    boolean forceUndirected = false;

    @Override
    public void calculate(TableWrapper tw, int gid) {
        int r = tw.getRow(gid);
        PGraph pg = ((PopulationTableWrapper)tw).getGraph(gid);
        Graph g = pg.getGraph();

        checkAddStat(betweennessCentralityG, tw);
        checkAddStat(betweennessCentralityN, pg.getNodeTable());
        Table t = tw.getTable();
        Table nt = g.getNodeTable();
        Map<Node, Double> m = computeBetweenness(g);
        DefaultHistogramModel dhm = new DefaultHistogramModel();
        for (Entry<Node, Double> e : m.entrySet()) {
            int id = e.getKey().getInt(PGraph.idField);
//            System.err.println("setting " + e.getKey().getRow() + " to " + e.getValue());
            nt.set(e.getKey().getRow(), betweennessCentralityN.getName(), 
                    e.getValue());
            dhm.addValue((double)e.getValue(),
                pg.getNodeTable().getRef(id));            
        }
        t.set(r, betweennessCentralityG.getName(), dhm);
    }

    public Map<Node, Double> computeBetweenness(Graph graph) {
        int numNodes = graph.getNodeCount();
        int numEdges = graph.getEdgeCount();
        boolean directed = forceUndirected ? false : graph.isDirected();

        HashMap<Node, Double> results = new HashMap<Node, Double>();

        Cb = new double[numNodes];

        for (int z = 0; z < numNodes; z++) {
            Cb[z] = 0.0;
        }

        Cb_e = new double[numEdges];

        for (int z = 0; z < numEdges; z++) {
            Cb_e[z] = 0.0;
        }

        for (int i = 0; i < numNodes; i++) {

            Node s = (Node) graph.getNode(i);
            //graph.getNodeTable().getInt(i, nodeKeyCol));

            Stack<Node> S = new Stack<Node>();
            ArrayList<Node>[] P = new ArrayList[numNodes];

            sigma = new double[numNodes];
            d = new double[numNodes];

            for (int j = 0; j < numNodes; j++) {
                sigma[j] = 0;
                d[j] = -1;
                P[j] = new ArrayList<Node>();
            }

            sigma[i] = 1;
            d[i] = 0;

            Buffer Q = new UnboundedFifoBuffer();
            Q.add(s);

            // find all shortest paths from s to all other nodes
            while (!Q.isEmpty()) {
                Node v = (Node) Q.remove();
                S.push(v);
                int vNum = v.getRow();

                int cnt = 0;
                Iterator<Node> neighborIterator = directed
                        ? v.outNeighbors() : v.neighbors();
                while (neighborIterator.hasNext()) {
                    cnt++;

                    Node w = neighborIterator.next();
                    int wNum = w.getRow();

                    // w found for the first time?
                    if (d[wNum] < 0) {
                        Q.add(w);
                        d[wNum] = d[vNum] + 1;
                    }

                    // shortest path to w via v?
                    if (d[wNum] == (d[vNum] + 1)) {
                        sigma[wNum] = sigma[wNum] + sigma[vNum];
                        P[wNum].add(v);
                    }
                }
            }

            delta = new double[numNodes];
            for (int j = 0; j < numNodes; j++) {
                delta[j] = 0;
            }

            // S returns vertices in order of non-increasing distance from s
            while (!S.isEmpty()) {
                Node w = S.pop();

                int wNum = w.getRow();
                int cnt = 0;
                for (Iterator<Node> vIt = P[wNum].iterator(); vIt.hasNext();) {
                    Node v = vIt.next();
                    int vNum = v.getRow();
                    double partialDependency = (1.0 * sigma[vNum] / sigma[wNum]) * (1.0 + delta[wNum]);
                    delta[vNum] += partialDependency;
                    cnt++;

                    Edge currentEdge = graph.getEdge(v, w);
                    if (currentEdge == null && !directed) {
                        currentEdge = graph.getEdge(w, v);
                    }
                    if (currentEdge == null) {
                        System.err.println(directed
                                ? ("BetweennessCentrality: No edge exists for " + v + "->" + w
                                + " or " + w + "->" + v)
                                : ("BetweennessCentrality: No edge exists for " + v + "->" + w));
                        continue;
                    }
                    int eNum = currentEdge.getRow();
                    Cb_e[eNum] += partialDependency;
                }

                if (w != s) {
                    Cb[wNum] = (Cb[wNum] + delta[wNum]);
                }
            }
        }

        for (int i = 0; i < numNodes; i++) {
            Node s = (Node) graph.getNode(i);
            //graph.getNodeTable().getInt(i, nodeKeyCol));
            results.put(s, Cb[i]);
        }
        return results;
    }
}
