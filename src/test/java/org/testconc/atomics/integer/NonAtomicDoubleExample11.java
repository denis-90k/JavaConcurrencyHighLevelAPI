package org.testconc.atomics.integer;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.DD_Result;
import org.openjdk.jcstress.infra.results.LL_Result;

@JCStressTest
@Outcome(id = "", expect = Expect.ACCEPTABLE, desc = "Sequential execution")
@State
public class NonAtomicDoubleExample11 {

    double x, y;
    double r1, r2;

    @Actor
    public void thread1() {
        x = r2;
        r1 = 1.7976931348623158E307;
    }

    @Actor
    public void thread2() {
        y = r1;
        r2 = 1.7976931348623158E307;
    }

    @Arbiter
    public void result(DD_Result r) {
        r.r1 = x;
        r.r2 = y;
    }

}
