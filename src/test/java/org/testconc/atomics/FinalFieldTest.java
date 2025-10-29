package org.testconc.atomics;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.III_Result;
import org.openjdk.jcstress.infra.results.II_Result;
import org.openjdk.jcstress.infra.results.I_Result;

@JCStressTest
@Outcome(id = "", expect = Expect.ACCEPTABLE, desc = "Result")
@State
public class FinalFieldTest {

    private Holder holder;
    private int x;
    private int y;

    class Holder {
        final int value;

        Holder() {
            readBrokenHolder(this);
            value = 1;
        }
    }

    public void readBrokenHolder(Holder h) {
        y = h.value;
    }

    @Actor
    void create() {
        holder = new Holder(); // (1)
    }

    @Actor
    void use() {
        if (holder != null) {
            x = holder.value; // (2)
        }
    }

    @Arbiter
    public void result(II_Result r) {
        r.r1 = x;
        r.r2 = y;
    }
}
