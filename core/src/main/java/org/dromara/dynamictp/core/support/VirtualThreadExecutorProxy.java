package org.dromara.dynamictp.core.support;

import org.dromara.dynamictp.core.aware.AwareManager;
import org.dromara.dynamictp.core.aware.TaskEnhanceAware;
import org.dromara.dynamictp.core.support.task.wrapper.TaskWrapper;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * ClassName: VirtualThreadExecutorProxy
 * Package: org.dromara.dynamictp.core.support
 * Description:
 * VirtualThreadExecutor Proxy
 *
 * @Author CYC
 * @Create 2024/10/14 15:59
 * @Version 1.0
 */
public class VirtualThreadExecutorProxy implements TaskEnhanceAware, ExecutorService {

    private final ExecutorService THREAD_PER_TASK_EXECUTOR;

    /**
     * Task wrappers, do sth enhanced.
     */
    private List<TaskWrapper> taskWrappers;


    public VirtualThreadExecutorProxy(ExecutorService executor) {
        super();
        THREAD_PER_TASK_EXECUTOR = executor;
    }

    @Override
    public void execute(Runnable command) {
        command = getEnhancedTask(command);
        AwareManager.execute(this, command);
        THREAD_PER_TASK_EXECUTOR.execute(command);
    }


    @Override
    public List<TaskWrapper> getTaskWrappers() {
        return taskWrappers;
    }

    @Override
    public void setTaskWrappers(List<TaskWrapper> taskWrappers) {
        this.taskWrappers = taskWrappers;
    }


    @Override
    public void shutdown() {
        THREAD_PER_TASK_EXECUTOR.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return THREAD_PER_TASK_EXECUTOR.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return THREAD_PER_TASK_EXECUTOR.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return THREAD_PER_TASK_EXECUTOR.isTerminated();
    }

    @Override
    public boolean awaitTermination(long l, TimeUnit timeUnit) throws InterruptedException {
        return THREAD_PER_TASK_EXECUTOR.awaitTermination(l, timeUnit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        FutureTask<T> futureTask = new FutureTask<T>(callable);
        futureTask = (FutureTask<T>) getEnhancedTask(futureTask);
        AwareManager.execute(this, futureTask);
        return THREAD_PER_TASK_EXECUTOR.submit(callable);
    }


    @Override
    public <T> Future<T> submit(Runnable runnable, T t) {
        runnable = getEnhancedTask(runnable);
        AwareManager.execute(this, runnable);
        return THREAD_PER_TASK_EXECUTOR.submit(runnable, t);
    }

    @Override
    public Future<?> submit(Runnable runnable) {
        runnable = getEnhancedTask(runnable);
        AwareManager.execute(this, runnable);
        return THREAD_PER_TASK_EXECUTOR.submit(runnable);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection) throws InterruptedException {
        return THREAD_PER_TASK_EXECUTOR.invokeAll(collection);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection, long l, TimeUnit timeUnit) throws InterruptedException {
        return THREAD_PER_TASK_EXECUTOR.invokeAll(collection, l, timeUnit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> collection) throws InterruptedException, ExecutionException {
        return THREAD_PER_TASK_EXECUTOR.invokeAny(collection);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> collection, long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        return THREAD_PER_TASK_EXECUTOR.invokeAny(collection, l, timeUnit);
    }
}
