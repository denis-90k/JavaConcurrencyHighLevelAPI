package org.testconc.atomics;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.function.LongBinaryOperator;

import static org.junit.Assert.assertEquals;

public class LongAccumulatorTest {

    @Test
    public void test_AddingDoubleBoundaries() {
        System.out.println(Double.NEGATIVE_INFINITY + Double.MAX_VALUE);
        System.out.println(Double.POSITIVE_INFINITY + Double.MIN_VALUE);
        System.out.println(Double.NEGATIVE_INFINITY + Double.POSITIVE_INFINITY);
    }

    @Test
    public void test_accumulateThenGetAndReset() {
        LongAccumulator la = new LongAccumulator(new LongBinaryOperator() {
            @Override
            public long applyAsLong(long currentBase, long delta) {
                return currentBase + delta;
            }
        }, 0);

        la.accumulate(1);
        la.accumulate(1);
        la.accumulate(1);
        la.accumulate(1);

        assertEquals(4, la.getThenReset());
        assertEquals(0, la.get());
    }

    @Test
    public void test_accumulateMultThenGetAndReset() {
        LongAccumulator la = new LongAccumulator(new LongBinaryOperator() {
            @Override
            public long applyAsLong(long currentBase, long delta) {
                return currentBase * delta;
            }
        }, 1);

        la.accumulate(1);
        la.accumulate(2);
        la.accumulate(3);
        la.accumulate(4);

        assertEquals(24, la.getThenReset());
        assertEquals(1, la.get());
    }

    @Test // Add -Xint to disable JIT
    public void test_underHighContention() throws InterruptedException {
        LongAccumulator la = new LongAccumulator(new LongBinaryOperator() {
            @Override
            public long applyAsLong(long currentBase, long delta) {
                return currentBase + delta;
            }
        }, 0);

        int interCount = 100000;
        int cores = 15;
        CountDownLatch cdl = new CountDownLatch(interCount * cores);
        List<Thread> thrs = new ArrayList<>();

        for (int k = 0; k < cores; k++) {
            Thread thr = new Thread(() -> {
                for (int i = 0; i < interCount; i++) {
                    new Thread(() -> {
                        la.accumulate(1);
                        cdl.countDown();
                    }).start();
                }
            });
            thrs.add(thr);
        }

        long start1 = System.currentTimeMillis();
        thrs.forEach(Thread::start);
        cdl.await();
        long delta1 = System.currentTimeMillis() - start1;

        AtomicLong al = new AtomicLong(0);
        CountDownLatch cdl2 = new CountDownLatch(interCount * cores);
        thrs.clear();
        for (int k = 0; k < cores; k++) {
            Thread thr = new Thread(() -> {
                for (int i = 0; i < interCount; i++) {
                    new Thread(() -> {
                        al.incrementAndGet();
                        cdl2.countDown();
                    }).start();
                }
            });
            thrs.add(thr);
        }

        long start2 = System.currentTimeMillis();
        thrs.forEach(Thread::start);
        cdl2.await();
        long delta2 = System.currentTimeMillis() - start2;

        assertEquals(interCount * cores, al.get());
        assertEquals(interCount * cores, la.get());

        System.out.println("LongAccumulator throughput takes time -> " + delta1);
        System.out.println("AtomicLong throughput takes time -> " + delta2);
    }
}
