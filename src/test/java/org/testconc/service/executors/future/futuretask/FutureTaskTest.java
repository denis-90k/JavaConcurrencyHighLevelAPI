package org.testconc.service.executors.future.futuretask;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.*;

import static org.junit.Assert.*;

public class FutureTaskTest {

    @Test
    public void testFT_runRunnable_Sync() throws InterruptedException, ExecutionException {

        CountDownLatch cdl = new CountDownLatch(1);
        FutureTask<String> future = new FutureTask<>(new Runnable() {
            @Override
            public void run() {
                cdl.countDown();
            }
        }, "Default result");

        future.run();

        cdl.await();

        Assert.assertEquals("Default result", future.get());
    }

    @Test
    public void testFT_runRunnable_Async() throws InterruptedException, ExecutionException {

        CountDownLatch cdl = new CountDownLatch(1);
        FutureTask<String> future = new FutureTask<>(new Runnable() {
            @Override
            public void run() {
                cdl.countDown();
            }
        }, "Default result");

        ForkJoinPool.commonPool().execute(future);

        Assert.assertEquals("Default result", future.get());
    }

    @Test(expected = CancellationException.class)
    public void testFT_cancel() throws ExecutionException, InterruptedException {
        FutureTask<String> future = new FutureTask<>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "Default result";
            }
        });
//        ForkJoinPool.commonPool().execute(future);

        assertTrue(future.cancel(true));

        future.get();
    }

    @Test(expected = CancellationException.class)
    public void testFT_cancelRunning() throws ExecutionException, InterruptedException {
        CountDownLatch cdl = new CountDownLatch(1);
        FutureTask<String> future = new FutureTask<>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                cdl.countDown();
                Thread.sleep(3000);

                return "Default result";
            }
        });
        ForkJoinPool.commonPool().execute(future);

        assertTrue(cdl.await(123, TimeUnit.SECONDS));

        assertFalse(future.isDone());
        assertEquals(Future.State.RUNNING, future.state());
        assertTrue(future.cancel(false));
        assertTrue(future.isCancelled());
        assertTrue(future.isDone());

        assertEquals(Future.State.CANCELLED, future.state());

        future.get();
    }

    @Test(expected = IllegalStateException.class)
    public void testFT_exceptionNow_NotCompleted() {
        FutureTask<String> future = new FutureTask<>(() -> "Default result");

        try {
            future.exceptionNow();
        } catch (IllegalStateException ex) {
            assertEquals("Task has not completed", ex.getMessage());
            throw ex;
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testFT_exceptionNow_Completed() {
        FutureTask<String> future = new FutureTask<>(() -> "Default result");
        future.run();
        try {
            future.exceptionNow();
        } catch (IllegalStateException ex) {
            assertEquals("Task completed with a result", ex.getMessage());
            throw ex;
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testFT_exceptionNow_Canceled() {
        FutureTask<String> future = new FutureTask<>(() -> "Default result");
        future.cancel(false);
        try {
            future.exceptionNow();
        } catch (IllegalStateException ex) {
            assertEquals("Task was cancelled", ex.getMessage());
            throw ex;
        }
    }
}
