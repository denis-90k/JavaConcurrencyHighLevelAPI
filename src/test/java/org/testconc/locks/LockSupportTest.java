package org.testconc.locks;

import org.junit.Test;

import java.util.concurrent.locks.LockSupport;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LockSupportTest {

    @Test
    public void test_setGetBlocker() {
        Object blocker = new Object();
        LockSupport.setCurrentBlocker(blocker);

        Object blocker1 = LockSupport.getBlocker(Thread.currentThread());

        assertTrue(blocker == blocker1);
    }

    @Test
    public void test_NullBlocker() {
        Object blocker = LockSupport.getBlocker(Thread.currentThread());
        assertNull(blocker);
    }

    @Test
    public void test_blockerOfParkedThread() throws InterruptedException {
        Thread thread = new Thread(() -> {
            LockSupport.park();
        });
        thread.start();

        Thread.sleep(200);

        Object blocker = LockSupport.getBlocker(thread);

        assertNull(blocker);

    }

    @Test
    public void test_UparkPark() {

        LockSupport.unpark(Thread.currentThread());
        LockSupport.park();

        assertTrue(true);
    }


}
