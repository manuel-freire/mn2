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

package edu.umd.cs.hcil.manynets.dgram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Model information for a dendrogram. Dendrograms have information in 2 dimensions:
 * two clusters can unite at certain points.
 *
 * @author mfreire
 */
public class DendrogramModel {

    private float[] highlights;
    private float[][] OD;
    private Leaf[] leaves;
    private HashMap<DNode, Integer> leafToInt; // what does it do ? 
    private HashSet<DNode> nodes;
    private LinkageModel linkage;

   
    /** Creates a new instance of DendrogramModel */
    public DendrogramModel(int nLeaves, LinkageModel linkage) {
        this.linkage = linkage;
        OD = new float[nLeaves][];
        leaves = new Leaf[nLeaves];
        leafToInt = new HashMap<DNode, Integer>();
        nodes = new HashSet<DNode>();
    }

    public void addLeaf(Object o, float[] distances) {
        Leaf next = new Leaf(0f, nodes.size(), this);
        next.setUserObject(o);
        OD[nodes.size()] = distances;
        leafToInt.put(next, nodes.size());
        leaves[nodes.size()] = next;
        nodes.add(next);
        if (nodes.size() == distances.length) {
            commit();
        }
    }

    public LinkageModel getLinkage() {
        return linkage;
    }

    public ArrayList<DNode> getClustersAtLevel(float level) {
        ArrayList<DNode> al = new ArrayList<DNode>();
        addClustersAbove(getRoot(), level, al);
        return al;
    }

    private void addClustersAbove(DNode n, float level, Collection<DNode> clusters) {
        if (n.getDistance() <= level) {
            return;
        }
        for (int i = 0; i < n.getChildCount(); i++) {
            DNode c = (DNode) n.getChildAt(i);
            clusters.add(c);
            addClustersAbove(c, level, clusters);
        }
    }

    private class NodePair implements Comparable<NodePair> {

        private DNode a;
        private DNode b;
        private float weight;

        public NodePair(DNode a, DNode b, float weight) {
            this.a = a;
            this.b = b;
            this.weight = weight;
        }

        public boolean intersects(NodePair o) {
            return a == o.a || b == o.b || a == o.b || b == o.a;
        }

        public int compareTo(NodePair o) {
            int c = Float.compare(weight, o.getWeight());
            return (c != 0) ? c : this.toString().compareTo(o.toString());
        }

        public float getWeight() {
            return weight;
        }

        public DNode getA() {
            return a;
        }

        public DNode getB() {
            return b;
        }

        public String toString() {
            return "" + a + ", " + b;
        }
    }

    /**
     * Run the clustering algorithm. Uses a priority queue (ordering by link mode - derived
     * clustering distances) instead of an array; extraction-insertion are O(log(N)), 
     * (but cleanup still requires O(N)). On the other hand, this method scales 
     * to higher-than-binary branches in the future.
     *
     */
    private void commit() {

        PriorityQueue<NodePair> queue = new PriorityQueue<NodePair>();
        for (DNode a : nodes) {
            for (DNode b : nodes) {
                if (a == b) {
                    break;
                }

                float d = OD[leafToInt.get(a)][leafToInt.get(b)];
                queue.add(new NodePair(a, b, d));
            }
        }

        int totalRequired = nodes.size();
        int step = totalRequired / 10;
        int total = 0;
        long startTime = System.currentTimeMillis();

        while (nodes.size() > 1) {
            if ((++total) % step == 0) {
                System.err.print(".");
            }

            // remove lowest
            NodePair low = queue.poll();

//            System.err.println("" + (i++) + "=====\n");
//            for (DNode n : nodes) System.err.println(n);
//            System.err.println("Extracted "+low);

            // remove discarded alternatives, enshrine chosen one

            nodes.remove(low.getA());
            nodes.remove(low.getB());
            for (Iterator<NodePair> it = queue.iterator(); it.hasNext(); /**/) {
                if ((it.next()).intersects(low)) {
                    it.remove();
                }
            }
            DNode m = buildInternalNode(low.getA(), low.getB(), low.getWeight());
            nodes.add(m);
//            System.err.println("Added " + m);

            // add new groupings             
            for (DNode n : nodes) {
                if (n != m) {
                    float d = linkage.distance(n, m, OD, leafToInt);
                    queue.add(new NodePair(n, m, d));
                }
            }
        }
        float secs = (System.currentTimeMillis() - startTime) / 1000;

        System.err.println("Construction using " + linkage + " required " + secs + " s");
    }

