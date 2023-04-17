package com.dtp.core.thread;

import com.dtp.core.support.Ordered;
import com.dtp.core.support.runnable.DtpRunnable;
import com.dtp.core.support.selector.ExecutorSelector;
import com.dtp.core.support.selector.HashedExecutorSelector;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;

/**
 * {@link OrderedDtpExecutor} can ensure that the delivered tasks are executed
 * according to the key and task submission order. It is applicable to scenarios
 * where the throughput is improved through parallel processing and the tasks
 * are run in a certain order.
 *
 * @author yanhom
 * @since 1.1.3
 */
@Slf4j
public class OrderedDtpExecutor extends DtpExecutor {

    private final ExecutorSelector selector = new HashedExecutorSelector();

    private final List<Executor> childExecutors = Lists.newArrayList();

    public OrderedDtpExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                Executors.defaultThreadFactory(), new AbortPolicy());
    }

    public OrderedDtpExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              ThreadFactory threadFactory) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                threadFactory, new AbortPolicy());
    }

    public OrderedDtpExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              RejectedExecutionHandler handler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                Executors.defaultThreadFactory(), handler);
    }

    public OrderedDtpExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              ThreadFactory threadFactory,
                              RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        for (int i = 0; i < corePoolSize; i++) {
//            ChildExecutor childExecutor = new ChildExecutor(workQueue.size() + workQueue.remainingCapacity());
            ChildExecutor childExecutor = new ChildExecutor(this);
            childExecutors.add(childExecutor);
        }
    }

    @Override
    public void execute(Runnable command) {
        if (command == null) {
            throw new NullPointerException();
        }
        if (command instanceof Ordered) {
            doOrderedExecute(command, ((Ordered) command).hashKey());
        } else {
            doUnorderedExecute(command);
        }
    }

    public void execute(Runnable command, Object hashKey) {
        if (Objects.nonNull(hashKey)) {
            doOrderedExecute(command, hashKey);
        } else {
            doUnorderedExecute(command);
        }
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        if (task == null) {
            throw new NullPointerException();
        }
        Object hashKey = task instanceof Ordered ? ((Ordered) task).hashKey() : null;
        RunnableFuture<T> futureTask = newTaskFor(task);
        execute(futureTask, hashKey);
        return futureTask;
    }

    public <T> Future<T> submit(Callable<T> task, Object hashKey) {
        if (task == null) {
            throw new NullPointerException();
        }
        RunnableFuture<T> futureTask = newTaskFor(task);
        execute(futureTask, hashKey);
        return futureTask;
    }

    private void doOrderedExecute(Runnable command, Object hashKey) {
        Executor executor = selector.select(childExecutors, hashKey);
        executor.execute(command);
    }

    private void doUnorderedExecute(Runnable command) {
        super.execute(command);
    }

    void onBeforeExecute(Thread t, Runnable r) {
        beforeExecute(t, r);
    }

    void onAfterExecute(Runnable r, Throwable t) {
        afterExecute(r, t);
    }

    @Override
    public long getCompletedTaskCount() {
        long count = 0;
        for (Executor executor : childExecutors) {
            count += ((ChildExecutor) executor).getCompletedTaskCount();
        }
        return super.getCompletedTaskCount() + count;
    }

    @Override
    public long getTaskCount() {
        long count = 0;
        for (Executor executor : childExecutors) {
            count += ((ChildExecutor) executor).getTaskCount();
        }
        return super.getTaskCount() + count;
    }

    @Override
    public long getRejectCount() {
        long count = 0;
        for (Executor executor : childExecutors) {
            count += ((ChildExecutor) executor).getRejectedTaskCount();
        }
        return super.getRejectCount() + count;
    }

    protected DtpRunnable getEnhancedTask(Runnable command) {
        DtpRunnable dtpRunnable = (DtpRunnable) wrapTasks(command);
        dtpRunnable.startQueueTimeoutTask(this);
        return dtpRunnable;
    }

    private final class ChildExecutor implements Executor, Runnable {

        /**
         * The parent executor.
         */
        private final Executor parentExecutor;

        /**
         * The task queue.
         */
        private final ConcurrentLinkedQueue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();

        /**
         * if running
         */
        private final AtomicBoolean running = new AtomicBoolean(false);

        /**
         * The runnable list.
         */
        private final List<Runnable> runnableList = new ArrayList<>();

        /**
         * The loop no work count.
         */
        private int noWorkCount = 0;

        /**
         *  The loop runnable count.
         */
        private int runCount = 0;

        /**
         * The completed task count.
         */
        private final LongAdder completedTaskCount = new LongAdder();

        /**
         * The rejected task count.
         */
        private final LongAdder rejectedTaskCount = new LongAdder();

        private ChildExecutor(Executor parentExecutor) {
            this.parentExecutor = parentExecutor;
        }

        @Override
        public void execute(Runnable command) {
            try {
                if (!taskQueue.add(getEnhancedTask(command))) {
                    rejectedTaskCount.increment();
                    throw new RejectedExecutionException("Task " + command.toString() + " rejected from " + this);
                }
            } catch (IllegalStateException ex) {
                rejectedTaskCount.increment();
                throw ex;
            }
            // start the thread if it is not running
            if (!running.get() && running.compareAndSet(false, true)) {
                parentExecutor.execute(this);
            }
        }

        @Override
        public void run() {
            boolean doneWork = getTasks();
            runCount++;
            if (!doneWork || runCount > 2 || runnableList.size() > 10) {
                runTask();
                runCount = 0;
            }
            if (doneWork) {
                noWorkCount = 0;
            } else {
                if (++noWorkCount > 5) {
                    running.set(false);
                    if (taskQueue.isEmpty() || !running.compareAndSet(false, true)) {
                        return;
                    }
                }
            }
            // if taskQueue is not empty, continue to execute
            // should sleep?
            parentExecutor.execute(this);
        }

        private void runTask() {
            for (Runnable task : runnableList) {
                Thread thread = Thread.currentThread();
                onBeforeExecute(thread, task);
                Throwable thrown = null;
                try {
                    task.run();
                } catch (RuntimeException x) {
                    thrown = x;
                    throw x;
                } finally {
                    onAfterExecute(task, thrown);
                    completedTaskCount.increment();
                }
            }
            runnableList.clear();
        }

        private boolean getTasks() {
            boolean doneWork = false;
            Runnable runnable;
            while ((runnable = taskQueue.poll()) != null) {
                runnableList.add(runnable);
                doneWork = true;
            }
            return doneWork;
        }

        public long getTaskCount() {
            return completedTaskCount.sum() + taskQueue.size();
        }

        public long getCompletedTaskCount() {
            return completedTaskCount.sum();
        }

        public long getRejectedTaskCount() {
            return rejectedTaskCount.sum();
        }

        @Override
        public String toString() {
            return super.toString() +
                    "[queue size = " + taskQueue.size() +
                    ", completed tasks = " + completedTaskCount +
                    ", rejected tasks = " + rejectedTaskCount +
                    ", running = " + running +
                    "]";
        }

    }

}

