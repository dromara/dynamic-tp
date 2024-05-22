package org.dromara.dynamictp.agent;

public class MyAgentWrapperTwoRunnable implements Runnable {

    private Runnable r1;

    private Runnable r2;

    private Object busiObj;

    public MyAgentWrapperTwoRunnable(Runnable r1, Runnable r2, Object busiObj) {
        this.r1 = r1;
        this.r2 = r2;
        this.busiObj = busiObj;
    }

    @Override
    public void run() {
        System.out.println("before");
        try {
            r1.run();
            r2.run();
        } finally {
            System.out.println("after");
        }
    }
}
