package org.testconc.service.executors.scheduledexecutorservice;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class ScheduledExecutorServiceImplTest3 extends MultithreadedTestCase {

    private ScheduledExecutorServiceImpl scheduledExecutorService;

    @Test
    public void testThisClass() throws Throwable {
        TestFramework.setGlobalRunLimit(20);
        TestFramework.runOnce(new ScheduledExecutorServiceImplTest3());
    }

    @Override
    public void initialize() {
        System.out.println("Initialize");
        scheduledExecutorService = new ScheduledExecutorServiceImpl();
    }

    public void thread1() throws InterruptedException {
        System.out.println("thread1");
        Collection<Callable<String>> c1 = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            c1.add(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    String key = "Some Result of invokeAll Task ";
                    return key;
                }
            });
        }
        freezeClock();
        System.out.println("InvokeAll thread1");
        List<Future<String>> futures = scheduledExecutorService.invokeAll(c1);
        System.out.println("InvokeAll thread1 completed tick=" + getTick());
        unfreezeClock();
        Thread.sleep(100);
    }

    public void thread2() throws InterruptedException {
        System.out.println("thread2 tick=" + getTick());
        waitForTick(1);
        System.out.println("thread2 tick=" + getTick());
        Collection<Callable<String>> c1 = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            c1.add(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    String key = "Some Result of invokeAll Task ";
                    return key;
                }
            });
        }
        freezeClock();
        System.out.println("InvokeAll thread2");
        List<Future<String>> futures = scheduledExecutorService.invokeAll(c1);
        System.out.println("InvokeAll thread2 completed tick=" + getTick());
        unfreezeClock();
        Thread.sleep(100);
    }

    public void thread3() throws InterruptedException {
        System.out.println("thread3 tick=" + getTick());
        waitForTick(2);
        System.out.println("thread3 tick=" + getTick());
        Collection<Callable<String>> c1 = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            c1.add(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    String key = "Some Result of invokeAll Task ";
                    return key;
                }
            });
        }

        System.out.println("InvokeAll thread3");
        List<Future<String>> futures = scheduledExecutorService.invokeAll(c1);
        System.out.println("InvokeAll thread3 completed tick=" + getTick());
    }

    @Override
    public void finish() {
        System.out.println("Finish");
        assertEquals(0, scheduledExecutorService.numberOfTasks.get());
        assertEquals(0, scheduledExecutorService.tasks.size());
        assertEquals(0, scheduledExecutorService.rTasks.size());
        assertEquals(300, scheduledExecutorService.completedTasksCount.get());
    }
}
