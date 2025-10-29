package org.testconc.atomics;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AtomicReferenceFieldUpdaterTest {
    private static class DummyClass {
        private volatile String name1;
        private volatile String name2;

        public String getName1() {
            return name1;
        }

        public String getName2() {
            return name2;
        }
    }

    @Test
    public void test_updater() {
        AtomicReferenceFieldUpdater<DummyClass, String> aifu = AtomicReferenceFieldUpdater.newUpdater(DummyClass.class, String.class, "name1");
        AtomicReferenceFieldUpdater<DummyClass, String> aifu1 = AtomicReferenceFieldUpdater.newUpdater(DummyClass.class, String.class, "name2");

        DummyClass dc = new DummyClass();

        assertEquals(null, aifu.get(dc));

        assertTrue(aifu.compareAndSet(dc, null, "5"));
        assertTrue(aifu1.compareAndSet(dc, null, "5"));
        assertEquals("5", aifu.get(dc));
        assertEquals("5", aifu1.get(dc));
    }
}
