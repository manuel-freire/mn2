/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umd.cs.hcil.manynets.model;

import edu.umd.cs.hcil.manynets.model.Dataset.DatasetListener;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import prefuse.data.Table;

/**
 * A single point to change settings for (eg) loading test datasets.
 *
 * @author mfreire
 */
public class TestUtils {

    public static boolean isWindows =
            (System.getProperty("os.name").toLowerCase().indexOf("win") != -1);

    public static Set<String> createSet(String... names) {
        TreeSet<String> s = new TreeSet<String>();
        for (String n : names) {
            s.add(n);
        }
        return s;
    }

    public static Dataset loadDataset(String name) throws IOException {
        return loadDataset(name, null, null);
    }

    public static Dataset loadDataset(String name, 
            Set<String> instances, DatasetListener listener) throws IOException {
        File d = new File(isWindows ?
                "c:/manynets/data/" + name + "/" :
                "/home/mfreire/dev/mn/data/" + name + "/");
        System.err.println("Loading dataset from " + d.getCanonicalPath());
        Dataset ds = new Dataset();
        ds.load(new File(d, "config.xml"), instances, listener);
        System.err.println("Dataset loaded OK");
        return ds;
    }


    public static void printTable(Table t) {
        System.err.print("0 ");
        for (int j = 0; j < t.getColumnCount(); j++) {
            System.err.print("\t" + t.getColumnName(j));
        }
        System.err.println();
        for (int i = 0; i < t.getRowCount(); i++) {
            System.err.print("" + i + " ");
            for (int j = 0; j < t.getColumnCount(); j++) {
                System.err.print("\t" + t.get(i, j));
            }
            System.err.println();
        }
    }
}
