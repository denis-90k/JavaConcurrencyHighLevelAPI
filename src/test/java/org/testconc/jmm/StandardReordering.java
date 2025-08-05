package org.testconc.jmm;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.III_Result;
import org.openjdk.jcstress.infra.results.II_Result;

@JCStressTest
@Outcome(id="1, 1", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Standard reordering")
@Outcome(id="" , expect = Expect.ACCEPTABLE, desc = "Mess")
@State
public class StandardReordering {

    int x, y;
    int r1, r2;

    @Actor
    public void denis_actor1() {
        r1 = x;
        y = 1;
    }

    @Actor
    public void denis_actor2() {
        r2 = y;
        x = r2;
    }

    @Arbiter
    public void result(II_Result r) {
        r.r1 = r1;
        r.r2 = r2;
    }
}
