/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umd.cs.hcil.manynets.engines;

import edu.umd.cs.hcil.manynets.model.Distribution;
import edu.umd.cs.hcil.manynets.model.DistributionFactory;
import edu.umd.cs.hcil.manynets.model.Stat;
import edu.umd.cs.hcil.manynets.model.TableWrapper.Level;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.python.core.Py;
import org.python.core.PyFloat;
import prefuse.data.Table;
import static org.junit.Assert.*;

/**
 *
 * @author Manuel Freire
 */
public class ExpressionCalculatorTest {

    private Stat dummyStat = new Stat(
            "dummy", "dummy description",
            Integer.TYPE, Level.Network, Level.Network);

    public ExpressionCalculatorTest() {
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
     * Test of compile method, of class ExpressionCalculator.
     */
    @Test
    public void testCreateAndCompile() throws Exception {
        System.out.println("createAndCompile");
        ExpressionCalculator instance = new ExpressionCalculator(
                dummyStat,
                "[Node count] == 3.0 and [Edge-node ratio] >= 1.0",
                "O(1)");
    }

    /**
     * Test of eval method, of class ExpressionCalculator.
     */
    @Test
    public void testEvalBoolean() {
        System.out.println("testEvalBoolean");
        ExpressionCalculator instance = new ExpressionCalculator(
                dummyStat,
                "['Node count] == 3.0 and [Edge-node ratio] >= 1.0",
                "O(1)");
        assertEquals(Py.False, instance.evalArgs(4, 1));
        assertEquals(Py.True, instance.evalArgs(3, 1));
    }

    /**
     * Test of eval method, of class ExpressionCalculator.
     */
    @Test
    public void testEvalDouble() {
        System.out.println("testEvalDouble");
        ExpressionCalculator instance = new ExpressionCalculator(
                dummyStat,
                "[Node count] * [Edge-node ratio]",
                "O(1)");
        assertEquals(new PyFloat(8), instance.evalArgs(4, 2));
        assertEquals(new PyFloat(9), instance.evalArgs(3, 3));
    }

    /**
     * Test of eval method, of class ExpressionCalculator.
     */
    @Test
    public void testEvalArray() {
        System.out.println("testEvalArray");
        ExpressionCalculator instance = new ExpressionCalculator(
                dummyStat,
                "Avg[Node count]",
                "O(1)");
        Table t = new Table();
        t.addColumn("Node count", Distribution.class);
        t.set(t.addRow(), 0, DistributionFactory.build(
                new int[] {0,1,2,3,4,5,6,7,8,9}, "test"));
        assertEquals(new PyFloat(4.5), instance.evalTuple(t.getTuple(0), null));
    }

    /**
     * Test of eval method, of class ExpressionCalculator.
     */
    @Test
    public void testBackReference() {
        System.out.println("testBackReference");
        ExpressionCalculator instance = new ExpressionCalculator(
                dummyStat,
                "function3([Node count], [Edge-node ratio])",
                "O(1)");
        Table t = new Table();
        t.addColumn("Node count", Integer.TYPE);
        t.addRow();
        t.set(0, 0, 5);
        t.addColumn("Edge-node ratio", Integer.TYPE);
        t.set(0, 1, 6);
        assertEquals(new PyFloat(30), instance.evalTuple(t.getTuple(0), null));
    }
}
