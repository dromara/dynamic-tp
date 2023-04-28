package org.dromara.dynamictp.core.support.selector;

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
        int idx = arg.hashCode() % executors.size();
        if (idx < 0) {
            idx = Math.abs(idx);
        }
        return executors.get(idx);
    }
}
