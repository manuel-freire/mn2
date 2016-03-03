/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umd.cs.hcil.blogalyzer;

import java.util.ArrayList;
import java.util.HashMap;
import prefuse.data.Table;

/**
 * Wraps a table that contains
 * - a column of class IDs
 * - a series of columns for relevant attributes
 *
 * Can calculate "similarity" between two rows given a set of weights.
 * Can compute all similarities between all row pairs, storing that in a matrix
 *
 * @author mfreire
 */
public class ClassificationTable {

    private Table table;
    private int classColumn;
    private int startAttribute;
    private int endAttribute;
    private ArrayList<ColumnGroup> groups = new ArrayList<ColumnGroup>();
    private int[] attributeToGroup;
    
    private HashMap<Integer, ArrayList<Integer>> classToRow
            = new HashMap<Integer, ArrayList<Integer>>();
    private HashMap<Integer, Integer> rowToClass
            = new HashMap<Integer, Integer>();

    public int getAttributeCount() {
        return endAttribute - startAttribute;
    }

    public int getStartAttribute() {
        return startAttribute;
    }

    public int getEndAttribute() {
        return endAttribute;
    }

    public void setGroups(ArrayList<ColumnGroup> groups) {
        this.groups = groups;
    }

    public void setTable(Table table) {
        this.table = table;
    }
    
    public Table getTable() {
        return table;
    }

    public ArrayList<ColumnGroup> getGroups() {
        return groups;
    }

    public int getGroupCount() {
        return groups.size();
    }

    public ClassificationTable(Table t, int classColumn, int startAttribute, int endAttribute, boolean grouped) {

        this.table = t;
        this.classColumn = classColumn;
        if(grouped){
            this.startAttribute = startAttribute;
            this.endAttribute = endAttribute;
        }
        else{
            this.startAttribute = 0;
            this.endAttribute = 0;
        }

        HashMap<String, Integer> idToClass = new HashMap<String, Integer>();
        for (int i=0; i<t.getRowCount(); i++) {
            String cs = t.getString(i, classColumn);
            if ( ! idToClass.containsKey(cs)) {
                idToClass.put(cs, idToClass.size());
            }
            int cid = idToClass.get(cs);

            ArrayList<Integer> al = classToRow.containsKey(cid) ?
                classToRow.get(cid) : new ArrayList<Integer>();
            al.add(i);
            classToRow.put(cid, al);

            rowToClass.put(i, cid);
        }

        groups = ColumnGroup.createGroups(this);

        attributeToGroup = new int[endAttribute - startAttribute];
        int groupNumber = 0;
        for (ColumnGroup cg : groups) {
            //System.err.println("Group from " + cg.getStart() + " " + cg.getEnd() + " " + cg.getPrefix());
            for(String att : cg.getAttNames()){
                //System.err.println("ATT: "+att);
            }
            for (int i=cg.getStart(); i<cg.getEnd(); i++) {
                attributeToGroup[i - startAttribute] = groupNumber;
            }
            groupNumber ++;
        }
    }
}

