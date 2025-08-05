package org.testconc.collections.queues;

import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class ArrayBlockingQueueTest {

    @Test
    public void test_offerThenPoll()
    {
        ArrayBlockingQueue<String> abq = new ArrayBlockingQueue<>(3);

        abq.offer("Msg0");
        abq.offer("Msg1");
        abq.offer("Msg2");

        assertEquals("Msg0", abq.poll());
        assertEquals("Msg1", abq.poll());
        assertEquals("Msg2", abq.poll());
    }

    @Test
    public void test_takeAndPollEmpty() throws InterruptedException {
        ArrayBlockingQueue<String> abq = new ArrayBlockingQueue<>(3);

        assertNull(abq.poll());

        CountDownLatch cdl = new CountDownLatch(1);
        new Thread(() -> {
            try {
                String take = abq.take();
                assertEquals("Msg0", take);
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        cdl.await(1, TimeUnit.SECONDS);
        abq.offer("Msg0");

        cdl.await();
    }

    @Test
    public void test_unfair_takeBehavior() throws InterruptedException {
        ArrayBlockingQueue<String> abq = new ArrayBlockingQueue<>(3);

        assertNull(abq.poll());

        CountDownLatch cdl = new CountDownLatch(3);
        new Thread(() -> {
            try {
                String take = abq.take();
                assertEquals("Msg0", take); //Not guaranteed by Queue according to doc of Queue.
                // According to doc of Condition used, the longest waiting thread will wake up, what guarantees
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> {
            try {
                String take = abq.take();
                assertEquals("Msg1", take);//Not guaranteed by Queue according to doc of Queue.
                // According to doc of Condition used, the longest waiting thread will wake up, what guarantees
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> {
            try {
                String take = abq.take();
                assertEquals("Msg2", take);//Not guaranteed by Queue according to doc of Queue.
                // According to doc of Condition used, the longest waiting thread will wake up, what guarantees
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        cdl.await(1, TimeUnit.SECONDS);
        abq.offer("Msg0");
        abq.offer("Msg1");
        abq.offer("Msg2");

        cdl.await();
    }

    @Test
    public void test_fair_takeBehavior() throws InterruptedException {
        ArrayBlockingQueue<String> abq = new ArrayBlockingQueue<>(3, true);

        assertNull(abq.poll());

        CountDownLatch cdl = new CountDownLatch(3);
        new Thread(() -> {
            try {
                String take = abq.take();
                assertEquals("Msg0", take);
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> {
            try {
                String take = abq.take();
                assertEquals("Msg1", take);
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> {
            try {
                String take = abq.take();
                assertEquals("Msg2", take);
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        cdl.await(1, TimeUnit.SECONDS);
        abq.offer("Msg0");
        abq.offer("Msg1");
        abq.offer("Msg2");

        cdl.await();
    }

    @Test
    public void test_fair_multipleIterators() throws InterruptedException {
        ArrayBlockingQueue<String> abq = new ArrayBlockingQueue<>(3, true);

        abq.offer("Msg0");
        abq.offer("Msg1");
        abq.offer("Msg2");

        Iterator<String> iterator1 = abq.iterator();
        Iterator<String> iterator2 = abq.iterator();
        Iterator<String> iterator3 = abq.iterator();

        assertTrue(iterator1.hasNext());
        assertTrue(iterator2.hasNext());
        assertTrue(iterator3.hasNext());

        assertEquals("Msg0", iterator1.next());
        assertEquals("Msg1", iterator1.next());
        assertEquals("Msg2", iterator1.next());

        assertEquals("Msg0", iterator2.next());
        assertEquals("Msg1", iterator2.next());
        assertEquals("Msg2", iterator2.next());

        assertEquals("Msg0", iterator3.next());
        assertEquals("Msg1", iterator3.next());
        assertEquals("Msg2", iterator3.next());

        abq.offer("Msg3");
        assertFalse(iterator1.hasNext());
        assertFalse(iterator2.hasNext());
        assertFalse(iterator3.hasNext());
    }

    @Test
    public void test_fair_takeWithIterator() throws InterruptedException {
        ArrayBlockingQueue<String> abq = new ArrayBlockingQueue<>(3, true);

        abq.offer("Msg0");
        abq.offer("Msg1");
        abq.offer("Msg2");

        Iterator<String> iterator1 = abq.iterator();

        assertEquals("Msg0", abq.take());
        assertEquals("Msg1", abq.take());

        assertEquals("Msg0", iterator1.next()); //!!!!!!!!!!
        assertEquals("Msg2", iterator1.next()); //!!!!!!!!!!
    }

    @Test(expected = IllegalStateException.class)
    public void test_fair_removeWithIterator_Except() throws InterruptedException {
        ArrayBlockingQueue<String> abq = new ArrayBlockingQueue<>(3, true);

        abq.offer("Msg0");
        abq.offer("Msg1");
        abq.offer("Msg2");

        Iterator<String> iterator1 = abq.iterator();

        iterator1.remove();
    }

    @Test
    public void test_fair_removeWithIterator() throws InterruptedException {
        ArrayBlockingQueue<String> abq = new ArrayBlockingQueue<>(3, true);

        abq.offer("Msg0");
        abq.offer("Msg1");
        abq.offer("Msg2");

        Iterator<String> iterator1 = abq.iterator();
        assertEquals("Msg0", iterator1.next());
        iterator1.remove();

        assertEquals("Msg1", abq.poll());
        assertEquals("Msg2", abq.poll());
    }

    @Test
    public void test_nextThenTakeThenRemoveThenPoll() throws InterruptedException {
        ArrayBlockingQueue<String> abq = new ArrayBlockingQueue<>(3, true);

        abq.offer("Msg0");
        abq.offer("Msg1");
        abq.offer("Msg2");

        Iterator<String> iterator1 = abq.iterator();
        assertEquals("Msg0", iterator1.next());
        assertEquals("Msg0", abq.take());
        iterator1.remove();
//        iterator1.remove(); will throw IllegalStateException

        assertEquals("Msg1", abq.poll());
        assertEquals("Msg2", abq.poll());
    }

    @Test
    public void test_spliterator() throws InterruptedException {
        ArrayBlockingQueue<String> abq = new ArrayBlockingQueue<>(3, true);

        abq.offer("Msg0");
        abq.offer("Msg1");
        abq.offer("Msg2");

        Spliterator<String> spliterator = abq.spliterator();
        assertEquals(3, spliterator.estimateSize());
        Spliterator<String> splitSpliterator = spliterator.trySplit();

        assertEquals(0, spliterator.estimateSize());
        assertEquals(3, splitSpliterator.estimateSize());
    }
}
