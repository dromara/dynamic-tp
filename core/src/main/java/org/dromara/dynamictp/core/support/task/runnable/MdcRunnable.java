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

package org.dromara.dynamictp.core.support.task.runnable;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.MDC;
import java.util.Map;
import java.util.Objects;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.TRACE_ID;

/**
 * MdcRunnable related
 *
 * @author yanhom
 * @since 1.0.8
 **/
public class MdcRunnable implements Runnable {

    private final Runnable runnable;

    private final Thread parentThread;

    /**
     * Saves the MDC value of the current thread
     */
    private final Map<String, String> parentMdc;

    public MdcRunnable(Runnable runnable) {
        this.runnable = runnable;
        this.parentMdc = MDC.getCopyOfContextMap();
        this.parentThread = Thread.currentThread();
    }

    public static MdcRunnable get(Runnable runnable) {
        return new MdcRunnable(runnable);
    }

    @Override
    public void run() {

        if (MapUtils.isEmpty(parentMdc) || Objects.equals(Thread.currentThread(), parentThread)) {
            runnable.run();
            return;
        }

        // Assign the MDC value of the parent thread to the child thread
        for (Map.Entry<String, String> entry : parentMdc.entrySet()) {
            MDC.put(entry.getKey(), entry.getValue());
        }
        try {
            // Execute the decorated thread run method
            runnable.run();
        } finally {
            // Remove MDC value at the end of execution
            for (Map.Entry<String, String> entry : parentMdc.entrySet()) {
                if (!TRACE_ID.equals(entry.getKey())) {
                    MDC.remove(entry.getKey());
                }
            }
        }
    }
}
