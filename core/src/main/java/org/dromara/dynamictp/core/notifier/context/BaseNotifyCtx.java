package org.dromara.dynamictp.core.notifier.context;

import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import lombok.Data;

/**
 * BaseNotifyCtx related
 *
 * @author yanhom
 * @since 1.0.8
 */
@Data
public class BaseNotifyCtx {

    private ExecutorWrapper executorWrapper;

    private NotifyItem notifyItem;

    public BaseNotifyCtx() { }

    public BaseNotifyCtx(ExecutorWrapper wrapper, NotifyItem notifyItem) {
        this.executorWrapper = wrapper.capture();
        this.notifyItem = notifyItem;
    }

    public NotifyItemEnum getNotifyItemEnum() {
        return NotifyItemEnum.of(notifyItem.getType());
    }
}
