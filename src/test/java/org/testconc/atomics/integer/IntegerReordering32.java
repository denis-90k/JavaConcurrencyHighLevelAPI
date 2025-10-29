package org.testconc.atomics.integer;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.III_Result;
import org.openjdk.jcstress.infra.results.II_Result;


/*
* Volatile
* No reordering of all accesses before write to after it(+) and write before write after it(cannot check with stress test)!!!!
* No reordering of all accesses after read to before it(+) and read before read after it(cannot check with stress test)!!!!
*
* */
@JCStressTest
@Outcome(id = "1, 1", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Reordering happened()")
@Outcome(id = "", expect = Expect.ACCEPTABLE, desc = "Sequential execution")
@State
public class IntegerReordering32 {

    int x, y, z;
    int r2, r3;
    volatile int r1;

    @Actor
    public void thread1() {
        x = r2;
        r1 = 1; //RELEASE FENCE x = r2 never happen before it

    }

    @Actor
    public void thread2() {
        y = r1; // ACQUIRE FENCE
        r2 = 1;
    }

    @Arbiter
    public void result(II_Result r) {
        r.r1 = x;
        r.r2 = y;
    }
}
