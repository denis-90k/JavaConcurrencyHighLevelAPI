package org.testconc.atomics.integer;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Arbiter;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.II_Result;

@JCStressTest
@Outcome(id = "1, 1", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Reordering happened(NEVER)")
@Outcome(id = "", expect = Expect.ACCEPTABLE, desc = "Sequential execution")
@State
public class IntegerReordering2 {

    volatile int x, y;
    int r1, r2;

    @Actor
    public void thread1() {
        x = r2; //RELEASE FENCE r1=1 never happen before it
        r1 = 1;
    }

    @Actor
    public void thread2() {
        y = r1; //RELEASE FENCE r2=1 never happen before it
        r2 = 1;
    }

    @Arbiter
    public void result(II_Result r) {
        r.r1 = x;
        r.r2 = y;
    }

}
