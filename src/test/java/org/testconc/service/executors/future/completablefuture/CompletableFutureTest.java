package org.testconc.service.executors.future.completablefuture;

import org.checkerframework.checker.units.qual.A;
import org.junit.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.Assert.*;

public class CompletableFutureTest {

    @Test
    public void testCF_completedFutureResult() throws ExecutionException, InterruptedException {
        CompletableFuture<String> someResult = CompletableFuture.completedFuture("Some result");

        assertEquals("Some result", someResult.get());

    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCF_minimalCompletedStage() throws ExecutionException, InterruptedException {
        CompletionStage<String> someCompletedStage = CompletableFuture.completedStage("Some completed stage");

        CompletableFuture<String> completableFuture = someCompletedStage.toCompletableFuture();

        assertEquals("Some completed stage", completableFuture.get());
        assertEquals("java.util.concurrent.CompletableFuture$MinimalStage", someCompletedStage.getClass().getName());

        // All method in MinimalStage will throw UOE
        ((CompletableFuture) someCompletedStage).get();
    }

    @Test(expected = ExecutionException.class)
    public void testCF_failedFuture() throws ExecutionException, InterruptedException {
        CompletableFuture<Object> exc = CompletableFuture.failedFuture(new RuntimeException("Exc"));

        exc.get();
    }

    @Test(expected = ExecutionException.class)
    public void testCF_failedStage() throws ExecutionException, InterruptedException {
        CompletionStage<Object> exc = CompletableFuture.failedStage(new RuntimeException("Exc"));
        CompletableFuture<Object> completableFuture = exc.toCompletableFuture();

        assertTrue(completableFuture.isCompletedExceptionally());
        completableFuture.get();
    }

    @Test
    public void testCF_allOf() throws ExecutionException, InterruptedException {
        int millis = 5000;
        CompletableFuture<Object> cf1 = new CompletableFuture<>().completeAsync(new Supplier<String>() {
            @Override
            public String get() {
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return "Hello1";
            }
        });
        CompletableFuture<Object> cf2 = new CompletableFuture<>().completeAsync(new Supplier<String>() {
            @Override
            public String get() {
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return "Hello2";
            }
        });
        CompletableFuture<Object> cf3 = new CompletableFuture<>().completeAsync(new Supplier<String>() {
            @Override
            public String get() {
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return "Hello2";
            }
        });
        CompletableFuture<Object> cf4 = new CompletableFuture<>().completeAsync(new Supplier<String>() {
            @Override
            public String get() {
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return "Hello2";
            }
        });
        CompletableFuture<Object> cf5 = new CompletableFuture<>().completeAsync(new Supplier<String>() {
            @Override
            public String get() {
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return "Hello2";
            }
        });
        CompletableFuture<Void> voidCompletableFuture = CompletableFuture.allOf(cf1, cf2, cf3, cf4, cf5);

        int numberOfDependents = voidCompletableFuture.getNumberOfDependents();
        assertEquals(0, numberOfDependents);
        assertEquals(1, cf1.getNumberOfDependents());
        assertEquals(1, cf2.getNumberOfDependents());
        assertEquals(1, cf3.getNumberOfDependents());
        assertEquals(1, cf4.getNumberOfDependents());
        assertEquals(1, cf5.getNumberOfDependents());

        while (!voidCompletableFuture.isDone()) {
            Thread.yield();
        }



        assertNull(voidCompletableFuture.get());
        assertEquals("Hello1", cf1.get());
        assertEquals("Hello2", cf2.get());
    }

    @Test
    public void testCF_anyOf() throws ExecutionException, InterruptedException {
        CompletableFuture<Object> cf1 = new CompletableFuture<>().completeAsync(new Supplier<String>() {
            @Override
            public String get() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return "Hello1";
            }
        });
        CompletableFuture<Object> cf2 = new CompletableFuture<>().completeAsync(new Supplier<String>() {
            @Override
            public String get() {
                try {
                    Thread.sleep(50000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return "Hello2";
            }
        });

        CompletableFuture<Object> objectCompletableFuture = CompletableFuture.anyOf(cf1, cf2);
//        objectCompletableFuture.
//        Thread.sleep(1200);
        while (!objectCompletableFuture.isDone()) {
        }

        int numberOfDependents = objectCompletableFuture.getNumberOfDependents();
        assertEquals(0, numberOfDependents);
        assertNotNull(objectCompletableFuture.get());
        assertEquals("Hello1", objectCompletableFuture.get());
        assertEquals("Hello1", cf1.get());
        assertFalse(cf2.isDone());
    }

    @Test
    public void testCF_delayedExecutorDefault() throws InterruptedException {
        Executor executor = CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS);

        var r = new AtomicReference<String>();
        executor.execute(() -> r.set("Execute"));

        assertNull(r.get());
        Thread.sleep(1200);
        assertEquals("Execute", r.get());
    }

    @Test
    public void testCF_delayedExecutor() throws InterruptedException {
        Executor executor = CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS, new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1)));

        var r = new AtomicReference<String>();
        executor.execute(() -> r.set("Execute"));

        assertNull(r.get());
        Thread.sleep(1200);
        assertEquals("Execute", r.get());
    }

    @Test
    public void testCF_runAsyncDefExecutor() {
        var r = new AtomicReference<String>();
        CompletableFuture<Void> ranAsync = CompletableFuture.runAsync(() -> r.set("Ran async"));

        while (!ranAsync.isDone()) {
        }

        assertEquals("Ran async", r.get());
    }

    @Test
    public void testCF_runAsync() {
        var r = new AtomicReference<String>();
        CompletableFuture<Void> ranAsync = CompletableFuture.runAsync(() -> r.set("Ran async"), new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1)));

        while (!ranAsync.isDone()) {
        }

        assertEquals("Ran async", r.get());
    }

    @Test
    public void testCF_supplyAsyncDefExecutor() throws ExecutionException, InterruptedException {
        CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> "Result");

        while (!cf.isDone()) {
        }

        assertEquals("Result", cf.get());
    }

    @Test
    public void testCF_supplyAsync() throws ExecutionException, InterruptedException {
        CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> "Result", new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1)));

        while (!cf.isDone()) {
        }

        assertEquals("Result", cf.get());
    }

    @Test
    public void testCF_CompleteIncompletedCF() throws ExecutionException, InterruptedException {
        CompletableFuture<Object> incompleteCF = new CompletableFuture<>();

        assertFalse(incompleteCF.isDone());

        boolean isCompleted = incompleteCF.complete("Some result");

        assertTrue(isCompleted);
        assertTrue(incompleteCF.isDone());
        assertEquals("Some result", incompleteCF.get());
    }

    @Test
    public void testCF_CompleteExceptionally_Wrong() throws ExecutionException, InterruptedException {
        CompletableFuture<Object> incompleteCF = new CompletableFuture<>();

        assertFalse(incompleteCF.isDone());

        boolean isCompleted = incompleteCF.complete(new RuntimeException("Some issue"));

        assertTrue(isCompleted);
        assertTrue(incompleteCF.isDone());
        assertFalse(incompleteCF.isCompletedExceptionally());

        assertEquals(RuntimeException.class, incompleteCF.get().getClass());
    }

    @Test(expected = ExecutionException.class)
    public void testCF_CompleteExceptionally() throws ExecutionException, InterruptedException {
        CompletableFuture<Object> incompleteCF = new CompletableFuture<>();

        assertFalse(incompleteCF.isDone());

        boolean isCompleted = incompleteCF.completeExceptionally(new RuntimeException("Some issue"));

        assertTrue(isCompleted);
        assertTrue(incompleteCF.isDone());
        assertTrue(incompleteCF.isCompletedExceptionally());

        incompleteCF.get();
    }

    @Test(expected = CancellationException.class)
    public void testCF_Cancel() throws ExecutionException, InterruptedException {
        CompletableFuture<Object> incompleteCF = new CompletableFuture<>();

        assertFalse(incompleteCF.isDone());

        boolean cancel = incompleteCF.cancel(false);

        assertTrue(cancel);
        assertTrue(incompleteCF.isCancelled());
        assertTrue(incompleteCF.isDone());
        assertTrue(incompleteCF.isCompletedExceptionally());

        incompleteCF.get();
    }

    @Test
    public void testCF_CancelAfterDone() throws ExecutionException, InterruptedException {
        CompletableFuture<Object> incompleteCF = new CompletableFuture<>();

        assertFalse(incompleteCF.isDone());
        boolean someValue = incompleteCF.complete("Some value");

        assertTrue(someValue);
        assertTrue(incompleteCF.isDone());

        boolean cancel = incompleteCF.cancel(false);
        assertFalse(cancel);
        assertFalse(incompleteCF.isCancelled());
    }

    @Test(expected = TimeoutException.class)
    public void testCF_getWithTimeoutElapsed() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<Object> incompleteCF = new CompletableFuture<>();

        incompleteCF.get(1, TimeUnit.SECONDS);
    }

    @Test
    public void testCF_NumberOfDependents() {
        CompletableFuture incompleteCF = new CompletableFuture<>();

        assertEquals(0, incompleteCF.getNumberOfDependents());

        CompletableFuture<Object> cf1 = new CompletableFuture<>().completeAsync(new Supplier<String>() {
            @Override
            public String get() {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return "Hello1";
            }
        });
        CompletableFuture<Object> cf2 = new CompletableFuture<>().completeAsync(new Supplier<String>() {
            @Override
            public String get() {
                try {
                    Thread.sleep(50000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return "Hello2";
            }
        });

        incompleteCF = CompletableFuture.allOf(cf1, cf2);

        assertEquals(0, incompleteCF.getNumberOfDependents());
        assertEquals(1, cf1.getNumberOfDependents());
        assertEquals(1, cf2.getNumberOfDependents());
    }

    @Test
    public void testCF_CompleteExceptionaly() throws InterruptedException {
        CountDownLatch cdl = new CountDownLatch(1);

        AtomicReference ar = new AtomicReference();
        CompletableFuture<Object> cf1 = new CompletableFuture<>().completeAsync(new Supplier<String>() {
            @Override
            public String get() {
                try {
                    Thread.sleep(3000);
                    ar.set("Hello1");
                    cdl.countDown();
                } catch (InterruptedException e) {
                    ar.set("Interrupted");
                    cdl.countDown();
                    throw new RuntimeException(e);
                }
                return "Hello1";
            }
        });

        Thread.sleep(1000);
        boolean b = cf1.completeExceptionally(new RuntimeException());

        assertTrue(b);
        assertTrue(cf1.isCompletedExceptionally());
        assertTrue(cf1.isDone());
        cdl.await();

        assertEquals("Hello1", ar.get());
    }

    @Test
    public void testCF_copy() throws ExecutionException, InterruptedException {
        CompletableFuture incompleteCF = new CompletableFuture<>();

        CompletableFuture copy = incompleteCF.copy();

        assertFalse(incompleteCF.isDone());
        assertFalse(copy.isDone());

        incompleteCF.complete("Some result");

        assertTrue(incompleteCF.isDone());
        assertTrue(copy.isDone());

        assertEquals("Some result", incompleteCF.get());
        assertEquals("Some result", copy.get());
    }

    @Test
    public void testCF_getNow() throws ExecutionException, InterruptedException {
        CompletableFuture incompleteCF = new CompletableFuture<>();

        Object spareValue = incompleteCF.getNow("Spare value");

        assertEquals("Spare value", spareValue);
        assertFalse(incompleteCF.isDone());

        incompleteCF.complete("Some value");

        assertTrue(incompleteCF.isDone());
        assertEquals("Some value", incompleteCF.get());
        assertEquals("Some value", incompleteCF.getNow("Spare value"));

    }

    @Test
    public void testCF_join() {
        CountDownLatch cdl = new CountDownLatch(1);

        AtomicReference ar = new AtomicReference();
        CompletableFuture<Object> cf1 = new CompletableFuture<>().completeAsync(new Supplier<String>() {
            @Override
            public String get() {
                try {
                    Thread.sleep(3000);
                    ar.set("Hello1");
                    cdl.countDown();
                } catch (InterruptedException e) {
                    ar.set("Interrupted");
                    cdl.countDown();
                    throw new RuntimeException(e);
                }
                return "Hello1";
            }
        });

        Object result = cf1.join();

        assertEquals("Hello1", result);
    }

    @Test
    public void testCF_obtrudeValue() throws ExecutionException, InterruptedException {
        CompletableFuture incompleteCF = new CompletableFuture<>();

        incompleteCF.complete("Some value");

        assertEquals("Some value", incompleteCF.get());
        incompleteCF.obtrudeValue("Another value");
        assertEquals("Another value", incompleteCF.get());
    }

    @Test(expected = ExecutionException.class)
    public void testCF_obtrudeException() throws ExecutionException, InterruptedException {
        CompletableFuture incompleteCF = new CompletableFuture<>();

        incompleteCF.complete("Some value");

        assertEquals("Some value", incompleteCF.get());
        incompleteCF.obtrudeException(new RuntimeException("Exc"));
        incompleteCF.get();
    }

    @Test(expected = IllegalStateException.class)
    public void testCF_exceptionNow() throws ExecutionException, InterruptedException {
        CompletableFuture incompleteCF = new CompletableFuture<>();

        incompleteCF.obtrudeException(new RuntimeException());

        Throwable throwable = incompleteCF.exceptionNow();

        assertEquals(RuntimeException.class, throwable.getClass());

        incompleteCF.obtrudeValue("Some not exception value OR Cancellation exc");
        incompleteCF.exceptionNow();
    }

    @Test
    public void testCF_WhenCompleteSuccess() throws ExecutionException, InterruptedException {
        CompletableFuture incompleteCF = CompletableFuture.supplyAsync("Zero"::toString);

        AtomicReference result = new AtomicReference();

        CompletableFuture cf = incompleteCF
                .whenComplete((res, ex) -> result.set(res + "One"))
                .whenComplete((res, ex) -> {
                    System.out.println(res);
                    System.out.println(ex);
                    result.set(result.get() + "Two");
                });

        assertEquals("ZeroOneTwo", result.get());
        assertEquals("Zero", cf.get());
    }

    @Test(expected = ArithmeticException.class)
    public void testCF_WhenCompleteFailure() throws Throwable {
        CompletableFuture incompleteCF = CompletableFuture.supplyAsync("Zero"::toString);

        AtomicReference result = new AtomicReference();

        CompletableFuture cf = incompleteCF
                .whenComplete((res, ex) -> result.set(res + "One"))
                .whenComplete((res, ex) -> {
                    System.out.println(res);
                    System.out.println(ex);
                    result.set(result.get() + "Two");
                    throw new ArithmeticException("");
                });

        assertEquals("ZeroOneTwo", result.get());
        try {
            cf.get();
        } catch (ExecutionException e) {
            throw e.getCause();
        }

    }

    @Test
    public void testCF_WhenComplete_AsyncVSSync_MainCFIsAlreadyDone() throws Throwable {
        CompletableFuture incompleteCF = CompletableFuture.supplyAsync("Zero"::toString);

        CountDownLatch cdl = new CountDownLatch(2);

        AtomicReference whenSyncResult = new AtomicReference();
        AtomicReference whenAsyncResult = new AtomicReference();
        incompleteCF
                .whenComplete((res, ex) -> {
                    cdl.countDown();
                    whenSyncResult.set(Thread.currentThread().getName());
                })
                .whenCompleteAsync((res, ex) -> {
                    cdl.countDown();
                    whenAsyncResult.set(Thread.currentThread().getName());
                });

        cdl.await();

        System.out.println(whenAsyncResult);
        System.out.println(whenSyncResult);
        assertTrue(whenSyncResult.get().toString().contains("main")); // Not only because whenComplete invoked, but also because incompleteCF is already Done while running .whenComplete(..)
        assertTrue(whenAsyncResult.get().toString().contains("commonPool-worker"));
    }

    @Test
    public void testCF_WhenComplete_AsyncVSSync_MainCFIsNotDoneYet() throws Throwable {
        CompletableFuture incompleteCF = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "Zero";
        });

        assertFalse(incompleteCF.isDone());

        CountDownLatch cdl = new CountDownLatch(2);

        AtomicReference whenSyncResult = new AtomicReference();
        AtomicReference whenAsyncResult = new AtomicReference();
        CompletableFuture cf1 = incompleteCF
                .whenComplete((res, ex) -> {
                    cdl.countDown();
                    whenSyncResult.set(Thread.currentThread().getName());
                });

        assertFalse(incompleteCF.isDone());
        assertFalse(cf1.isDone());

        CompletableFuture cf2 = cf1.whenCompleteAsync((res, ex) -> {
            cdl.countDown();
            whenAsyncResult.set(Thread.currentThread().getName());
        });

        assertFalse(incompleteCF.isDone());
        assertFalse(cf1.isDone());
        assertFalse(cf2.isDone());

        cdl.await();

        assertTrue(incompleteCF.isDone());
        assertTrue(cf1.isDone());
        assertTrue(cf2.isDone());

        System.out.println(whenAsyncResult);
        System.out.println(whenSyncResult);
        assertTrue(whenSyncResult.get().toString().contains("commonPool-worker")); // It is because while .whenComplete(..) invocations by main thread, current CF was incomplete yet
        assertTrue(whenAsyncResult.get().toString().contains("commonPool-worker"));
    }

    @Test
    public void testCF_WhenComplete_Async_MainCFIsAlreadyDone() throws Throwable {
        CompletableFuture incompleteCF = CompletableFuture.supplyAsync("Zero"::toString);

        Thread.sleep(500);
        CountDownLatch cdl = new CountDownLatch(2);

        AtomicReference whenSyncResult = new AtomicReference();
        AtomicReference whenAsyncResult = new AtomicReference();
        incompleteCF
                .whenCompleteAsync((res, ex) -> {
                    cdl.countDown();
                    whenSyncResult.set(Thread.currentThread().getName());
                })
                .whenCompleteAsync((res, ex) -> {
                    cdl.countDown();
                    whenAsyncResult.set(Thread.currentThread().getName());
                });

        cdl.await();

        System.out.println(whenAsyncResult);
        System.out.println(whenSyncResult);
        assertTrue(whenSyncResult.get().toString().contains("commonPool-worker")); // because whenComplete always uses Executor to complete tasks
        assertTrue(whenAsyncResult.get().toString().contains("commonPool-worker"));
    }

    @Test
    public void testCF_handle() throws ExecutionException, InterruptedException {
        CompletableFuture cf = CompletableFuture.supplyAsync("Zero"::toString);

        CompletableFuture handleResult = cf
                .handle((res, ex) -> {
                    assertEquals("Zero", res);
                    assertNull(ex);
                    return res + "One";
                })
                .handle((res, ex) -> {
                    assertEquals("ZeroOne", res);
                    assertNull(ex);
                    return res + "Two";
                });

        assertEquals("ZeroOneTwo", handleResult.get());
    }

    @Test
    public void testCF_handle_AsyncVSSync_MainIsAlreadyDone() throws ExecutionException, InterruptedException {
        CompletableFuture cf = CompletableFuture.supplyAsync("Zero"::toString);

        CountDownLatch cdl = new CountDownLatch(2);

        AtomicReference whenSyncResult = new AtomicReference();
        AtomicReference whenAsyncResult = new AtomicReference();

        CompletableFuture handleResult = cf
                .handle((res, ex) -> {
                    assertEquals("Zero", res);
                    assertNull(ex);
                    cdl.countDown();
                    whenSyncResult.set(Thread.currentThread().getName());
                    return res + "One";
                })
                .handleAsync((res, ex) -> {
                    assertEquals("ZeroOne", res);
                    assertNull(ex);
                    cdl.countDown();
                    whenAsyncResult.set(Thread.currentThread().getName());
                    return res + "Two";
                });

        cdl.await();
        System.out.println(whenAsyncResult);
        System.out.println(whenSyncResult);
        assertTrue(whenSyncResult.get().toString().contains("main"));
        assertTrue(whenAsyncResult.get().toString().contains("commonPool-worker"));
    }

    @Test
    public void testCF_handle_AsyncVSSync_MainIsNotDoneYet() throws ExecutionException, InterruptedException {
        CompletableFuture cf = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "Zero";
        });

        assertFalse(cf.isDone());

        CountDownLatch cdl = new CountDownLatch(2);

        AtomicReference whenSyncResult = new AtomicReference();
        AtomicReference whenAsyncResult = new AtomicReference();

        CompletableFuture first = cf
                .handle((res, ex) -> {
                    assertEquals("Zero", res);
                    assertNull(ex);
                    cdl.countDown();
                    whenSyncResult.set(Thread.currentThread().getName());
                    return res + "One";
                });

        assertFalse(cf.isDone());
        assertFalse(first.isDone());

        CompletableFuture handleResult = first
                .handleAsync((res, ex) -> {
                    assertEquals("ZeroOne", res);
                    assertNull(ex);
                    cdl.countDown();
                    whenAsyncResult.set(Thread.currentThread().getName());
                    return res + "Two";
                });

        assertFalse(cf.isDone());
        assertFalse(first.isDone());
        assertFalse(handleResult.isDone());

        cdl.await();
        System.out.println(whenAsyncResult);
        System.out.println(whenSyncResult);
        assertTrue(cf.isDone());
        assertTrue(first.isDone());
        assertTrue(handleResult.isDone());
        assertTrue(whenSyncResult.get().toString().contains("commonPool-worker"));
        assertTrue(whenAsyncResult.get().toString().contains("commonPool-worker"));
    }

    @Test
    public void testCF_thenAccept_MainAlreadyDone() throws ExecutionException, InterruptedException {
        CompletableFuture cf = CompletableFuture.supplyAsync("Zero"::toString);

        AtomicReference whenSyncResult = new AtomicReference();
        AtomicReference whenAsyncResult = new AtomicReference();

        CountDownLatch cdl = new CountDownLatch(2);

        Thread.sleep(1000);
        assertTrue(cf.isDone());
        CompletableFuture zero = cf.thenAccept((res) -> {
            assertEquals("Zero", res);
            whenSyncResult.set(Thread.currentThread().getName());
            cdl.countDown();
        });

        assertTrue(cf.isDone());
        assertTrue(zero.isDone());

        CompletableFuture r = zero.thenAcceptAsync((res) -> {
            assertEquals(null, res);
            whenAsyncResult.set(Thread.currentThread().getName());
            cdl.countDown();
        });

        cdl.await();

        assertEquals("Zero", cf.get());
        assertNull(r.get());

        assertTrue(whenSyncResult.get().toString().contains("main"));
        assertTrue(whenAsyncResult.get().toString().contains("commonPool-worker"));
    }

    @Test
    public void testCF_thenAccept_MainIsNotDoneYet() throws ExecutionException, InterruptedException {
        CompletableFuture cf = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "Zero";
        });

        AtomicReference whenSyncResult = new AtomicReference();
        AtomicReference whenAsyncResult = new AtomicReference();

        CountDownLatch cdl = new CountDownLatch(2);

        Thread.sleep(500);
        assertFalse(cf.isDone());

        CompletableFuture zero = cf.thenAccept((res) -> {
            assertEquals("Zero", res);
            whenSyncResult.set(Thread.currentThread().getName());
            cdl.countDown();
        });

        assertFalse(cf.isDone());
        assertFalse(zero.isDone());
        assertEquals(null, zero.get());

        CompletableFuture r = zero.thenAcceptAsync((res) -> {
            assertEquals(null, res);
            whenAsyncResult.set(Thread.currentThread().getName());
            cdl.countDown();
        });

        cdl.await();

        assertEquals("Zero", cf.get());
        assertNull(r.get());

        assertTrue(whenSyncResult.get().toString().contains("commonPool-worker"));
        assertTrue(whenAsyncResult.get().toString().contains("commonPool-worker"));
    }

    @Test(expected = ExecutionException.class)
    public void testCF_thenAccept_Exceptional() throws ExecutionException, InterruptedException {
        CompletableFuture cf = CompletableFuture.supplyAsync("Zero"::toString);

        AtomicReference result = new AtomicReference();

        CompletableFuture zero = cf.thenAccept((res) -> {
            assertEquals("Zero", res);
            throw new RuntimeException("");
        }).thenAccept((res) -> {
            result.set(res + "One");
        });

        assertEquals("Zero", cf.get());
        assertNull(result.get());
        zero.get();
    }

    @Test
    public void testCF_acceptEither_OnlyOneGoesInsideConsumer() throws InterruptedException {
        CompletableFuture cf1 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "Zero";
        });
        CompletableFuture cf2 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "One";
        });

        CountDownLatch cdl = new CountDownLatch(2);

        AtomicReference<String> result = new AtomicReference<String>("");

        cf1.acceptEither(cf2, (r) -> {
            System.out.println(r);
            result.set(result.get() + r);
            cdl.countDown();
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        assertFalse(cdl.await(3, TimeUnit.SECONDS));
        assertEquals("Zero", result.get());
    }

    @Test
    public void testCF_acceptEither_Exceptional_NoOneGoesInsideConsumer() throws InterruptedException {
        CompletableFuture cf1 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(500);

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            throw new RuntimeException("");
//            return "Zero";
        });
        CompletableFuture cf2 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "One";
        });

        CountDownLatch cdl = new CountDownLatch(1);

        AtomicReference<String> result = new AtomicReference<String>("");

        cf1.acceptEither(cf2, (r) -> {
            System.out.println(r);
            result.set(result.get() + r);
            cdl.countDown();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        assertFalse(cdl.await(3, TimeUnit.SECONDS));
        assertEquals("", result.get());
    }

    @Test
    public void testCF_acceptEither_Sync() throws InterruptedException {
        CompletableFuture cf1 = CompletableFuture.supplyAsync("Zero"::toString);
        CompletableFuture cf2 = CompletableFuture.supplyAsync("One"::toString);

        CountDownLatch cdl = new CountDownLatch(1);

        AtomicReference<String> result = new AtomicReference<String>("");

        cf1.acceptEither(cf2, (r) -> {
            result.set(Thread.currentThread().getName());
            cdl.countDown();
        });

        assertTrue(cdl.await(1, TimeUnit.SECONDS));
        assertTrue(result.get().contains("main"));
    }

    @Test
    public void testCF_acceptEither_Async() throws InterruptedException {
        CompletableFuture cf1 = CompletableFuture.supplyAsync("Zero"::toString);
        CompletableFuture cf2 = CompletableFuture.supplyAsync("One"::toString);

        CountDownLatch cdl = new CountDownLatch(1);

        AtomicReference<String> result = new AtomicReference<String>("");

        cf1.acceptEitherAsync(cf2, (r) -> {
            result.set(Thread.currentThread().getName());
            cdl.countDown();
        });

        assertTrue(cdl.await(1, TimeUnit.SECONDS));
        assertTrue(result.get().contains("commonPool-worker"));
    }

    @Test
    public void testCF_AcceptBoth() throws InterruptedException, ExecutionException {
        CompletableFuture cf1 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "Zero";
        });
        CompletableFuture cf2 = CompletableFuture.supplyAsync("One"::toString);

        CountDownLatch cdl = new CountDownLatch(1);

        CompletableFuture result = cf1.thenAcceptBoth(cf2, (r1, r2) -> {
            assertEquals("Zero", r1);
            assertEquals("One", r2);
            System.out.println(r1);
            System.out.println(r2);
            cdl.countDown();
        });

        assertTrue(cdl.await(2, TimeUnit.SECONDS));
        assertNull(result.get());
    }

    @Test
    public void testCF_AcceptBothAsync() throws InterruptedException, ExecutionException {
        CompletableFuture cf1 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "Zero";
        });
        CompletableFuture cf2 = CompletableFuture.supplyAsync("One"::toString);

        CountDownLatch cdl = new CountDownLatch(1);
        AtomicReference<String> threadName = new AtomicReference<>("");

        CompletableFuture result = cf1.thenAcceptBoth(cf2, (r1, r2) -> {
            assertEquals("Zero", r1);
            assertEquals("One", r2);
            System.out.println(r1);
            System.out.println(r2);
            cdl.countDown();
            threadName.set(Thread.currentThread().getName());
        });

        assertTrue(cdl.await(2, TimeUnit.SECONDS));
        assertNull(result.get());
        assertTrue(threadName.get().contains("commonPool-worker"));
    }

    @Test
    public void testCF_thenComplete() throws ExecutionException, InterruptedException {
        CompletableFuture cf1 = CompletableFuture.supplyAsync("Zero"::toString);

        AtomicReference<String> firstSuplierThread = new AtomicReference<>("");
        AtomicReference<String> secondSuplierThread = new AtomicReference<>("");

        CompletableFuture result = cf1
                .thenCompose((res) -> {
                    System.out.println(res);
                    firstSuplierThread.set(Thread.currentThread().getName());
                    return CompletableFuture.completedFuture(res + "One");
                })
                .thenCompose((res) -> {
                    System.out.println(res);
                    secondSuplierThread.set(Thread.currentThread().getName());
                    return CompletableFuture.completedFuture(res + "Two");
                });
        System.out.println(result.get());
        assertEquals("ZeroOneTwo", result.get());

        assertTrue(firstSuplierThread.get().contains("main"));
        assertTrue(secondSuplierThread.get().contains("main"));
    }

    @Test(expected = RuntimeException.class)
    public void testCF_thenComplete_Exceptionally() throws Throwable {
        CompletableFuture cf1 = CompletableFuture.supplyAsync("Zero"::toString);

        CompletableFuture cf2 = cf1
                .thenCompose((res) -> {
                    System.out.println(res);
                    return CompletableFuture.completedFuture(res + "One");
                });
        CompletableFuture result = cf2
                .thenCompose((res) -> {
                    System.out.println(res);
                    throw new RuntimeException("Some exception");
                    //return CompletableFuture.completedFuture(res + "Two");
                });

        assertEquals("Zero", cf1.get());
        assertEquals("ZeroOne", cf2.get());
        try {
            result.get();
        }catch(ExecutionException ex) {
            Throwable cause = ex.getCause();

            assertEquals("Some exception", cause.getMessage());

            throw cause;
        }
    }

    @Test
    public void testCF_thenComplete_Delay() throws ExecutionException, InterruptedException {

        AtomicReference<String> mainSuplierThread = new AtomicReference<>("");

        CompletableFuture cf1 = CompletableFuture.supplyAsync(() -> {
            mainSuplierThread.set(Thread.currentThread().getName());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "Zero";
        });

        AtomicReference<String> firstSuplierThread = new AtomicReference<>("");
        AtomicReference<String> secondSuplierThread = new AtomicReference<>("");

        CompletableFuture result = cf1
                .thenCompose((res) -> {
                    System.out.println(res);
                    firstSuplierThread.set(Thread.currentThread().getName());
                    return CompletableFuture.completedFuture(res + "One");
                })
                .thenCompose((res) -> {
                    System.out.println(res);
                    secondSuplierThread.set(Thread.currentThread().getName());
                    return CompletableFuture.completedFuture(res + "Two");
                });

        System.out.println(result.get());
        assertEquals("ZeroOneTwo", result.get());

        assertTrue(mainSuplierThread.get().contains("commonPool-worker"));
        assertTrue(firstSuplierThread.get().contains("commonPool-worker"));
        assertTrue(secondSuplierThread.get().contains("commonPool-worker"));
    }

    @Test
    public void testCF_thenComplete_Async() throws ExecutionException, InterruptedException {
        CompletableFuture cf1 = CompletableFuture.supplyAsync("Zero"::toString);

        AtomicReference<String> firstSuplierThread = new AtomicReference<>("");
        AtomicReference<String> secondSuplierThread = new AtomicReference<>("");

        CompletableFuture result = cf1
                .thenComposeAsync((res) -> {
                    System.out.println(res);
                    firstSuplierThread.set(Thread.currentThread().getName());
                    return CompletableFuture.completedFuture(res + "One");
                })
                .thenComposeAsync((res) -> {
                    System.out.println(res);
                    secondSuplierThread.set(Thread.currentThread().getName());
                    return CompletableFuture.completedFuture(res + "Two");
                });
        System.out.println(result.get());
        assertEquals("ZeroOneTwo", result.get());

        assertTrue(firstSuplierThread.get().contains("commonPool-worker"));
        assertTrue(secondSuplierThread.get().contains("commonPool-worker"));
    }

    @Test
    public void testCF_completedExceptionally_withoutExcept() throws ExecutionException, InterruptedException {
        CompletableFuture cf1 = CompletableFuture.supplyAsync("Zero"::toString);

        AtomicReference cf2Ex = new AtomicReference("");
        AtomicReference cf3Res = new AtomicReference("");

        CompletableFuture cf2 = cf1.exceptionallyCompose((ex) -> {
            cf2Ex.set("Inside");
            return CompletableFuture.completedFuture("Errored");
        });

        CompletableFuture cf3 = cf2.handle((res, ex) -> {
            if(ex == null)
                cf3Res.set(res);
            return cf3Res;
        });

        assertEquals("", cf2Ex.get());
        assertEquals("Zero", cf3Res.get());

        assertEquals(cf3Res, cf3.get());

    }

    @Test
    public void testCF_completedExceptionally_withExcept() throws ExecutionException, InterruptedException {
        CompletableFuture cf1 = CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("Some exception");
        });

        AtomicReference cf2Ex = new AtomicReference("");
        AtomicReference cf3Res = new AtomicReference("");

        AtomicReference<String> cf2Thread = new AtomicReference<>("");

        CompletableFuture cf2 = cf1.exceptionallyCompose((ex) -> {
            cf2Thread.set(Thread.currentThread().getName());
            cf2Ex.set(((Exception)ex).getCause().getMessage());
            return CompletableFuture.completedFuture("Errored");
        });

        AtomicReference<String> cf3Thread = new AtomicReference<>("");
        CompletableFuture cf3 = cf2.handle((res, ex) -> {
            cf3Thread.set(Thread.currentThread().getName());
            if(ex == null)
                cf3Res.set(res);
            return cf3Res;
        });

        assertEquals("Some exception", cf2Ex.get());
        assertEquals("Errored", cf3Res.get());
        assertTrue(cf2Thread.get().contains("main"));
        assertTrue(cf3Thread.get().contains("main"));

        assertEquals(cf3Res, cf3.get());

    }

    @Test
    public void testCF_completedExceptionallyAsync_withExcept() throws ExecutionException, InterruptedException {
        CompletableFuture cf1 = CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("Some exception");
        });

        AtomicReference cf2Ex = new AtomicReference("");
        AtomicReference cf3Res = new AtomicReference("");

        AtomicReference<String> cf2Thread = new AtomicReference<>("");

        CountDownLatch cdl = new CountDownLatch(2);

        CompletableFuture cf2 = cf1.exceptionallyComposeAsync((ex) -> {
            cf2Thread.set(Thread.currentThread().getName());
            cf2Ex.set(((Exception)ex).getCause().getMessage());
            cdl.countDown();
            return CompletableFuture.completedFuture("Errored");
        });

        AtomicReference<String> cf3Thread = new AtomicReference<>("");
        CompletableFuture cf3 = cf2.handleAsync((res, ex) -> {
            cf3Thread.set(Thread.currentThread().getName());
            if(ex == null)
                cf3Res.set(res);
            cdl.countDown();
            return cf3Res;
        });

        cdl.await();
        assertEquals("Some exception", cf2Ex.get());
        assertEquals("Errored", cf3Res.get());
        assertTrue(cf2Thread.get().contains("commonPool-worker"));
        assertTrue(cf3Thread.get().contains("commonPool-worker"));

        assertEquals(cf3Res, cf3.get());

    }

    @Test
    public void testCF_ExceptionallyVSExceptionallyCompose() throws ExecutionException, InterruptedException {
        CompletableFuture cf1 = CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("Some exception");
        });
        CompletableFuture cf2 = cf1.exceptionally((ex) -> "Not compose result");

        assertEquals("Not compose result", cf2.get());

        CompletableFuture cf3 = cf2.handle((r, e) -> {
            throw new RuntimeException("Some except");
        });

        CountDownLatch cdl = new CountDownLatch(1);
        CompletableFuture cf4 = cf3.exceptionallyCompose((ex) -> CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            cdl.countDown();
            return "Inner task result";
        }));

        assertFalse(cf4.isDone());
        cdl.await();
        assertEquals("Inner task result", cf4.get());
    }

    @Test
    public void testCF_run_runAsync() throws ExecutionException, InterruptedException {
        CompletableFuture cf1 = CompletableFuture.supplyAsync("Zero"::toString);

        AtomicReference<String> cf2Res = new AtomicReference<>("");
        AtomicReference<String> cf2Thread = new AtomicReference<>("");
        CompletableFuture cf2 = cf1.thenRun(() -> {
            cf2Res.set("RAN");
            cf2Thread.set(Thread.currentThread().getName());
        });

        AtomicReference<String> cf3Thread = new AtomicReference<>("");
        CountDownLatch cdl = new CountDownLatch(1);
        CompletableFuture cf3 = cf2.thenRunAsync(() -> {
            cf3Thread.set(Thread.currentThread().getName());
            cdl.countDown();
        });

        cdl.await();

        assertEquals("RAN", cf2Res.get());
        assertTrue(cf2Thread.get().contains("main"));
        assertTrue(cf3Thread.get().contains("commonPool-worker"));

        assertEquals("Zero", cf1.get());
        assertEquals(null, cf2.get());
        assertEquals(null, cf3.get());

    }

    @Test
    public void testCF_runAfterBoth() throws ExecutionException, InterruptedException {
        CompletableFuture cf1 = CompletableFuture.supplyAsync("Zero"::toString);
        CompletableFuture cf2 = CompletableFuture.supplyAsync("One"::toString);

        AtomicReference<String> result = new AtomicReference<>("");
        CompletableFuture cf3 = cf1.runAfterBoth(cf2, () -> result.set("2 actions done"));

        assertEquals("2 actions done", result.get());
        assertEquals(null, cf3.get());
    }

    @Test(expected = RuntimeException.class)
    public void testCF_runAfterBoth_OneExceptionally() throws Throwable {
        CompletableFuture cf1 = CompletableFuture.supplyAsync("Zero"::toString);
        CompletableFuture cf2 = CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("Some exception");
        });

        AtomicReference<String> result = new AtomicReference<>("");
        CompletableFuture cf3 = cf1.runAfterBoth(cf2, () -> result.set("2 actions done"));

        assertEquals("", result.get());

        try {
            cf3.get();
        }catch (ExecutionException ex) {
            assertEquals("Some exception", ex.getCause().getMessage());
            throw ex.getCause();
        }
    }

    @Test
    public void testCF_runAfterEither() throws ExecutionException, InterruptedException {
        CompletableFuture cf1 = CompletableFuture.supplyAsync("Zero"::toString);
        CompletableFuture cf2 = CompletableFuture.supplyAsync("One"::toString);

        AtomicReference<String> result = new AtomicReference<>("");
        CompletableFuture cf3 = cf1.runAfterEither(cf2, () -> result.set("2 actions done"));

        assertEquals("2 actions done", result.get());
        assertEquals(null, cf3.get());
    }

    @Test
    public void testCF_runAfterEither_SecondExceptionally() throws ExecutionException, InterruptedException {
        CompletableFuture cf1 = CompletableFuture.supplyAsync("Zero"::toString);
        CompletableFuture cf2 = CompletableFuture.supplyAsync(() -> {throw new RuntimeException("Some exception");});

        Thread.sleep(1000);
        AtomicReference<String> result = new AtomicReference<>("");
        CompletableFuture cf3 = cf1.runAfterEither(cf2, () -> result.set("2 actions done"));

        assertEquals("2 actions done", result.get());
        assertEquals(null, cf3.get());
    }

    @Test(expected = RuntimeException.class)
    public void testCF_runAfterEither_FirstExceptionally() throws Throwable {
        CompletableFuture cf1 = CompletableFuture.supplyAsync(() -> {throw new RuntimeException("Some exception");});
        CompletableFuture cf2 = CompletableFuture.supplyAsync("One"::toString);

        Thread.sleep(1000);
        AtomicReference<String> result = new AtomicReference<>("");
        CompletableFuture cf3 = cf1.runAfterEither(cf2, () -> result.set("2 actions done"));

        assertEquals("", result.get());

        try {
            cf3.get();
        }catch (ExecutionException ex) {
            assertEquals("Some exception", ex.getCause().getMessage());
            throw ex.getCause();
        }
    }

    @Test
    public void testCF_applyToEither() throws ExecutionException, InterruptedException {
        CompletableFuture cf1 = CompletableFuture.supplyAsync("Zero"::toString);
        CompletableFuture cf2 = CompletableFuture.supplyAsync("One"::toString);

        Thread.sleep(1000);
        CompletableFuture cf3 = cf1.applyToEither(cf2, Function.identity());

        assertEquals("Zero", cf3.get());
    }

    @Test(expected = RuntimeException.class)
    public void testCF_applyToEither_FirstExceptionally() throws Throwable {
        CompletableFuture cf1 = CompletableFuture.supplyAsync(() -> {throw new RuntimeException("Some exception");});
        CompletableFuture cf2 = CompletableFuture.supplyAsync("One"::toString);

        Thread.sleep(1000);
        CompletableFuture cf3 = cf1.applyToEither(cf2, Function.identity());

        try {
            cf3.get();
        }catch (ExecutionException ex) {
            assertEquals("Some exception", ex.getCause().getMessage());
            throw ex.getCause();
        }
    }

    @Test
    public void testCF_applyToEither_SecondExceptionally() throws ExecutionException, InterruptedException {
        CompletableFuture cf1 = CompletableFuture.supplyAsync("Zero"::toString);
        CompletableFuture cf2 = CompletableFuture.supplyAsync(() -> {throw new RuntimeException("Some exception");});

        Thread.sleep(1000);
        CompletableFuture cf3 = cf1.applyToEither(cf2, Function.identity());

        assertEquals("Zero", cf3.get());
    }

    @Test(expected = IllegalStateException.class)
    public void testCF_applyToEither_FunctionExceptionally() throws Throwable {
        CompletableFuture cf1 = CompletableFuture.supplyAsync("Zero"::toString);
        CompletableFuture cf2 = CompletableFuture.supplyAsync("One"::toString);

        Thread.sleep(1000);
        CompletableFuture cf3 = cf1.applyToEither(cf2, (x) -> {throw new IllegalStateException("Function exception");});

        try {
            cf3.get();
        }catch (ExecutionException ex) {
            assertEquals("Function exception", ex.getCause().getMessage());
            throw ex.getCause();
        }
    }

    @Test
    public void testCF_apply() throws ExecutionException, InterruptedException {
        CompletableFuture cf1 = CompletableFuture.supplyAsync("Zero"::toString);

        CompletableFuture cf3 = cf1.thenApply((r) -> r + "Two");

        assertEquals("ZeroTwo", cf3.get());
        assertEquals("Zero", cf1.get());
    }

    @Test(expected = TimeoutException.class)
    public void testCF_orTimeout() throws Throwable {
        CountDownLatch cdl = new CountDownLatch(1);

        CompletableFuture cf1 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            cdl.countDown();
            return "Zero";
        });

        CompletableFuture completableFuture = cf1.orTimeout(1000, TimeUnit.MILLISECONDS);
        cdl.await();

        try {
            completableFuture.get();
        } catch (ExecutionException ex) {
            throw ex.getCause();
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCF_minimalCompletionStage() throws Throwable {
        CompletableFuture cf1 = CompletableFuture.supplyAsync("Zero"::toString);

        CompletionStage minCf2 = cf1.minimalCompletionStage();
        AtomicReference<String> result = new AtomicReference<>("");
        minCf2.whenComplete((res, ex) -> result.set((String)res));

        assertEquals("Zero", result.get());
        ((CompletableFuture)minCf2).get();
    }

    @Test
    public void testCF_thenCombine() throws Throwable {
        CompletableFuture<String> cf1 = CompletableFuture.supplyAsync("Zero"::toString);
        CompletableFuture<String> cf2 = CompletableFuture.supplyAsync("One"::toString);

        CompletableFuture<String> cf3 = cf1.thenCombine(cf2, (r1, r2) -> r1 + r2);

        assertEquals("ZeroOne", cf3.get());
    }
}
