package org.testconc.atomics.integer;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.III_Result;


/**
 * Release/Acquire pair
 * Example from https://alexn.org/blog/2023/06/19/java-volatiles/
 */
@JCStressTest
@Outcome(id = "0, 0, 1", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Reordering happened")
@Outcome(id = "", expect = Expect.ACCEPTABLE, desc = "Sequential execution")
@State
public class IntegerReordering4 {

    volatile boolean hasValue;
    int r1, r2, r3, r4, r5, r6;

    @Actor
    public void thread1() {
        r1 = 1;
        r2 = 1;
        hasValue = true;
        r5 = 1; // As I tested in IntegerReordering2, it cannot be reordered above hasValue = true
    }

    @Actor
    public void thread2() {
        r6 = r5; // Most probably this was moved below if statement. Because r5=1 movement was tested in IntegerReordering31. It's not possible
        if (hasValue) {
            r3 = r1;
            r4 = r2;
        }
    }

    @Arbiter
    public void result(III_Result r) {
        r.r1 = r3;
        r.r2 = r4;
        r.r3 = r6;
    }
}
