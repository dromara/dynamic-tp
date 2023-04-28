package org.dromara.dynamictp.core.support.selector;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * ExecutorSelector related
 *
 * @author yanhom
 * @since 1.1.3
 **/
public interface ExecutorSelector {

    /**
     * select executor
     *
     * @param executors executors
     * @param arg arg
     * @return executor
     */
    Executor select(List<Executor> executors, Object arg);
}
