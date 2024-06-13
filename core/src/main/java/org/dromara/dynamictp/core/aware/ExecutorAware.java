/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.dynamictp.core.aware;

import org.dromara.dynamictp.common.entity.TpExecutorProps;
import org.dromara.dynamictp.core.support.ExecutorWrapper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * ExecutorAware related
 *
 * @author kyao
 * @since 1.1.4
 */
public interface ExecutorAware extends DtpAware {

    /**
     * aware order
     *
     * @return order
     */
    int getOrder();

    /**
     * aware name
     *
     * @return name
     */
    String getName();

    /**
     * register Executor
     *
     * @param wrapper executor wrapper
     */
    default void register(ExecutorWrapper wrapper) { }

    /**
     * refresh props
     *
     * @param wrapper executor wrapper
     * @param props  executor props
     */
    default void refresh(ExecutorWrapper wrapper, @Nullable TpExecutorProps props) {
        // default no Operation
    }

    /**
     * remove Executor
     *
     * @param wrapper executor wrapper
     */
    default void remove(ExecutorWrapper wrapper) { }

    /**
     * enhance execute
     *
     * @param executor executor
     * @param r       runnable
     */
    default void execute(Executor executor, Runnable r) {
        // default no Operation
    }

    /**
     * enhance beforeExecute
     *
     * @param executor executor
     * @param t        thread
     * @param r        runnable
     */
    default void beforeExecute(Executor executor, Thread t, Runnable r) {
        // default no Operation
    }

    default Runnable beforeExecuteWrap(Executor executor, Thread t, Runnable r) {
        beforeExecute(executor, t, r);
        return r;
    }

    /**
     * enhance afterExecute
     *
     * @param executor executor
     * @param r        runnable
     * @param t        throwable
     */
    default void afterExecute(Executor executor, Runnable r, Throwable t) {
        // default no Operation
    }

    default Runnable afterExecuteWrap(Executor executor, Runnable r, Throwable t) {
        afterExecute(executor, r, t);
        return r;
    }

    /**
     * enhance shutdown
     *
     * @param executor executor
     */
    default void shutdown(Executor executor) {
        // default no Operation
    }

    /**
     * enhance shutdownNow
     *
     * @param executor executor
     * @param tasks tasks
     */
    default void shutdownNow(Executor executor, List<Runnable> tasks) {
        // default no Operation
    }

    /**
     * enhance terminated
     *
     * @param executor executor
     */
    default void terminated(Executor executor) {
        // default no Operation
    }

    /**
     * enhance before reject
     * @param r runnable
     * @param executor executor
     */
    default void beforeReject(Runnable r, Executor executor) {
        // default no Operation
    }

    default Runnable beforeRejectWrap(Runnable r, Executor executor) {
        beforeReject(r, executor);
        return r;
    }

    /**
     * enhance after reject
     * @param r runnable
     * @param executor executor
     */
    default void afterReject(Runnable r, Executor executor) {
        // default no Operation
    }

    default Runnable afterRejectWrap(Runnable r, Executor executor) {
        afterReject(r, executor);
        return r;
    }
}
