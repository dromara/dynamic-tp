package com.dtp.common.timer;

import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Schedules {@link TimerTask}s for one-time future execution in a background thread.
 * <p>
 * Copy from dubbo, see <a href="https://github.com/apache/dubbo/blob/3.2/dubbo-common/src/main/java/org/apache/dubbo/common/timer/Timer.java">here</a> for more details.
 * </p>
 */
public interface Timer {

    /**
     * Schedules the specified {@link TimerTask} for one-time execution after
     * the specified delay.
     * @param task the task to execute
     * @param delay the time from now to delay execution
     * @param unit the time unit of the delay parameter
     *
     * @return a handle which is associated with the specified task
     * @throws IllegalStateException      if this timer has been {@linkplain #stop() stopped} already
     * @throws RejectedExecutionException if the pending timeouts are too many and creating new timeout
     *                                    can cause instability in the system.
     */
    Timeout newTimeout(TimerTask task, long delay, TimeUnit unit);

    /**
     * Releases all resources acquired by this {@link Timer} and cancels all
     * tasks which were scheduled but not executed yet.
     *
     * @return the handles associated with the tasks which were canceled by
     * this method
     */
    Set<Timeout> stop();

    /**
     * the timer is stop
     *
     * @return true for stop
     */
    boolean isStop();
}
