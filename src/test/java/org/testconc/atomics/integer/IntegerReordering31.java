package org.testconc.atomics.integer;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.III_Result;

// ????????????????????????????????????  HOW TO CHECK ACQUIRE(no way)
@JCStressTest
@Outcome(id = "0, 0, 1", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Reordering happened(NEVER)")
@Outcome(id = "1, 0, 1", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Reordering happened(NEVER)")
@Outcome(id = "", expect = Expect.ACCEPTABLE, desc = "Sequential execution")
@State
public class IntegerReordering31 {

    int x, y, z;
    int r2, r3;
    volatile int r1;

    @Actor
    public void thread1() {
        r1 = 1; //RELEASE FENCE x = r2 never happen before it
        x = r2;
        r3 = 1;
    }

    //  x  y  z
    //  _________
    //  0, 0, 1 possible when swap 37 and 38(r2=1 with y=r1)
    //  0, 0, 0 possible when swap 37 and 38(r2=1 with y=r1)
    //  1, 0, 1 +
    //  1, 0, 0 +
    //  0, 1, 1 +
    //  0, 1, 0 +
    //  1, 1, 1 +
    //  1, 1, 0 +
    @Actor
    public void thread2() {
        r2 = 1;
        y = r1; //ACQUIRE FENCE not working for line r2=1(checked with 3 vars and received results 0,0,0 and 0,0,1 what
                // would not be possible without reordering r2=1 after y=r1
        z = r3; // didn't find a way to check whether z=r3 is moved before y=r1
    }

    @Arbiter
    public void result(III_Result r) {
        r.r1 = x;
        r.r2 = y;
        r.r3 = z;
    }
}
