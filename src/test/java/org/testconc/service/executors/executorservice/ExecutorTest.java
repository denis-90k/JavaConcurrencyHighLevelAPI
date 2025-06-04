package org.testconc.service.executors.executorservice;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Executor;

import static org.junit.Assert.*;

public class ExecutorTest {

    private static ThreadLocal threadLocal = new ThreadLocal<String>();

    @Before
    public void setUp()
    {
        threadLocal.remove();
    }

    @Test
    public void testSyncExectorImplementation_SameThreadRunsRunnable() {

        Executor executor = new Executor() {
            @Override
            public void execute(Runnable command) {
                command.run();
            }
        };

        executor.execute(() -> {
            threadLocal.set(Thread.currentThread().getName());
        });

        assertEquals(Thread.currentThread().getName(), threadLocal.get());
        assertEquals(Thread.currentThread().getName(), "main");

    }

    @Test
    public void testAsyncExecutorImplementation_AnotherThreadRunsRunnable() throws InterruptedException {

        Executor executor = new Executor() {
            @Override
            public void execute(Runnable command) {
                Thread thread = new Thread(command);
                thread.setName("new_Thread_name");
                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        executor.execute(() -> {
            threadLocal.set(Thread.currentThread().getName());
        });

        assertNotEquals(Thread.currentThread().getName(), threadLocal.get());
        assertEquals("main", Thread.currentThread().getName());
        assertNull(threadLocal.get());
    }
}
