package org.testconc.collections.others;

import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.Assert.*;

public class CopyOnWriteArrayListTest {

    @Test
    public void test_AddAndIterate()
    {
        CopyOnWriteArrayList<String> cowal = new CopyOnWriteArrayList<>();
        cowal.add("Value1");
        cowal.add("Value2");
        cowal.add("Value3");

        Iterator<String> iterator = cowal.iterator();

        cowal.add("Value4");
        assertEquals("Value1", iterator.next());
        assertEquals("Value2", iterator.next());
        assertEquals("Value3", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void test_AddIteratorRemove()
    {
        CopyOnWriteArrayList<String> cowal = new CopyOnWriteArrayList<>();
        cowal.add("Value1");
        cowal.add("Value2");
        cowal.add("Value3");

        Iterator<String> iterator = cowal.iterator();

        cowal.remove("Value3");
        assertEquals("Value1", iterator.next());
        assertEquals("Value2", iterator.next());
        assertEquals("Value3", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_listIterator()
    {
        CopyOnWriteArrayList<String> cowal = new CopyOnWriteArrayList<>();
        cowal.add("Value1");
        cowal.add("Value2");
        cowal.add("Value3");

        ListIterator<String> li = cowal.listIterator();

        assertEquals("Value1", li.next());
        assertEquals("Value1", li.previous());
        li.remove();
    }

    @Test
    public void test_reversedView() {
        CopyOnWriteArrayList<String> cowal = new CopyOnWriteArrayList<>();
        cowal.add("Value1");
        cowal.add("Value2");
        cowal.add("Value3");

        List<String> reversed = cowal.reversed();

        assertEquals("Value3", reversed.get(0));
        assertEquals("Value2", reversed.get(1));
        assertEquals("Value1", reversed.get(2));
    }

    @Test
    public void test_reversedEqual() {
        CopyOnWriteArrayList<String> cowal = new CopyOnWriteArrayList<>();
        cowal.add("Value1");
        cowal.add("Value2");
        cowal.add("Value3");

        List<String> reversed = cowal.reversed();

        assertFalse(cowal.equals(reversed));
        assertFalse(reversed.equals(cowal));
    }

}