    private DNode buildInternalNode(DNode a, DNode b, float w) {
        DNode m = new DNode(w, this);
        m.left = a;
        m.add(a);
        m.right = b;
        m.add(b);

        Leaf[] leftLeaves = m.left.isLeaf() ? (new Leaf[]{(Leaf) m.left}) : m.left.allLeafs;

        Leaf[] rightLeaves = m.right.isLeaf() ? (new Leaf[]{(Leaf) m.right}) : m.right.allLeafs;

        m.allLeafs = new Leaf[leftLeaves.length + rightLeaves.length];
        System.arraycopy(leftLeaves, 0, m.allLeafs, 0, leftLeaves.length);
        System.arraycopy(rightLeaves, 0, m.allLeafs, leftLeaves.length, rightLeaves.length);
        m.numLeafs = m.allLeafs.length;
        return m;
    }

    public DNode getRoot() {
        return nodes.iterator().next();
    }

    public Collection<DNode> getLeaves() {
        return leafToInt.keySet();
    }

    /**
     * Returns the current leaf sort order as a 0-based permutation 
     * @param ra return array
     */
    public void getLeafSortOrder(int[] ra) {
        int pos = 0;
        for (DNode n : getSortedLeaves()) {
            ra[pos++] = leafToInt.get(n);
        }
    }

    public ArrayList<DNode> getSortedLeaves() {
        return getRoot().getLeaves(new ArrayList<DNode>());
    }

    public void setHighlights(List<Float> tmpList) {
        if (tmpList.size() == 0) {
            return;
        }
        highlights = new float[tmpList.size()];
        int i = 0;
        for (float d : tmpList) {
            highlights[i++] = (float) d;
        }
    }

    public static interface LinkageModel {

        public float distance(DNode a, DNode b, float[][] dt, Map<DNode, Integer> m);
    }
    //store the distance of the leafpair

    public static class LeafDist {

        int n;
        float dist;
        LeafPair best;

        public LeafDist(int to, float d, LeafPair p) {
            n = to;
            dist = d;
            best = p;
        }
    }

    static class LeafPair {

        int leftLeaf;
        int rightLeaf;
        LeafPair preLeft;
        LeafPair preRight;
        int n1, n2;

        public LeafPair(int l, int r, LeafPair pl, LeafPair pr, int t1, int t2) {

            leftLeaf = l;
            rightLeaf = r;
            preLeft = pl;
            preRight = pr;
            n1 = t1;
            n2 = t2;
        }
    }

///////////////////////////////////////////////////
    /**
     * leaf is a special type of node
     */
    public static class Leaf extends DNode {

        LeafPair bestPair;
        LeafDist curDist[], newDist[];
        int index;
        int listSize, newSize;

        public Leaf(float dist, int num, DendrogramModel dm) {
            super(dist, dm);
            this.index = num;

            LeafPair oneLeaf = new LeafPair(index, -1, null, null, 1, 0);
            curDist = new LeafDist[1];
            curDist[0] = new LeafDist(index, 0, oneLeaf);
            listSize = 1;
            newDist = null;
            newSize = 0;
        }

        public int getIndex() {
            return index;
        }

        public LeafDist[] getList() {
            return curDist;
        }

        void setSize(int size) {
            listSize = size;
        }

        public int getSize() {
            // System.err.print("listSize " + listSize);
            return listSize;
        }

