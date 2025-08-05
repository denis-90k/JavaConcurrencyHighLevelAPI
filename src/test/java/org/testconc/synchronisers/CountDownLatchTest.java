package org.testconc.synchronisers;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CountDownLatchTest {

    @Test
    public void test_3countDownAwait() throws InterruptedException {
        CountDownLatch cdl = new CountDownLatch(1);

        cdl.countDown();
        cdl.countDown();
        cdl.countDown();

        cdl.await();
    }

    @Test
    public void test_awaitTimely() throws InterruptedException {
        CountDownLatch cdl = new CountDownLatch(1);

        assertEquals(1, cdl.getCount());
        assertFalse(cdl.await(1, TimeUnit.SECONDS));
    }
}
