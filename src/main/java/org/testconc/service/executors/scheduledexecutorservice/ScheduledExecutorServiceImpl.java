package org.testconc.service.executors.scheduledexecutorservice;

import org.testconc.service.executors.executorservice.ExecutorServiceImpl;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ScheduledExecutorServiceImpl extends ExecutorServiceImpl implements ScheduledExecutorService {

    public List<ScheduledRunnableWorkerThread> runnableWorkers = new ArrayList<>();

    public DelayQueue<ScheduledFuture<?>> tasks = new DelayQueue<>();
    private BlockingQueue<Runnable> runningTasks = new LinkedBlockingQueue<>(); //tasks which are working and came from scheduleAtFixedRate or/and scheduleWithFixedDelay

    public AtomicInteger completedTasksCount = new AtomicInteger();

    @Override
    public ScheduledFuture<Void> schedule(Runnable command, long delay, TimeUnit unit) {
        if (isShutdown.get()) {
            return null;
        }
        ScheduledFutureImpl<Void> f = new ScheduledFutureImpl<Void>(command, null, delay < 0 ? 0 : delay, unit);
        numberOfTasks.getAndIncrement();
        tasks.add(f);
        if (runnableWorkers.size() < 5) {
            addWorker();
        }
        return f;
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        if (isShutdown.get()) {
            return null;
        }
        ScheduledFutureImpl<V> f = new ScheduledFutureImpl<>(callable, delay < 0 ? 0 : delay, unit);
        numberOfTasks.getAndIncrement();
        tasks.add(f);
        if (runnableWorkers.size() < 5) {
            addWorker();
        }
        return f;
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        if (isShutdown.get()) {
            return null;
        }
        if (period <= 0) {
            throw new IllegalArgumentException("period must be greater than zero");
        }
        ScheduledAtFixedRateFutureImpl<Void> f = new ScheduledAtFixedRateFutureImpl<>(command, initialDelay < 0 ? 0 : initialDelay, period, unit);
        numberOfTasks.getAndIncrement();
        tasks.add(f);
        if (runnableWorkers.size() < 5) {
            addWorker();
        }
        return f;
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        if (isShutdown.get()) {
            return null;
        }
        if (delay <= 0) {
            throw new IllegalArgumentException("delay must be greater than zero");
        }
        ScheduledWithFixedDelayFutureImpl<Void> f = new ScheduledWithFixedDelayFutureImpl<>(command, initialDelay < 0 ? 0 : initialDelay, delay, unit);
        numberOfTasks.getAndIncrement();
        tasks.add(f);
        if (runnableWorkers.size() < 5) {
            addWorker();
        }
        return f;
    }

    @Override
    public List<Runnable> shutdownNow() {
        if (isShutdown.get())
            return null;
        shutdown();
        isShutdownNow.set(true);

        var awaitingExecTasks = new ArrayList<ScheduledFuture>();
        Object[] at = tasks.toArray();
        for (var t : at) {
            if (t != null && tasks.remove(t)) {
                awaitingExecTasks.add((ScheduledFuture)t);
            }
        }

        numberOfTasks.addAndGet(awaitingExecTasks.size() * -1);
        new ArrayList(runningTasks).forEach(t -> awaitingExecTasks.add((ScheduledFuture) t));
        runningTasks.clear();
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
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        if (isShutdown.get())
            return null;
        if (tasks.isEmpty())
            throw new IllegalArgumentException("No tasks provided");
        final List<Future<T>> fts = new ArrayList<>(tasks.size());
        for (Callable c : tasks) {
            fts.add(new ScheduledFutureImpl(c, 0, TimeUnit.NANOSECONDS));
        }
        boolean isThreadWait = false;
        lock.lock();
        int tasksDone = 0;
        try {
            numberOfTasks.addAndGet(fts.size());
            fts.forEach(t -> this.tasks.add((ScheduledFuture) t));

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
                        if (this.tasks.remove(t)) {
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
            fts.add(new ScheduledFutureImpl(c, 0, TimeUnit.NANOSECONDS));
        }
        boolean isThreadWait = false;

        long nanos = unit.toNanos(timeout);
        long start = System.nanoTime();

        lock.lock();
        int tasksDone = 0;
        try {
            numberOfTasks.addAndGet(fts.size());
            fts.forEach(t -> this.tasks.add((ScheduledFuture) t));

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
                        if (this.tasks.remove(t)) {
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
        final List<ScheduledFuture<T>> fts = new ArrayList<>(tasks.size());
        for (Callable c : tasks) {
            fts.add(new ScheduledFutureImpl(c, 0, TimeUnit.NANOSECONDS));
        }
        boolean isThreadWait = false;
        lock.lock();
        try {
            numberOfTasks.addAndGet(fts.size());
            this.tasks.addAll(fts);

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
                for (ScheduledFuture t : fts) {
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
                    if (this.tasks.remove(t)) {
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
        final List<ScheduledFuture<T>> fts = new ArrayList<>(tasks.size());
        for (Callable c : tasks) {
            fts.add(new ScheduledFutureImpl(c, 0, TimeUnit.NANOSECONDS));
        }
        boolean isThreadWait = false;

        long nanos = unit.toNanos(timeout);
        long start = System.nanoTime();

        lock.lock();
        try {
            numberOfTasks.addAndGet(fts.size());
            this.tasks.addAll(fts);

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
                for (ScheduledFuture t : fts) {
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
            if(succDone == 0)
                throw new TimeoutException("No tasks successfully completed within timeout");

            return result;
        } finally {
            if (!isShutdownNow.get()) {
                int tasksWaiting = 0;
                for (Future t : fts) {
                    if (this.tasks.remove(t)) {
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
    public <T> Future<T> submit(Callable<T> task) {
        if (isShutdown.get())
            return null;
        ScheduledFutureImpl<T> ft = new ScheduledFutureImpl<>(task, 0, TimeUnit.NANOSECONDS);
        numberOfTasks.getAndIncrement();
        tasks.add(ft);
        if (runnableWorkers.size() < 5) {
            addWorker();
        }
        return ft;
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        if (isShutdown.get())
            return null;
        ScheduledFutureImpl<T> ft = new ScheduledFutureImpl<>(task, result, 0, TimeUnit.NANOSECONDS);
        numberOfTasks.getAndIncrement();
        tasks.add(ft);
        if (runnableWorkers.size() < 5) {
            addWorker();
        }
        return ft;
    }

    @Override
    public void execute(Runnable command) {
        if (isShutdown.get())
            return;
        numberOfTasks.getAndIncrement();
        tasks.add(new ScheduledFutureImpl<Void>(command, null, 0, TimeUnit.NANOSECONDS));

        if (runnableWorkers.size() < 5) {
            addWorker();
        }
    }

    private void addWorker() {
        ScheduledExecutorServiceImpl.ScheduledRunnableWorkerThread t = new ScheduledExecutorServiceImpl.ScheduledRunnableWorkerThread();
        t.setName("ScheduledExecutorServiceImpl-RunnableThread-" + runnableWorkers.size());
        runnableWorkers.add(t);
        t.start();
    }

    private static class ScheduledFutureImpl<V> extends FutureTask<V> implements ScheduledFuture<V> {

        private long startTime = System.nanoTime();

        private long desiredDelayNanos = 0;

        public ScheduledFutureImpl(Callable callable, long desiredDelay, TimeUnit unit) {
            super(callable);
            this.desiredDelayNanos = unit.toNanos(desiredDelay);
        }

        public ScheduledFutureImpl(Runnable runnable, V result, long desiredDelay, TimeUnit unit) {
            super(runnable, result);
            this.desiredDelayNanos = unit.toNanos(desiredDelay);
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long startInUnit = unit.convert(Duration.ofNanos(startTime));
            long currentInUnit = unit.convert(Duration.ofNanos(System.nanoTime()));
            long desiredDelayInUnit = unit.convert(Duration.ofNanos(desiredDelayNanos));
            return desiredDelayInUnit - currentInUnit + startInUnit;
        }

        @Override
        public int compareTo(Delayed o) {
            long thisDelay = this.getDelay(TimeUnit.NANOSECONDS);
            long thatDelay = o.getDelay(TimeUnit.NANOSECONDS);
            boolean isThisLessThat = thisDelay < thatDelay;
            boolean isThisGreaterThat = thisDelay > thatDelay;
            return isThisLessThat ? -1 : isThisGreaterThat ? 1 : 0;
        }


    }

    private static class ScheduledAtFixedRateFutureImpl<V> extends FutureTask<V> implements ScheduledFuture<V> {

        protected long startTime = System.nanoTime();

        protected long initialDelayNanos = 0;
        protected long periodNanos = 0;

        public ScheduledAtFixedRateFutureImpl(Runnable runnable, long initialDelay, long period, TimeUnit unit) {
            super(runnable, null);
            this.initialDelayNanos = unit.toNanos(initialDelay);
            this.periodNanos = unit.toNanos(period);
        }


        @Override
        public long getDelay(TimeUnit unit) {
            long startInUnit = unit.convert(Duration.ofNanos(startTime));
            long currentInUnit = unit.convert(Duration.ofNanos(System.nanoTime()));
            long desiredDelayInUnit = unit.convert(Duration.ofNanos(initialDelayNanos));
            return desiredDelayInUnit - currentInUnit + startInUnit;
        }

        @Override
        public int compareTo(Delayed o) {
            long thisDelay = this.getDelay(TimeUnit.NANOSECONDS);
            long thatDelay = o.getDelay(TimeUnit.NANOSECONDS);
            boolean isThisLessThat = thisDelay < thatDelay;
            boolean isThisGreaterThat = thisDelay > thatDelay;
            return isThisLessThat ? -1 : isThisGreaterThat ? 1 : 0;
        }

        protected void resetStartTime() {
            startTime = System.nanoTime();
        }

        public void updateSchedule() {
            initialDelayNanos = initialDelayNanos + periodNanos;
            long delay = this.getDelay(TimeUnit.NANOSECONDS);
            if (delay <= 0) {
                initialDelayNanos = initialDelayNanos + delay * -1 + TimeUnit.MILLISECONDS.toNanos(100);
            }
        }

        public void runWithReset() {
            super.runAndReset();
        }
    }

    private static class ScheduledWithFixedDelayFutureImpl<V> extends ScheduledAtFixedRateFutureImpl<V> {
        public ScheduledWithFixedDelayFutureImpl(Runnable runnable, long initialDelay, long delay, TimeUnit unit) {
            super(runnable, initialDelay, delay, unit);
        }

        @Override
        public void updateSchedule() {
            initialDelayNanos = periodNanos;
            resetStartTime();
        }
    }

    private class ScheduledRunnableWorkerThread extends Thread {

        @Override
        public void run() {
            int count = 0;
            while (true) {

                Runnable runTask = null;
                try {
                    runTask = (Runnable) tasks.poll(1, TimeUnit.SECONDS);

                    if (runTask != null && !((ScheduledFuture) runTask).isCancelled()) {
                        /*if (runTask instanceof ScheduledAtFixedRateFutureImpl<?> afr) {
                            runningTasks.add(runTask);
                            afr.runWithReset();
                        } else {
                            runTask.run();
                        }*/

                        count = 0;
                        if (runTask instanceof ScheduledAtFixedRateFutureImpl<?> fixedRateFuture) {
                            runningTasks.add(runTask);
                            fixedRateFuture.runWithReset();

                            if (isShutdown.get()) {
                                fixedRateFuture.cancel(false);
                                numberOfTasks.getAndDecrement();
                                completedTasksCount.getAndIncrement();
                            } else if (fixedRateFuture.isCancelled()) {
                                numberOfTasks.getAndDecrement();
                                completedTasksCount.getAndIncrement();
                            } else {
                                fixedRateFuture.updateSchedule();
                                tasks.add(fixedRateFuture);
                            }
                            runningTasks.remove(fixedRateFuture);
                        } else {
                            runTask.run();
                            completedTasksCount.getAndIncrement();
                            numberOfTasks.getAndDecrement();
                            wakeUpWaitingThreads();
                        }

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
                            runningTasks.remove(runTask);
                        }

                    }
                } catch (Exception e) {
                    if (runTask != null) {
                        numberOfTasks.getAndDecrement();
                        completedTasksCount.getAndIncrement();
                        runningTasks.remove(runTask);
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

                   /* if (isShutdownNow.get())
                        break;*/
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
