package com.dtp.core.notify.manager;

import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.em.NotifyTypeEnum;
import com.dtp.common.pattern.filter.Filter;
import com.dtp.common.pattern.filter.InvokerChain;
import com.dtp.common.pattern.filter.InvokerChainFactory;
import com.dtp.core.context.BaseNotifyCtx;
import com.dtp.core.notify.filter.AlarmBaseFilter;
import com.dtp.core.notify.filter.NoticeBaseFilter;
import com.dtp.core.notify.filter.NotifyFilter;
import com.dtp.core.notify.invoker.AlarmInvoker;
import com.dtp.core.notify.invoker.NoticeInvoker;
import com.google.common.collect.Lists;
import lombok.val;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * NotifyFilterBuilder related
 *
 * @author yanhom
 * @since 1.0.0
 */
public class NotifyFilterBuilder {

    private NotifyFilterBuilder() { }

    public static InvokerChain<BaseNotifyCtx> getAlarmNoticeFilter() {
        val filters = ApplicationContextHolder.getBeansOfType(NotifyFilter.class);
        Collection<NotifyFilter> alarmNoticeFilters = Lists.newArrayList(filters.values());
        alarmNoticeFilters.add(new AlarmBaseFilter());
        alarmNoticeFilters = alarmNoticeFilters.stream()
                .filter(x -> x.supports(NotifyTypeEnum.ALARM))
                .sorted(Comparator.comparing(Filter::getOrder))
                .collect(Collectors.toList());
        return InvokerChainFactory.buildInvokerChain(new AlarmInvoker(),
                alarmNoticeFilters.toArray(new NotifyFilter[0]));
    }

    public static InvokerChain<BaseNotifyCtx> getCommonNoticeFilter() {
        val filters = ApplicationContextHolder.getBeansOfType(NotifyFilter.class);
        Collection<NotifyFilter> commonNoticeFilters = Lists.newArrayList(filters.values());
        commonNoticeFilters.add(new NoticeBaseFilter());
        commonNoticeFilters = commonNoticeFilters.stream()
                .filter(x -> x.supports(NotifyTypeEnum.COMMON))
                .sorted(Comparator.comparing(Filter::getOrder))
                .collect(Collectors.toList());
        return InvokerChainFactory.buildInvokerChain(new NoticeInvoker(),
                commonNoticeFilters.toArray(new NotifyFilter[0]));
    }
}
