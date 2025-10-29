package org.testconc.collections.others;

import org.junit.*;

import java.util.concurrent.ConcurrentSkipListMap;

import static org.junit.Assert.*;

public class CincurrentSkipListMapTest
{
    @Test
    public void test_putGetRemove()
    {
        ConcurrentSkipListMap<Integer, String> cslm = new ConcurrentSkipListMap<>();

        cslm.put(1, "Value2");

        assertEquals("Value2", cslm.get(1));
        assertEquals("Value2", cslm.remove(1));
    }

    @Test
    public void test_remove()
    {
        ConcurrentSkipListMap<Integer, String> cslm = new ConcurrentSkipListMap<>();

        cslm.put(1, "Value2");
        assertFalse(cslm.remove(1, "Value3"));
        assertTrue(cslm.remove(1, "Value2"));
    }

    @Test
    public void test_navigable()
    {
        ConcurrentSkipListMap<Integer, String> cslm = new ConcurrentSkipListMap<>();

        cslm.put(1, "Value1");
        cslm.put(2, "Value2");
        cslm.put(3, "Value3");
        cslm.put(4, "Value4");

        assertEquals(new Integer(2), cslm.ceilingKey(2)); // GT | EQ
        assertEquals(new Integer(1), cslm.ceilingKey(0)); // GT | EQ
        assertEquals(null, cslm.ceilingKey(5)); // GT | EQ
        assertEquals(new Integer(2), cslm.ceilingEntry(2).getKey()); // GT | EQ
        assertEquals(new Integer(2), cslm.floorKey(2)); // LT | EQ
        assertEquals(null, cslm.floorKey(0)); // LT | EQ
        assertEquals(new Integer(4), cslm.floorKey(5)); // LT | EQ

        assertEquals(new Integer(4), cslm.lowerKey(5)); // LT
        assertEquals(null, cslm.lowerKey(0)); // LT
        assertEquals(new Integer(1), cslm.higherKey(0)); // GT
        assertEquals(new Integer(2), cslm.higherKey(1)); // GT
        assertEquals(null, cslm.higherKey(5)); // GT
    }

    @Test
    public void test_compute()
    {
        ConcurrentSkipListMap<Integer, String> cslm = new ConcurrentSkipListMap<>();

        cslm.put(1, "Value1");
        cslm.put(2, "Value2");
        cslm.put(3, "Value3");
        cslm.put(4, "Value4");

        cslm.compute(4, (k, v) -> "NewValue4");
        assertEquals("NewValue4", cslm.get(4));

        cslm.compute(5, (k, v) -> "NewValue5");
        assertEquals("NewValue5", cslm.get(5));
    }

}
