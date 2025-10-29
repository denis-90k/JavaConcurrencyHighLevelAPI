package org.testconc.collections.others;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

public class ConcurrentHashMapTest {

    @Test
    public void test_PutGetRemove()
    {
        ConcurrentHashMap<String, String> chm = new ConcurrentHashMap<>();

        chm.put("abca", "Value1");
        chm.put("abcA", "Value2");
        chm.put("Key3", "Value3");

        chm.get("abca");

        chm.remove("abca");

    }

    @Test(expected = NullPointerException.class)
    public void test_NullPointerException()
    {
        ConcurrentHashMap<String, String> chm = new ConcurrentHashMap<>();
        chm.put("null", null);
    }

    @Test
    public void test_iterator()
    {
        ConcurrentHashMap<String, String> chm = new ConcurrentHashMap<>();

        chm.put("key1", "value1");
        chm.put("key2", "value2");
        chm.put("key3", "value3");

        Set<Map.Entry<String, String>> entries = chm.entrySet();
        Iterator<Map.Entry<String, String>> iterator = entries.iterator();

        iterator.hasNext();
        Map.Entry<String, String> next = iterator.next();
        iterator.next();
        iterator.next();

    }

    @Test
    public void test_iterationWithCollisions()
    {
        class DummyKey
        {
            @Override
            public int hashCode() {
                return 1231321321;
            }

            @Override
            public boolean equals(Object obj) {
                return false;
            }
        }
        ConcurrentHashMap<DummyKey, String> chm = new ConcurrentHashMap<>(64);

        chm.put(new DummyKey(), "value1");
        chm.put(new DummyKey(), "value2");
        chm.put(new DummyKey(), "value3");
        chm.put(new DummyKey(), "value4");
        chm.put(new DummyKey(), "value4");
        chm.put(new DummyKey(), "value4");
        chm.put(new DummyKey(), "value4");
        chm.put(new DummyKey(), "value4");
        chm.put(new DummyKey(), "value4");

        Set<Map.Entry<DummyKey, String>> entries = chm.entrySet();
        Iterator<Map.Entry<DummyKey, String>> iterator = entries.iterator();

        iterator.next();
        iterator.next();
        iterator.next();
        iterator.next();

    }

    @Test
    public void test_splitarators() {

        ConcurrentHashMap<String, String> chm = new ConcurrentHashMap<>(4, 1);
        chm.put("key1", "value1");
        chm.put("key2", "value2");
        chm.put("key3", "value3");
        chm.put("key4", "value3");
        chm.put("key5", "value3");
        chm.put("key6", "value3");
        chm.put("key7", "value3");
        chm.put("key8", "value3");

        Set<Map.Entry<String, String>> entries = chm.entrySet();
        Spliterator<Map.Entry<String, String>> spliterator = entries.spliterator();

        Assert.assertEquals(8, chm.size());
        Spliterator<Map.Entry<String, String>> entrySpliterator = spliterator.trySplit();
        entrySpliterator.forEachRemaining(System.out::println);
        System.out.println("====================================");
        spliterator.forEachRemaining(System.out::println);

    }

    // add '--add-opens java.base/java.util.concurrent=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED'
    @Test
    public void test_customInitialCapacityAndDefaultLoadFactor() throws NoSuchFieldException, IllegalAccessException {
        ConcurrentHashMap<String, String> chm = new ConcurrentHashMap<>(4);
        chm.put("Test", "Test");

        Field f = chm.getClass().getDeclaredField("table"); //NoSuchFieldException
        f.setAccessible(true);
        Object[] iWantThis = (Object[]) f.get(chm);
        Assert.assertEquals(8, iWantThis.length);

        chm.put("Test1", "Test");
        chm.put("Test2", "Test");
        chm.put("Test3", "Test");
        chm.put("Test4", "Test");
        chm.put("Test5", "Test");
        f = chm.getClass().getDeclaredField("table"); //NoSuchFieldException
        f.setAccessible(true);
        iWantThis = (Object[]) f.get(chm);
        Assert.assertEquals(16, iWantThis.length);

        chm.put("Test6", "Test");
        chm.put("Test7", "Test");
        chm.put("Test8", "Test");
        chm.put("Test9", "Test");
        chm.put("Test10", "Test");

        f = chm.getClass().getDeclaredField("table"); //NoSuchFieldException
        f.setAccessible(true);
        iWantThis = (Object[]) f.get(chm);
        Assert.assertEquals(16, iWantThis.length);

        chm.put("Test11", "Test");
        f = chm.getClass().getDeclaredField("table"); //NoSuchFieldException
        f.setAccessible(true);
        iWantThis = (Object[]) f.get(chm);
        Assert.assertEquals(32, iWantThis.length);
    }

    // add '--add-opens java.base/java.util.concurrent=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED'
    // loadFactor plays role only during initial table size
    @Test
    public void test_customInitialCapacityAndCustomLoadFactor() throws NoSuchFieldException, IllegalAccessException {
        ConcurrentHashMap<String, String> chm = new ConcurrentHashMap<>(4, 1);
        chm.put("Test", "Test");

        Field f = chm.getClass().getDeclaredField("table"); //NoSuchFieldException
        f.setAccessible(true);
        Object[] iWantThis = (Object[]) f.get(chm);
        Assert.assertEquals(8, iWantThis.length);

        chm.put("Test1", "Test");
        chm.put("Test2", "Test");
        chm.put("Test3", "Test");
        chm.put("Test4", "Test");
        chm.put("Test5", "Test");

        f = chm.getClass().getDeclaredField("table"); //NoSuchFieldException
        f.setAccessible(true);
        iWantThis = (Object[]) f.get(chm);
        Assert.assertEquals(16, iWantThis.length);
    }