        void addToNew(Leaf corner1[], Leaf corner2[], int n1, int n2, int c1,
                int c2, float max1, int tot) {
            int fromNum, fromIndex;
            LeafDist fDist[];
            Leaf curLeaf;
            float bestC[] = new float[tot + 1];
            float curVal, bestD;
            float bestPos;
            int bForC[] = new int[tot + 1];
            int cForD = 0, dPlace = 0;
            float maxAC = Float.POSITIVE_INFINITY;

//            System.err.print(" bestC " + bestC.length);

            for (int j = 0; j < c1; j++) {
                fromIndex = corner1[j].getIndex();
//                System.err.print(" fromIndex  " + fromIndex);
                bestC[fromIndex] = Float.POSITIVE_INFINITY;
                for (int i = 0; i < listSize; i++) {
                    bestPos = curDist[i].dist + max1;
                    if (bestPos > bestC[fromIndex]) {
                        i = listSize;
                    } else {
                        // what is the distance matrix
                        curVal = curDist[i].dist + dm.OD[curDist[i].n][fromIndex];
                        if (curVal < bestC[fromIndex]) {
                            bestC[fromIndex] = curVal;
                            bForC[fromIndex] = i;
                        }
                    }
                }
                if (bestC[fromIndex] < maxAC) {
                    maxAC = bestC[fromIndex];
                }
            }

            for (int j = 0; j < c2; j++) {
                bestD = Float.POSITIVE_INFINITY;
                curLeaf = corner2[j];
                fromNum = curLeaf.getSize();
//                System.err.println("  <"+ this.index+  "> addToNew, fromnum = listsize " + fromNum );
                fromIndex = curLeaf.getIndex();
                fDist = curLeaf.getList();
                for (int i = 0; i < fromNum; i++) {
                    bestPos = curLeaf.curDist[i].dist + maxAC;
                    if (bestPos > bestD) {
                        i = fromNum; // optimization
                    } else {
                        curVal = bestC[fDist[i].n] + curLeaf.curDist[i].dist;
                        if (curVal < bestD) {
                            bestD = curVal;
                            cForD = curLeaf.curDist[i].n;
                            dPlace = i;
                        }
                    }
                }
//                System.err.print ( "<" + this.index + "> cForD " + cForD + "/" + bForC.length);
//                System.err.print("  bForC[cForD] " + bForC[cForD] + "/" + curDist.length);
//                System.err.println("  dPlace " + dPlace + "/" + fDist.length);
                LeafPair newPair = new LeafPair(index, fromIndex,
                        curDist[bForC[cForD]].best,
                        fDist[dPlace].best,
                        n1, n2);

                newDist[newSize] = new LeafDist(fromIndex, bestD, newPair);
                newSize++;
//                System.err.println("<" + this.index+ "> after incrmnt, newSize "  + newSize );
                LeafPair cornerPair = new LeafPair(fromIndex, index,
                        fDist[dPlace].best,
                        curDist[bForC[cForD]].best,
                        n2, n1);
                curLeaf.addNewDist(index, bestD, cornerPair);
            }
        }

        void addNewDist(int n, float dist, LeafPair p) {
            LeafDist[] tmp = new LeafDist[newSize + 1];
            System.arraycopy(newDist, 0, tmp, 0, newSize);
            tmp[newSize] = new LeafDist(n, dist, p);
            newDist = tmp;
            newSize++;
//            System.err.println( "<" + this.index+  ">addNewDist::after incrmnt, newSize  " + newSize);
        }

        public LeafPair findMin(float[] min) {
            min[0] = curDist[0].dist;
            return curDist[0].best;
        }

        public int findLast(Leaf corner1[], Leaf corner2[], int n1, int n2) {
            LeafDist myDist[];
            Leaf curLeaf;
            float curVal, bestD = Float.POSITIVE_INFINITY;
            float myVal;
            int myInd, bestIndL = 0, bestIndR = 0, mBestL = 0, mBestR = 0;
            LeafPair lpre = null, rpre = null;
            LeafDist fDist[][] = new LeafDist[n2][];
            for (int i = 0; i < n2; i++) {
                fDist[i] = corner2[i].getList();
            }

            for (int j = 0; j < n1; j++) {
                curLeaf = corner1[j];
                myDist = curLeaf.getList();
                myVal = myDist[0].dist;
                myInd = curLeaf.getIndex();
                for (int i = 0; i < n2; i++) {
                    // what is the distance matrix
                    curVal = myVal + fDist[i][0].dist + dm.OD[myInd][corner2[i].getIndex()];
                    if (bestD > curVal) {
                        bestD = curVal;
                        bestIndL = myDist[0].n;
                        bestIndR = fDist[i][0].n;
                        mBestL = myInd;
                        mBestR = corner2[i].getIndex();
                    }
                }
            }
            LeafPair newPair;
            int place = 0, size;
            for (int i = 0; i < n2; i++) {
                if (corner2[i].getIndex() == bestIndR) {
                    size = corner2[i].getSize();
                    for (int j = 0; j < size; j++) {
                        if (fDist[i][j].n == mBestR) {
                            rpre = fDist[i][j].best;
                            j = size;
                        }
                    }
                    i = n2;
                }
            }

            for (int i = 0; i < n1; i++) {
                if (corner1[i].getIndex() == bestIndL) {
                    size = corner1[i].getSize();
                    myDist = corner1[i].getList();
                    for (int j = 0; j < size; j++) {
                        if (myDist[j].n == mBestL) {
                            lpre = myDist[j].best;
                            j = size;
                        }
                    }

                    newPair = new LeafPair(bestIndL, bestIndR, lpre, rpre, n1, n2);
                    place = i;
                    corner1[i].initNewSize(1);
                    corner1[i].addNewDist(bestIndR, bestD, newPair);
                    corner1[i].replace();
                    i = n1;
                }
            }
            return place;
        }

