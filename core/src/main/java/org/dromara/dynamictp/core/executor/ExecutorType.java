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

package org.dromara.dynamictp.core.executor;

import lombok.AllArgsConstructor;
import org.dromara.dynamictp.core.executor.eager.EagerDtpExecutor;
import lombok.Getter;
import org.dromara.dynamictp.core.executor.priority.PriorityDtpExecutor;

/**
 * ExecutorType related
 *
 * @author yanhom
 * @since 1.0.4
 **/
@Getter
@AllArgsConstructor
public enum ExecutorType {

    /**
     * Common executor type.
     */
    COMMON("common", DtpExecutor.class),

    /**
     * Eager executor type.
     */
    EAGER("eager", EagerDtpExecutor.class),

    /**
     * Scheduled executor type.
     */
    SCHEDULED("scheduled", ScheduledDtpExecutor.class),

    /**
     * Ordered executor type.
     */
    ORDERED("ordered", OrderedDtpExecutor.class),

    /**
     * Priority executor type.
     */
    PRIORITY("priority", PriorityDtpExecutor.class);

    private final String name;

    private final Class<?> clazz;

    public static Class<?> getClass(String name) {
        for (ExecutorType type : ExecutorType.values()) {
            if (type.name.equals(name)) {
                return type.getClazz();
            }
        }
        return COMMON.getClazz();
    }

}
