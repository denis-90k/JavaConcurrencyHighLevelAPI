package org.testconc.collections.queues;

import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class DelayQueueTest {

    @Test
    public void test_expiring() throws InterruptedException {
        class Dummy implements Delayed
        {
            long delaySeconds;
            long initialTime = System.currentTimeMillis();

            Dummy(long delaySeconds)
            {
                this.delaySeconds = delaySeconds;
            }

            @Override
            public long getDelay(TimeUnit unit) {
                return TimeUnit.MILLISECONDS.toSeconds(initialTime) + delaySeconds - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
            }

            @Override
            public int compareTo(Delayed o) {
                return this.getDelay(null) >= o.getDelay(null) ? 1 : -1;
            }
        }
        DelayQueue<Dummy> dq = new DelayQueue<>();

        Dummy three = new Dummy(3);
        dq.add(three);
        Dummy two = new Dummy(2);
        dq.add(two);
        Dummy one = new Dummy(1);
        dq.add(one);

        assertSame(dq.take(), one);
        assertSame(dq.take(), two);
        assertSame(dq.take(), three);

    }

    @Test(expected = NoSuchElementException.class)
    public void test_iterator() throws InterruptedException {
        class Dummy implements Delayed
        {
            long delaySeconds;
            long initialTime = System.currentTimeMillis();

            Dummy(long delaySeconds)
            {
                this.delaySeconds = delaySeconds;
            }

            @Override
            public long getDelay(TimeUnit unit) {
                return TimeUnit.MILLISECONDS.toSeconds(initialTime) + delaySeconds - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
            }

            @Override
            public int compareTo(Delayed o) {
                return this.getDelay(null) >= o.getDelay(null) ? 1 : -1;
            }
        }
        DelayQueue<Dummy> dq = new DelayQueue<>();

        Dummy three = new Dummy(3);
        dq.add(three);
        Dummy two = new Dummy(2);
        dq.add(two);
        Dummy one = new Dummy(1);
        dq.add(one);

        Iterator<Dummy> iterator = dq.iterator();

        Dummy four = new Dummy(4);
        dq.add(four);// Despite the iterator is weakly-consistent, this element will not be visible for iterator because snapshot of the array is already taken

        // Despite the order is matched with add here, it is not guaranteed by iterator
        assertEquals(one, iterator.next());
        assertEquals(three, iterator.next());
        assertEquals(two, iterator.next());

        assertFalse(iterator.hasNext());
        iterator.next();
    }

    @Test
    public void test_spliterator() throws InterruptedException {
        class Dummy implements Delayed {
            long delaySeconds;
            long initialTime = System.currentTimeMillis();

            Dummy(long delaySeconds) {
                this.delaySeconds = delaySeconds;
            }

            @Override
            public long getDelay(TimeUnit unit) {
                return TimeUnit.MILLISECONDS.toSeconds(initialTime) + delaySeconds - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
            }

            @Override
            public int compareTo(Delayed o) {
                return this.getDelay(null) >= o.getDelay(null) ? 1 : -1;
            }
        }
        DelayQueue<Dummy> dq = new DelayQueue<>();

        Dummy three = new Dummy(30);
        dq.add(three);
        Dummy two = new Dummy(20);
        dq.add(two);
        Dummy one = new Dummy(10);
        dq.add(one);

        Spliterator<Dummy> spliterator1 = dq.spliterator();
        Spliterator<Dummy> spliterator2 = spliterator1.trySplit();

        assertEquals(0, spliterator1.estimateSize());
        assertEquals(3, spliterator2.estimateSize());

        assertEquals(3, dq.size());
        assertEquals(null, dq.poll());

        assertEquals(one, dq.peek());
    }
}
