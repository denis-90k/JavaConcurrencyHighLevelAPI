package org.testconc.atomics.integer;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Arbiter;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.III_Result;


/**
 * Simple memory barrier with volatile.
 * Accesses after the write will not be reordered before AND accesses before the read will not be reordered after.
 */
@JCStressTest
@Outcome(id = "0, 1, 0", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Reordering happened(NEVER)") // it requires r3=1 happend after FULL FENCE or t=r3 happend before FULL FENCE
@Outcome(id = "1, 1, 0", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Reordering happened(NEVER)") // it requires r3=1 happend after FULL FENCE or t=r3 happend before FULL FENCE
@Outcome(id = "0, 0, 0", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Reordering happened(NEVER)") // It requires reordering oart of the FULL FENCE
@Outcome(id = "0, 0, 1", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Reordering happened(NEVER)") // It requires reordering oart of the FULL FENCE
@Outcome(id = "", expect = Expect.ACCEPTABLE, desc = "Sequential execution")
@State
public class IntegerReordering1FF1 {

    int x, y, t;
    volatile int r1, r2;
    int r3;

    @Actor
    public void thread1() {
        r3 = 1;

        //FULL FENCE
        r1 = 1;
        x = r2;
        //FULL FENCE
    }

    @Actor
    public void thread2() {
        //FULL FENCE
        r2 = 1;
        y = r1;
        //FULL FENCE

        t = r3;
    }

    @Arbiter
    public void result(III_Result r) {
        r.r1 = x;
        r.r2 = y;
        r.r3 = t;
    }
}
