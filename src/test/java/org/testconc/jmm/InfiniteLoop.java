package org.testconc.jmm;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.II_Result;

@JCStressTest
@Outcome(id = "42, 42", expect = Expect.ACCEPTABLE, desc = "All 1")
@Outcome(id = "0, 0", expect = Expect.ACCEPTABLE, desc = "All 0")
@Outcome(id = "", expect = Expect.ACCEPTABLE, desc = "Mess")
@State
public class InfiniteLoop {

    volatile int x, y;
    int r1, r2;

    @Actor
    public void thread1() {
        int k=0;
        do {
            r1 = x;
            if(k++==5)
                return;
        } while (r1 == 0);
        y = 42;
    }

    @Actor
    public void thread2() {
        int k=0;
        do {
            r2 = y;
            if(k++==5)
                return;
        } while (r2 == 0);
        x = 42;
    }

    @Arbiter
    public void result(II_Result r) {
        r.r1 = x;
        r.r2 = y;
    }
}
