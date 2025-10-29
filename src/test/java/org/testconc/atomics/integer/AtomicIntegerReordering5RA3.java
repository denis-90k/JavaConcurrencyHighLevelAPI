package org.testconc.atomics.integer;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.II_Result;

import java.util.concurrent.atomic.AtomicInteger;

@JCStressTest
@Outcome(id = "0, 0", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Reordering happened") // Possible
@Outcome(id = "", expect = Expect.ACCEPTABLE, desc = "Sequential execution")
@State
public class AtomicIntegerReordering5RA3 {

    AtomicInteger ai1 = new AtomicInteger(0);
    AtomicInteger ai2 = new AtomicInteger(0);
    int r1, r2, r3, r4;

    @Actor
    public void thread1() {
        ai1.setRelease(1);
        if (ai2.getAcquire() > 0) {
            r4 = ai2.getAcquire();
        }

    }

    @Actor
    public void thread2() {
        ai2.setRelease(1);
        if (ai1.getAcquire() > 0) {
            r3 = ai1.getAcquire();
        }

    }

    @Arbiter
    public void result(II_Result r) {
        r.r1 = r3;
        r.r2 = r4;
    }
}


