package org.testconc.collections.queues;

import org.junit.Assert;
import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class LinkedTransferQueueTest {

    @Test
    public void test_OfferPoll() {
        LinkedTransferQueue<String> ltq = new LinkedTransferQueue<>();

        ltq.offer("Msg1");
        ltq.offer("Msg2");
        ltq.offer("Msg3");

        assertEquals("Msg1", ltq.poll());
        assertEquals("Msg2", ltq.poll());
        assertEquals("Msg3", ltq.poll());
        assertNull(ltq.poll());
    }

    @Test(expected = NoSuchElementException.class)
    public void test_AddRemove() {
        LinkedTransferQueue<String> ltq = new LinkedTransferQueue<>();

        ltq.add("Msg1");
        ltq.add("Msg2");
        ltq.add("Msg3");

        assertEquals("Msg1", ltq.remove());
        assertEquals("Msg2", ltq.remove());
        assertEquals("Msg3", ltq.remove());
        ltq.remove();

    }

    @Test
    public void test_transferFaster() throws InterruptedException {
        LinkedTransferQueue<String> ltq = new LinkedTransferQueue<>();
        CountDownLatch cdl = new CountDownLatch(2);
        new Thread(() -> {
            try {
                ltq.transfer("Msg1");
                System.out.println("Thread 1 transfered Msg");
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(1000);
                String take = ltq.take();
                System.out.println("Thread 2 taken Msg ==> " + take);
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        cdl.await();
    }

    @Test
    public void test_transferSlower() throws InterruptedException {
        LinkedTransferQueue<String> ltq = new LinkedTransferQueue<>();
        CountDownLatch cdl = new CountDownLatch(2);
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                ltq.transfer("Msg1");
                System.out.println("Thread 1 transfered Msg");
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> {
            try {
                String take = ltq.take();
                System.out.println("Thread 2 taken Msg ==> " + take);
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        cdl.await();
    }

    @Test
    public void test_offerThenTransferThenTake() throws InterruptedException {
        LinkedTransferQueue<String> ltq = new LinkedTransferQueue<>();
        CountDownLatch cdl = new CountDownLatch(4);
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                ltq.transfer("Msg1");
                System.out.println("Thread 1 transfered Msg1");
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> {
            ltq.offer("Msg2");
            System.out.println("Thread 2 transfered Msg2");
            cdl.countDown();
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(1500);
                String take = ltq.take();
                System.out.println("Thread 3 taken Msg ==> " + take);
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        cdl.await(3, TimeUnit.SECONDS);
    }

    @Test
    public void test_transferThenOfferThenTake() throws InterruptedException {
        LinkedTransferQueue<String> ltq = new LinkedTransferQueue<>();
        CountDownLatch cdl = new CountDownLatch(4);
        new Thread(() -> {
            try {

                ltq.transfer("Msg1");
                System.out.println("Thread 1 transfered Msg1");
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(1000);
                ltq.offer("Msg2");
                System.out.println("Thread 2 transfered Msg2");
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(1500);
                String take = ltq.take();
                System.out.println("Thread 3 taken Msg ==> " + take);
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        cdl.await(3, TimeUnit.SECONDS);
    }

    @Test
    public void test_transferThenOfferThenPoll() throws InterruptedException {
        LinkedTransferQueue<String> ltq = new LinkedTransferQueue<>();
        CountDownLatch cdl = new CountDownLatch(4);
        new Thread(() -> {
            try {

                ltq.transfer("Msg1");
                System.out.println("Thread 1 transfered Msg1");
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(1000);
                ltq.offer("Msg2");
                System.out.println("Thread 2 transfered Msg2");
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(1500);
                String take = ltq.poll();
                System.out.println("Thread 3 taken Msg ==> " + take);
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        cdl.await(3, TimeUnit.SECONDS);
    }

    @Test
    public void test_transferThenOfferThenPeek() throws InterruptedException {
        LinkedTransferQueue<String> ltq = new LinkedTransferQueue<>();
        CountDownLatch cdl = new CountDownLatch(4);
        new Thread(() -> {
            try {

                ltq.transfer("Msg1");
                System.out.println("Thread 1 transfered Msg1");
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(1000);
                ltq.offer("Msg2");
                System.out.println("Thread 2 transfered Msg2");
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(1500);
                String take = ltq.peek();
                System.out.println("Thread 3 taken Msg ==> " + take);
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        cdl.await(3, TimeUnit.SECONDS);

        assertEquals("Msg1", ltq.poll());
        assertEquals("Msg2", ltq.poll());
        assertEquals(null, ltq.poll());
    }

    @Test
    public void test_takeThenPollThenTakeThenTransferThenOffer() throws InterruptedException {
        LinkedTransferQueue<String> ltq = new LinkedTransferQueue<>();
        CountDownLatch cdl = new CountDownLatch(4);

        new Thread(() -> {
            try {
                String take = ltq.take();
                System.out.println("Thread 1 taken Msg ==> " + take);
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(1000);
                String take = ltq.poll();
                System.out.println("Thread 2 taken Msg ==> " + take);
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        AtomicBoolean ab = new AtomicBoolean(false);
        new Thread(() -> {
            try {
                Thread.sleep(1500);
                ab.set(true);
                String take = ltq.take();
                System.out.println("Thread 3 taken Msg ==> " + take);
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(2000);
                ltq.transfer("Msg1");
                System.out.println("Thread 4 transfered Msg1");
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(2500);
                ltq.offer("Msg2");
                System.out.println("Thread 5 offered Msg2");
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        while(!ab.get())
            Thread.onSpinWait();

        Thread.sleep(100);

        assertTrue(ltq.hasWaitingConsumer());
        assertEquals(2, ltq.getWaitingConsumerCount());
        cdl.await(5, TimeUnit.SECONDS);

        assertEquals(null, ltq.poll());
    }

    @Test
    public void test_MultipleTransferFaster() throws InterruptedException {
        LinkedTransferQueue<String> ltq = new LinkedTransferQueue<>();
        CountDownLatch cdl = new CountDownLatch(20);

        AtomicInteger ai = new AtomicInteger();
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    String msg = "Msg" + ai.getAndIncrement();
                    ltq.transfer(msg);
                    System.out.println("Thread " + ai.get() + " transfered " + msg);
                    cdl.countDown();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }


        new Thread(() -> {
            try {
                int count = 0;
                while (true)
                {
                    String take = ltq.take();
                    System.out.println("Thread 2 taken Msg ==> " + take);
                    cdl.countDown();
                    if(++count == 10)
                        break;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        cdl.await();
    }
}
