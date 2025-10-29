package org.testconc.atomics;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class AtomicLongFieldUpdaterTest {

    private static class DummyClass {
        private volatile long name1;
        private volatile long name2;

        public long getName1() {
            return name1;
        }

        public long getName2() {
            return name2;
        }
    }

    @Test
    public void test_updater()
    {
        AtomicLongFieldUpdater<DummyClass> aifu = AtomicLongFieldUpdater.newUpdater(DummyClass.class, "name1");
        AtomicLongFieldUpdater<DummyClass> aifu1 = AtomicLongFieldUpdater.newUpdater(DummyClass.class, "name2");

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
