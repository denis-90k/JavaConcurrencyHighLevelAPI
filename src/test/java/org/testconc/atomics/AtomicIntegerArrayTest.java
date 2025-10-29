package org.testconc.atomics;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicIntegerArray;

import static org.junit.Assert.assertEquals;

public class AtomicIntegerArrayTest {

    @Test
    public void test_AddAndGet()
    {
        AtomicIntegerArray aia = new AtomicIntegerArray(16);

        assertEquals(1, aia.addAndGet(1, 1));
        assertEquals(3, aia.addAndGet(1, 2));
        assertEquals(6, aia.addAndGet(1, 3));
        assertEquals(10, aia.addAndGet(1, 4));

        assertEquals(10, aia.get(1));
    }

    @Test
    public void test_CompareAndExchange()
    {
        AtomicIntegerArray aia = new AtomicIntegerArray(16);

        assertEquals(0, aia.compareAndExchange(1, 0, 10));

        assertEquals(10, aia.get(1));
    }

    @Test
    public void test_compareAndExchangeAcquire()
    {
        AtomicIntegerArray aia = new AtomicIntegerArray(16);

        assertEquals(0, aia.compareAndExchangeAcquire(1, 0, 7));
        assertEquals(7, aia.get(1));
    }

    @Test
    public void test_GetAndSet()
    {
        AtomicIntegerArray aia = new AtomicIntegerArray(16);

        assertEquals(-1, aia.decrementAndGet(0));
        assertEquals(0, aia.getAndDecrement(1));
        assertEquals(-1, aia.getAcquire(0));
        assertEquals(0, aia.getPlain(2));

        aia.compareAndExchangeRelease(2, 0, 10);
        assertEquals(10, aia.getPlain(2));
    }
}
