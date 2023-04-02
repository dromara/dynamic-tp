package com.dtp.common.timer;

import java.util.concurrent.TimeUnit;

/**
 * A task which is executed after the delay specified with
 * {@link Timer#newTimeout(TimerTask, long, TimeUnit)} (TimerTask, long, TimeUnit)}.
 * <p>
 * Copy from dubbo, see <a href="https://github.com/apache/dubbo/blob/3.2/dubbo-common/src/main/java/org/apache/dubbo/common/timer/TimeTask.java">here</a> for more details.
 * </p>
 */
public interface TimerTask {

    /**
     * Executed after the delay specified with
     * {@link Timer#newTimeout(TimerTask, long, TimeUnit)}.
     *
     * @param timeout a handle which is associated with this task
     * @throws Exception if an error occurs
     */
    void run(Timeout timeout) throws Exception;
}
