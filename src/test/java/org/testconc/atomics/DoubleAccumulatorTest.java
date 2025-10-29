package org.testconc.atomics;

import org.junit.Test;

import java.util.concurrent.atomic.DoubleAccumulator;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.function.DoubleBinaryOperator;
import java.util.function.LongBinaryOperator;

import static org.junit.Assert.assertEquals;

public class DoubleAccumulatorTest {

    @Test
    public void test_accumulateThenGetAndReset() {
        DoubleAccumulator la = new DoubleAccumulator(new DoubleBinaryOperator() {
            @Override
            public double applyAsDouble(double left, double right) {
                return left + right;
            }
        }, 0);

        la.accumulate(1.0);
        la.accumulate(1.0);
        la.accumulate(1.0);
        la.accumulate(1.0);

        System.out.println(la.get());
        assertEquals(4.0, la.getThenReset(), 0.001);
        assertEquals(0.0, la.get(), 0.001);
    }
}
