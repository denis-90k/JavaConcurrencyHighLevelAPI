package org.testconc.atomics;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.LongAdder;

public class LongAdderTest
{
    @Test
    public void test_addAndSum()
    {
        LongAdder la = new LongAdder();

        la.add(1);
        la.add(2);
        la.add(3);
        la.add(4);
        la.add(5);

        Assert.assertEquals(15, la.sum());
    }

    @Test
    public void test_AddDecrementSum()
    {
        LongAdder la = new LongAdder();

        la.add(1);
        la.add(2);
        la.add(3);
        la.add(4);
        la.add(5);

        la.decrement();
        Assert.assertEquals(14, la.sum());
    }

    @Test
    public void test_doubleLong()
    {
        LongAdder la = new LongAdder();

        la.add(1);
        la.add(2);
        la.add(3);
        la.add(4);
        la.add(5);

        Assert.assertEquals(15.0, la.doubleValue(), 0);
        Assert.assertEquals(15, la.longValue());
    }

    @Test
    public void test_reset()
    {
        LongAdder la = new LongAdder();

        la.add(1);
        la.add(2);
        la.add(3);
        la.add(4);
        la.add(5);

        la.reset();
        Assert.assertEquals(0, la.sum());
    }
}