        public void initNewSize(int nSize) {
            newDist = new LeafDist[nSize];
        }

        //replace previous pair list, from previous subtree with a new one from the current subtree
        public void replace() {

            listSize = newSize;
//            System.err.print("<" +this.index +"> Replace()::listsize = newsize, create same size curDist array " + newSize);
            curDist = new LeafDist[listSize];
            for (int i = 0; i < listSize; i++) {
                curDist[i] = newDist[i];
            }
            newDist = null;

            Arrays.sort(curDist, new Comparator<LeafDist>() {

                @Override
                public int compare(LeafDist o1, LeafDist o2) {
                    return Float.compare(o1.dist, o2.dist);
                }
            });

            newSize = 0;
//            System.err.println(" // Assign newSize = 0 ");
        }
    }

/////////////if we have a node, we can also get the tree ///
    public static class DNode extends DefaultMutableTreeNode {

        protected DendrogramModel dm;
        protected float distance;
        protected int leafPos;
        protected Leaf allLeafs[];
        protected DNode left, right;
        int numLeafs;
        static final int LEFTTREE = 1;
        static final int RIGHTTREE = 2;

        public DNode(float distance, DendrogramModel dm) {
            this.distance = distance;
            this.dm = dm;            
        }

        public int getLeafPos() {
            return isLeaf() ? -1 : leafPos;
        }

        public Leaf[] getLeaves() {
            Leaf[] leaves = new Leaf[getLeafCount()];
            ArrayList<DNode> l = new ArrayList<DNode>();
            Iterator it = getLeaves(l).iterator();
            int i = 0;
            while (it.hasNext()) {
                leaves[i] = (Leaf) it.next();
                i++;
            }
            return leaves;
        }
// optimization, perfromed for combining the last two subtrees
        int lastTree(int n1, int n2) {
            Leaf l1[] = left.getLeaves();
            Leaf l2[] = right.getLeaves();
            int res = l1[0].findLast(l1, l2, n1, n2);
            return res;
        }

// compute the optimal order for this tree
        int compDist() {
            if (getLeafCount() == 1) { // no nodes to flip
                return 0;
            }
            int i, j;
            left.compDist(); // compute optimal order matrix t subtree
            right.compDist();
            int n1 = left.getLeafCount();
            int n2 = right.getLeafCount();
            if (n1 + n2 == dm.OD.length) { // last two subtrees
                return lastTree(n1, n2);
                // optimization which reduces the running time by half
            }
            if (n1 > 1 && n2 > 1) {
                return compDist(dm.OD.length, n1, n2); // calling another optimization
            } else { // one subtree has only one leaf
                Leaf l1[] = left.getLeaves();
                Leaf l2[] = right.getLeaves();
                for (j = 0; j < n2; j++) {
                    l2[j].initNewSize(n1);
                }
                for (i = 0; i < n1; i++) {
                    l1[i].initNewSize(n2);
                    // this function actually computes the set of optimal orders
                    // of leftmost and rightmost leaves in the combined tree.
                    l1[i].addToNew(l2, l2, n1, n2, n2, n2, Float.NEGATIVE_INFINITY, dm.OD.length);
                    l1[i].replace();
                }
                for (j = 0; j < n2; j++) {
                    l2[j].replace();
                }
            }
            return 0;
        }
// optimization which allows for early termination of the search

