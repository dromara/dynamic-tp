package com.dtp.core.support.selector;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * HashedExecutorSelector related
 *
 * @author yanhom
 * @since 1.1.3
 **/
public class HashedExecutorSelector implements ExecutorSelector {

    @Override
    public Executor select(List<Executor> executors, Object arg) {
        int value = arg.hashCode() % executors.size();
        if (value < 0) {
            value = Math.abs(value);
        }
        return executors.get(value);
    }
}
