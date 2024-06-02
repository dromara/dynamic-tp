package org.dromara.dynamictp.agent;

public class MyAgentTwoPathRunnableChildWrapper implements Runnable {

    private MyAgentWrapperChild myAgentWrapperChild;

    private MyAgentWrapper myAgentWrapper;

    private Object busi;

    public MyAgentTwoPathRunnableChildWrapper(MyAgentWrapperChild myAgentWrapperChild, MyAgentWrapper myAgentWrapper, Object busi) {
        this.myAgentWrapperChild = myAgentWrapperChild;
        this.myAgentWrapper = myAgentWrapper;
        this.busi = busi;
    }

    @Override
    public void run() {
        System.out.println("MyAgentTwoPathRunnableChildWrapper");
    }
}
