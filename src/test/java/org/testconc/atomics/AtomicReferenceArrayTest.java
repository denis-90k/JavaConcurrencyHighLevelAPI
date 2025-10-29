package org.testconc.atomics;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReferenceArray;

import static org.junit.Assert.assertEquals;

public class AtomicReferenceArrayTest {

    @Test
    public void test_SetGet()
    {
        AtomicReferenceArray<String> ara = new AtomicReferenceArray<>(16);

        ara.set(0, "Hello");
        ara.set(1, "world");

        assertEquals("Hello", ara.get(0));
        assertEquals("world", ara.get(1));
    }

    @Test
    public void test_lazySet() {
        AtomicReferenceArray<String> ara = new AtomicReferenceArray<>(16);

        ara.lazySet(0, "Hello");
        assertEquals("Hello", ara.getPlain(0));
    }

    @Test
    public void test_accumulateAndGet() {
        AtomicReferenceArray<String> ara = new AtomicReferenceArray<>(16);

        ara.accumulateAndGet(0, "Hello", (old, delta) -> old + " " + delta);
        ara.accumulateAndGet(0, "world", (old, delta) -> old + " " + delta);

        Assert.assertEquals("null Hello world", ara.get(0));
    }

    @Test
    public void test_compareAndExchange_ReleaseAcquire() {
        AtomicReferenceArray<String> ara = new AtomicReferenceArray<>(16);

        ara.compareAndExchangeRelease(0, null, "Hello");
        ara.compareAndExchangeAcquire(0, "Hello", "Hello world");

        Assert.assertEquals("Hello world", ara.get(0));
    }

    @Test
    public void test_length() {
        AtomicReferenceArray<String> ara = new AtomicReferenceArray<>(16);

        assertEquals(16, ara.length());
    }

    @Test
    public void test_weakCompareAndSetVolatile() {
        AtomicReferenceArray<String> ara = new AtomicReferenceArray<>(16);

        ara.weakCompareAndSetVolatile(0, null, "Hello");
        ara.weakCompareAndSetVolatile(0, "Hello", "Hello world");

        Assert.assertEquals("Hello world", ara.get(0));
    }
}
