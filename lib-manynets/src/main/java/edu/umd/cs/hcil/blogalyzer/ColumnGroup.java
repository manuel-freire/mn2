/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umd.cs.hcil.blogalyzer;

import java.util.ArrayList;

/**
 *
 * @author mfreire
 */
public class ColumnGroup {

    private String prefix;
    private int start, end;
    private ArrayList<String> attNames = new ArrayList<String>();

    public static ArrayList<ColumnGroup> createGroups(ClassificationTable ct) {
        ArrayList<ColumnGroup> all = new ArrayList<ColumnGroup>();
        String prefix = "$!@#";
        ColumnGroup cg = null;
        //TODO: Change it for something dynamic
//        for(int i =0;i<ct.getStartAttribute();i++){
//
//         String colName = ct.getTable().getColumnName(i);
//         cg = new ColumnGroup();
//         cg.prefix = ct.getTable().getColumnName(i);
//         all.add(cg);
//        }
//        for (int i=0; i<ct.getTable().getColumnCount(); i++) {
        
        for (int i=ct.getStartAttribute(); i<ct.getEndAttribute(); i++) {
            String colName = ct.getTable().getColumnName(i);
            if (colName.startsWith(prefix)) {
                cg.end = i+1;
                cg.prefix = prefix;
                cg.attNames.add(colName.substring(colName.indexOf("(")+1,colName.indexOf(")")));
            }

            else {
                if (cg != null) {
//                    System.err.println(cg.getPrefix());
                    all.add(cg);
                }
                cg = new ColumnGroup();
                cg.start = i;
                if(colName.indexOf("(") !=-1)
                {
                    prefix = colName.substring(0, colName.indexOf("("));
                    cg.attNames.add(colName.substring(colName.indexOf("(")+1,colName.indexOf(")")));
                }
                else{
                    cg.prefix = colName;
                }

                //System.err.println("ColName: "+colName);
                
            }
        }
        if (cg != null) all.add(cg);

        return all;
    }

    public int getEnd() {
        return end;
    }

    public ArrayList<String> getAttNames() {
        return attNames;
    }

    public int getStart() {
        return start;
    }

    public String getPrefix() {
        return prefix;
    }
}
