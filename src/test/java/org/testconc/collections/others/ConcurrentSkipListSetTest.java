package org.testconc.collections.others;

import org.junit.Test;

import java.util.Iterator;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.junit.Assert.assertEquals;

public class ConcurrentSkipListSetTest
{

    @Test
    public void test_Iterator()
    {
        ConcurrentSkipListSet<String> csls = new ConcurrentSkipListSet<>();

        csls.add("Key1");
        csls.add("Key2");
        csls.add("Key3");
        csls.add("Key4");
        csls.add("Key5");

        Iterator<String> iterator = csls.iterator();

        assertEquals("Key1", iterator.next());
        assertEquals("Key2", iterator.next());
        assertEquals("Key3", iterator.next());
        assertEquals("Key4", iterator.next());
        assertEquals("Key5", iterator.next());
    }

    @Test
    public void test_first()
    {
        ConcurrentSkipListSet<String> csls = new ConcurrentSkipListSet<>();
        csls.add("Key1");
        csls.add("Key2");
        csls.add("Key3");
        csls.add("Key4");
        csls.add("Key5");

        assertEquals("Key1", csls.first());
        assertEquals("Key5", csls.last());
        assertEquals("Key1", csls.pollFirst());
        assertEquals("Key5", csls.pollLast());
        assertEquals("Key2", csls.first());
        assertEquals("Key4", csls.last());
    }

    @Test
    public void test_tailSet()
    {
        ConcurrentSkipListSet<String> csls = new ConcurrentSkipListSet<>();
        csls.add("Key1");
        csls.add("Key2");
        csls.add("Key3");
        csls.add("Key4");
        csls.add("Key5");

        NavigableSet<String> key3 = csls.tailSet("Key3", true);

        Iterator<String> iterator = key3.iterator();
        assertEquals("Key3", iterator.next());
        assertEquals("Key4", iterator.next());
        assertEquals("Key5", iterator.next());
    }
}