    // add '--add-opens java.base/java.util.concurrent=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED'
    // loadFactor plays role only during initial table size
    @Test
    public void test_exactInitialSize() throws NoSuchFieldException, IllegalAccessException {
        ConcurrentHashMap<String, String> chm = new ConcurrentHashMap<>(4-1, 1);

        chm.put("Test", "Test");

        Field f = chm.getClass().getDeclaredField("table"); //NoSuchFieldException
        f.setAccessible(true);
        Object[] iWantThis = (Object[]) f.get(chm);
        Assert.assertEquals(4, iWantThis.length);

        //OR
        ConcurrentHashMap<String, String> chm2 = new ConcurrentHashMap<>(4, 2);

        chm2.put("Test", "Test");

        f = chm2.getClass().getDeclaredField("table"); //NoSuchFieldException
        f.setAccessible(true);
        iWantThis = (Object[]) f.get(chm2);
        Assert.assertEquals(4, iWantThis.length);
    }

    @Test
    public void test_simpleForEachOrder()
    {
        ConcurrentHashMap<String, String> chm = new ConcurrentHashMap<>();

        chm.put("Test1", "Test");
        chm.put("Test2", "Test");
        chm.put("Test3", "Test");
        chm.put("Test4", "Test");
        chm.put("Test5", "Test");

        List<String> keys = new ArrayList<>();

        chm.forEach((k, v) -> keys.add(k));

        Assert.assertArrayEquals(new String[]{"Test1","Test5","Test4","Test3","Test2"}, keys.toArray());
    }

    @Test
    public void test_forEachOrderWithParallelism()
    {
        ConcurrentHashMap<String, String> chm = new ConcurrentHashMap<>();

        chm.put("Test1", "Test");
        chm.put("Test2", "Test");
        chm.put("Test3", "Test");
        chm.put("Test4", "Test");
        chm.put("Test5", "Test");

        List<String> keys = new ArrayList<>();

        chm.forEach(3, (k, v) -> keys.add(k + " " + Thread.currentThread().getName()));

        keys.forEach(System.out::println);
        assertTrue(keys.contains("Test1 main"));
        assertEquals(4, keys.stream().filter(k -> k.contains("ForkJoinPool.commonPool-worker-1")).count());
    }

    @Test
    public void test_forEachTransformConsume()
    {
        ConcurrentHashMap<String, String> chm = new ConcurrentHashMap<>();

        chm.put("Test1", "Test");
        chm.put("Test2", "Test");
        chm.put("Test3", "Test");
        chm.put("Test4", "Test");
        chm.put("Test5", "Test");

        List<String> keys = new ArrayList<>();

        chm.forEach(3,(k,v)-> k+v, (t) -> {
            synchronized (keys)
            {
                keys.add(t + " " + Thread.currentThread().getName());
            }
        });

        keys.forEach(System.out::println);
        assertTrue(keys.contains("Test1Test main"));
        assertEquals(4, keys.stream().filter(k -> k.contains("ForkJoinPool.commonPool-worker-1")).count());
    }

    @Test
    public void test_remove() {
        ConcurrentHashMap<String, String> chm = new ConcurrentHashMap<>();

        chm.put("Test1", "Test");
        chm.put("Test2", "Test");
        chm.put("Test3", "Test");
        chm.put("Test4", "Test");
        chm.put("Test5", "Test");

        assertFalse(chm.remove("Test1", null));
        assertTrue(chm.containsKey("Test1"));
        assertTrue(chm.remove("Test1", "Test"));
    }

    // Requires too much space
    //@Test
    public void test_mappingCount() {
        ConcurrentHashMap<String, Boolean> chm = new ConcurrentHashMap<>();

        for(int i = 0; i <= Integer.MAX_VALUE; i++)
        {
            chm.put("T"+i, Boolean.TRUE);
        }


        assertEquals(Integer.MAX_VALUE, chm.mappingCount());
        assertEquals(Integer.MAX_VALUE, chm.size());

        chm.put("T"+(Integer.MAX_VALUE+1), Boolean.TRUE);

        assertEquals(Integer.MAX_VALUE+1l, chm.mappingCount());
        assertEquals(Integer.MAX_VALUE, chm.size());
    }

    @Test
    public void test_reduce() {
        ConcurrentHashMap<String, String> chm = new ConcurrentHashMap<>();

        chm.put("Test1", "Test");
        chm.put("Test2", "Test");
        chm.put("Test3", "Test");
        chm.put("Test4", "Test");
        chm.put("Test5", "Test");

        String reduce = chm.reduce(3, (k, v) -> k + v, (r, predT) -> r + predT);

        assertEquals("Test1TestTest5TestTest4TestTest3TestTest2Test", reduce);
    }

    @Test
    public void test_computeIfAbsent() {
        ConcurrentHashMap<String, String> chm = new ConcurrentHashMap<>();

        chm.put("Test1", "Test");
        chm.put("Test2", "Test");
        chm.put("Test3", "Test");
        chm.put("Test4", "Test");
        chm.put("Test5", "Test");

        chm.computeIfAbsent("Test6", (k)->k);

        assertEquals("Test6", chm.get("Test6"));
    }
}
