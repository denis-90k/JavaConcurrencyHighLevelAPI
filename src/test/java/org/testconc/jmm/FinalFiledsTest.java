package org.testconc.jmm;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.II_Result;
import org.openjdk.jcstress.infra.results.I_Result;

@JCStressTest
@Outcome(id = "42", expect = Expect.ACCEPTABLE, desc = "One 1")
@Outcome(id = "", expect = Expect.ACCEPTABLE, desc = "Mess")
@State
public class FinalFiledsTest {

    int t2;
    SampleCls cls1, cls2;


    @Actor
    public void thread1() {
        cls1 = new SampleCls();
    }

    @Actor
    public void thread2() {
        cls2 = new SampleCls();
    }

    @Arbiter
    public void result(I_Result r) {
        r.r1 = t2;
    }

    class SampleCls {
        final int t1;


        SampleCls() {
            t1 = 42;
            t2 = t1;
        }
    }
}
