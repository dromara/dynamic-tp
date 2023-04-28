package org.dromara.dynamictp.core.notifier.context;

import org.dromara.dynamictp.common.entity.AlarmInfo;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.common.entity.NotifyItem;
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
