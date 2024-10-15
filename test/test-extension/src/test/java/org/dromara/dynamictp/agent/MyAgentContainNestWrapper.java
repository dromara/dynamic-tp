package org.dromara.dynamictp.agent;

public class MyAgentContainNestWrapper implements Runnable {

    private MyAgentNestWrapper agentNestWrapper;

    public MyAgentContainNestWrapper(MyAgentNestWrapper agentNestWrapper) {
        this.agentNestWrapper = agentNestWrapper;
    }

    @Override
    public void run() {
        System.out.println("before");
        agentNestWrapper.run();
        System.out.println("after");
    }
}
