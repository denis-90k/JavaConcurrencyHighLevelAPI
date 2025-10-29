package org.testconc.atomics;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicStampedReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AtomicStampedReferenceTest {

    @Test
    public void test_() {
        AtomicStampedReference<String> amr = new AtomicStampedReference("Hello ", 1);

        assertEquals("Hello ", amr.getReference());
        assertEquals(1, amr.getStamp());

        assertTrue(amr.attemptStamp("Hello ", 2));
        assertEquals(2, amr.getStamp());

        assertTrue(amr.compareAndSet("Hello ", "world", 2, 3));

        assertEquals("world", amr.getReference());
        assertEquals(3, amr.getStamp());
    }

}
