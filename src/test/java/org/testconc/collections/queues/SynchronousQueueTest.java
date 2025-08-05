package org.testconc.collections.queues;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class SynchronousQueueTest {

    @Test
    public void test_putThenTake() throws InterruptedException {
        SynchronousQueue<String> sq = new SynchronousQueue<>(true);

        CountDownLatch cdl = new CountDownLatch(2);
        new Thread(() -> {
            try {
                sq.put("Msg1");
                System.out.println("Thread put has put the message");
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(500);
                String msg = sq.take();
                assertEquals("Msg1", msg);
                System.out.println("Thread take is done");
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        cdl.await();
    }

    @Test
    public void test_TakeThenPut() throws InterruptedException {
        SynchronousQueue<String> sq = new SynchronousQueue<>(true);

        CountDownLatch cdl = new CountDownLatch(2);
        new Thread(() -> {
            try {
                Thread.sleep(500);
                sq.put("Msg1");
                System.out.println("Thread put has put the message");
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> {
            try {
                String msg = sq.take();
                assertEquals("Msg1", msg);
                System.out.println("Thread take is done");
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        cdl.await();
    }

    @Test
    public void test_offerThenPoll() throws InterruptedException {
        SynchronousQueue<String> sq = new SynchronousQueue<>(true);

        CountDownLatch cdl = new CountDownLatch(2);
        new Thread(() -> {
            assertFalse(sq.offer("Msg1"));
            System.out.println("Thread offer has failed to offer the message");
            cdl.countDown();
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(500);
                String msg = sq.poll();
                assertEquals(null, msg);
                System.out.println("Thread poll is too late to poll the message");
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        cdl.await();
    }

    @Test
    public void test_putThenPoll() throws InterruptedException {
        SynchronousQueue<String> sq = new SynchronousQueue<>(true);

        CountDownLatch cdl = new CountDownLatch(2);
        new Thread(() -> {
            try {
                sq.put("Msg1");
                System.out.println("Thread put has put the message");
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(500);
                String msg = sq.poll();
                assertEquals("Msg1", msg);
                System.out.println("Thread poll is done");
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        cdl.await();
    }

    @Test
    public void test_takeThenOffer() throws InterruptedException {
        SynchronousQueue<String> sq = new SynchronousQueue<>(true);

        CountDownLatch cdl = new CountDownLatch(2);
        new Thread(() -> {
            try {
                Thread.sleep(500);
                assertTrue(sq.offer("Msg1"));
                System.out.println("Thread put has put the message");
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> {
            try {
                String msg = sq.take();
                assertEquals("Msg1", msg);
                System.out.println("Thread poll is done");
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        cdl.await();
    }

    @Test
    public void test_3PutThen3take_faired() throws InterruptedException {
        SynchronousQueue<String> sq = new SynchronousQueue<>(true);

        CountDownLatch cdl = new CountDownLatch(4);
        for (int i=0; i<3; i++)
        {
            Integer ai = Integer.valueOf(i);
            new Thread(() -> {
                try {
                    Thread.sleep(500 + ai*100);
                    sq.put("Msg" + ai);
                    System.out.println("Thread put has put the message == Msg" + ai);
                    cdl.countDown();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }


        new Thread(() -> {
            try {
                Thread.sleep(2000);
                String msg0 = sq.take();
                assertEquals("Msg0", msg0);
                String msg1 = sq.take();
                assertEquals("Msg1", msg1);
                String msg2 = sq.take();
                assertEquals("Msg2", msg2);
                System.out.println("Thread take is done");
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        cdl.await();
    }

    @Test
    public void test_3PutThen3take_unfaired() throws InterruptedException {
        SynchronousQueue<String> sq = new SynchronousQueue<>(false);

        CountDownLatch cdl = new CountDownLatch(4);
        for (int i=0; i<3; i++)
        {
            Integer ai = Integer.valueOf(i);
            new Thread(() -> {
                try {
                    Thread.sleep(500 + ai*100);
                    sq.put("Msg" + ai);
                    System.out.println("Thread put has put the message == Msg" + ai);
                    cdl.countDown();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }


        new Thread(() -> {
            try {
                Thread.sleep(2000);
                String msg0 = sq.take();
                assertEquals("Msg2", msg0);
                String msg1 = sq.take();
                assertEquals("Msg1", msg1);
                String msg2 = sq.take();
                assertEquals("Msg0", msg2);
                System.out.println("Thread take is done");
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        cdl.await();
    }

    @Test
    public void test_otherMethods()
    {
        SynchronousQueue<String> sq = new SynchronousQueue<>();

        assertTrue(sq.containsAll(Collections.EMPTY_LIST));
        assertFalse(sq.contains(null));
        assertTrue(sq.isEmpty());
        assertFalse(sq.iterator().hasNext());
        assertEquals(0, sq.remainingCapacity());
        assertEquals(0, sq.size());
        assertEquals(0, sq.spliterator().estimateSize());
        assertFalse(sq.remove(null));
        assertEquals("[]", sq.toString());
        assertEquals(0, sq.toArray().length);
    }

    @Test
    public void test_drainTo_faired() throws InterruptedException {
        SynchronousQueue<String> sq = new SynchronousQueue<>(true);

        CountDownLatch cdl = new CountDownLatch(4);
        for (int i=0; i<3; i++)
        {
            Integer ai = Integer.valueOf(i);
            new Thread(() -> {
                try {
                    Thread.sleep(500 + ai*100);
                    sq.put("Msg" + ai);
                    System.out.println("Thread put has put the message == Msg" + ai);
                    cdl.countDown();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }


        new Thread(() -> {
            try {
                Thread.sleep(2000);
                ArrayList<String> coll = new ArrayList<>();
                int i = sq.drainTo(coll);
                assertEquals(3, i);
                assertEquals("Msg0", coll.get(0));
                assertEquals("Msg1", coll.get(1));
                assertEquals("Msg2", coll.get(2));
                System.out.println("Thread take is done");
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        cdl.await();
    }

    @Test
    public void test_drainTo_unfaired() throws InterruptedException {
        SynchronousQueue<String> sq = new SynchronousQueue<>(false);

        CountDownLatch cdl = new CountDownLatch(4);
        for (int i=0; i<3; i++)
        {
            Integer ai = Integer.valueOf(i);
            new Thread(() -> {
                try {
                    Thread.sleep(500 + ai*100);
                    sq.put("Msg" + ai);
                    System.out.println("Thread put has put the message == Msg" + ai);
                    cdl.countDown();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }


        new Thread(() -> {
            try {
                Thread.sleep(2000);
                ArrayList<String> coll = new ArrayList<>();
                int i = sq.drainTo(coll);
                assertEquals(3, i);
                assertEquals("Msg2", coll.get(0));
                assertEquals("Msg1", coll.get(1));
                assertEquals("Msg0", coll.get(2));
                System.out.println("Thread take is done");
                cdl.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        cdl.await();
    }
}
