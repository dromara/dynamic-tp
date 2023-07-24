package org.dromara.dynamictp.core.notifier.alarm;

import org.dromara.dynamictp.common.spring.ApplicationContextHolder;
import org.dromara.dynamictp.common.timer.HashedWheelTimer;
import org.dromara.dynamictp.common.timer.Timeout;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.timer.QueueTimeoutTimerTask;
import org.dromara.dynamictp.core.timer.RunTimeoutTimerTask;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * alarm helper
 *
 * @author hanli
 * @date 2023年07月19日 6:31 PM
 */
public class ThreadPoolAlarmHelper {

    /**
     * executorWrapper
     */
    private final ExecutorWrapper executorWrapper;

    /**
     * Task execute timeout, unit (ms), just for statistics.
     */
    private long runTimeout = 0;

    /**
     * Task queue wait timeout, unit (ms), just for statistics.
     */
    private long queueTimeout = 0;

    /**
     * Total reject count.
     */
    private final LongAdder rejectCount = new LongAdder();

    /**
     * Count run timeout tasks.
     */
    private final LongAdder runTimeoutCount = new LongAdder();

    /**
     * Count queue wait timeout tasks.
     */
    private final LongAdder queueTimeoutCount = new LongAdder();

    /**
     * runTimeoutMap  key -> Runnable  value -> Timeout
     */
    private final Map<Runnable, Timeout> runTimeoutMap = new ConcurrentHashMap<>();

    /**
     * queueTimeoutMap  key -> Runnable  value -> Timeout
     */
    private final Map<Runnable, Timeout> queueTimeoutMap = new ConcurrentHashMap<>();

    private ThreadPoolAlarmHelper(ExecutorWrapper executorWrapper) {
        this.executorWrapper = executorWrapper;
        executorWrapper.setThreadPoolAlarmHelper(this);
    }

    public static ThreadPoolAlarmHelper of(ExecutorWrapper executorWrapper) {
        return new ThreadPoolAlarmHelper(executorWrapper);
    }

    public long getRejectedTaskCount() {
        return rejectCount.sum();
    }

    public long getRunTimeout() {
        return runTimeout;
    }

    public void setRunTimeout(long runTimeout) {
        this.runTimeout = runTimeout;
    }

    public long getQueueTimeout() {
        return queueTimeout;
    }

    public void setQueueTimeout(long queueTimeout) {
        this.queueTimeout = queueTimeout;
    }

    public void incRejectCount(int count) {
        rejectCount.add(count);
    }

    public long getRunTimeoutCount() {
        return runTimeoutCount.sum();
    }

    public void incRunTimeoutCount(int count) {
        runTimeoutCount.add(count);
    }

    public long getQueueTimeoutCount() {
        return queueTimeoutCount.sum();
    }

    public void incQueueTimeoutCount(int count) {
        queueTimeoutCount.add(count);
    }

    public void startQueueTimeoutTask(Runnable r) {
        if (queueTimeout <= 0) {
            return;
        }
        HashedWheelTimer hashedWheelTimer = ApplicationContextHolder.getBean(HashedWheelTimer.class);
        QueueTimeoutTimerTask queueTimeoutTimerTask = new QueueTimeoutTimerTask(executorWrapper, r);
        queueTimeoutMap.put(r, hashedWheelTimer.newTimeout(queueTimeoutTimerTask, queueTimeout, TimeUnit.MILLISECONDS));
    }

    public void cancelQueueTimeoutTask(Runnable r) {
        Timeout queueTimeoutTimer = queueTimeoutMap.get(r);
        if (queueTimeoutTimer != null) {
            queueTimeoutTimer.cancel();
            queueTimeoutMap.remove(r);
        }
    }

    public void startRunTimeoutTask(Thread t, Runnable r) {
        if (runTimeout <= 0) {
            return;
        }
        HashedWheelTimer hashedWheelTimer = ApplicationContextHolder.getBean(HashedWheelTimer.class);
        RunTimeoutTimerTask runTimeoutTimerTask = new RunTimeoutTimerTask(executorWrapper, r, t);
        runTimeoutMap.put(r, hashedWheelTimer.newTimeout(runTimeoutTimerTask, runTimeout, TimeUnit.MILLISECONDS));
    }

    public void cancelRunTimeoutTask(Runnable r) {
        Timeout runTimeoutTimer = runTimeoutMap.get(r);
        if (runTimeoutTimer != null) {
            runTimeoutTimer.cancel();
            runTimeoutMap.remove(r);
        }
    }

    public ExecutorWrapper getExecutorWrapper() {
        return executorWrapper;
    }
}
