package org.testconc.atomics.integer;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.II_Result;

import java.util.concurrent.atomic.AtomicInteger;

@JCStressTest
@Outcome(id = "0, 1", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Reordering happened")
@Outcome(id = "1, 0", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Reordering happened")
@Outcome(id = "", expect = Expect.ACCEPTABLE, desc = "Sequential execution")
@State
public class AtomicIntegerReordering5RA1 {

    AtomicInteger ai = new AtomicInteger(0);
    int r1, r2, r3, r4;

    @Actor
    public void thread1() {
        r1 = 1;
        r2 = 1;
        ai.setRelease(1);
    }

    @Actor
    public void thread2() {
        if (ai.getAcquire() > 0) {
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


