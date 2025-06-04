package org.testconc.service.executors;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class ExecutorsTest {

    @Test
    public void testSingleThreadExecutor()
    {
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

        Executors.newSingleThreadExecutor();
        Executors.newCachedThreadPool();
        Executors.newScheduledThreadPool(12);
        Executors.newFixedThreadPool(12);
        Executors.newSingleThreadScheduledExecutor();
        Executors.newWorkStealingPool();
        Executors.unconfigurableExecutorService(null);
        Executors.unconfigurableScheduledExecutorService(null);
        //noinspection removal
        Executors.privilegedCallable(null);
        Executors.newThreadPerTaskExecutor(Executors.defaultThreadFactory());
        Executors.newVirtualThreadPerTaskExecutor();
    }

    @Test
    public void testSingleThreadExecutorWithCustomThreadFactory()
    {
        ExecutorService singleThreadExecutorWithCustomTFactory = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r);
            }
        });
    }
}
