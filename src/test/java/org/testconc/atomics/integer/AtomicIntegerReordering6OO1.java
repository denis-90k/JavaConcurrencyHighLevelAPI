package org.testconc.atomics.integer;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Arbiter;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.II_Result;

import java.util.concurrent.atomic.AtomicBoolean;


/*
 * When setOpaque / getOpaque makes sense
 * 1. Single-writer, single-reader queues (SPSC queues)
 * 2. Lazy initialization (not fully published yet)
 *    Sometimes you initialize an object that will only be read after you publish it with stronger synchronization.
 * 3. Reducing contention in hot paths
 *    If you have a data structure with thousands of updates per second, but only need visibility eventually
 *    (not immediately), opaque ops can give a big speedup(at compared to volatile).
 * 4. Paired with stronger operations
 * Opaque operations often make sense in combination with other, stronger operations.
 * Example: Write a bunch of values with setOpaque.
 * Then "publish" the result with a setRelease or setVolatile.
 * This ensures visibility, but without paying the price for every single intermediate write.
 * */
@JCStressTest
@Outcome(id = "0, 1", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Reordering happened")
@Outcome(id = "1, 0", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Reordering happened")
@Outcome(id = "", expect = Expect.ACCEPTABLE, desc = "Sequential execution")
@State
public class AtomicIntegerReordering6OO1 {

    AtomicBoolean ai = new AtomicBoolean(false);
    int r1, r2, r3, r4;

    @Actor
    public void thread1() {
        r1 = 1;
        r2 = 1;
        ai.setOpaque(true);
    }

    @Actor
    public void thread2() {
        if (ai.getOpaque()) {
            r3 = r1;
            r4 = r2;
        }
    }

    @Arbiter
    public void result(II_Result r) {
        r.r1 = r3;
        r.r2 = r4;
    }
}
