package org.dromara.dynamictp.agent;

public class MyAgentTwoPathRunnableWrapper implements Runnable {

    private final MyAgentWrapper myAgentWrapper;

    private final Object busiObj;

    public MyAgentTwoPathRunnableWrapper(MyAgentWrapper myAgentWrapper, Object busiObj) {
        this.myAgentWrapper = myAgentWrapper;
        this.busiObj = busiObj;
    }


    @Override
    public void run() {
        System.out.println("before");
        try {
            myAgentWrapper.run();
        } finally {
            System.out.println("finally");
        }
    }
}
