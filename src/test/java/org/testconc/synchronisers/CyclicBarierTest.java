package org.testconc.synchronisers;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class CyclicBarierTest {

    @Test
    public void test_SimpleBarrier() throws InterruptedException, BrokenBarrierException {
        CyclicBarrier cb = new CyclicBarrier(2, new Runnable() {
            @Override
            public void run() {
                System.out.println("Barrier passed");
            }
        });

        for (int i = 0; i < 2; i++) {
            new Thread(() -> {
                try {
                    cb.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (BrokenBarrierException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }

        Thread.sleep(500);
    }

    @Test(expected = BrokenBarrierException.class)
    public void test_Exception() throws InterruptedException, BrokenBarrierException {

        CyclicBarrier cb = new CyclicBarrier(2, new Runnable() {
            @Override
            public void run() {
                System.out.println("Barrier passed");
            }
        });

        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            cb.reset();
        }).start();

        cb.await();
    }

    @Test
    public void test_ClearOrder() throws InterruptedException, BrokenBarrierException {
        CyclicBarrier cb = new CyclicBarrier(7, new Runnable() {
            @Override
            public void run() {
                System.out.println("The last one");
                Assert.assertTrue(Thread.currentThread().getName().contains("main"));
            }
        });

        new Thread(() -> {
            try {
                Assert.assertEquals(6, cb.await());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
        }).start();
        
        Thread.sleep(500);
        for(int i = 0; i<5;i++) {
            new Thread(() -> {
                try {
                    cb.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (BrokenBarrierException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }

        Thread.sleep(1000);
        Assert.assertEquals(0, cb.await());
    }
}
