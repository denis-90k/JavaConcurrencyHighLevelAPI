package org.testconc.collections.others;

import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArraySet;

import static org.junit.Assert.*;

public class CopyOnWriteArraySetTest {

    @Test
    public void test_addIterator()
    {
        CopyOnWriteArraySet<String> cowas = new CopyOnWriteArraySet<>();

        cowas.add("Value1");
        cowas.add("Value1");
        cowas.add("Value2");
        cowas.add("Value2");

        Iterator<String> it = cowas.iterator();

        assertEquals("Value1", it.next());
        assertEquals("Value2", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void test_Equal()
    {
        CopyOnWriteArraySet<String> cowas1 = new CopyOnWriteArraySet<>();
        cowas1.add("Value1");
        CopyOnWriteArraySet<String> cowas2 = new CopyOnWriteArraySet<>();
        cowas2.add("Value1");

        assertTrue(cowas1.equals(cowas2));

        cowas1.add("Value2");

        assertFalse(cowas1.equals(cowas2));
    }
}