        int compDist(int imp, int tot1, int tot2) {

            DNode t1 = left;
            DNode t2 = right;
            DNode t1l = t1.left;
            DNode t1r = t1.right;
            DNode t2l = t2.left;
            DNode t2r = t2.right;

            int n1 = t1l.getLeafCount();
            int n2 = t1r.getLeafCount();
            int n3 = t2l.getLeafCount();
            int n4 = t2r.getLeafCount();

            Leaf l1[] = t1l.getLeaves();
            Leaf l2[] = t1r.getLeaves();
            Leaf l3[] = t2l.getLeaves();
            Leaf l4[] = t2r.getLeaves();
            Leaf c2[] = t2.getLeaves();

            float mint1lt2l, mint1lt2r, mint1rt2l, mint1rt2r;
            mint1lt2l = mint1lt2r = mint1rt2l = mint1rt2r = 1;
            int i1, i2, j3, j4;
// compute minimum similarity between genes in these two subtrees
            for (int i = 0; i < n1; i++) {
                i1 = l1[i].getIndex();
                for (int j = 0; j < n3; j++) {
                    j3 = l3[j].getIndex();
                    if (dm.OD[i1][j3] < mint1rt2r) {
                        mint1rt2r = dm.OD[i1][j3];
                    }
                }
                for (int j = 0; j < n4; j++) {
                    j4 = l4[j].getIndex();
                    if (dm.OD[i1][j4] < mint1rt2l) {
                        mint1rt2l = dm.OD[i1][j4];
                    }
                }
            }
            for (int i = 0; i < n2; i++) {
                i2 = l2[i].getIndex();
                for (int j = 0; j < n3; j++) {
                    j3 = l3[j].getIndex();
                    if (dm.OD[i2][j3] < mint1lt2r) {
                        mint1lt2r = dm.OD[i2][j3];
                    }
                }
                for (int j = 0; j < n4; j++) {
                    j4 = l4[j].getIndex();
                    if (dm.OD[i2][j4] < mint1lt2l) {
                        mint1lt2l = dm.OD[i2][j4];
                    }
                }
            }

            for (int j = 0; j < tot2; j++) {
                c2[j].initNewSize(tot1);
            }

            for (int i = 0; i < n1; i++) {
                l1[i].initNewSize(tot2);
                // use precomutinted minimums to terminate the search early
                l1[i].addToNew(l4, l3, tot1, tot2, n4, n3, mint1lt2l, imp);
                l1[i].addToNew(l3, l4, tot1, tot2, n3, n4, mint1lt2r, imp);
                l1[i].replace();
            }
            for (int i = 0; i < n2; i++) {
                l2[i].initNewSize(tot2);
                l2[i].addToNew(l4, l3, tot1, tot2, n4, n3, mint1rt2l, imp);
                l2[i].addToNew(l3, l4, tot1, tot2, n3, n4, mint1rt2r, imp);
                l2[i].replace();
            }
            for (int j = 0; j < tot2; j++) {
                c2[j].replace();
            }
            return 0;
        }

// returns the optimal order of the tree leaves, called by main program
        public int[] returnOrder(float[] bestDist) {
            int start = compDist();
            System.err.println(" in return order,Start= " + start);

            LeafDist myDist[] = allLeafs[start].getList();
//            LeafDist myDist[] = getLeaves()[start].getList();
            bestDist[0] = myDist[0].dist;
            LeafPair best = myDist[0].best;
            int res[] = new int[getLeafCount()];

            // used to find the correct order of the leaves of the tree
            compTree(best.preLeft, res, 0, best.n1 - 1, LEFTTREE);
            compTree(best.preRight, res, best.n1, getLeafCount() - 1, RIGHTTREE);

            return res;
        }

        public DNode getLeft() {
            return (DNode) getChildAt(0);
        }

        public DNode getRight() {
            return getChildCount() > 1 ? (DNode) getChildAt(1) : null;
        }

        public DNode getRightMostLeaf() {
            return (DNode) getLastLeaf();
        }

