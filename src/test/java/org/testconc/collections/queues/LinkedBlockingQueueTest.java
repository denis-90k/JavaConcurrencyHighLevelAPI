package org.testconc.collections.queues;

import org.junit.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class LinkedBlockingQueueTest {

    @Test
    public void test_FIFOQueue() {
        LinkedBlockingQueue<String> lbq = new LinkedBlockingQueue<>();

        lbq.offer("Msg1");
        lbq.offer("Msg2");
        lbq.offer("Msg3");

        assertEquals("Msg1", lbq.poll());
        assertEquals("Msg2", lbq.poll());
        assertEquals("Msg3", lbq.poll());
    }

    @Test
    public void test_FIFO_blockWhenEmpty() throws InterruptedException {
        LinkedBlockingQueue<String> lbq = new LinkedBlockingQueue<>();

        lbq.offer("Msg1");
        lbq.offer("Msg2");
        lbq.offer("Msg3");

        assertEquals("Msg1", lbq.poll());
        assertEquals("Msg2", lbq.poll());
        assertEquals("Msg3", lbq.poll());
        assertNull(lbq.poll(1, TimeUnit.SECONDS));
    }

    @Test
    public void test_BlockingProducerConsumer() throws InterruptedException {
        LinkedBlockingQueue<String> lbq = new LinkedBlockingQueue<>(1);
        AtomicInteger countConsumer = new AtomicInteger();
        AtomicInteger countProducer = new AtomicInteger();

        int counter = 5;
        CountDownLatch cdl = new CountDownLatch(counter);
        new Thread(() -> {
            while (countConsumer.get() < counter) {
                try {
                    countConsumer.getAndIncrement();
                    String result = lbq.take();
                    assertEquals("Msg" + countConsumer.get(), result);
                    cdl.countDown();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        new Thread(() -> {
            while (countProducer.get() < counter) {
                try {
                    countProducer.getAndIncrement();
                    lbq.put("Msg" + countProducer.get());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        }).start();

        cdl.await();
    }

    @Test
    public void test_SplitaratorWithExecutor() throws InterruptedException {
        LinkedBlockingQueue<String> lbq = new LinkedBlockingQueue<>();

        lbq.offer("Msg1");
        lbq.offer("Msg2");
        lbq.offer("Msg3");
        lbq.offer("Msg4");
        lbq.offer("Msg5");

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Spliterator<String> split1 = lbq.spliterator();
        Spliterator<String> split2 = split1.trySplit();

        CountDownLatch cdl = new CountDownLatch(5);
        executorService.execute(() -> {
            while (split1.tryAdvance((x) -> {
                System.out.println(x + " ==== " + Thread.currentThread().getName());
            })) {
                cdl.countDown();
            }
        });
        executorService.execute(() -> {
            while (split2.tryAdvance((x) -> {
                System.out.println(x + " ==== " + Thread.currentThread().getName());
            })) {
                cdl.countDown();
            }
        });

        cdl.await();
    }

    @Test(expected = NoSuchElementException.class)
    public void test_Iterator_WeaklyConsistent() throws InterruptedException {
        LinkedBlockingQueue<String> lbq = new LinkedBlockingQueue<>();

        lbq.offer("Msg1");
        lbq.offer("Msg2");
        lbq.offer("Msg3");
        lbq.offer("Msg4");
        lbq.offer("Msg5");

        Iterator<String> iterator = lbq.iterator();

        assertEquals("Msg1", iterator.next());
        assertEquals("Msg2", iterator.next());
        assertEquals("Msg3", iterator.next());

        lbq.offer("Msg6");

        assertEquals("Msg4", iterator.next());
        assertEquals("Msg5", iterator.next());
        assertEquals("Msg6", iterator.next());

        lbq.offer("Msg7");

        assertFalse(iterator.hasNext());
        iterator.next();
    }

    @Test
    public void test_remainingCapacity() throws InterruptedException {
        LinkedBlockingQueue<String> lbq = new LinkedBlockingQueue<>();

        lbq.offer("Msg1");
        lbq.offer("Msg2");
        lbq.offer("Msg3");

        assertEquals(Integer.MAX_VALUE - 3, lbq.remainingCapacity());
    }

    @Test
    public void test_BlockingMultipleProducerConsumer() throws InterruptedException {
        LinkedBlockingQueue<String> lbq = new LinkedBlockingQueue<>(1);
        AtomicInteger countConsumer = new AtomicInteger(0);
        AtomicInteger countProducer = new AtomicInteger();

        int counter = 20;
        CountDownLatch cdl = new CountDownLatch(counter * 2);

        new Thread(() -> {
            while (countConsumer.get() <= counter) {
                try {
                    Thread.sleep(200);
                    countConsumer.getAndIncrement();
                    String result = lbq.take();
                    System.out.println(result);
                    cdl.countDown();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                while (countProducer.get() < counter) {
                    try {
                        Thread.sleep(200);
                        lbq.put("Msg" + countProducer.getAndIncrement());
                        cdl.countDown();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                }
            }).start();
        }
        cdl.await();
    }
}