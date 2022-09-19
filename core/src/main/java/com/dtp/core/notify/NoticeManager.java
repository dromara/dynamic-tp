package com.dtp.core.notify;

import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.pattern.filter.Filter;
import com.dtp.common.pattern.filter.FilterChain;
import com.dtp.common.pattern.filter.FilterChainFactory;
import com.dtp.core.context.BaseNotifyCtx;
import com.dtp.core.context.NoticeCtx;
import com.dtp.core.notify.filter.NotifyFilter;
import com.dtp.core.notify.invoker.NoticeInvoker;
import com.google.common.collect.Lists;
import lombok.val;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * NoticeManager related
 *
 * @author yanhom
 * @since 1.0.8
 */
public class NoticeManager {

    private NoticeManager() {}

    private static final FilterChain<BaseNotifyCtx> NOTICE_CTX_FILTER_CHAIN;

    static {
        val filters = ApplicationContextHolder.getBeansOfType(NotifyFilter.class);
        Collection<NotifyFilter> noticeFilters = Lists.newArrayList(filters.values());
        noticeFilters = noticeFilters.stream()
                .sorted(Comparator.comparing(Filter::getOrder))
                .collect(Collectors.toList());
        NOTICE_CTX_FILTER_CHAIN = FilterChainFactory.buildFilterChain(new NoticeInvoker(),
                noticeFilters.toArray(new NotifyFilter[0]));
    }

    public static void doNotice(NoticeCtx noticeCtx) {
        NOTICE_CTX_FILTER_CHAIN.fire(noticeCtx);
    }
}
