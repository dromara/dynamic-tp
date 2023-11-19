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

package org.dromara.dynamictp.adapter.common;

import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.entity.TpExecutorProps;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.aware.MetricsAware;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.Map;

/**
 * DtpAdapter related
 *
 * @author yanhom
 * @since 1.0.6
 */
public interface DtpAdapter extends MetricsAware {

    /**
     * Get specify thread pool executor wrapper.
     *
     * @return specify executor
     */
    default ExecutorWrapper getExecutorWrapper() {
        return null;
    }

    /**
     * Get executor wrappers.
     *
     * @return executors
     */
    default Map<String, ExecutorWrapper> getExecutorWrappers() {
        return Collections.emptyMap();
    }

    /**
     * Refresh the thread pool with specify properties.
     *
     * @param dtpProperties the targeted dtpProperties
     */
    void refresh(DtpProperties dtpProperties);

    /**
     * Check update params.
     *
     * @param props the targeted properties
     * @param log logger
     * @return true or false
     */
    default boolean containsInvalidParams(TpExecutorProps props, Logger log) {
        if (props.getCorePoolSize() < 0
                || props.getMaximumPoolSize() <= 0
                || props.getMaximumPoolSize() < props.getCorePoolSize()
                || props.getKeepAliveTime() < 0) {
            log.error("DynamicTp adapter refresh, invalid parameters exist, properties: {}", props);
            return true;
        }
        return false;
    }
}
