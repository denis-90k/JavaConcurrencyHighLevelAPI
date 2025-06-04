package org.testconc.service.executors.scheduledexecutorservice;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class ScheduledThreadPoolExecutorTest {

    @Test
    public void testSTPExecutor_scheduleRunnable() throws InterruptedException, ExecutionException {
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(5);

        CountDownLatch cdl = new CountDownLatch(1);
        ScheduledFuture<?> schedule = scheduledThreadPoolExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                cdl.countDown();
            }
        }, 3, TimeUnit.SECONDS);

        assertTrue(cdl.await(4, TimeUnit.SECONDS));
        assertEquals(null, schedule.get());
    }

    @Test
    public void testSTPExecutor_scheduleCallable() throws InterruptedException, ExecutionException {
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(5);

        CountDownLatch cdl = new CountDownLatch(1);
        ScheduledFuture<?> schedule = scheduledThreadPoolExecutor.schedule(new Callable<String>() {
            @Override
            public String call() throws Exception {
                cdl.countDown();
                return "Hello";
            }
        }, 3, TimeUnit.SECONDS);

        assertTrue(cdl.await(4, TimeUnit.SECONDS));
        assertEquals("Hello", schedule.get());
    }

    @Test
    public void testSTPExecutor_scheduleAtFixedRate() throws InterruptedException, ExecutionException {
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(5);

        CountDownLatch cdl = new CountDownLatch(10);
        AtomicInteger a = new AtomicInteger();
        ScheduledFuture<?> schedule = scheduledThreadPoolExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                cdl.countDown();
                a.getAndIncrement();
            }
        }, 1000, 500, TimeUnit.MILLISECONDS);

        assertFalse(cdl.await(3, TimeUnit.SECONDS));
        scheduledThreadPoolExecutor.shutdown();
        int a1 = a.get();
        assertFalse(cdl.await(2, TimeUnit.SECONDS));
        assertEquals(a1, a.get());

    }

    @Test
    public void testSTPExecutor_scheduleAtFixedRate_ContinueTaskAfterShutdown() throws InterruptedException, ExecutionException {
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(5);
        scheduledThreadPoolExecutor.setContinueExistingPeriodicTasksAfterShutdownPolicy(true);

        CountDownLatch cdl = new CountDownLatch(10);
        AtomicInteger a = new AtomicInteger();
        ScheduledFuture<?> schedule = scheduledThreadPoolExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                cdl.countDown();
                a.getAndIncrement();
            }
        }, 1000, 500, TimeUnit.MILLISECONDS);

        assertFalse(cdl.await(3, TimeUnit.SECONDS));
        scheduledThreadPoolExecutor.shutdown();
        int a1 = a.get();
        assertTrue(cdl.await(4, TimeUnit.SECONDS));
        assertNotEquals(a1, a.get());

    }

    @Test
    public void testSTPExecutor_scheduleAtFixedRate_ContinueTaskAfterShutdownNow() throws InterruptedException, ExecutionException {
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(5);
        scheduledThreadPoolExecutor.setContinueExistingPeriodicTasksAfterShutdownPolicy(true);

        CountDownLatch cdl = new CountDownLatch(10);
        ArrayList<Long> times = new ArrayList<>();
        long l = System.currentTimeMillis();
        AtomicInteger c = new AtomicInteger(0);
        ScheduledFuture<?> schedule = scheduledThreadPoolExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                times.add(System.currentTimeMillis() - l);
                cdl.countDown();
                try {
                    if(c.getAndIncrement() < 3) {
                        Thread.sleep(1000);
                    }

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 1000, 500, TimeUnit.MILLISECONDS);

        cdl.await();
        System.out.println(Arrays.toString(times.toArray()));
        assertTrue(times.get(7) > 4500);
    }

    @Test
    public void testSTPExecutor_scheduleWithFixedDelay_ContinueTaskAfterShutdownNow() throws InterruptedException, ExecutionException {
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(5);
        scheduledThreadPoolExecutor.setContinueExistingPeriodicTasksAfterShutdownPolicy(true);

        CountDownLatch cdl = new CountDownLatch(10);
        ArrayList<Long> times = new ArrayList<>();
        long l = System.currentTimeMillis();
        AtomicInteger c = new AtomicInteger(0);
        ScheduledFuture<?> schedule = scheduledThreadPoolExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                times.add(System.currentTimeMillis() - l);
                cdl.countDown();
                try {
                    if(c.getAndIncrement() < 3) {
                        Thread.sleep(1000);
                    }

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 1000, 500, TimeUnit.MILLISECONDS);

        cdl.await();
        System.out.println(Arrays.toString(times.toArray()));
        assertTrue(times.get(7) > 7500);
    }

}
