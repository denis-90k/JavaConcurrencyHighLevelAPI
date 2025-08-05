package org.testconc.collections.queues;

import org.junit.Assert;
import org.junit.Test;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PriorityBlockingQueueTest {

    @Test
    public void test_offerPoll_Comparable() {
        PriorityBlockingQueue<String> pbq = new PriorityBlockingQueue<>();

        pbq.offer("Msg0");
        pbq.offer("Msg1");
        pbq.offer("Msg2");
        pbq.offer("Msg3");

        assertEquals("Msg0", pbq.poll());
        assertEquals("Msg1", pbq.poll());
        assertEquals("Msg2", pbq.poll());
        assertEquals("Msg3", pbq.poll());
    }

    @Test
    public void test_offerRandomlyAndPollInOrder_Comparable() {
        PriorityBlockingQueue<String> pbq = new PriorityBlockingQueue<>();

        pbq.offer("Msg2");
        pbq.offer("Msg0");
        pbq.offer("Msg3");
        pbq.offer("Msg1");

        assertEquals("Msg0", pbq.poll());
        assertEquals("Msg1", pbq.poll());
        assertEquals("Msg2", pbq.poll());
        assertEquals("Msg3", pbq.poll());
    }

    @Test
    public void test_iteratorNotInPrioty_but_inOrderOfInternalArray() {
        PriorityBlockingQueue<String> pbq = new PriorityBlockingQueue<>();

        pbq.offer("Msg2");
        pbq.offer("Msg0");
        pbq.offer("Msg3");
        pbq.offer("Msg1");

        Iterator<String> iterator = pbq.iterator();

        Object[] arrayMinHeapFromPbq = pbq.toArray();

        assertEquals(arrayMinHeapFromPbq[0], iterator.next());
        assertEquals(arrayMinHeapFromPbq[1], iterator.next());
        assertEquals(arrayMinHeapFromPbq[2], iterator.next());
        assertEquals(arrayMinHeapFromPbq[3], iterator.next());

        assertEquals("Msg0", arrayMinHeapFromPbq[0]);
        assertEquals("Msg1", arrayMinHeapFromPbq[2*0+1]);
        assertEquals("Msg3", arrayMinHeapFromPbq[2*0+2]);
        assertEquals("Msg2", arrayMinHeapFromPbq[2*1+1]);
    }

    @Test(expected = IllegalStateException.class)
    public void test_iterator_removeWithExcep_ifNoLastReturnedElem() {
        PriorityBlockingQueue<String> pbq = new PriorityBlockingQueue<>();

        pbq.offer("Msg2");
        pbq.offer("Msg0");
        pbq.offer("Msg3");
        pbq.offer("Msg1");

        Iterator<String> iterator = pbq.iterator();

        iterator.remove();
    }

    @Test
    public void test_forEachRemaining() {
        PriorityBlockingQueue<String> pbq = new PriorityBlockingQueue<>();

        pbq.offer("Msg2");
        pbq.offer("Msg0");
        pbq.offer("Msg3");
        pbq.offer("Msg1");

        AtomicInteger ai = new AtomicInteger(0);
        Object[] arrayElems = pbq.toArray();
        pbq.forEach((elem) -> assertEquals(arrayElems[ai.getAndIncrement()], elem));
    }

    @Test
    public void test_Comparator() {
        record Dummy(String firstName){};
        PriorityBlockingQueue<Dummy> pbq = new PriorityBlockingQueue<>(4, new Comparator<Dummy>(){
            @Override
            public int compare(Dummy o1, Dummy o2) {
                return o1.firstName.compareTo(o2.firstName);
            }
        });

        pbq.offer(new Dummy("Msg0"));
        pbq.offer(new Dummy("Msg2"));
        pbq.offer(new Dummy("Msg1"));
        pbq.offer(new Dummy("Msg3"));

        assertEquals(new Dummy("Msg0"), pbq.poll());
        assertEquals(new Dummy("Msg1"), pbq.poll());
        assertEquals(new Dummy("Msg2"), pbq.poll());
        assertEquals(new Dummy("Msg3"), pbq.poll());
    }

    @Test
    public void test_Splitarator() {
        record Dummy(String firstName){};
        PriorityBlockingQueue<Dummy> pbq = new PriorityBlockingQueue<>(4, new Comparator<Dummy>(){
            @Override
            public int compare(Dummy o1, Dummy o2) {
                return o1.firstName.compareTo(o2.firstName);
            }
        });

        pbq.offer(new Dummy("Msg0"));
        pbq.offer(new Dummy("Msg2"));
        pbq.offer(new Dummy("Msg1"));
        pbq.offer(new Dummy("Msg3"));

        Spliterator<Dummy> spliterator1 = pbq.spliterator();
        Spliterator<Dummy> splitarator2 = spliterator1.trySplit();

        spliterator1.forEachRemaining((elem) -> System.out.println("Spliterator1 => " + elem));
        splitarator2.forEachRemaining((elem) -> System.out.println("Spliterator2 => " + elem));
    }

    @Test
    public void test_putTake() throws InterruptedException {
        record Dummy(String firstName) {
        }
        PriorityBlockingQueue<Dummy> pbq = new PriorityBlockingQueue<>(4, new Comparator<Dummy>() {
            @Override
            public int compare(Dummy o1, Dummy o2) {
                return o1.firstName.compareTo(o2.firstName);
            }
        });

        CountDownLatch cdl = new CountDownLatch(2);
        new Thread(()->{
            try {
                Dummy takeResult = pbq.take();
                assertEquals(new Dummy("Msg0"), takeResult);
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
        Thread.sleep(500);
        pbq.put(new Dummy("Msg0"));

        Thread.sleep(1000);

        pbq.put(new Dummy("Msg2"));
        pbq.put(new Dummy("Msg1"));
        pbq.put(new Dummy("Msg3"));
        pbq.put(new Dummy("Msg4"));

        new Thread(()->{
            pbq.put(new Dummy("Msg5"));
            assertTrue(true);
            cdl.countDown();
        }).start();

        Thread.sleep(500);
        Dummy takeRes = pbq.take();
        assertEquals(new Dummy("Msg1"), takeRes);

        cdl.await();
        assertEquals(new Dummy("Msg2"), pbq.take());
        assertEquals(new Dummy("Msg3"), pbq.take());
        assertEquals(new Dummy("Msg4"), pbq.take());
        assertEquals(new Dummy("Msg5"), pbq.take());
    }
}