        public DNode getLeftMostLeaf() {
            return (DNode) getFirstLeaf();

        }

// computes the optimal order of the leaves
        void compTree(LeafPair best, int res[], int start, int last, int l) {

//            System.err.println("compTree: " + best + " " + res.length + " " + start + " " + last + " " + l);
            if (start == last) {
//                System.err.print(" start " + start);
                res[start] = best.leftLeaf;
                return;
            }
            if (l == LEFTTREE) {
//                System.err.print(" left>");
                compTree(best.preLeft, res, start, start + best.n1 - 1, LEFTTREE);
                compTree(best.preRight, res, start + best.n1, last, RIGHTTREE);

            }
            if (l == RIGHTTREE) {
//                System.err.print(" right>");
                compTree(best.preLeft, res, start + best.n2, last, RIGHTTREE);
                compTree(best.preRight, res, start, start + best.n2 - 1, LEFTTREE);
            }
        }
////////////////////////////////////////////////////////////////////////////////
/////////end of optimal ordering code //////////////////////////////////////////

        public float getDistance() {
            return distance;
        }
        //return the leaves in the arraylist

        public ArrayList<DNode> getLeaves(ArrayList<DNode> al) {
            if (isLeaf()) {
                al.add(this);
            } else {
                for (int i = 0; i < getChildCount(); i++) {
                    ((DNode) getChildAt(i)).getLeaves(al);
                }
            }
            return al;
        }

        public void dump(StringBuilder sb, String indent) {
            if (isLeaf()) {
                sb.append(indent + getUserObject() + "\n");
            } else {
                sb.append(indent + distance + "\n");
                for (int i = 0; i < getChildCount(); i++) {
                    ((DNode) getChildAt(i)).dump(sb, indent + "  ");
                }
            }
        }

        public StringBuilder dump() {
            StringBuilder sb = new StringBuilder();
            dump(sb, "  ");
            return sb;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (DNode ln : getLeaves(new ArrayList<DNode>())) {
                sb.append(ln.getUserObject() + ",");
            }
            return sb.toString();
        }
    }

    public static class SingleLinkage implements LinkageModel {

        private ArrayList<DNode> leavesInA = new ArrayList<DNode>(100);
        private ArrayList<DNode> leavesInB = new ArrayList<DNode>(100);

        public float distance(DNode a, DNode b, float[][] dt, Map<DNode, Integer> m) {
            leavesInA.clear();
            a.getLeaves(leavesInA);
            leavesInB.clear();
            b.getLeaves(leavesInB);
            float d = Float.MAX_VALUE;
            for (DNode aa : leavesInA) {
                int i = m.get(aa);
                for (DNode bb : leavesInB) {
                    int j = m.get(bb);
                    d = Math.min(d, dt[i][j]);
                }
            }
            return d;
        }

        public String toString() {
            return "Single linkage";
        }
    }

    public static class AverageLinkage implements LinkageModel {

        private ArrayList<DNode> leavesInA = new ArrayList(100);
        private ArrayList<DNode> leavesInB = new ArrayList(100);

        public float distance(DNode a, DNode b, float[][] dt, Map<DNode, Integer> m) {
            leavesInA.clear();
            a.getLeaves(leavesInA);
            leavesInB.clear();
            b.getLeaves(leavesInB);
            float total = 0;
            int n = 0;
            for (DNode aa : leavesInA) {
                int i = m.get(aa);
                for (DNode bb : leavesInB) {
                    int j = m.get(bb);
                    total += dt[i][j];
                    n++;
                }
            }
            return (float) (total / n);
        }

        public String toString() {
            return "Average linkage";
        }
    }

    public static class CompleteLinkage implements LinkageModel {

        private ArrayList<DNode> leavesInA = new ArrayList(100);
        private ArrayList<DNode> leavesInB = new ArrayList(100);

        public float distance(DNode a, DNode b, float[][] dt, Map<DNode, Integer> m) {
            leavesInA.clear();
            a.getLeaves(leavesInA);
            leavesInB.clear();
            b.getLeaves(leavesInB);
            float d = Float.MIN_VALUE;
            for (DNode aa : leavesInA) {
                int i = m.get(aa);
                for (DNode bb : leavesInB) {
                    int j = m.get(bb);
                    d = Math.max(d, dt[i][j]);
                }
            }
            return d;
        }

        public String toString() {
            return "Complete linkage";
        }
    }
}
