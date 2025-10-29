package org.testconc.thread;

import org.apache.commons.lang3.ClassLoaderUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class ThreadTest {

    @Test(expected = IllegalThreadStateException.class)
    public void test_newPlatformThreadWithEmptyConstr() throws InterruptedException {
        Thread thread = new Thread();

        assertEquals("Thread-0", thread.getName());
        assertFalse(thread.isVirtual());
        assertFalse(thread.isDaemon());
        assertFalse(thread.isAlive());
        assertEquals(Thread.State.NEW, thread.getState());

        thread.start();

        Thread.sleep(500);
        assertEquals(Thread.State.TERMINATED, thread.getState());

        thread.start();
    }

    @Test
    public void test_PlatformThread_CustomConstructorArgs() throws InterruptedException {
        InheritableThreadLocal<String> threadLocal = new InheritableThreadLocal<>();
        threadLocal.set("Hello from thread 1");
        AtomicInteger counter = new AtomicInteger(0);
        CountDownLatch cdl = new CountDownLatch(1);
        /*
         * For stackSize = 2, 344 last value before StackOverflowError. It is not permanent. No straight dependency. Look like JRE can decide on
         * */
        Thread thread = new Thread(new ThreadGroup("Custom thread group"), new Runnable() {
            @Override
            public void run() {
                System.out.println("Hello from another thread");
                System.out.println(Thread.currentThread().getThreadGroup().getName());
                assertEquals("Custom thread", Thread.currentThread().getName());
                assertEquals("Hello from thread 1", threadLocal.get());
                dummyFunc();
                System.out.println("Thread 2 finished");
                cdl.countDown();
            }

            private void dummyFunc() {
                if (counter.getAndIncrement() < 344) {
                    System.out.println(counter);
                    dummyFunc();
                }

            }
        }, "Custom thread", 256, true); //

        thread.start();

        cdl.await();
    }

    @Test
    public void test_Interrupt() throws InterruptedException {
        AtomicBoolean cond = new AtomicBoolean(false);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!cond.getAcquire()) {
                    Thread.onSpinWait();
                }
                System.out.println("Exit from busy waiting");
                Thread.interrupted();
            }
        });
        thread.start();

        assertEquals(Thread.State.RUNNABLE, thread.getState());

        thread.interrupt();
        assertTrue(thread.isInterrupted());
        assertEquals(Thread.State.RUNNABLE, thread.getState());
        cond.setRelease(true);

        Thread.sleep(100);
        assertFalse(thread.isInterrupted());
        assertEquals(Thread.State.TERMINATED, thread.getState());
    }

    @Test
    public void test_setAsDaemon() throws InterruptedException {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Running daemon thread");
                assertTrue(Thread.currentThread().isDaemon());

                Thread innerThread = new Thread(() -> {
                    System.out.println("Inner thread is daemon " + Thread.currentThread().isDaemon() + " despite I didn't set it as daemon explicitly");
                    assertTrue(Thread.currentThread().isDaemon());
                });
                innerThread.start();
            }
        });
        thread.setDaemon(true);
        thread.start();
        Thread.sleep(500);
    }

    @SuppressWarnings("removal")
    @Test(expected = UnsupportedOperationException.class)
    public void test_stop() {
        new Thread().stop();
    }

    @SuppressWarnings("removal")
    @Test(expected = UnsupportedOperationException.class)
    public void test_suspend() {
        new Thread().suspend();
    }

    @SuppressWarnings("removal")
    @Test(expected = UnsupportedOperationException.class)
    public void test_resume() {
        new Thread().resume();
    }

    @Test
    public void test_VirtualThread() throws InterruptedException {
        CountDownLatch cdl = new CountDownLatch(2);
        Thread thread1 = Thread.startVirtualThread(() -> {
            assertTrue(Thread.currentThread().isVirtual());
            assertTrue(Thread.currentThread().isDaemon());
            System.out.println("Running virtual thread 1");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("END Running virtual thread 1");
            cdl.countDown();
        });

        Thread thread2 = Thread.startVirtualThread(() -> {
            assertTrue(Thread.currentThread().isVirtual());
            assertTrue(Thread.currentThread().isDaemon());
            System.out.println("Running virtual thread 2");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("END Running virtual thread 2");
            cdl.countDown();
        });


        while (cdl.getCount() > 0) {
            Thread.onSpinWait();
            System.out.println("In spin");
            Thread.sleep(1000);
        }
        System.out.println("END Running main thread");
    }

    @Test
    public void test_UncaughtExceptionHandler() throws InterruptedException {
        Thread thr = new Thread(() -> {
            int t = 0;
            int k = 10 / t; // Didveded by zero
            System.out.println(k);
        });
        thr.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.out.println("Hello from uncaught exception!");
                System.out.println(e.getMessage());
            }
        });
        thr.start();

        Thread.sleep(1000);
    }

    @Test
    public void test_ClassLoader() throws ClassNotFoundException {
        Thread thr = new Thread();
//        Class<?> aClass = ClassLoader.getSystemClassLoader().loadClass("org.testconc.service.executors.forkjoin.countedcompleter.MyMapper");

        System.out.println(thr.getContextClassLoader());
    }

    @Test
    public void test_Priority() throws InterruptedException {
        LinkedBlockingQueue<String> highQ = new LinkedBlockingQueue<>();
        LinkedBlockingQueue<String> lowQ = new LinkedBlockingQueue<>();

        Thread high = new Thread(() -> {
            while (true) {
                try {
                    highQ.put("HIGH");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        high.setPriority(Thread.MAX_PRIORITY);

        Thread low = new Thread(() -> {
            while (true) {
                try {
                    lowQ.put("low");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        low.setPriority(Thread.MIN_PRIORITY);

        high.start();
        low.start();

        Thread.sleep(2000);

        System.out.println(highQ.size());
        System.out.println(lowQ.size());
    }

    @Test
    public void test_Builder() throws InterruptedException {
        Thread startedThread = Thread.ofPlatform().name("Sample Platform Thread").priority(10).start(() -> {
            System.out.println("Thread started");
        });

        Thread startVirtThread = Thread.ofVirtual().name("Sample Virtual Thread").start(() -> {
            System.out.println("Virtual thread started");
        });
        startVirtThread.threadId();
        Thread.sleep(200);
        System.out.println("End of main thread");
    }

    @Test
    public void test_() throws InterruptedException {
        AtomicBoolean flag = new AtomicBoolean(true);
        Runnable task = new Runnable() {
            @Override
            public void run() {
                while (flag.getAcquire())
                    Thread.onSpinWait();
                try {
                    Thread.sleep(1000);

                    synchronized (this) {
                        this.wait();
                    }

                    synchronized (this) {
                        System.out.println("Come in wait with timeout");
                        this.wait(1000);
                    }
                    Thread.sleep(500);
                    synchronized (this)
                    {
                        System.out.println("After unblocking");
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        Thread thr = new Thread(task);
        assertEquals(Thread.State.NEW, thr.getState());
        thr.start();
        Thread.sleep(100);
        assertEquals(Thread.State.RUNNABLE, thr.getState());
        flag.setRelease(false);
        Thread.sleep(100);
        assertEquals(Thread.State.TIMED_WAITING, thr.getState()); //sleep(1000)
        Thread.sleep(1100);
        assertEquals(Thread.State.WAITING, thr.getState());//this.wait()
        synchronized (task) {
            task.notify();
        }
        Thread.sleep(500);
        System.out.println("Expect Come in wait with timeout");
        assertEquals(Thread.State.TIMED_WAITING, thr.getState());
        Thread.sleep(1000);
        synchronized (task) {
            Thread.sleep(500);
            assertEquals(Thread.State.BLOCKED, thr.getState());
        }
        Thread.sleep(1000);
        assertEquals(Thread.State.TERMINATED, thr.getState());
    }
}
