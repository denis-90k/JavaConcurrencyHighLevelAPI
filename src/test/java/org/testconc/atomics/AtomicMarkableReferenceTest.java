package org.testconc.atomics;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicMarkableReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AtomicMarkableReferenceTest {

    @Test
    public void test_() {
        AtomicMarkableReference amr = new AtomicMarkableReference("Hello ", false);

        assertEquals("Hello ", amr.getReference());
        assertFalse(amr.isMarked());

        assertTrue(amr.attemptMark("Hello ", true));
        assertTrue(amr.isMarked());

        assertTrue(amr.compareAndSet("Hello ", "world", true, true));

        assertEquals("world", amr.getReference());
    }
}
