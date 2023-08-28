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
import org.slf4j.Logger;
import javax.annotation.Nullable;
import java.util.concurrent.Executor;

/**
 * ExecutorAware related
 *
 * @author kyao
 * @Since 1.1.4
 */
public interface ExecutorAware {

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
     * registry and update
     *
     * @param wrapper
     * @param props
     */
    void updateInfo(ExecutorWrapper wrapper, @Nullable TpExecutorProps props);

    /**
     * remove Executor
     *
     * @param wrapper
     */
    void remove(ExecutorWrapper wrapper);

    /**
     * execute enhance
     *
     * @param executor
     * @param r
     */
    default void executeEnhance(Executor executor, Runnable r) {
        // default no Operation
    }

    /**
     * beforeExecute enhance
     *
     * @param executor
     * @param t
     * @param r
     */
    default void beforeExecuteEnhance(Executor executor, Thread t, Runnable r) {
        // default no Operation
    }

    /**
     * afterExecute enhance
     *
     * @param executor
     * @param r
     * @param t
     */
    default void afterExecuteEnhance(Executor executor, Runnable r, Throwable t) {
        // default no Operation
    }

    /**
     * reject enhance
     * @param r
     * @param executor
     * @param log
     */
    default void beforeReject(Runnable r, Executor executor, Logger log) {
        // default no Operation
    }


}
