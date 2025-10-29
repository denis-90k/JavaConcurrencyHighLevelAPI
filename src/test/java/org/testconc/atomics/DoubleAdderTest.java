package org.testconc.atomics;

import org.junit.Test;

import java.util.concurrent.atomic.DoubleAccumulator;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.DoubleBinaryOperator;

import static org.junit.Assert.assertEquals;

public class DoubleAdderTest {

    @Test
    public void test_accumulateThenGetAndReset() {
        DoubleAdder da = new DoubleAdder();

        da.add(1.0);
        da.add(1.0);
        da.add(1.0);
        da.add(1.0);

        System.out.println(da.sum());
        assertEquals(4.0, da.sumThenReset(), 0.001);
        assertEquals(0.0, da.sum(), 0.001);
    }

}
