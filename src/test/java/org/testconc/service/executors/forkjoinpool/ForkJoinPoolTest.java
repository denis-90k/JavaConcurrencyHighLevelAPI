package org.testconc.service.executors.forkjoinpool;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class ForkJoinPoolTest {

    @Test
    public void testForkJoinPool_asyncMode_false() throws InterruptedException {
        ForkJoinPool fjp = new ForkJoinPool(1, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null,
                false, 1, 1,
                1, null, 60, TimeUnit.SECONDS);

        List<String> elems = new ArrayList<>();

        CountDownLatch cdl = new CountDownLatch(10);
        fjp.execute(ForkJoinTask.adapt(new Runnable() {
            @Override
            public void run() {
                for (int t = 0; t < 10; t++) {
                    final int taskNumber = t;
                    fjp.execute(new Runnable() {
                        @Override
                        public void run() {
                            elems.add(String.valueOf(taskNumber));
                            cdl.countDown();
                        }
                    });
                }

            }
        }).fork());
        cdl.await();

        Assert.assertEquals("9876543210", String.join("", elems));
    }

    @Test
    public void testForkJoinPool_asyncMode_true() throws InterruptedException {
        ForkJoinPool fjp = new ForkJoinPool(1, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null,
                true, 1, 1,
                1, null, 60, TimeUnit.SECONDS);


        List<String> elems = new ArrayList<>();

        CountDownLatch cdl = new CountDownLatch(10);
        fjp.execute(ForkJoinTask.adapt(new Runnable() {
            @Override
            public void run() {
                for (int t = 0; t < 10; t++) {
                    final int taskNumber = t;
                    fjp.execute(new Runnable() {
                        @Override
                        public void run() {
                            elems.add(String.valueOf(taskNumber));
                            cdl.countDown();
                        }
                    });
                }

            }
        }).fork());
        cdl.await();

        Assert.assertEquals("0123456789", String.join("", elems));
    }

    @Test
    public void testForkJoinPool_awaitQuiescence() throws InterruptedException {
        ForkJoinPool fjp = new ForkJoinPool(1, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null,
                true, 1, 1,
                1, null, 60, TimeUnit.SECONDS);

        fjp.execute(new RecursiveAction() {
            @Override
            protected void compute() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Hello");
            }
        });

        assertFalse(fjp.isQuiescent());
        assertTrue(fjp.awaitQuiescence(1, TimeUnit.SECONDS));

    }

    @Test
    public void testForkJoinPool_invokeInOrder_Action() {
        List<String> words = List.of("one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve",
                "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen", "twenty", "twenty-one", "twenty-two", "twenty-three",
                "twenty-four", "twenty-five", "twenty-six", "twenty-seven", "twenty-eight", "twenty-nine", "thirteen", "thirty-one", "thirty-two",
                "thirty-three", "thirty-four", "thirty-five", "thirty-six", "thirty-seven", "thirty-eight", "thirty-nine", "forty", "forty-one",
                "forty-two", "forty-three", "forty-four", "forty-five");
        assertEquals(45, words.size());

        AtomicReference<String> res = new AtomicReference<>("");

        ForkJoinPool fjp = new ForkJoinPool(15, ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                null, true, 15, 15, 1, null, 60, TimeUnit.SECONDS);

        class RecursiveActionPrinter extends RecursiveAction {

            List<String> words;
            CountDownLatch predecessor;
            CountDownLatch current;

            RecursiveActionPrinter(List<String> words, CountDownLatch predecessor, CountDownLatch current) {
                this.words = words;
                this.predecessor = predecessor;
                this.current = current;
            }

            @Override
            protected void compute() {
                if (words.size() != 1) {
                    int k = 0;
                    CountDownLatch predCdl = null;
                    CountDownLatch currentCdl = new CountDownLatch(1);
                    while (words.size() != k) {
                        new RecursiveActionPrinter(List.of(words.get(k++)), predCdl, currentCdl).fork();
                        predCdl = currentCdl;
                        currentCdl = new CountDownLatch(1);
                    }
                } else {
                    try {
                        if (predecessor != null)
                            predecessor.await();
                        System.out.println(words.get(0));
                        res.setRelease(res.get().isEmpty() ? words.get(0) : res.get() + "_" + words.get(0));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } finally {
                        if (current != null)
                            current.countDown();
                    }
                }
            }
        }

        fjp.invoke(new RecursiveActionPrinter(words, null, null));
        fjp.awaitQuiescence(2, TimeUnit.SECONDS);
        assertEquals(String.join("_", words), res.get());
    }

    @Test
    public void testForkJoinPool_invokeInOrder_Task() {
        List<String> words = List.of("one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve",
                "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen", "twenty", "twenty-one", "twenty-two", "twenty-three",
                "twenty-four", "twenty-five", "twenty-six", "twenty-seven", "twenty-eight", "twenty-nine", "thirteen", "thirty-one", "thirty-two",
                "thirty-three", "thirty-four", "thirty-five", "thirty-six", "thirty-seven", "thirty-eight", "thirty-nine", "forty", "forty-one",
                "forty-two", "forty-three", "forty-four", "forty-five");
        assertEquals(45, words.size());
        List<String> expectedWords = List.of("ONE_TWO_THREE_FOUR_FIVE_SIX_SEVEN_EIGHT_NINE_TEN",
                "ELEVEN_TWELVE_THIRTEEN_FOURTEEN_FIFTEEN_SIXTEEN_SEVENTEEN_EIGHTEEN_NINETEEN_TWENTY",
                "TWENTY-ONE_TWENTY-TWO_TWENTY-THREE_TWENTY-FOUR_TWENTY-FIVE_TWENTY-SIX_TWENTY-SEVEN_TWENTY-EIGHT_TWENTY-NINE_THIRTEEN",
                "THIRTY-ONE_THIRTY-TWO_THIRTY-THREE_THIRTY-FOUR_THIRTY-FIVE_THIRTY-SIX_THIRTY-SEVEN_THIRTY-EIGHT_THIRTY-NINE_FORTY",
                "FORTY-ONE_FORTY-TWO_FORTY-THREE_FORTY-FOUR");

        ForkJoinPool fjp = new ForkJoinPool(15, ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                null, true, 15, 15, 1, null, 60, TimeUnit.SECONDS);

        class RecursiveTaskJoiner extends RecursiveTask<List<String>> {

            List<String> wordsToJoin;

            RecursiveTaskJoiner(List<String> words) {
                this.wordsToJoin = words;
            }

            @Override
            protected List<String> compute() {
                if (wordsToJoin.size() == 1) {
                    return List.of(wordsToJoin.get(0).toUpperCase());
                } else if (wordsToJoin.size() > 1 && wordsToJoin.size() <= 10) {
                    int k = 0;
                    ForkJoinTask<List<String>>[] ts = new ForkJoinTask[wordsToJoin.size()];
                    while (wordsToJoin.size() != k) {
                        ts[k] = new RecursiveTaskJoiner(List.of(wordsToJoin.get(k++))).fork();
                    }
                    String result = "";
                    for (int i = 0; i < ts.length; i++)
                        result = i == 0 ?
                                ts[i].join().get(0) :
                                result + "_" + ts[i].join().get(0);
                    return List.of(result);
                } else {
                    int k = 0;
                    List<String> result = new ArrayList<>();
                    ForkJoinTask<List<String>>[] ts = new ForkJoinTask[wordsToJoin.size() / 10 + 1];
                    while (k < wordsToJoin.size()) {
                        ts[k / 10] = new RecursiveTaskJoiner(wordsToJoin.subList(k, (k = k + 10) > wordsToJoin.size() ? wordsToJoin.size() - 1 : k)).fork();
                    }
                    for (int i = 0; i < ts.length; i++)
                        result.add(ts[i].join().get(0));
                    return result;
                }
            }
        }

        List<String> res = fjp.invoke(new RecursiveTaskJoiner(words));
        fjp.awaitQuiescence(2, TimeUnit.SECONDS);

        assertEquals(String.join(",", expectedWords), String.join(",", res));
    }

    @Test(expected = IllegalStateException.class)
    public void testForkJoinPool_invokeActions_Exception() {

        Thread.UncaughtExceptionHandler ueh = (t, e) -> {
            System.out.println("INSIDE UEH");
            if (t instanceof ForkJoinWorkerThread fjwt) {
                System.out.println("In Uncaught Exception");
                e.printStackTrace();
            }
        };
        ForkJoinPool fjp = new ForkJoinPool(15, ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                ueh, true, 15, 15, 1, null, 60, TimeUnit.SECONDS);

        AtomicInteger count = new AtomicInteger();
        class RecursiveActionExc extends RecursiveAction {

            List<String> words;


            RecursiveActionExc(List<String> words) {
                if (getPool() != null) {
                    Thread.currentThread().setUncaughtExceptionHandler(getPool().getUncaughtExceptionHandler());
                }
                this.words = words;
            }

            @Override
            protected void compute() {
                if (words.size() != 1) {
                    int k = 0;
                    ForkJoinTask[] ts = new ForkJoinTask[words.size()];
                    while (words.size() != k) {
                        ts[k] = new RecursiveActionExc(List.of(words.get(k++))).fork();
                    }
                    for (int i = 0; i < k; i++)
                        ts[i].join();
                } else {
                    if (count.addAndGet(1) >= 10)
                        throw new IllegalStateException("Wrong state");
                    System.out.println(count.get());
                }
            }
        }

        List<String> data = new ArrayList<>();
        for (int i = 0; i < 100; i++)
            data.add("");
        RecursiveActionExc task = new RecursiveActionExc(data);
        fjp.invoke(task);
    }

    @Test
    public void testForkJoinPool_ManagedBlocker_ResponseTime() throws InterruptedException {
        int maximumPoolSize = 64;
        ForkJoinPool fjp = new ForkJoinPool(16, ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                null, true, 16, maximumPoolSize, 16, null, 60, TimeUnit.SECONDS);

        int size = 64;
        List<Consumer<String>> rawTasks = new ArrayList<>();
        ArrayBlockingQueue<String> results = new ArrayBlockingQueue<>(size);
        CountDownLatch cdl = new CountDownLatch(size);

        for (int i = 0; i < size; i++) {
            rawTasks.add(s -> {
                results.add(s);
            });
        }

        class TimeConsumingAction extends RecursiveAction {

            List<Consumer<String>> tasks;

            TimeConsumingAction(List<Consumer<String>> tasks) {
                this.tasks = tasks;
            }

            @Override
            protected void compute() {

                if (tasks.size() > 1) {
                    for (int i = 1; i < tasks.size(); i++) {
                        new TimeConsumingAction(List.of(tasks.get(i))).fork();
                    }
                }

                try {

                    ForkJoinPool.ManagedBlocker blocker = new ForkJoinPool.ManagedBlocker() {

                        private boolean isDone = false;

                        @Override
                        public boolean block() throws InterruptedException {
                            Thread.sleep(5000);
                            isDone = true;
                            tasks.get(0).accept("Done");
                            cdl.countDown();

                            return true;
                        }

                        @Override
                        public boolean isReleasable() {
                            return isDone;
                        }
                    };

                    if(fjp.getPoolSize() == maximumPoolSize) {
                        blocker.block();
                    } else {
                        ForkJoinPool.managedBlock(blocker);
                    }

                } catch (Throwable e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }

        long start = System.nanoTime();
        fjp.invoke(new TimeConsumingAction(rawTasks));

        while (!cdl.await(1, TimeUnit.SECONDS)) {
            Thread.onSpinWait();
        }
        long diff1 = System.nanoTime() - start;
        System.out.println("Duration of execution with ManagedBlocker: " + TimeUnit.NANOSECONDS.toMillis(diff1));
        System.out.println("Stolen number of tasks - " + fjp.getStealCount());
        System.out.println(fjp.toString());

        for (int t = 0; t < rawTasks.size(); t++) {
            Assert.assertEquals("Done", results.poll());
        }
    }

    @Test
    public void testForkJoinPool_WithoutManagedBlocker_ResponseTime() throws InterruptedException {
        int maximumPoolSize = 64;
        ForkJoinPool fjp = new ForkJoinPool(16, ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                null, true, 16, maximumPoolSize, 16, null, 60, TimeUnit.SECONDS);

        int size = 64;
        List<Consumer<String>> rawTasks = new ArrayList<>();
        ArrayBlockingQueue<String> results = new ArrayBlockingQueue<>(size);
        CountDownLatch cdl = new CountDownLatch(size);

        for (int i = 0; i < size; i++) {
            rawTasks.add(s -> {
                results.add(s);
            });
        }

        class TimeConsumingAction extends RecursiveAction {

            List<Consumer<String>> tasks;

            TimeConsumingAction(List<Consumer<String>> tasks) {
                this.tasks = tasks;
            }

            @Override
            protected void compute() {

                if (tasks.size() > 1) {
                    for (int i = 1; i < tasks.size(); i++) {
                        new TimeConsumingAction(List.of(tasks.get(i))).fork();
                    }
                }

                try {

                    ForkJoinPool.ManagedBlocker blocker = new ForkJoinPool.ManagedBlocker() {

                        private boolean isDone = false;

                        @Override
                        public boolean block() throws InterruptedException {
                            Thread.sleep(5000);
                            isDone = true;
                            tasks.get(0).accept("Done");
                            cdl.countDown();

                            return true;
                        }

                        @Override
                        public boolean isReleasable() {
                            return isDone;
                        }
                    };

//                    if(fjp.getPoolSize() == maximumPoolSize) {
                        blocker.block();
//                    } else {
//                        ForkJoinPool.managedBlock(blocker);
//                    }

                } catch (Throwable e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }

        long start = System.nanoTime();
        fjp.invoke(new TimeConsumingAction(rawTasks));

        while (!cdl.await(1, TimeUnit.SECONDS)) {
            Thread.onSpinWait();
        }
        long diff1 = System.nanoTime() - start;
        System.out.println("Duration of execution with ManagedBlocker: " + TimeUnit.NANOSECONDS.toMillis(diff1));
        System.out.println("Stolen number of tasks - " + fjp.getStealCount());
        System.out.println(fjp.toString());

        for (int t = 0; t < rawTasks.size(); t++) {
            Assert.assertEquals("Done", results.poll());
        }
    }
}
