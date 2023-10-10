package org.dromara.dynamictp.adapter.dubbo.apache;

import org.apache.dubbo.common.threadpool.support.eager.EagerThreadPoolExecutor;
import org.apache.dubbo.common.threadpool.support.eager.TaskQueue;
import org.dromara.dynamictp.core.aware.AwareManager;
import org.dromara.dynamictp.core.aware.RejectHandlerAware;
import org.dromara.dynamictp.core.aware.TaskEnhanceAware;
import org.dromara.dynamictp.core.reject.RejectHandlerGetter;
import org.dromara.dynamictp.core.support.task.wrapper.TaskWrapper;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * EagerThreadPoolExecutorProxy related
 *
 * @author yanhom
 * @since 1.1.5
 */
public class EagerThreadPoolExecutorProxy extends EagerThreadPoolExecutor implements TaskEnhanceAware, RejectHandlerAware {

    /**
     * Task wrappers, do sth enhanced.
     */
    private List<TaskWrapper> taskWrappers;

    private final String rejectHandlerType;

    public EagerThreadPoolExecutorProxy(EagerThreadPoolExecutor executor) {
        super(executor.getCorePoolSize(), executor.getMaximumPoolSize(),
                executor.getKeepAliveTime(TimeUnit.MILLISECONDS),
                TimeUnit.MILLISECONDS, (TaskQueue<Runnable>) executor.getQueue(),
                executor.getThreadFactory(), executor.getRejectedExecutionHandler());
        this.rejectHandlerType = getRejectedExecutionHandler().getClass().getSimpleName();
        setRejectedExecutionHandler(RejectHandlerGetter.getProxy(getRejectedExecutionHandler()));
        ((TaskQueue<Runnable>) getQueue()).setExecutor(this);
        executor.shutdownNow();
    }

    @Override
    public void execute(Runnable command) {
        command = getEnhancedTask(command, taskWrappers);
        AwareManager.execute(this, command);
        super.execute(command);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        AwareManager.beforeExecute(this, t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        AwareManager.afterExecute(this, r, t);
    }

    @Override
    public void setTaskWrappers(List<TaskWrapper> taskWrappers) {
        this.taskWrappers = taskWrappers;
    }

    @Override
    public String getRejectHandlerType() {
        return rejectHandlerType;
    }
}
