package org.dromara.dynamictp.agent;

public class MyAgentWrapper implements Runnable {

    private Runnable runnable;

    private Object busiObj;

    public MyAgentWrapper(Runnable runnable, Object busiObj) {
        this.runnable = runnable;
        this.busiObj = busiObj;
    }

    @Override
    public void run() {
        System.out.println("before");
        try {
            runnable.run();
        } finally {
            System.out.println("finally");
        }
    }
}
