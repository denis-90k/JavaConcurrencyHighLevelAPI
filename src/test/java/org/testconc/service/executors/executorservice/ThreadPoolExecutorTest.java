package org.testconc.service.executors.executorservice;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ThreadPoolExecutorTest {

    @Test
    public void testSingleThreadPoolExecutor_execute() throws InterruptedException {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

        CountDownLatch cdl = new CountDownLatch(1);

        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                cdl.countDown();
            }
        });

        cdl.await();

        assertTrue(true);
    }

    @Test(expected = RejectedExecutionException.class)
    public void testSTPExecutor_MultExecute_ExceededTaskAndThreadLimit_AbortPolicy() throws InterruptedException {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(5));

        CountDownLatch cdl = new CountDownLatch(100);

        for (int i = 0; i < 100; i++) {
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    cdl.countDown();
                }
            });
        }


        cdl.await();

        assertTrue(true);
    }

    @Test
    public void testSTPExecutor_MultExecute_ExceededTaskAndThreadLimit_CallersRunsTaskPolicy() throws InterruptedException {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(5), new ThreadPoolExecutor.CallerRunsPolicy());

        CountDownLatch cdl = new CountDownLatch(100);

        for (int i = 0; i < 100; i++) {
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    cdl.countDown();
                }
            });
        }


        cdl.await();

        assertTrue(true);
    }

    @Test
    public void testSTPExecutor_MultExecute_ExceededTaskAndThreadLimit_DiscardTaskPolicy() throws InterruptedException {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(5), new ThreadPoolExecutor.DiscardPolicy());

        CountDownLatch cdl = new CountDownLatch(100);

        for (int i = 0; i < 100; i++) {
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    cdl.countDown();
                }
            });
        }

        assertTrue(!cdl.await(3, TimeUnit.SECONDS));
    }

    @Test
    public void testSTPExecutor_MultExecute_ExceededTaskAndThreadLimit_DiscardOldestPolicy() throws InterruptedException {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(5), new ThreadPoolExecutor.DiscardOldestPolicy());

        CountDownLatch cdl = new CountDownLatch(100);

        for (int i = 0; i < 100; i++) {
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    cdl.countDown();
                }
            });
        }

        assertTrue(!cdl.await(3, TimeUnit.SECONDS));
    }

    @Test
    public void testSTPExecutor_SubmitCallable() throws ExecutionException, InterruptedException {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        String helloWorld = "HelloWorld";
        Future<String> submit = threadPoolExecutor.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {

                return helloWorld;
            }
        });

        assertEquals(helloWorld, submit.get());
    }

    @Test
    public void testSTPExecutor_SubmitRunnableWithResult() throws ExecutionException, InterruptedException {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        String helloWorld = "HelloWorld";
        Future<String> submit = threadPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                assert true;
            }
        }, helloWorld);

        assertEquals(helloWorld, submit.get());
    }

    @Test
    public void testSTPExecutor_SubmitRunnable() throws ExecutionException, InterruptedException {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        Future<?> submit = threadPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                assert true;
            }
        });

        assertEquals(null, submit.get());
    }

    @Test
    public void testSTPExecutor_InvokeAll() throws ExecutionException, InterruptedException {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

        List<Callable<String>> tasks = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            tasks.add(new Callable() {
                @Override
                public Object call() throws Exception {
                    return null;
                }
            });
        }

        List<Future<String>> futures = threadPoolExecutor.invokeAll(tasks);
        for (Future<String> f : futures) {
            assertTrue(f.isDone());
        }
    }

    @Test
    public void testSTPExecutor_InvokeAllWithTimer() throws ExecutionException, InterruptedException {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

        List<Callable<String>> tasks = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            tasks.add(new Callable() {
                @Override
                public Object call() throws Exception {
                    Thread.sleep(1000);
                    return null;
                }
            });
        }

        List<Future<String>> futures = threadPoolExecutor.invokeAll(tasks, 1000, TimeUnit.MILLISECONDS);
        for (Future<String> f : futures) {
            assertTrue(f.isDone());
        }
    }

    @Test
    public void testSTPExecutor_InvokeAny() throws ExecutionException, InterruptedException {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

        List<Callable<String>> tasks = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            tasks.add(new Callable() {
                @Override
                public Object call() throws Exception {
                    return "HelloWorld";
                }
            });
        }

        String result = threadPoolExecutor.invokeAny(tasks);
        assertTrue(result.startsWith("HelloWorld"));
    }

    @Test
    public void testSTPExecutor_InvokeAnyTimeout() throws ExecutionException, InterruptedException, TimeoutException {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

        List<Callable<String>> tasks = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            tasks.add(new Callable() {
                @Override
                public Object call() throws Exception {
                    return "HelloWorld";
                }
            });
        }

        String result = threadPoolExecutor.invokeAny(tasks, 1, TimeUnit.SECONDS);
        assertTrue(result.startsWith("HelloWorld"));
    }

    @Test(expected = TimeoutException.class)
    public void testSTPExecutor_InvokeAnyTimeoutElapsed() throws ExecutionException, InterruptedException, TimeoutException {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

        List<Callable<String>> tasks = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            tasks.add(new Callable() {
                @Override
                public Object call() throws Exception {
                    Thread.sleep(1000);
                    return "HelloWorld";
                }
            });
        }

        String result = threadPoolExecutor.invokeAny(tasks, 1, TimeUnit.MILLISECONDS);
        assertTrue(result.startsWith("HelloWorld"));
    }

    @Test
    public void testFixedThreadPoolExecutor_shutdown() throws InterruptedException, ExecutionException {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(20, 20, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

        List<Callable<String>> tasks = new ArrayList<>();

        CountDownLatch cdl = new CountDownLatch(100);
        for (int i = 0; i < 100; i++) {
            tasks.add(new Callable() {
                @Override
                public Object call() throws Exception {
                    Thread.sleep(3000);
                    cdl.countDown();
                    return "HelloWorld";
                }
            });
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                threadPoolExecutor.shutdown();
            }
        }).start();
        List<Future<String>> futures = threadPoolExecutor.invokeAll(tasks);

        cdl.await();

        for (Future<String> f : futures) {
            Assert.assertEquals("HelloWorld", f.get());
            assertTrue(f.isDone());
            assertEquals(FutureTask.State.SUCCESS, f.state());
        }

    }

    @Test
    public void testFixedThreadPoolExecutor_shutdownNow() throws InterruptedException, ExecutionException {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(20, 20, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

        List<Callable<String>> tasks = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            tasks.add(new Callable() {
                @Override
                public Object call() throws Exception {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return "HelloWorld";
                }
            });
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                threadPoolExecutor.shutdownNow();
            }
        }).start();

        List<Future<String>> futures = threadPoolExecutor.invokeAll(tasks, 10000, TimeUnit.MILLISECONDS); //Never returns without timeout if shutDownNow because of worker thread not see main thread in time

        threadPoolExecutor.awaitTermination(5000, TimeUnit.MILLISECONDS);

    }

    @Test
    public void testFixedThreadPoolExecutor_zeroKeepAliveTime() throws NoSuchFieldException, IllegalAccessException, InterruptedException, ClassNotFoundException {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 5, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(15));
//        threadPoolExecutor.allowCoreThreadTimeOut(true);

        List<Callable<String>> tasks = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            tasks.add(new Callable() {
                @Override
                public Object call() throws Exception {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return "HelloWorld";
                }
            });
        }
        threadPoolExecutor.invokeAll(tasks);

        Thread.sleep(5000);

        tasks.clear();
        for (int i = 0; i < 20; i++) {
            tasks.add(new Callable() {
                @Override
                public Object call() throws Exception {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return "HelloWorld";
                }
            });
        }
        threadPoolExecutor.invokeAll(tasks);

        Field field = ThreadPoolExecutor.class.getDeclaredField("workers");
        field.setAccessible(true);

        HashSet workersf = (HashSet)field.get(threadPoolExecutor);

        for(Object t : workersf) {
            AbstractQueuedSynchronizer t1 = (AbstractQueuedSynchronizer) t;
            Field thread = Class.forName("java.util.concurrent.ThreadPoolExecutor$Worker").getDeclaredField("thread");
            thread.setAccessible(true);


            System.out.println(((Thread)thread.get(t1)).getName());
        }
        assertTrue(true);
    }
}
