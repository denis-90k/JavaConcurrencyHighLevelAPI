package org.testconc.collections.queues;

import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class ConcurrentLinkedQueueTest {

    @Test
    public void test_OfferPollOperationsFIFO() {
        ConcurrentLinkedQueue<String> clq = new ConcurrentLinkedQueue<>();
        clq.offer("Msg1");
        clq.offer("Msg2");
        clq.offer("Msg3");

        assertEquals("Msg1", clq.poll());
        assertEquals("Msg2", clq.poll());
        assertEquals("Msg3", clq.poll());
    }

    @Test(expected = NoSuchElementException.class)
    public void test_AddRemoveOperationsFIFO() {
        ConcurrentLinkedQueue<String> clq = new ConcurrentLinkedQueue<>();

        clq.add("Msg1");
        clq.add("Msg2");
        clq.add("Msg3");

        assertEquals("Msg1", clq.element());
        assertEquals("Msg1", clq.remove());
        assertEquals("Msg2", clq.remove());
        assertEquals("Msg3", clq.remove());
        clq.remove();
    }

    @Test
    public void test_IterateFIFO() {
        ConcurrentLinkedQueue<String> clq = new ConcurrentLinkedQueue<>();

        clq.add("Msg1");
        clq.add("Msg2");
        clq.add("Msg3");

        Iterator<String> iterator = clq.iterator();

        while (iterator.hasNext()) {
            assertEquals("Msg1", iterator.next());
            assertEquals("Msg2", iterator.next());
            assertEquals("Msg3", iterator.next());
        }
    }

    @Test
    public void test_forEach() {
        ConcurrentLinkedQueue<String> clq = new ConcurrentLinkedQueue<>();

        AtomicInteger ai = new AtomicInteger(1);
        clq.forEach((elem) -> {
            assertEquals("Msg" + ai.getAndIncrement(), elem);
        });
    }

    @Test
    public void test_retainAll() {
        ConcurrentLinkedQueue<String> clq = new ConcurrentLinkedQueue<>();
        clq.add("Msg1");
        clq.add("Msg2");
        clq.add("Msg3");

        clq.retainAll(List.of("Msg2", "Msg3"));

        assertEquals("Msg2", clq.poll());
        assertEquals("Msg3", clq.remove());
        assertEquals(null, clq.poll());
    }

    @Test
    public void test_splitaratorWithExecutor() throws InterruptedException {
        ConcurrentLinkedQueue<String> clq = new ConcurrentLinkedQueue<>();

        clq.add("Msg1");
        clq.add("Msg2");
        clq.add("Msg3");
        clq.add("Msg4");
        clq.add("Msg5");

        Spliterator<String> split1 = clq.spliterator();
        Spliterator<String> split2 = split1.trySplit();
        Spliterator<String> split3 = split1.trySplit();
        Spliterator<String> split4 = split1.trySplit();

        Executor executor = Executors.newFixedThreadPool(4);
        CountDownLatch cdl = new CountDownLatch(5);

        executor.execute(() -> {
            cdl.countDown();
            split2.forEachRemaining((elem -> assertEquals("Msg1", elem)));
        });

        executor.execute(() -> {
            split3.forEachRemaining((elem -> {
                cdl.countDown();
                assertEquals("Msg2".equals(elem) ? "Msg2" : "Msg3", elem);
            }));
        });

        executor.execute(() -> {
            split4.forEachRemaining((elem -> {
                cdl.countDown();
                assertEquals( "Msg4", elem);
            }));
        });

        executor.execute(() -> {
            cdl.countDown();
            split1.forEachRemaining((elem -> assertEquals("Msg5", elem)));
        });

        cdl.await();

    }

    @Test
    public void test_splitaratorWithStream() throws InterruptedException {
        ConcurrentLinkedQueue<String> clq = new ConcurrentLinkedQueue<>();

        clq.add("Msg1");
        clq.add("Msg2");
        clq.add("Msg3");
        clq.add("Msg4");
        clq.add("Msg5");

        Stream<String> parallelStream = clq.parallelStream();

//        parallelStream.forEach(e -> System.out.println(e + " " + Thread.currentThread().getName()));

        clq.offer("Msg6");
        parallelStream.forEach(e -> System.out.println(e + " " + Thread.currentThread().getName()));
    }

    @Test
    public void test_ProducerConsumer() throws InterruptedException {
        ConcurrentLinkedQueue<String> clq = new ConcurrentLinkedQueue<>();
        CountDownLatch cdl = new CountDownLatch(20);
        new Thread(() -> {
            for(int i =0; i<10; i++)
            {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                clq.offer("Msg"+i);
                cdl.countDown();
            }
        }).start();

        new Thread(() -> {
            int ind = 0;
            while(true)
            {
                String msg = null;
                msg = clq.poll();
                if(msg != null)
                {
                    System.out.println("Received message: " + msg);
                    ind++;
                    cdl.countDown();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        cdl.await();
    }
}
