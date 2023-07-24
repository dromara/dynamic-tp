package org.dromara.dynamictp.core.notifier.alarm;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.reject.RejectHandlerGetter;
import org.dromara.dynamictp.core.support.ExecutorWrapper;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ThreadPoolExecutor Proxy
 *
 * @author kyao
 * @since 1.1.4
 */
@Slf4j
public class ThreadPoolExecutorProxy extends ThreadPoolExecutor implements ThreadPoolAlarm {

    private ThreadPoolAlarmHelper helper;

    private ThreadPoolExecutorProxy(ThreadPoolExecutor executor) {
        super(executor.getCorePoolSize(), executor.getMaximumPoolSize(), executor.getKeepAliveTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS, executor.getQueue(), executor.getThreadFactory(), executor.getRejectedExecutionHandler());
    }

    public ThreadPoolExecutorProxy(ExecutorWrapper executorWrapper) {
        this((ThreadPoolExecutor) executorWrapper.getExecutor().getOriginal());
        helper = ThreadPoolAlarmHelper.of(executorWrapper);

        RejectedExecutionHandler handler = getRejectedExecutionHandler();
        setRejectedExecutionHandler(RejectHandlerGetter.getProxy(handler));
    }

    @Override
    public void execute(Runnable command) {
        executeAlarmEnhance(command);
        super.execute(command);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        beforeExecuteAlarmEnhance(t, r);
    }


    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        afterExecuteAlarmEnhance(r, t);
        super.afterExecute(r, t);
    }

    @Override
    public ThreadPoolAlarmHelper getThirdPartTpAlarmHelper() {
        return helper;
    }
}
