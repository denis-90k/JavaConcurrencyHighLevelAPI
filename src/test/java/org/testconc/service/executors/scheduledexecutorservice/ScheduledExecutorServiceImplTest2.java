package org.testconc.service.executors.scheduledexecutorservice;

import com.google.code.tempusfugit.concurrency.ConcurrentRule;
import com.google.code.tempusfugit.concurrency.IntermittentTestRunner;
import com.google.code.tempusfugit.concurrency.RepeatingRule;
import com.google.code.tempusfugit.concurrency.annotations.Intermittent;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(IntermittentTestRunner.class)
public class ScheduledExecutorServiceImplTest2 {

    ScheduledExecutorServiceImpl scheduledExecutorService = new ScheduledExecutorServiceImpl();
    CountDownLatch cdl1 = new CountDownLatch(500);
    CountDownLatch cdl2 = new CountDownLatch(10_000);
    CountDownLatch cdl3 = new CountDownLatch(100);
    @Rule
    public RepeatingRule rule = new RepeatingRule();
    @Rule
    public ConcurrentRule concurrently = new ConcurrentRule();

    /*@Test
    @Intermittent(repetition = 30)*/
    public void testRepeatingTask_ScheduleAtFixedRate_AwaitTermination() throws Throwable {
        ScheduledExecutorServiceImpl scheduledExecutorService = new ScheduledExecutorServiceImpl();

        List<String> result = new ArrayList<>();
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                result.add("Some Result in 10 Seconds");
            }
        }, 3, 1, TimeUnit.SECONDS);

        Thread.sleep(10000);
        boolean b = scheduledExecutorService.awaitTermination(1000, TimeUnit.MILLISECONDS);

        assertTrue(result.size() >= 7);
        assertTrue(b);
        assertTrue(scheduledExecutorService.isTerminated());
        assertEquals(0, scheduledExecutorService.numberOfTasks.get());
        assertEquals(0, scheduledExecutorService.rTasks.size());
    }

    @Test
    @Intermittent(repetition = 10)
    public void testMultipleTypesOfTasks_shutdownNow() throws InterruptedException {
        ScheduledExecutorServiceImpl scheduledExecutorService = new ScheduledExecutorServiceImpl();

        Map<String, Integer> result = new HashMap<>();

        utility_testMultipleInvokeAnyTasks(result, scheduledExecutorService, 1);
        utility_testMultipleInvokeAnyTasks(result, scheduledExecutorService, 2);
        utility_testMultipleInvokeAnyTasks(result, scheduledExecutorService, 3);

        utility_testMultipleInvokeAllTasks(result, scheduledExecutorService, 1);
        utility_testMultipleInvokeAllTasks(result, scheduledExecutorService, 2);
        utility_testMultipleInvokeAllTasks(result, scheduledExecutorService, 3);

        utility_testMultipleRepeatingTasks_ScheduleAtFixedRate(scheduledExecutorService, result, 1);
        utility_testMultipleRepeatingTasks_ScheduleAtFixedRate(scheduledExecutorService, result, 2);
        utility_testMultipleRepeatingTasks_ScheduleAtFixedRate(scheduledExecutorService, result, 3);

        utility_testMultipleRepeatingTasks_ScheduleWithFixedDelay(scheduledExecutorService, result, 1);
        utility_testMultipleRepeatingTasks_ScheduleWithFixedDelay(scheduledExecutorService, result, 2);
        utility_testMultipleRepeatingTasks_ScheduleWithFixedDelay(scheduledExecutorService, result, 3);

        Thread.sleep(5_000);

        scheduledExecutorService.shutdownNow();

        Thread.sleep(3000);
        /*result.forEach((key, value) -> System.out.println(key + "=" + value));
        System.out.flush();*/

        assertEquals(0, scheduledExecutorService.numberOfTasks.get());
        assertEquals(0, scheduledExecutorService.tasks.size());
        assertEquals(0, scheduledExecutorService.rTasks.size());
        assertTrue(scheduledExecutorService.completedTasksCount.get() >= 309);
    }

    private static void utility_testMultipleInvokeAnyTasks(Map<String, Integer> result, ScheduledExecutorServiceImpl scheduledExecutorService, int index) {
        ReentrantLock lock1 = new ReentrantLock(true);
        Collection<Callable<String>> c1 = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            c1.add(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    lock1.lock();
                    String key = "Some Result of InvokeAny Task " + index;
                    try {
                        result.put(key, result.getOrDefault(key, 0) + 1);
                        Thread.sleep(100);
                    } finally {
                        lock1.unlock();
                    }
                    return key;
                }
            });
        }
        new Thread() {
            @Override
            public void run() {
                try {
                    scheduledExecutorService.invokeAny(c1);
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }.start();
    }

    private static void utility_testMultipleInvokeAllTasks(Map<String, Integer> result, ScheduledExecutorServiceImpl scheduledExecutorService, int index) {
        ReentrantLock lock1 = new ReentrantLock(true);
        Collection<Callable<String>> c1 = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            c1.add(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    lock1.lock();
                    String key = "Some Result of invokeAll Task " + index;
                    result.put(key, result.getOrDefault(key, 0) + 1);
                    lock1.unlock();
                    return key;
                }
            });
        }
        new Thread() {
            @Override
            public void run() {
                try {
                    scheduledExecutorService.invokeAll(c1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }.start();
    }

    private static void utility_testMultipleRepeatingTasks_ScheduleAtFixedRate(ScheduledExecutorServiceImpl scheduledExecutorService, Map<String, Integer> result, int index) {
        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                String key = "Some Result of ScheduleAtFixedRate Task " + index;
                result.put(key, result.getOrDefault(key, 0) + 1);
            }
        }, 3, 1, TimeUnit.SECONDS);
    }

    private static void utility_testMultipleRepeatingTasks_ScheduleWithFixedDelay(ScheduledExecutorServiceImpl scheduledExecutorService, Map<String, Integer> result, int index) {
        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                String key = "Some Result of ScheduleWithFixedDelay Task " + index;
                result.put(key, result.getOrDefault(key, 0) + 1);
            }
        }, 3, 1, TimeUnit.SECONDS);
    }

    /*@Test
    @Concurrent(count = 100)
    @Repeating(repetition = 1)*/
    public void testSchedultAtFixedRate() throws InterruptedException {
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                cdl1.countDown();
            }
        }, 3, 1, TimeUnit.SECONDS);

        afterTestSchedultAtFixedRat(cdl1, 100);
    }


    public void afterTestSchedultAtFixedRat(CountDownLatch cdl, int numberOfTasks) throws InterruptedException {
        cdl.await();

        Thread.sleep(10000);

        boolean isTerminated = scheduledExecutorService.awaitTermination(3000, TimeUnit.MILLISECONDS);

        assertTrue(isTerminated);
        assertEquals(0, scheduledExecutorService.numberOfTasks.get());
        assertEquals(0, scheduledExecutorService.tasks.size());
        assertEquals(0, scheduledExecutorService.rTasks.size());
        assertEquals(numberOfTasks, scheduledExecutorService.completedTasksCount.get());

        /*System.out.println(isTerminated);
        System.out.flush();*/
    }

   /* @Test
    @Concurrent(count = 100)
    @Repeating(repetition = 1)*/
    public void testMultipleInvokeAllTasks() throws InterruptedException {
        Collection<Callable<String>> c1 = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            c1.add(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    String key = "Some Result of invokeAll Task ";
                    cdl2.countDown();
                    return key;
                }
            });
        }

        scheduledExecutorService.invokeAll(c1);

        afterTestSchedultAtFixedRat(cdl2, 100*100);
    }

   /* @Test
    @Concurrent(count = 100)
    @Repeating(repetition = 1)*/
    public void testMultipleInvokeAnyTasks() throws InterruptedException, ExecutionException {
        Collection<Callable<String>> c1 = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            c1.add(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    String key = "Some Result of invokeAll Task ";
                    cdl3.countDown();
                    return key;
                }
            });
        }

        scheduledExecutorService.invokeAny(c1);

        afterTestMultipleInvokeAnyTasks(cdl3, 100);
    }

    public void afterTestMultipleInvokeAnyTasks(CountDownLatch cdl, int numberOfTasks) throws InterruptedException {
        cdl.await();

        Thread.sleep(10000);

        boolean isTerminated = scheduledExecutorService.awaitTermination(3000, TimeUnit.MILLISECONDS);

        assertTrue(isTerminated);
        assertEquals(0, scheduledExecutorService.numberOfTasks.get());
        assertEquals(0, scheduledExecutorService.tasks.size());
        assertEquals(0, scheduledExecutorService.rTasks.size());
        assertTrue(scheduledExecutorService.completedTasksCount.get() >= numberOfTasks);

        /*System.out.println(isTerminated);
        System.out.flush();*/
    }
}
