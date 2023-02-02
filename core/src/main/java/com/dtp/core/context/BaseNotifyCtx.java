package com.dtp.core.context;

import com.dtp.common.dto.ExecutorWrapper;
import com.dtp.common.dto.NotifyItem;
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
        this.executorWrapper = wrapper;
        this.notifyItem = notifyItem;
    }

    public NotifyItemEnum getNotifyItemEnum() {
        return NotifyItemEnum.of(notifyItem.getType());
    }
}
