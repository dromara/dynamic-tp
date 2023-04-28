package org.dromara.dynamictp.core.notifier.chain.filter;

import org.dromara.dynamictp.common.em.NotifyTypeEnum;
import org.dromara.dynamictp.common.pattern.filter.Filter;
import org.dromara.dynamictp.core.notifier.context.BaseNotifyCtx;

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
