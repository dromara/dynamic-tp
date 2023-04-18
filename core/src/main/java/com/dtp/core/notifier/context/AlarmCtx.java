package com.dtp.core.notifier.context;

import com.dtp.common.entity.AlarmInfo;
import com.dtp.core.support.ExecutorWrapper;
import com.dtp.common.entity.NotifyItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AlarmCtx related
 *
 * @author yanhom
 * @since 1.0.8
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AlarmCtx extends BaseNotifyCtx {

    private AlarmInfo alarmInfo;

    public AlarmCtx(ExecutorWrapper wrapper, NotifyItem notifyItem) {
        super(wrapper, notifyItem);
    }
}
