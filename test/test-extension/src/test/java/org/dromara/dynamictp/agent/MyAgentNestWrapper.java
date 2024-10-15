package org.dromara.dynamictp.agent;

import org.dromara.dynamictp.core.support.task.runnable.DtpRunnable;

public class MyAgentNestWrapper implements Runnable {

    private MyAgentNestWrapper myAgentNestWrapper = this;

    private DtpRunnable dtpRunnable;

    public MyAgentNestWrapper(DtpRunnable dtpRunnable) {
        this.dtpRunnable = dtpRunnable;
    }

    @Override
    public void run() {
        System.out.println("before");
        try {
            dtpRunnable.run();
        } finally {
            System.out.println("finally");
        }
    }
}
