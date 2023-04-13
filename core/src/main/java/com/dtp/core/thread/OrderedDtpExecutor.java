package com.dtp.core.thread;

import com.dtp.core.support.Ordered;
import com.dtp.core.support.selector.ExecutorSelector;
import com.dtp.core.support.selector.HashedExecutorSelector;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

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
            ChildExecutor childExecutor = new ChildExecutor();
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

    private final class ChildExecutor implements Executor, Runnable {

        private final LinkedList<Runnable> tasks = new LinkedList<>();

        @Override
        public void execute(Runnable command) {
            boolean needExecution;
            synchronized (tasks) {
                needExecution = tasks.isEmpty();
                tasks.add(wrapTasks(command));
            }
            if (needExecution) {
                doUnorderedExecute(this);
            }
        }

        @Override
        public void run() {
            Thread thread = Thread.currentThread();
            for (;;) {
                final Runnable task;
                synchronized (tasks) {
                    task = tasks.getFirst();
                }

                boolean ran = false;
                onBeforeExecute(thread, task);
                try {
                    task.run();
                    ran = true;
                    onAfterExecute(task, null);
                } catch (RuntimeException e) {
                    if (!ran) {
                        onAfterExecute(task, e);
                    }
                    throw e;
                } finally {
                    synchronized (tasks) {
                        tasks.removeFirst();
                        if (tasks.isEmpty()) {
                            break;
                        }
                    }
                }
            }
        }
    }
}

