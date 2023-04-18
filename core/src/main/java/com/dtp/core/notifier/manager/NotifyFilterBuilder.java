package com.dtp.core.notifier.manager;

import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.em.NotifyTypeEnum;
import com.dtp.common.pattern.filter.Filter;
import com.dtp.common.pattern.filter.InvokerChain;
import com.dtp.common.pattern.filter.InvokerChainFactory;
import com.dtp.core.notifier.context.BaseNotifyCtx;
import com.dtp.core.notifier.chain.filter.AlarmBaseFilter;
import com.dtp.core.notifier.chain.filter.NoticeBaseFilter;
import com.dtp.core.notifier.chain.filter.NotifyFilter;
import com.dtp.core.notifier.chain.invoker.AlarmInvoker;
import com.dtp.core.notifier.chain.invoker.NoticeInvoker;
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

    public static InvokerChain<BaseNotifyCtx> getAlarmInvokerChain() {
        val filters = ApplicationContextHolder.getBeansOfType(NotifyFilter.class);
        Collection<NotifyFilter> alarmFilters = Lists.newArrayList(filters.values());
        alarmFilters.add(new AlarmBaseFilter());
        alarmFilters = alarmFilters.stream()
                .filter(x -> x.supports(NotifyTypeEnum.ALARM))
                .sorted(Comparator.comparing(Filter::getOrder))
                .collect(Collectors.toList());
        return InvokerChainFactory.buildInvokerChain(new AlarmInvoker(), alarmFilters.toArray(new NotifyFilter[0]));
    }

    public static InvokerChain<BaseNotifyCtx> getCommonInvokerChain() {
        val filters = ApplicationContextHolder.getBeansOfType(NotifyFilter.class);
        Collection<NotifyFilter> noticeFilters = Lists.newArrayList(filters.values());
        noticeFilters.add(new NoticeBaseFilter());
        noticeFilters = noticeFilters.stream()
                .filter(x -> x.supports(NotifyTypeEnum.COMMON))
                .sorted(Comparator.comparing(Filter::getOrder))
                .collect(Collectors.toList());
        return InvokerChainFactory.buildInvokerChain(new NoticeInvoker(), noticeFilters.toArray(new NotifyFilter[0]));
    }
}
