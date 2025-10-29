package org.testconc.atomics;

import com.google.common.util.concurrent.AtomicDoubleArray;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AtomicDoubleArrayTest {

    @Test
    public void test_AddAndGet() {
        AtomicDoubleArray ada = new AtomicDoubleArray(16);

        assertEquals(1.0, ada.addAndGet(1, 1.0), 0.000001);
        assertEquals(3., ada.addAndGet(1, 2.0), 0.000001);
        assertEquals(6., ada.addAndGet(1, 3.0), 0.000001);
        assertEquals(10., ada.addAndGet(1, 4.0), 0.000001);

        assertEquals(10., ada.get(1), 0.000001);
    }

    @Test
    public void test_compareAndSet() {
        AtomicDoubleArray ada = new AtomicDoubleArray(16);

        assertTrue(ada.compareAndSet(0, 0, 12));
        assertFalse(ada.compareAndSet(0, 0, 12));

        assertEquals(12, ada.get(0), 0.0001);
    }

    @Test
    public void test_accumulateAndGet() {
        AtomicDoubleArray aia = new AtomicDoubleArray(16);

        assertEquals(5., aia.accumulateAndGet(1, 5, (old, delta) -> old + delta), 0.00001);

        assertEquals(5., aia.get(1), 0.00001);
    }

    @Test
    public void test_lazySet() {
        AtomicDoubleArray aia = new AtomicDoubleArray(16);

        aia.lazySet(2, 25.0);

        assertEquals(25.0, aia.get(2), 0.00001);
    }
}
