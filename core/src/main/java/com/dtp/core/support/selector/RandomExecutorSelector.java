package com.dtp.core.support.selector;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;

/**
 * RandomExecutorSelector related
 *
 * @author yanhom
 * @since 1.1.3
 **/
public class RandomExecutorSelector implements ExecutorSelector {

    private final Random random = new Random(System.currentTimeMillis());

    @Override
    public Executor select(List<Executor> executors, Object arg) {
        int value = random.nextInt(executors.size());
        return executors.get(value);
    }
}
