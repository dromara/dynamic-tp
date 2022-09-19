package com.dtp.core.context;

import com.dtp.common.dto.AlarmInfo;
import com.dtp.common.dto.ExecutorWrapper;
import com.dtp.common.dto.NotifyItem;
import com.dtp.common.em.NotifyTypeEnum;
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

    public AlarmCtx(ExecutorWrapper wrapper, NotifyItem notifyItem, NotifyTypeEnum notifyType) {
        super(wrapper, notifyItem, notifyType);
    }
}
