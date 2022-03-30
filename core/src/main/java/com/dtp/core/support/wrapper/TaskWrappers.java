package com.dtp.core.support.wrapper;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * TaskWrapperHolder related
 *
 * @author: linyanhong@ihuman.com
 * @since 1.0.0
 **/
public class TaskWrappers {

    private static final List<TaskWrapper> TASK_WRAPPERS = Lists.newArrayList();

    private TaskWrappers() {
        ServiceLoader<TaskWrapper> loader = ServiceLoader.load(TaskWrapper.class);
        for (TaskWrapper taskWrapper : loader) {
            TASK_WRAPPERS.add(taskWrapper);
        }

        TASK_WRAPPERS.add(new TtlTaskWrapper());
    }

    public List<TaskWrapper> getByNames(List<String> names) {
        if (CollUtil.isEmpty(names)) {
            return Collections.emptyList();
        }
        return TASK_WRAPPERS.stream().filter(t -> names.contains(t.name())).collect(Collectors.toList());
    }

    public static TaskWrappers getInstance() {
        return TaskWrappers.TaskWrappersHolder.INSTANCE;
    }

    private static class TaskWrappersHolder {
        private static final TaskWrappers INSTANCE = new TaskWrappers();
    }
}
