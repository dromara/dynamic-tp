package org.dromara.dynamictp.core;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.ThirdPartTpAlarm;
import org.dromara.dynamictp.core.ThirdPartTpAlarmHelper;
import org.dromara.dynamictp.core.reject.ThreadPartTpRejectedInvocationHandler;
import org.dromara.dynamictp.core.support.ExecutorWrapper;

import java.lang.reflect.Proxy;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author hanli
 * @date 2023年07月19日 4:06 PM
 */
@Slf4j
public class ThreadPoolExecutorProxy extends ThreadPoolExecutor implements ThirdPartTpAlarm {

    private ThirdPartTpAlarmHelper helper;

    private ThreadPoolExecutorProxy(ThreadPoolExecutor executor) {
        super(executor.getCorePoolSize(), executor.getMaximumPoolSize(), executor.getKeepAliveTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS, executor.getQueue(), executor.getThreadFactory(), executor.getRejectedExecutionHandler());
    }

    public ThreadPoolExecutorProxy(ExecutorWrapper executorWrapper) {
        this((ThreadPoolExecutor) executorWrapper.getExecutor().getOriginal());
        helper = new ThirdPartTpAlarmHelper(executorWrapper);

        RejectedExecutionHandler handler = getRejectedExecutionHandler();
        setRejectedExecutionHandler((RejectedExecutionHandler) Proxy
                .newProxyInstance(handler.getClass().getClassLoader(),
                        new Class[]{RejectedExecutionHandler.class},
                        new ThreadPartTpRejectedInvocationHandler(handler)));

        executorWrapper.setThirdPartTpAlarm(this);
    }

    @Override
    public void execute(Runnable command) {
        helper.startQueueTimeoutTask(command);
        super.execute(command);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        helper.cancelQueueTimeoutTask(r);
        helper.startRunTimeoutTask(t, r);
        log.info("beforeExecute增强");
    }


    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        helper.cancelRunTimeoutTask(r);
        System.out.println("afterExecute增强");
        super.afterExecute(r, t);
    }

    @Override
    public ThirdPartTpAlarmHelper getThirdPartTpAlarmHelper() {
        return helper;
    }
}
