package org.testconc.collections.queues;

import org.junit.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class LinkedBlockingDequeTest {

    @Test
    public void test_FIFOQueue() {
        LinkedBlockingDeque<String> lbd = new LinkedBlockingDeque<>();

        lbd.offer("Msg1");
        lbd.offer("Msg2");
        lbd.offer("Msg3");

        assertEquals("Msg1", lbd.poll());
        assertEquals("Msg2", lbd.poll());
        assertEquals("Msg3", lbd.poll());
    }

    @Test
    public void test_LIFOQueue() {
        LinkedBlockingDeque<String> lbd = new LinkedBlockingDeque<>();

        lbd.push("Msg1");
        lbd.push("Msg2");
        lbd.offerFirst("Msg3");

        assertEquals("Msg3", lbd.pop());
        assertEquals("Msg2", lbd.pop());
        assertEquals("Msg1", lbd.pop());
    }

    @Test(expected = NoSuchElementException.class)
    public void test_Iterator_WeaklyConsistent() throws InterruptedException {
        LinkedBlockingDeque<String> lbq = new LinkedBlockingDeque<>();

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

    @Test(expected = NoSuchElementException.class)
    public void test_descendingIterator_WeaklyConsistent() throws InterruptedException {
        LinkedBlockingDeque<String> lbq = new LinkedBlockingDeque<>();

        lbq.offer("Msg1");
        lbq.offer("Msg2");
        lbq.offer("Msg3");
        lbq.offer("Msg4");
        lbq.offer("Msg5");

        Iterator<String> iterator = lbq.descendingIterator();

        assertEquals("Msg5", iterator.next());
        assertEquals("Msg4", iterator.next());
        assertEquals("Msg3", iterator.next());

        lbq.offerFirst("Msg6");

        assertEquals("Msg2", iterator.next());
        assertEquals("Msg1", iterator.next());
        assertEquals("Msg6", iterator.next());

        lbq.offer("Msg7");
        lbq.offerFirst("Msg7");

        assertFalse(iterator.hasNext());
        iterator.next();
    }

    @Test
    public void test_SplitaratorWithExecutor() throws InterruptedException {
        LinkedBlockingDeque<String> lbd = new LinkedBlockingDeque<>();

        lbd.offer("Msg1");
        lbd.offer("Msg2");
        lbd.offer("Msg3");
        lbd.offer("Msg4");
        lbd.offer("Msg5");

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Spliterator<String> split1 = lbd.spliterator();
        Spliterator<String> split2 = split1.trySplit();

        CountDownLatch cdl = new CountDownLatch(5);
        lbd.offer("Msg6");
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

    @Test
    public void test_BlockingProducerConsumer() throws InterruptedException {
        LinkedBlockingDeque<String> lbd = new LinkedBlockingDeque<>(3);
        AtomicInteger countConsumer = new AtomicInteger();
        AtomicInteger countProducer = new AtomicInteger();

        int counter = 10;
        CountDownLatch cdcl = new CountDownLatch(counter);
        new Thread(() -> {
            while (countConsumer.get() < counter) {
                try {
                    countConsumer.getAndIncrement();
                    String result = lbd.takeLast();
                    System.out.println(result);
                    cdcl.countDown();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        new Thread(() -> {
            while (countProducer.get() < counter) {
                try {
                    countProducer.getAndIncrement();
                    lbd.put("Msg" + countProducer.get());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        }).start();
        cdcl.await();
    }
}
