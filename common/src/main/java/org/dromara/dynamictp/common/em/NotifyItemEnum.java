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

package org.dromara.dynamictp.common.em;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * NotifyItemEnum related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Getter
@AllArgsConstructor
public enum NotifyItemEnum {

    /**
     * Config change notify.
     */
    CHANGE("change", ""),

    /**
     * ThreadPool liveness notify.
     * liveness = activeCount / maximumPoolSize
     */
    LIVENESS("liveness", "%"),

    /**
     * Capacity threshold notify
     */
    CAPACITY("capacity", "%"),

    /**
     * Reject notify.
     */
    REJECT("reject", ""),

    /**
     * Task run timeout alarm.
     */
    RUN_TIMEOUT("run_timeout", ""),

    /**
     * Task queue wait timeout alarm.
     */
    QUEUE_TIMEOUT("queue_timeout", "");

    private final String value;

    private final String unit;

    public static NotifyItemEnum of(String value) {
        for (NotifyItemEnum notifyItem : NotifyItemEnum.values()) {
            if (notifyItem.value.equals(value)) {
                return notifyItem;
            }
        }
        return null;
    }
}
