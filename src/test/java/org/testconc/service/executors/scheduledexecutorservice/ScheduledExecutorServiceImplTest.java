package org.testconc.service.executors.scheduledexecutorservice;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ScheduledExecutorServiceImplTest {

    @Test
    public void testExecute_oneTimeTaskWithoutDelay() throws InterruptedException {
        ScheduledExecutorServiceImpl scheduledExecutorService = new ScheduledExecutorServiceImpl();
        List<String> workerName = new ArrayList<>();
        scheduledExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                workerName.add(Thread.currentThread().getName());
            }
        });
        Thread.sleep(3000);
        assertEquals("ScheduledExecutorServiceImpl-RunnableThread-0", workerName.get(0));
        assertEquals("main", Thread.currentThread().getName());

        assertEquals(0, scheduledExecutorService.numberOfTasks.get());
    }

    @Test(expected = NullPointerException.class)
    public void testExecute_NullCommandException() throws InterruptedException {
        ScheduledExecutorServiceImpl scheduledExecutorService = new ScheduledExecutorServiceImpl();

        scheduledExecutorService.execute(null);

    }

    @Test
    public void testExecute_10oneTimeTasksWithoutDelay() throws InterruptedException {
        ScheduledExecutorServiceImpl scheduledExecutorService = new ScheduledExecutorServiceImpl();
        List<String> workerName = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            scheduledExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if (!workerName.contains(Thread.currentThread().getName()))
                        workerName.add(Thread.currentThread().getName());
                }
            });
        }

        Thread.sleep(5000);
        assertEquals(5, workerName.size());
        for (int i = 0; i < 5; i++) {
            assertTrue(workerName.contains("ScheduledExecutorServiceImpl-RunnableThread-" + i));
        }

        assertEquals("main", Thread.currentThread().getName());

        assertEquals(0, scheduledExecutorService.numberOfTasks.get());
        assertEquals(0, scheduledExecutorService.tasks.size());
        //assertEquals(0, scheduledExecutorService.runnableWorkers.size());
    }

    @Test
    public void testSubmitRunnable_oneTimeTaskWithoutDelay() throws InterruptedException, ExecutionException {
        ScheduledExecutorServiceImpl scheduledExecutorService = new ScheduledExecutorServiceImpl();
        List<String> workerName = new ArrayList<>();
        Future<Void> submit = scheduledExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                workerName.add(Thread.currentThread().getName());
            }
        });


        Thread.sleep(3000);
        assertEquals(null, submit.get());
        assertEquals("ScheduledExecutorServiceImpl-RunnableThread-0", workerName.get(0));
        assertEquals("main", Thread.currentThread().getName());
        assertEquals(0, scheduledExecutorService.numberOfTasks.get());
    }

    @Test(expected = NullPointerException.class)
    public void testSubmit_NullCommandException() throws InterruptedException {
        ScheduledExecutorServiceImpl scheduledExecutorService = new ScheduledExecutorServiceImpl();
        scheduledExecutorService.submit(null, null);
    }

    @Test
    public void testSubmitRunnable_oneTimeTaskWithoutDelay_customResult() throws InterruptedException, ExecutionException {
        ScheduledExecutorServiceImpl scheduledExecutorService = new ScheduledExecutorServiceImpl();
        List<String> workerName = new ArrayList<>();
        String customResult = "Custom Result";
        Future<?> submit = scheduledExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                workerName.add(Thread.currentThread().getName());
            }
        }, customResult);

        assertEquals(customResult, submit.get());
        assertEquals("ScheduledExecutorServiceImpl-RunnableThread-0", workerName.get(0));
        assertEquals("main", Thread.currentThread().getName());
        assertEquals(0, scheduledExecutorService.numberOfTasks.get());
    }

    @Test
    public void testSubmitCallable_oneTimeTaskWithoutDelay() throws InterruptedException, ExecutionException {
        ScheduledExecutorServiceImpl scheduledExecutorService = new ScheduledExecutorServiceImpl();
        List<String> workerName = new ArrayList<>();
        String callableResult = "Callable Result";
        Future<Object> submit = scheduledExecutorService.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                workerName.add(Thread.currentThread().getName());
                return callableResult;
            }
        });

        Thread.sleep(1000);
        assertEquals(callableResult, submit.get());
        assertEquals("ScheduledExecutorServiceImpl-RunnableThread-0", workerName.get(0));
        assertEquals("main", Thread.currentThread().getName());
        assertEquals(0, scheduledExecutorService.numberOfTasks.get());
    }

    @Test
    public void testSubmitCallable_CancelTask() throws InterruptedException {
        ScheduledExecutorServiceImpl scheduledExecutorService = new ScheduledExecutorServiceImpl();

        for (int i = 0; i < 5; i++) {
            scheduledExecutorService.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    Thread.sleep(1000);
                    return null;
                }
            });
        }

        List<String> lastSubmitWorkerName = new ArrayList<>();
        Future<Object> lastSubmit = scheduledExecutorService.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                lastSubmitWorkerName.add(Thread.currentThread().getName());
                return null;
            }
        });

        lastSubmit.cancel(true);

        Thread.sleep(5000);
        assertEquals(0, lastSubmitWorkerName.size());
        assertEquals(0, scheduledExecutorService.numberOfTasks.get());
    }

    @Test(expected = ExecutionException.class)
    public void testSubmitCallable_ThrowRandomException() throws InterruptedException, ExecutionException {
        ScheduledExecutorServiceImpl scheduledExecutorService = new ScheduledExecutorServiceImpl();

        Future<Object> lastSubmit = scheduledExecutorService.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                throw new IllegalStateException("Some Exception");
                //return null;
            }
        });

        Thread.sleep(3000);
        assertEquals(0, scheduledExecutorService.numberOfTasks.get());
        //assertEquals(0, scheduledExecutorService.runnableWorkers.size());

        lastSubmit.get();
    }

    @Test(expected = InterruptedException.class)
    public void testSubmitCallable_throwInterruptedException() throws Throwable {
        ScheduledExecutorServiceImpl scheduledExecutorService = new ScheduledExecutorServiceImpl();
        ReentrantLock lock = new ReentrantLock();
        Condition condition = lock.newCondition();

        Future<Object> lastSubmit = scheduledExecutorService.submit(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                Thread.currentThread().interrupt();
                lock.lock();
                try {
                    condition.await(10, TimeUnit.MILLISECONDS);
                } finally {
                    lock.unlock();
                }
                return null;
            }
        });

        Thread.sleep(3000);
        assertEquals(0, scheduledExecutorService.numberOfTasks.get());
        //assertEquals(0, scheduledExecutorService.runnableWorkers.size());

        try {
            lastSubmit.get();
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test
    public void testInvokeAllCallable_allFutureCompleted() throws Throwable {
        ScheduledExecutorServiceImpl scheduledExecutorService = new ScheduledExecutorServiceImpl();

        AtomicInteger count = new AtomicInteger();
        List<Callable<String>> c = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            c.add(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    count.getAndIncrement();
                    return "Some Result";
                }
            });
        }
        List<Future<String>> futures = scheduledExecutorService.invokeAll(c);
        for (Future<String> f : futures) {
            assertEquals("Some Result", f.get());
        }
        assertEquals(100, count.get());

        Thread.sleep(3000);
        assertEquals(0, scheduledExecutorService.numberOfTasks.get());
        //assertEquals(0, scheduledExecutorService.runnableWorkers.size());
    }

    @Test
    public void testInvokeAllWithTimeLimitCallable_NOTallFutureCompleted() throws Throwable {
        ScheduledExecutorServiceImpl scheduledExecutorService = new ScheduledExecutorServiceImpl();

        AtomicInteger count = new AtomicInteger();
        List<Callable<String>> c = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            c.add(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    count.getAndIncrement();
                    Thread.sleep(100);
                    return "Some Result";
                }
            });
        }
        List<Future<String>> futures = scheduledExecutorService.invokeAll(c, 1000, TimeUnit.MILLISECONDS);
        Thread.sleep(3000);
        int completedTasks = 0;
        for (Future<String> f : futures) {
            try {
                assertEquals("Some Result", f.get());
                completedTasks++;
            } catch (CancellationException ex) {
                assertTrue(true);
            }
        }

        assertEquals(0, scheduledExecutorService.numberOfTasks.get());
        assertEquals(completedTasks, count.get());
    }

    @Test
    public void testInvokeAllCallable_allFutureCompletedNormalBesideOne() throws Throwable {
        ScheduledExecutorServiceImpl scheduledExecutorService = new ScheduledExecutorServiceImpl();

        AtomicInteger count = new AtomicInteger();
        List<Callable<String>> c = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            c.add(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    if (count.getAndIncrement() == 0) {
                        throw new IllegalArgumentException("Some error");
                    }
                    return "Some Result";
                }
            });
        }
        List<Future<String>> futures = scheduledExecutorService.invokeAll(c);
        Thread.sleep(3000);
        int numberOfErrors = 0;
        for (Future<String> f : futures) {
            try {
                assertEquals("Some Result", f.get());
            } catch (Exception ex) {
                numberOfErrors++;
                assertEquals(IllegalArgumentException.class, ex.getCause().getClass());
            }

        }
        assertEquals(100, count.get());
        assertEquals(1, numberOfErrors);

        assertEquals(0, scheduledExecutorService.numberOfTasks.get());
        //assertEquals(0, scheduledExecutorService.runnableWorkers.size());
    }

    @Test
    public void testInvokeAny_oneOrMoreFuturesCompleted() throws Throwable {
        ScheduledExecutorServiceImpl scheduledExecutorService = new ScheduledExecutorServiceImpl();

        AtomicInteger count = new AtomicInteger();
        List<Callable<String>> c = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            c.add(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    count.getAndIncrement();
                    Thread.sleep(100);
                    return "Some Result";
                }
            });
        }

        String result = scheduledExecutorService.invokeAny(c);

        Thread.sleep(3000);
        assertEquals("Some Result", result);

        assertEquals(0, scheduledExecutorService.numberOfTasks.get());
    }

    @Test(expected = ExecutionException.class)
    public void testInvokeAny_10FuturesExceptioned_FinallyThrowExcNoTasksCompletedSuccessfully() throws Throwable {
        ScheduledExecutorServiceImpl scheduledExecutorService = new ScheduledExecutorServiceImpl();

        AtomicInteger count = new AtomicInteger();
        List<Callable<String>> c = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            c.add(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    throw new IllegalArgumentException("Some error");
//                    return "Some Result";
                }
            });
        }

        scheduledExecutorService.invokeAny(c);

    }

    @Test
    public void testInvokeAnyNoHangingTasksLeft_10FuturesExceptioned_FinallyThrowExc() throws Throwable {
        ScheduledExecutorServiceImpl scheduledExecutorService = new ScheduledExecutorServiceImpl();

        AtomicInteger count = new AtomicInteger();
        List<Callable<String>> c = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            c.add(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    throw new IllegalArgumentException("Some error");
                }
            });
        }

        String result = "";
        try {
            result = scheduledExecutorService.invokeAny(c);
        } catch (ExecutionException ex) {

        }

        Thread.sleep(3000);
        assertEquals("", result);

        assertEquals(0, scheduledExecutorService.numberOfTasks.get());

    }

    @Test
    public void testInvokeAnyWithTimeout_oneOrMoreFuturesCompleted() throws Throwable {
        ScheduledExecutorServiceImpl scheduledExecutorService = new ScheduledExecutorServiceImpl();

        AtomicInteger count = new AtomicInteger();
        List<Callable<String>> c = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            c.add(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    count.getAndIncrement();
                    Thread.sleep(100);
                    return "Some Result";
                }
            });
        }

        String result = scheduledExecutorService.invokeAny(c, 1000, TimeUnit.MILLISECONDS);

        Thread.sleep(3000);
        assertEquals("Some Result", result);

        assertEquals(0, scheduledExecutorService.numberOfTasks.get());
    }

    @Test(expected = TimeoutException.class)
    public void testInvokeAnyWithTimeout_NoTasksCompletedWithinTimeout_ThownException() throws Throwable {
        ScheduledExecutorServiceImpl scheduledExecutorService = new ScheduledExecutorServiceImpl();

        AtomicInteger count = new AtomicInteger();
        List<Callable<String>> c = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            c.add(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    count.getAndIncrement();
                    Thread.sleep(100);
                    return "Some Result";
                }
            });
        }

        scheduledExecutorService.invokeAny(c, 1, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testInvokeAnyWithTimeout_NoTasksCompletedWithinTimeout_NoLeftTasks() throws Throwable {
        ScheduledExecutorServiceImpl scheduledExecutorService = new ScheduledExecutorServiceImpl();

        AtomicInteger count = new AtomicInteger();
        List<Callable<String>> c = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            c.add(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    count.getAndIncrement();
                    Thread.sleep(100);
                    return "Some Result";
                }
            });
        }

        String result = "";
        try {
            result = scheduledExecutorService.invokeAny(c, 1, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {

        }

        Thread.sleep(3000);

        assertEquals(0, scheduledExecutorService.numberOfTasks.get());
    }

    @Test
    public void testOneTimeTask_ScheduleCallable_WithinInitialDelay() throws Throwable {
        ScheduledExecutorServiceImpl scheduledExecutorService = new ScheduledExecutorServiceImpl();

        ScheduledFuture<String> schedule = scheduledExecutorService.schedule(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "Some Result in 10 Seconds";
            }
        }, 10, TimeUnit.SECONDS);

        assertEquals(false, schedule.isDone());

        while (!schedule.isDone()) {
            Thread.sleep(1000);
        }

        assertEquals("Some Result in 10 Seconds", schedule.get());
    }

    @Test
    public void testOneTimeTask_ScheduleRunnable_WithInitialDelay() throws Throwable {
        ScheduledExecutorServiceImpl scheduledExecutorService = new ScheduledExecutorServiceImpl();

        List<String> result = new ArrayList<>();
        ScheduledFuture<Void> schedule = scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                result.add("Some Result in 10 Seconds");
            }
        }, 10, TimeUnit.SECONDS);

        assertEquals(false, schedule.isDone());

        while (!schedule.isDone()) {
            Thread.sleep(1000);
        }

        assertEquals("Some Result in 10 Seconds", result.get(0));
    }

    @Test
    public void testRepeatingTask_ScheduleAtFixedRate_WithInitialDelayAndPeriod() throws Throwable {
        ScheduledExecutorServiceImpl scheduledExecutorService = new ScheduledExecutorServiceImpl();

        List<String> result = new ArrayList<>();
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                result.add("Some Result in 10 Seconds");
            }
        }, 3, 1, TimeUnit.SECONDS);

        Thread.sleep(10000);
        scheduledExecutorService.shutdown();
        scheduledExecutorService.awaitTermination(3000, TimeUnit.MILLISECONDS);

        assertTrue(result.size() >= 7);
        assertEquals(0, scheduledExecutorService.numberOfTasks.get());
        assertEquals(0, scheduledExecutorService.rTasks.size());
    }

    @Test
    public void testRepeatingTask_ScheduleAtFixedRate_ShutdownNow() throws Throwable {
        ScheduledExecutorServiceImpl scheduledExecutorService = new ScheduledExecutorServiceImpl();

        List<String> result = new ArrayList<>();
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                result.add("Some Result in 10 Seconds");
            }
        }, 3, 1, TimeUnit.SECONDS);

        Thread.sleep(10000);
        scheduledExecutorService.shutdownNow();
        Thread.sleep(1000);

        assertTrue(result.size() >= 7);
        assertEquals(0, scheduledExecutorService.numberOfTasks.get());
        assertEquals(0, scheduledExecutorService.rTasks.size());
    }

    @Test
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
        boolean b = scheduledExecutorService.awaitTermination(2000, TimeUnit.MILLISECONDS);

        assertTrue(result.size() >= 7);
        assertTrue(b);
        assertTrue(scheduledExecutorService.isTerminated());
        assertEquals(0, scheduledExecutorService.numberOfTasks.get());
        assertEquals(0, scheduledExecutorService.rTasks.size());
    }

    @Test
    public void testRepeatingTask_ScheduleWithFixedDelay_WithInitialDelayAndPeriod() throws Throwable {
        ScheduledExecutorServiceImpl scheduledExecutorService = new ScheduledExecutorServiceImpl();

        List<String> result = new ArrayList<>();
        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                result.add("Some Result in 10 Seconds");
            }
        }, 3, 1, TimeUnit.SECONDS);

        Thread.sleep(10000);

        assertTrue(result.size() >= 7);
    }

    @Test
    public void testMultipleRepeatingTasks_ScheduleWithFixedDelay() throws InterruptedException {
        ScheduledExecutorServiceImpl scheduledExecutorService = new ScheduledExecutorServiceImpl();

        Map<String, Integer> result = new HashMap<>();
        utility_testMultipleRepeatingTasks_ScheduleWithFixedDelay(scheduledExecutorService, result, 1);
        utility_testMultipleRepeatingTasks_ScheduleWithFixedDelay(scheduledExecutorService, result, 2);
        utility_testMultipleRepeatingTasks_ScheduleWithFixedDelay(scheduledExecutorService, result, 3);
        utility_testMultipleRepeatingTasks_ScheduleWithFixedDelay(scheduledExecutorService, result, 4);

        Thread.sleep(10000);

        assertTrue(result.get("Some Result of ScheduleWithFixedDelay Task 1") > 1);
        assertTrue(result.get("Some Result of ScheduleWithFixedDelay Task 2") > 1);
        assertTrue(result.get("Some Result of ScheduleWithFixedDelay Task 3") > 1);
        assertTrue(result.get("Some Result of ScheduleWithFixedDelay Task 4") > 1);

        scheduledExecutorService.shutdown();
        Thread.sleep(1000);

        assertEquals(0, scheduledExecutorService.numberOfTasks.get());

        /*System.out.println(result);
        System.out.flush();*/
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

    @Test
    public void testMultipleRepeatingTasks_ScheduleAtFixedRate() throws InterruptedException {
        ScheduledExecutorServiceImpl scheduledExecutorService = new ScheduledExecutorServiceImpl();

        Map<String, Integer> result = new HashMap<>();
        utility_testMultipleRepeatingTasks_ScheduleAtFixedRate(scheduledExecutorService, result, 1);
        utility_testMultipleRepeatingTasks_ScheduleAtFixedRate(scheduledExecutorService, result, 2);
        utility_testMultipleRepeatingTasks_ScheduleAtFixedRate(scheduledExecutorService, result, 3);
        utility_testMultipleRepeatingTasks_ScheduleAtFixedRate(scheduledExecutorService, result, 4);

        Thread.sleep(10000);

        assertTrue(result.get("Some Result of ScheduleAtFixedRate Task 1") > 1);
        assertTrue(result.get("Some Result of ScheduleAtFixedRate Task 2") > 1);
        assertTrue(result.get("Some Result of ScheduleAtFixedRate Task 3") > 1);
        assertTrue(result.get("Some Result of ScheduleAtFixedRate Task 4") > 1);

        scheduledExecutorService.shutdown();
        Thread.sleep(1000);

        assertEquals(0, scheduledExecutorService.numberOfTasks.get());

        System.out.println(result);
        System.out.flush();
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

    @Test
    public void testMultipleInvokeAllTasks() throws InterruptedException {
        ScheduledExecutorServiceImpl scheduledExecutorService = new ScheduledExecutorServiceImpl();

        Map<String, Integer> result = new HashMap<>();

        utility_testMultipleInvokeAllTasks(result, scheduledExecutorService, 1);
        utility_testMultipleInvokeAllTasks(result, scheduledExecutorService, 2);
        utility_testMultipleInvokeAllTasks(result, scheduledExecutorService, 3);
        utility_testMultipleInvokeAllTasks(result, scheduledExecutorService, 4);

        Thread.sleep(3000);
        while (scheduledExecutorService.numberOfTasks.get() > 0)
            Thread.sleep(1000);

        System.out.println(result);
        System.out.flush();

        assertEquals(10, (int) result.get("Some Result of invokeAll Task 1"));
        assertEquals(10, (int) result.get("Some Result of invokeAll Task 2"));
        assertEquals(10, (int) result.get("Some Result of invokeAll Task 3"));
        assertEquals(10, (int) result.get("Some Result of invokeAll Task 4"));

        assertEquals(0, scheduledExecutorService.numberOfTasks.get());
    }

    private static void utility_testMultipleInvokeAllTasks(Map<String, Integer> result, ScheduledExecutorServiceImpl scheduledExecutorService, int index) {
        ReentrantLock lock1 = new ReentrantLock(true);
        Collection<Callable<String>> c1 = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
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

    @Test
    public void testMultipleInvokeAnyTasks() throws InterruptedException {
        ScheduledExecutorServiceImpl scheduledExecutorService = new ScheduledExecutorServiceImpl();

        Map<String, Integer> result = new HashMap<>();

        utility_testMultipleInvokeAnyTasks(result, scheduledExecutorService, 1);
        utility_testMultipleInvokeAnyTasks(result, scheduledExecutorService, 2);
        utility_testMultipleInvokeAnyTasks(result, scheduledExecutorService, 3);
        utility_testMultipleInvokeAnyTasks(result, scheduledExecutorService, 4);

        Thread.sleep(3000);
        while (scheduledExecutorService.numberOfTasks.get() > 0)
            Thread.sleep(1000);

        System.out.println(result);
        System.out.flush();

        assertTrue((int) result.get("Some Result of InvokeAny Task 1") >= 1);
        assertTrue((int) result.get("Some Result of InvokeAny Task 2") >= 1);
        assertTrue((int) result.get("Some Result of InvokeAny Task 3") >= 1);
        assertTrue((int) result.get("Some Result of InvokeAny Task 4") >= 1);

        assertEquals(0, scheduledExecutorService.numberOfTasks.get());
    }

    private static void utility_testMultipleInvokeAnyTasks(Map<String, Integer> result, ScheduledExecutorServiceImpl scheduledExecutorService, int index) {
        ReentrantLock lock1 = new ReentrantLock(true);
        Collection<Callable<String>> c1 = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
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

    @Test
    public void testMultipleTypesOfTasks_awaitTermination() throws InterruptedException {
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

        Thread.sleep(10_000);

        scheduledExecutorService.awaitTermination(3000, TimeUnit.MILLISECONDS);

        result.forEach((key, value) -> System.out.println(key + "=" + value));
        System.out.flush();

        assertEquals(0, scheduledExecutorService.numberOfTasks.get());
        assertEquals(0, scheduledExecutorService.tasks.size());
        assertEquals(0, scheduledExecutorService.rTasks.size());
    }

    @Test
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
    }
}
