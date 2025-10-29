package org.testconc.atomics.integer;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.III_Result;


/**
 * Simple memory barrier with volatile.
 * Accesses after the write will not be reordered before AND accesses before the read will not be reordered after.
 */
@JCStressTest
@Outcome(id = "1, 0, 1", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Reordering happened")
@Outcome(id = "0, 1, 0", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Reordering happened")
@Outcome(id = "1, 0, 0", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Reordering happened")
@Outcome(id = "0, 0, 0", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Reordering happened(NEVER)") // It requires reordering part of the FULL FENCE
@Outcome(id = "0, 0, 1", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Reordering happened(NEVER)") // It requires reordering part of the FULL FENCE
@Outcome(id = "", expect = Expect.ACCEPTABLE, desc = "Sequential execution")
@State
public class IntegerReordering1FF2 {

    int x, y, t;
    volatile int r1, r2;
    int r3;

    @Actor
    public void thread1() {
        //FULL FENCE
        r1 = 1;
        r3 = 1;
        x = r2;
        //FULL FENCE
    }

    @Actor
    public void thread2() {
        //FULL FENCE
        r2 = 1;
        t = r3;
        y = r1;
        //FULL FENCE
    }

    @Arbiter
    public void result(III_Result r) {
        r.r1 = x;
        r.r2 = y;
        r.r3 = t;
    }
}
