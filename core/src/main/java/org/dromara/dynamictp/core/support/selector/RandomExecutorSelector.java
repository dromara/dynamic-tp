package org.dromara.dynamictp.core.support.selector;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadLocalRandom;

/**
 * RandomExecutorSelector related
 *
 * @author yanhom
 * @since 1.1.3
 **/
public class RandomExecutorSelector implements ExecutorSelector {

    @Override
    public Executor select(List<Executor> executors, Object arg) {
        return executors.get(ThreadLocalRandom.current().nextInt(executors.size()));
    }
}
