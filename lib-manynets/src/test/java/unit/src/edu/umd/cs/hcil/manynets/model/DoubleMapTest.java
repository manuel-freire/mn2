/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umd.cs.hcil.manynets.model;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Manuel Freire
 */
public class DoubleMapTest {

    public DoubleMapTest() {
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
     * Test of put method, of class DoubleMap.
     */
    @Test
    public void testPut() {
        System.out.println("put");

        DoubleMap<Integer, String> instance = new DoubleMap<Integer, String>();

        for (int i=0; i<1000; i++) {
            instance.put(i, ""+i);
        }

        for (int i=0; i<1000; i++) {
            assertTrue(instance.containsKey(i));
            assertFalse(instance.containsKey(i+1000));
            assertEquals(instance.get(i), "" + i);
            assertTrue(instance.containsValue("" + i));
            assertFalse(instance.containsValue("" + (i + 1000)));
            assertEquals(instance.getKey("" + i), (Integer)i);
        }
    }

    /**
     * Test of remove method, of class DoubleMap.
     */
    @Test
    public void testRemove() {
        System.out.println("remove");

        DoubleMap<Integer, String> instance = new DoubleMap<Integer, String>();

        for (int i=0; i<1000; i++) {
            instance.put(i, ""+i);
        }

        for (int i=0; i<1000; i++) {
            if ((i % 2) == 0) instance.remove(i);
        }

        for (int i=0; i<1000; i++) {
            if ((i % 2) == 0) {
                assertEquals(instance.get(i), null);
                assertEquals(instance.getKey("" + i), null);
            } else {
                assertEquals(instance.get(i), "" + i);
                assertEquals(instance.getKey("" + i), (Integer)i);
            }
        }
    }

    /**
     * Test of clear method, of class DoubleMap.
     */
    @Test
    public void testClear() {
        System.out.println("clear");

        DoubleMap<Integer, String> instance = new DoubleMap<Integer, String>();

        for (int i=0; i<1000; i++) {
            instance.put(i, ""+i);
        }

        instance.clear();
        assertEquals(instance.size(), 0);
        for (int i=0; i<1000; i++) {
            assertEquals(instance.getKey("" + i), null);
        }
    }
}