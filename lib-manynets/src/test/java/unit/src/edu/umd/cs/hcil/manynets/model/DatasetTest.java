/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umd.cs.hcil.manynets.model;

import edu.umd.cs.hcil.manynets.model.SchemaInstance.EntityTableWrapper;
import edu.umd.cs.hcil.manynets.model.SchemaInstance.RelTableWrapper;
import java.awt.MediaTracker;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Manuel Freire
 */
public class DatasetTest {

    public DatasetTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of load method, of class Dataset.
     */
    @Test
    public void testLoad_File_DatasetDatasetListener() throws Exception {
        System.out.println("load movies");

        TestUtils.loadDataset("movies", null, new Dataset.StderrDatasetListener());
    }

    /**
     * Test of load method, of class Dataset.
     */
    @Test
    public void testLoad3_File_DatasetDatasetListener() throws Exception {
        System.out.println("load many");

        TestUtils.loadDataset("many", null, new Dataset.StderrDatasetListener());
    }

    /**
     * Test of load method, of class Dataset.
     */
    @Test
    public void testLoad2_File_DatasetDatasetListener() throws Exception {
        System.out.println("load");

        Dataset ds = TestUtils.loadDataset("blog", TestUtils.createSet("blog"),
                new Dataset.StderrDatasetListener());
        for (SchemaInstance si : ds.getInstances()) {
            for (EntityTableWrapper tw : si.getEntities()) {
                String s = "Weird";
                switch (tw.getIcon().getImageLoadStatus()) {
                    case MediaTracker.ABORTED: s = "aborted"; break;
                    case MediaTracker.COMPLETE: s = "complete"; break;
                    case MediaTracker.ERRORED: s = "errored"; break;
                    case MediaTracker.LOADING: s = "loading"; break;
                }
                System.err.println("Image status for " + tw.getName() + " is " + s);
            }
            for (RelTableWrapper tw : si.getRelationships()) {
                System.err.println(tw.getIcon().getIconWidth());
            }
        }
    }

//    /**
//     * Test of save method, of class Dataset.
//     */
//    @Test
//    public void testSave_5args() throws Exception {
//        System.out.println("save");
//        File f = null;
//        String name = "";
//        String author = "";
//        String description = "";
//        DatasetListener listener = null;
//        Dataset instance = new Dataset();
//        instance.save(f, name, author, description, listener);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of load method, of class Dataset.
//     */
//    @Test
//    public void testLoad_3args() throws Exception {
//        System.out.println("load");
//        Element e = null;
//        File baseDir = null;
//        DatasetListener listener = null;
//        Dataset instance = new Dataset();
//        instance.load(e, baseDir, listener);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of loadEntityTable method, of class Dataset.
//     */
//    @Test
//    public void testLoadEntityTable() throws Exception {
//        System.out.println("loadEntityTable");
//        File f = null;
//        Entity e = null;
//        Table expResult = null;
//        Table result = Dataset.loadEntityTable(f, e);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of loadRelationshipTable method, of class Dataset.
//     */
//    @Test
//    public void testLoadRelationshipTable() throws Exception {
//        System.out.println("loadRelationshipTable");
//        File f = null;
//        Relationship r = null;
//        Table expResult = null;
//        Table result = Dataset.loadRelationshipTable(f, r);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of loadTable method, of class Dataset.
//     */
//    @Test
//    public void testLoadTable() throws Exception {
//        System.out.println("loadTable");
//        File f = null;
//        Table expResult = null;
//        Table result = Dataset.loadTable(f);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of save method, of class Dataset.
//     */
//    @Test
//    public void testSave_4args() {
//        System.out.println("save");
//        Element e = null;
//        String name = "";
//        String author = "";
//        String description = "";
//        Dataset instance = new Dataset();
//        instance.save(e, name, author, description);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}