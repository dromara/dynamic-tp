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
import java.util.Set;

import static java.util.stream.Collectors.toList;

/**
 * TaskWrapperHolder related
 *
 * @author yanhom
 * @since 1.0.4
 **/
public enum TaskWrappers {
    INSTANCE;

    private static final List<TaskWrapper> TASK_WRAPPERS = Lists.newArrayList();

    static {
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
}
