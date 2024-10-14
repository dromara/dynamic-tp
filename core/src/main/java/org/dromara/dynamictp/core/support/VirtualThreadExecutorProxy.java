package org.dromara.dynamictp.core.support;

import org.dromara.dynamictp.core.aware.AwareManager;
import org.dromara.dynamictp.core.aware.RejectHandlerAware;
import org.dromara.dynamictp.core.aware.TaskEnhanceAware;
import org.dromara.dynamictp.core.support.task.wrapper.TaskWrapper;

import java.util.List;

/**
 * ClassName: VirtualThreadExecutorProxy
 * Package: org.dromara.dynamictp.core.support
 * Description:
 *
 * @Author CYC
 * @Create 2024/10/14 15:59
 * @Version 1.0
 */
public class VirtualThreadExecutorProxy extends VirtualThreadExecutorAdapter implements TaskEnhanceAware, RejectHandlerAware {

    /**
     * Task wrappers, do sth enhanced.
     */
    private List<TaskWrapper> taskWrappers;

    /**
     * Reject handler type.
     */
    private String rejectHandlerType;
    public VirtualThreadExecutorProxy() {
        super();
    }

    @Override
    public void execute(Runnable command) {
        command = getEnhancedTask(command);
        AwareManager.execute(this, command);
        super.execute(command);
    }


    @Override
    public List<TaskWrapper> getTaskWrappers() {
        return taskWrappers;
    }

    @Override
    public void setTaskWrappers(List<TaskWrapper> taskWrappers) {
        this.taskWrappers = taskWrappers;
    }

    @Override
    public String getRejectHandlerType() {
        return rejectHandlerType;
    }

    @Override
    public void setRejectHandlerType(String rejectHandlerType) {
        this.rejectHandlerType = rejectHandlerType;
    }
}
