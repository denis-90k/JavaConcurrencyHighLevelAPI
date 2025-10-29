package org.testconc.atomics;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class AtomicIntegerFieldUpdaterTest {

    private static class DummyClass {
        private volatile int name1;
        private volatile int name2;

        public int getName1() {
            return name1;
        }

        public int getName2() {
            return name2;
        }
    }

    @Test
    public void test_updater()
    {
        AtomicIntegerFieldUpdater<DummyClass> aifu = AtomicIntegerFieldUpdater.newUpdater(DummyClass.class, "name1");
        AtomicIntegerFieldUpdater<DummyClass> aifu1 = AtomicIntegerFieldUpdater.newUpdater(DummyClass.class, "name2");

        DummyClass dc = new DummyClass();

        assertEquals(0, aifu.get(dc));

        assertEquals(5, aifu.addAndGet(dc, 5));
        assertEquals(5, aifu1.addAndGet(dc, 5));
        assertEquals(5, aifu.get(dc));
        assertEquals(5, aifu1.get(dc));

        assertFalse(aifu.compareAndSet(dc, 23, 7));
        aifu.compareAndSet(dc, 5, 7);
        assertEquals(7, aifu.get(dc));

        assertEquals(4, aifu1.decrementAndGet(dc));

        aifu1.lazySet(dc, 78);
        assertEquals(78, aifu1.get(dc));
    }
}
