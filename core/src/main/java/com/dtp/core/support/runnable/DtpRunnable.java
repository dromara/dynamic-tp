package com.dtp.core.support.runnable;

import com.dtp.common.util.TimeUtil;

/**
 * DtpRunnable related
 *
 * @author yanhom
 * @since 1.0.4
 */
public class DtpRunnable implements Runnable {

    private final Runnable runnable;

    private final Long submitTime;

    private Long startTime;

    private final String taskName;

    public DtpRunnable(Runnable runnable, String taskName) {
        this.runnable = runnable;
        submitTime = TimeUtil.currentTimeMillis();
        this.taskName = taskName;
    }

    @Override
    public void run() {
        runnable.run();
    }

    public Long getSubmitTime() {
        return submitTime;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public String getTaskName() {
        return taskName;
    }
}
