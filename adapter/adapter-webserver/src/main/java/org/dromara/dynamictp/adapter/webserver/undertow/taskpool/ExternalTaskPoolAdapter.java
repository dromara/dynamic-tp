package org.dromara.dynamictp.adapter.webserver.undertow.taskpool;

import org.dromara.dynamictp.adapter.webserver.undertow.TaskPoolHandlerFactory;
import org.dromara.dynamictp.adapter.webserver.undertow.UndertowTaskPoolEnum;
import org.dromara.dynamictp.common.util.ReflectionUtil;
import org.dromara.dynamictp.core.support.ExecutorAdapter;
import lombok.val;

import static org.dromara.dynamictp.adapter.webserver.undertow.UndertowTaskPoolEnum.EXTERNAL_TASK_POOL;

/**
 * ExternalTaskPoolHandler related
 *
 * @author yanhom
 * @since 1.1.3
 */
public class ExternalTaskPoolAdapter implements TaskPoolAdapter {

    @Override
    public UndertowTaskPoolEnum taskPoolType() {
        return EXTERNAL_TASK_POOL;
    }

    @Override
    public ExecutorAdapter<?> adapt(Object obj) {
        String taskPoolClassName = obj.getClass().getSimpleName();
        val handler = TaskPoolHandlerFactory.getTaskPoolHandler(taskPoolClassName);
        Object executor = ReflectionUtil.getFieldValue(obj.getClass(), handler.taskPoolType().getInternalExecutor(), obj);
        return handler.adapt(executor);
    }
}
