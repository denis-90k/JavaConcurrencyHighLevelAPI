package org.testconc.synchronisers;

import org.junit.Test;

import java.util.concurrent.Semaphore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SemaphoreTest {

    @Test
    public void test_simpleAcquireRelease() throws InterruptedException {
        Semaphore sem = new Semaphore(1);

        sem.acquire();

        sem.release();
    }

    @Test
    public void test_DoubleAcquire() throws InterruptedException {
        Semaphore sem = new Semaphore(1);

        sem.acquire();

        new Thread(() -> {
            try {
                sem.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        Thread.sleep(2000);
        sem.release();
    }

    @Test
    public void test_ReleaseExtrAcquire() throws InterruptedException {
        Semaphore sem = new Semaphore(1);

        sem.release(10);

        assertEquals(11, sem.availablePermits());

        sem.acquire(11);
    }

    @Test
    public void test_drainPermits() {
        Semaphore sem = new Semaphore(10);

        assertEquals(10, sem.drainPermits());
    }

    @Test
    public void test_NegativePermits() throws InterruptedException {
        Semaphore sem = new Semaphore(-10);

//        sem.acquire();
        assertFalse(sem.tryAcquire());
    }

    @Test
    public void test_MultipleAcquires() throws InterruptedException {
        Semaphore sem = new Semaphore(1);

        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    sem.acquire();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }

        sem.release();
    }
}
