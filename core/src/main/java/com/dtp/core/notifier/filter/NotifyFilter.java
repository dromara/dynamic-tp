package com.dtp.core.notifier.filter;

import com.dtp.common.em.NotifyTypeEnum;
import com.dtp.common.pattern.filter.Filter;
import com.dtp.core.context.BaseNotifyCtx;

/**
 * NotifyFilter related
 *
 * @author yanhom
 * @since 1.0.8
 **/
public interface NotifyFilter extends Filter<BaseNotifyCtx> {

    /**
     * If supports this type.
     *
     * @param notifyType notifyType
     * @return true if supported, else false
     */
    default boolean supports(NotifyTypeEnum notifyType) {
        return true;
    }
}
