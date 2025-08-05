package org.testconc.collections.queues;

import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.*;

import static org.junit.Assert.*;

public class ConcurrentLinkedDequeTest {

    @Test
    public void test_OfferPollOperationsFIFO() {
        ConcurrentLinkedDeque<String> clq = new ConcurrentLinkedDeque<>();
        clq.offer("Msg1");
        clq.offer("Msg2");
        clq.offer("Msg3");

        assertEquals("Msg1", clq.poll());
        assertEquals("Msg2", clq.poll());
        assertEquals("Msg3", clq.poll());
    }

    @Test
    public void test_OfferLastPollFirstOperationsFIFO() {
        ConcurrentLinkedDeque<String> clq = new ConcurrentLinkedDeque<>();
        clq.offerLast("Msg1");
        clq.offerLast("Msg2");
        clq.offerLast("Msg3");

        assertEquals("Msg1", clq.pollFirst());
        assertEquals("Msg2", clq.pollFirst());
        assertEquals("Msg3", clq.pollFirst());
    }

    @Test
    public void test_OfferLastPollFirstOperationsLIFO() {
        ConcurrentLinkedDeque<String> clq = new ConcurrentLinkedDeque<>();
        clq.offerLast("Msg1");
        clq.offerLast("Msg2");
        clq.offerLast("Msg3");

        assertEquals("Msg3", clq.pollLast());
        assertEquals("Msg2", clq.pollLast());
        assertEquals("Msg1", clq.pollLast());
    }

    @Test
    public void test_Iterator() {
        ConcurrentLinkedDeque<String> clq = new ConcurrentLinkedDeque<>();
        clq.offerLast("Msg1");
        clq.offerLast("Msg2");
        clq.offerLast("Msg3");

        Iterator<String> iterator = clq.iterator();

        assertEquals("Msg1", iterator.next());
        assertEquals("Msg2", iterator.next());
        clq.offer("Msg4");
        assertEquals("Msg3", iterator.next());
        assertEquals("Msg4", iterator.next());
    }

    @Test
    public void test_IteratorDescending() {
        ConcurrentLinkedDeque<String> clq = new ConcurrentLinkedDeque<>();
        clq.offerLast("Msg1");
        clq.offerLast("Msg2");
        clq.offerLast("Msg3");

        Iterator<String> iterator = clq.descendingIterator();

        assertEquals("Msg3", iterator.next());
        assertEquals("Msg2", iterator.next());
        clq.offer("Msg4");
        assertEquals("Msg1", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void test_pushTake_LIFOStack() {
        ConcurrentLinkedDeque<String> clq = new ConcurrentLinkedDeque<>();
        clq.push("Msg1");
        clq.push("Msg2");
        clq.push("Msg3");

        assertEquals("Msg3", clq.pop());
        assertEquals("Msg2", clq.pop());
        assertEquals("Msg1", clq.pop());
    }

    @Test
    public void test_splitaratorWithExecutor() throws InterruptedException {
        ConcurrentLinkedDeque<String> clq = new ConcurrentLinkedDeque<>();

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
    public void test_toArray()
    {
        ConcurrentLinkedDeque<String> clq = new ConcurrentLinkedDeque<>();

        clq.add("Msg1");
        clq.add("Msg2");
        clq.add("Msg3");
        clq.add("Msg4");
        clq.add("Msg5");

        Object[] array = clq.toArray();
        String[] a1 = new String[5];
        String[] a2 = clq.toArray(a1);

        assertTrue(Arrays.equals(a1, array));
        assertTrue(Arrays.equals(a1, a2));
        assertTrue(a1 == a2);

        String[] a3 = new String[2];
        String[] a4 = clq.toArray(a3);
        assertTrue(a4 != a3);
        assertTrue(a3[0] == "Msg1");
        assertTrue(a3[1] == "Msg2");
        System.out.println(Arrays.toString(a4));
    }
}


