package org.testconc.atomics.integer;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.*;

@JCStressTest
//@Outcome(id = "1, 1", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Reordering happened(NEVER)")
@Outcome(id = "", expect = Expect.ACCEPTABLE, desc = "Sequential execution")
@State
public class NonAtomicLongExample10 {

    long x, y;
    long r1, r2;

    @Actor
    public void thread1() {
        x = r2;
        r1 = 4611686018527387903l;
    }

    @Actor
    public void thread2() {
        y = r1;
        r2 = 4611686018527387903l;
    }

    @Arbiter
    public void result(LL_Result r) {
        r.r1 = x;
        r.r2 = y;
    }

}
