package com.dtp.core.support.wrapper;

import cn.hutool.core.collection.CollUtil;
import com.dtp.common.util.StringUtil;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import static java.util.stream.Collectors.toList;

/**
 * TaskWrapperHolder related
 *
 * @author: yanhom
 * @since 1.0.4
 **/
public class TaskWrappers {

    private static final List<TaskWrapper> TASK_WRAPPERS = Lists.newArrayList();

    private TaskWrappers() {
        ServiceLoader<TaskWrapper> loader = ServiceLoader.load(TaskWrapper.class);
        for (TaskWrapper taskWrapper : loader) {
            TASK_WRAPPERS.add(taskWrapper);
        }

        TASK_WRAPPERS.add(new TtlTaskWrapper());
        TASK_WRAPPERS.add(new MdcTaskWrapper());
    }

    public List<TaskWrapper> getByNames(Set<String> names) {
        if (CollUtil.isEmpty(names)) {
            return Collections.emptyList();
        }

        return TASK_WRAPPERS.stream().filter(t -> StringUtil.containsIgnoreCase(t.name(), names)).collect(toList());
    }

    public static TaskWrappers getInstance() {
        return TaskWrappersHolder.INSTANCE;
    }

    private static class TaskWrappersHolder {
        private static final TaskWrappers INSTANCE = new TaskWrappers();
    }
}
