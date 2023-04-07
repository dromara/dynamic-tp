package com.dtp.core.context;

import com.dtp.core.support.ExecutorWrapper;
import com.dtp.common.entity.NotifyItem;
import com.dtp.common.em.NotifyItemEnum;
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
        this.executorWrapper = wrapper.clone();
        this.notifyItem = notifyItem;
    }

    public NotifyItemEnum getNotifyItemEnum() {
        return NotifyItemEnum.of(notifyItem.getType());
    }
}
