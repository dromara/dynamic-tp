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

package org.dromara.dynamictp.core.support.task.wrapper;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.dromara.dynamictp.common.util.ExtensionServiceLoader;
import org.dromara.dynamictp.common.util.StringUtil;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * TaskWrapperHolder related
 *
 * @author yanhom
 * @since 1.0.4
 **/
public class TaskWrappers {

    private static final List<TaskWrapper> TASK_WRAPPERS = Lists.newArrayList();

    private TaskWrappers() {
        List<TaskWrapper> loadedWrappers = ExtensionServiceLoader.get(TaskWrapper.class);
        if (CollectionUtils.isNotEmpty(loadedWrappers)) {
            TASK_WRAPPERS.addAll(loadedWrappers);
        }

        TASK_WRAPPERS.add(new TtlTaskWrapper());
        TASK_WRAPPERS.add(new MdcTaskWrapper());
    }

    public List<TaskWrapper> getByNames(Set<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return Collections.emptyList();
        }

        return TASK_WRAPPERS.stream().filter(t -> StringUtil.containsIgnoreCase(t.name(), names)).collect(toList());
    }

    /**
     * Merge a wrapper that dtp created itself while taking over an executor (currently the
     * adapted {@code TaskDecorator} of the wrapped Spring executor) with the wrappers resolved
     * from {@code taskWrapperNames}.
     *
     * <p>A config refresh always overwrites the wrappers with {@link #getByNames(Set)}, which is
     * empty when nothing is configured. Without merging, the first config change would silently
     * and permanently drop the decorator dtp took over from the bean.</p>
     *
     * <p>The internal wrapper stays first, i.e. innermost, so configured wrappers (mdc, ttl ...)
     * establish their context around it.</p>
     *
     * @param internal   the wrapper dtp created itself, may be null
     * @param configured the wrappers resolved from the configuration, may be null or empty
     * @return the wrappers to apply
     */
    public static List<TaskWrapper> merge(TaskWrapper internal, List<TaskWrapper> configured) {
        if (Objects.isNull(internal)) {
            return configured;
        }
        List<TaskWrapper> merged = Lists.newArrayList(internal);
        if (CollectionUtils.isNotEmpty(configured)) {
            configured.stream().filter(t -> t != internal).forEach(merged::add);
        }
        return merged;
    }

    public static void register(TaskWrapper taskWrapper) {
        Set<String> names = TASK_WRAPPERS.stream().map(TaskWrapper::name).collect(Collectors.toSet());
        if (names.contains(taskWrapper.name())) {
            return;
        }
        TASK_WRAPPERS.add(taskWrapper);
    }

    public static TaskWrappers getInstance() {
        return TaskWrappersHolder.INSTANCE;
    }

    private static class TaskWrappersHolder {
        private static final TaskWrappers INSTANCE = new TaskWrappers();
    }
}
