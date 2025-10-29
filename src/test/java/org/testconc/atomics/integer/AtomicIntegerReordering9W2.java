package org.testconc.atomics.integer;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.II_Result;

import java.util.concurrent.atomic.AtomicInteger;

@JCStressTest
@Outcome(id = "0, 1", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Reordering happened!") // Possible
@Outcome(id = "", expect = Expect.ACCEPTABLE, desc = "Sequential execution")
@State
public class AtomicIntegerReordering9W2 {

    AtomicInteger ai = new AtomicInteger(0);
    int r1, r2, r3, r4;

    @Actor
    public void thread1() {
        r1 = 1;
        ai.weakCompareAndSetRelease(0, 1);
    }

    @Actor
    public void thread2() {
        r4 = ai.getPlain();
        r2 = r1;

    }

    @Arbiter
    public void result(II_Result r) {
        r.r1 = r2;
        r.r2 = r4;
    }
}
