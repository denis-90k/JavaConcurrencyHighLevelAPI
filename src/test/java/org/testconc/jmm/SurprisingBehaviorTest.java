package org.testconc.jmm;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.III_Result;

@JCStressTest
@Outcome(id = "1, 1, 1", expect = Expect.ACCEPTABLE, desc = "All 1")
@Outcome(id = "0, 0, 0", expect = Expect.ACCEPTABLE, desc = "All 0")
@Outcome(id = "", expect = Expect.ACCEPTABLE, desc = "Mess")
@State
public class SurprisingBehaviorTest {

    int x, y;
    int r1, r2, r3;

    @Actor
    public void actor1() {
        r1 = x;
        if(r1 == 0)
            x = 1;
        r2 = x;
        y = 1;
    }

    @Actor
    public void actor2() {
        r3 = y;
        x = r3;
    }

    @Arbiter
    public void result(III_Result r) {
        r.r1 = r1;
        r.r2 = r2;
        r.r3 = r3;
    }
}
