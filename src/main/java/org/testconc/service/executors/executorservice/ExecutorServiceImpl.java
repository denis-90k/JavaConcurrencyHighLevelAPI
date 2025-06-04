package org.testconc.service.executors.executorservice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ExecutorServiceImpl implements ExecutorService {

    public List<RunnableWorkerThread> runnableWorkers = new ArrayList<>();

    public LinkedBlockingQueue<FutureTask> rTasks = new LinkedBlockingQueue();

    protected ReentrantLock lock = new ReentrantLock(true);

    protected Condition awaitTermiantionCond = lock.newCondition();

    protected Condition newTasksInvoked = lock.newCondition();

    protected AtomicBoolean isShutdown = new AtomicBoolean(false);
    protected AtomicBoolean isShutdownNow = new AtomicBoolean(false);

    protected final AtomicInteger waitingThreadCount = new AtomicInteger();
    public final AtomicInteger numberOfTasks = new AtomicInteger();

    @Override
    public void shutdown() {
        isShutdown.set(true);
    }

    @Override
    public List<Runnable> shutdownNow() {
        if (isShutdown.get())
            return null;
        shutdown();
        isShutdownNow.set(true);

        var awaitingExecTasks = new ArrayList<ScheduledFuture>();
        Object[] at = rTasks.toArray();
        for (var t : at) {
            if (t != null && rTasks.remove(t)) {
                awaitingExecTasks.add((ScheduledFuture) t);
            }
        }

        numberOfTasks.addAndGet(awaitingExecTasks.size() * -1);
        awaitingExecTasks.forEach(t -> t.cancel(true));

        runnableWorkers.forEach(Thread::interrupt);

        lock.lock();
        try {
            newTasksInvoked.signalAll();
        } finally {
            lock.unlock();
        }
        return (List) awaitingExecTasks;
    }

    @Override
    public boolean isShutdown() {
        return isShutdown.get();
    }

    @Override
    public boolean isTerminated() {
        return isShutdown.get() && numberOfTasks.get() == 0;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        if (isTerminated())
            return true;
        shutdown();

        lock.lock();
        try {
            awaitTermiantionCond.await(timeout, unit);
        } finally {
            lock.unlock();
        }

        return isTerminated();
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        if (isShutdown.get())
            return null;
        FutureTask<T> ft = new FutureTask<>(task);
        numberOfTasks.getAndIncrement();
        rTasks.add(ft);
        if (runnableWorkers.size() < 5) {
            addWorker();
        }
        return ft;
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        if (isShutdown.get())
            return null;
        FutureTask<T> ft = new FutureTask<>(task, result);
        numberOfTasks.getAndIncrement();
        rTasks.add(ft);
        if (runnableWorkers.size() < 5) {
            addWorker();
        }
        return ft;
    }

    @Override
    public Future<Void> submit(Runnable task) {
        return submit(task, null);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        if (isShutdown.get())
            return null;
        if (tasks.isEmpty())
            throw new IllegalArgumentException("No tasks provided");
        final List<Future<T>> fts = new ArrayList<>(tasks.size());
        for (Callable c : tasks) {
            fts.add(new FutureTask(c));
        }
        boolean isThreadWait = false;
        lock.lock();
        int tasksDone = 0;
        try {
            numberOfTasks.addAndGet(fts.size());
            fts.forEach(t -> this.rTasks.add((FutureTask) t));

            while (runnableWorkers.size() < 5) {
                addWorker();
            }

            waitingThreadCount.getAndIncrement();
            isThreadWait = true;


            while (fts.size() != tasksDone) {
                newTasksInvoked.await();
                tasksDone = 0;
                for (Future t : fts) {
                    if (t.isDone()) {
                        tasksDone++;
                    }
                }
            }


            return fts;
        } finally {
            if (!isShutdownNow.get()) {
                if (fts.size() != tasksDone) {
                    int tasksWaiting = 0;
                    for (Future t : fts) {
                        if (this.rTasks.remove(t)) {
                            tasksWaiting++;
                            t.cancel(true);
                        }
                    }
                    numberOfTasks.addAndGet(tasksWaiting * -1);
                }
            }
            if (isThreadWait)
                waitingThreadCount.getAndDecrement();
            lock.unlock();
        }

    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        if (isShutdown.get())
            return null;
        if (tasks.isEmpty())
            throw new IllegalArgumentException("No tasks provided");
        final List<Future<T>> fts = new ArrayList<>(tasks.size());
        for (Callable c : tasks) {
            fts.add(new FutureTask<>(c));
        }
        boolean isThreadWait = false;

        long nanos = unit.toNanos(timeout);
        long start = System.nanoTime();

        lock.lock();
        int tasksDone = 0;
        try {
            numberOfTasks.addAndGet(fts.size());
            fts.forEach(t -> this.rTasks.add((FutureTask) t));

            while (runnableWorkers.size() < 5) {
                addWorker();
            }

            waitingThreadCount.getAndIncrement();
            isThreadWait = true;

            boolean retWait = true;
            while (fts.size() != tasksDone && retWait) {
                retWait = newTasksInvoked.await(nanos - System.nanoTime() + start, TimeUnit.NANOSECONDS);
                tasksDone = 0;
                for (Future t : fts) {
                    if (t.isDone()) {
                        tasksDone++;
                    }
                }
            }

            return fts;
        } finally {
            if (!isShutdownNow.get()) {
                if (fts.size() != tasksDone) {
                    int tasksWaiting = 0;
                    for (Future t : fts) {
                        if (this.rTasks.remove(t)) {
                            tasksWaiting++;
                            t.cancel(true);
                        }
                    }
                    numberOfTasks.addAndGet(tasksWaiting * -1);
                }
            }
            if (isThreadWait)
                waitingThreadCount.getAndDecrement();
            lock.unlock();
        }
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        if (isShutdown.get())
            return null;
        if (tasks.isEmpty())
            throw new IllegalArgumentException("No tasks provided");
        final List<Future<T>> fts = new ArrayList<>(tasks.size());
        for (Callable c : tasks) {
            fts.add(new FutureTask<>(c));
        }
        boolean isThreadWait = false;
        lock.lock();
        try {
            numberOfTasks.addAndGet(fts.size());
            fts.forEach(t -> this.rTasks.add((FutureTask) t));

            while (runnableWorkers.size() < 5) {
                addWorker();
            }

            T result = null;
            int succDone = 0;

            waitingThreadCount.getAndIncrement();
            isThreadWait = true;
            while (succDone == 0) {
                newTasksInvoked.await();
                int tasksDone = 0;
                for (Future t : fts) {
                    if (t.isDone()) {
                        if (t.state() == Future.State.SUCCESS) {
                            result = (T) t.get();
                            succDone++;
                            break;
                        } else {
                            tasksDone++;
                        }

                        if (tasksDone == fts.size())
                            throw new ExecutionException("No tasks successfully completed", new IllegalStateException(""));
                    }
                }
            }

            return result;
        } finally {
            if (!isShutdownNow.get()) {
                int tasksWaiting = 0;
                for (Future t : fts) {
                    if (this.rTasks.remove(t)) {
                        tasksWaiting++;
                    }
                }
                numberOfTasks.addAndGet(tasksWaiting * -1);
                fts.stream().forEach(ft -> ft.cancel(true));
            }
            if (isThreadWait)
                waitingThreadCount.getAndDecrement();

            lock.unlock();
        }
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (isShutdown.get())
            return null;
        if (tasks.isEmpty())
            throw new IllegalArgumentException("No tasks provided");
        final List<Future<T>> fts = new ArrayList<>(tasks.size());
        for (Callable c : tasks) {
            fts.add(new FutureTask(c));
        }
        boolean isThreadWait = false;

        long nanos = unit.toNanos(timeout);
        long start = System.nanoTime();

        lock.lock();
        try {
            numberOfTasks.addAndGet(fts.size());
            fts.forEach(t -> this.rTasks.add((FutureTask) t));

            while (runnableWorkers.size() < 5) {
                addWorker();
            }

            T result = null;
            int succDone = 0;

            waitingThreadCount.getAndIncrement();
            isThreadWait = true;

            boolean retWait = true;
            while (succDone == 0 && retWait) {
                long time = nanos - System.nanoTime() + start;
                retWait = newTasksInvoked.await(time, TimeUnit.NANOSECONDS);
                int tasksDone = 0;
                for (Future t : fts) {
                    if (t.isDone()) {
                        if (t.state() == Future.State.SUCCESS) {
                            result = (T) t.get();
                            succDone++;
                            break;
                        } else {
                            tasksDone++;
                        }

                        if (tasksDone == fts.size())
                            throw new ExecutionException("No tasks successfully completed", new IllegalStateException(""));
                    }
                }
            }
            if (succDone == 0)
                throw new TimeoutException("No tasks successfully completed within timeout");

            return result;
        } finally {
            if (!isShutdownNow.get()) {
                int tasksWaiting = 0;
                for (Future t : fts) {
                    if (this.rTasks.remove(t)) {
                        tasksWaiting++;
                    }
                }
                numberOfTasks.addAndGet(tasksWaiting * -1);
                fts.stream().forEach(ft -> ft.cancel(true));
            }
            if (isThreadWait)
                waitingThreadCount.getAndDecrement();

            lock.unlock();
        }

    }

    @Override
    public void execute(Runnable command) {
        if (isShutdown.get())
            return;
        numberOfTasks.getAndIncrement();
        rTasks.add(new FutureTask<Void>(command, null));

        if (runnableWorkers.size() < 5) {
            addWorker();
        }
    }

    private void addWorker() {
        RunnableWorkerThread t = new RunnableWorkerThread();
        t.setName("ExecutoServiceImpl-RunnableThread-" + runnableWorkers.size());
        runnableWorkers.add(t);
        t.start();

    }

    private class RunnableWorkerThread extends Thread {

        @Override
        public void run() {
            int count = 0;
            while (true) {

                Runnable runTask = null;
                try {
                    runTask = rTasks.poll(1, TimeUnit.SECONDS);

                    if (runTask != null && !((Future) runTask).isCancelled()) {
                        runTask.run();
                        count = 0;
                        numberOfTasks.getAndDecrement();
                        wakeUpWaitingThreads();
                    } else {
                        if (numberOfTasks.get() == 0) {
                            lock.lock();
                            try {
                                awaitTermiantionCond.signal();
                                newTasksInvoked.signalAll();
                            } finally {
                                lock.unlock();
                            }
                            if (count >= 5) { //it is needed because workers can be too fast that main thread doesn't jump into wait but worker exits.
                                break;
                            }
                            count++;
                        }
                        if (runTask != null) {
                            numberOfTasks.getAndDecrement();
                        }
                    }
                } catch (Exception e) {
                    if (runTask != null) {
                        numberOfTasks.getAndDecrement();
                        wakeUpWaitingThreads();
                    }

                    if (numberOfTasks.get() == 0) {
                        lock.lock();
                        try {
                            awaitTermiantionCond.signal();
                        } finally {
                            lock.unlock();
                        }
                        break;
                    }
                }

            }
            runnableWorkers.remove(this);
        }

        private void wakeUpWaitingThreads() {
            if (waitingThreadCount.get() > 0) {
                if (lock.tryLock()) {
                    try {
                        newTasksInvoked.signalAll();
                    } finally {
                        lock.unlock();
                    }

                }
            }
        }
    }
}
