package org.testconc.jmm;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.II_Result;

@JCStressTest
@Outcome(id = "1, 1", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Reordering happened")
@Outcome(id = "", expect = Expect.ACCEPTABLE, desc = "Sequential execution")
@State
public class SOF2Reordering {

    int x, y;
    int r1, r2;

    @Actor
    public void DTest_thread1() {
        x = r2;
        r1 = 1;
    }

    @Actor
    public void DTest_thread2() {
        y = r1;
        r2 = 1;
    }

    @Arbiter
    public void DTest_result(II_Result r) {
        r.r1 = x;
        r.r2 = y;
    }
}
