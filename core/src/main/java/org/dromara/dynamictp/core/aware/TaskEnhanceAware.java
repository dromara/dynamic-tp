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

import org.apache.commons.collections4.CollectionUtils;
import org.dromara.dynamictp.core.support.task.runnable.DtpRunnable;
import org.dromara.dynamictp.core.support.task.runnable.NamedRunnable;
import org.dromara.dynamictp.core.support.task.wrapper.TaskWrapper;

import java.util.List;

/**
 * TaskEnhanceAware related
 *
 * @author yanhom
 * @since 1.1.4
 **/
public interface TaskEnhanceAware extends DtpAware {

    /**
     * Enhance task
     *
     * @param command      command
     * @param taskWrappers task wrappers
     * @return enhanced task
     */
    default Runnable getEnhancedTask(Runnable command, List<TaskWrapper> taskWrappers) {
        Runnable wrapRunnable = command;
        String taskName = (wrapRunnable instanceof NamedRunnable) ? ((NamedRunnable) wrapRunnable).getName() : null;
        if (CollectionUtils.isNotEmpty(taskWrappers)) {
            for (TaskWrapper t : taskWrappers) {
                wrapRunnable = t.wrap(wrapRunnable);
            }
        }
        return new DtpRunnable(command, wrapRunnable, taskName);
    }

    /**
     * Enhance task
     *
     * @param command command
     * @return enhanced task
     */
    default Runnable getEnhancedTask(Runnable command) {
        return getEnhancedTask(command, getTaskWrappers());
    }

    /**
     * Get task wrappers
     *
     * @return task wrappers
     */
    List<TaskWrapper> getTaskWrappers();

    /**
     * Set task wrappers
     *
     * @param taskWrappers task wrappers
     */
    void setTaskWrappers(List<TaskWrapper> taskWrappers);
}
