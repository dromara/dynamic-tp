package org.dromara.dynamictp.agent;

public class MyAgentWrapperChild extends MyAgentWrapper {
    public MyAgentWrapperChild(MyAgentWrapper runnable, Object busiObj) {
        super(runnable, busiObj);
    }

    @Override
    public void run() {
        System.out.println("MyAgentWrapperChild");
    }
}
