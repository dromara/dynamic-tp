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

import java.util.Optional;
import java.util.concurrent.Executor;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.DTP_EXECUTE_ENHANCED;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.TRUE_STR;

/**
 * PerformanceMonitorAware related
 *
 * @author kyao
 * @since 1.1.5
 */
public class PerformanceMonitorAware extends TaskStatAware {

    @Override
    public int getOrder() {
        return AwareTypeEnum.PERFORMANCE_MONITOR_AWARE.getOrder();
    }

    @Override
    public String getName() {
        return AwareTypeEnum.PERFORMANCE_MONITOR_AWARE.getName();
    }

    @Override
    public void execute(Executor executor, Runnable r) {
        if (TRUE_STR.equals(System.getProperty(DTP_EXECUTE_ENHANCED, TRUE_STR))) {
            Optional.ofNullable(statProviders.get(executor)).ifPresent(p -> p.startTask(r));
        }
    }

    @Override
    public void afterExecute(Executor executor, Runnable r, Throwable t) {
        Optional.ofNullable(statProviders.get(executor)).ifPresent(p -> p.completeTask(r));
    }

    @Override
    public void afterReject(Runnable r, Executor executor) {
        Optional.ofNullable(statProviders.get(executor)).ifPresent(p -> p.completeTask(r));
    }
}
