/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.dynamictp.core.notifier.manager;

import org.dromara.dynamictp.common.em.NotifyTypeEnum;
import org.dromara.dynamictp.common.pattern.filter.Filter;
import org.dromara.dynamictp.common.pattern.filter.InvokerChain;
import org.dromara.dynamictp.common.pattern.filter.InvokerChainFactory;
import org.dromara.dynamictp.core.notifier.context.BaseNotifyCtx;
import org.dromara.dynamictp.core.notifier.chain.filter.AlarmBaseFilter;
import org.dromara.dynamictp.core.notifier.chain.filter.NoticeBaseFilter;
import org.dromara.dynamictp.core.notifier.chain.filter.NotifyFilter;
import org.dromara.dynamictp.core.notifier.chain.invoker.AlarmInvoker;
import org.dromara.dynamictp.core.notifier.chain.invoker.NoticeInvoker;
import com.google.common.collect.Lists;
import lombok.val;

import org.dromara.dynamictp.common.manager.ContextManagerHelper;

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
        val filters = ContextManagerHelper.getBeansOfType(NotifyFilter.class);
        Collection<NotifyFilter> alarmFilters = Lists.newArrayList(filters.values());
        alarmFilters.add(new AlarmBaseFilter());
        alarmFilters = alarmFilters.stream()
                .filter(x -> x.supports(NotifyTypeEnum.ALARM))
                .sorted(Comparator.comparing(Filter::getOrder))
                .collect(Collectors.toList());
        return InvokerChainFactory.buildInvokerChain(new AlarmInvoker(), alarmFilters.toArray(new NotifyFilter[0]));
    }

    public static InvokerChain<BaseNotifyCtx> getCommonInvokerChain() {
        val filters = ContextManagerHelper.getBeansOfType(NotifyFilter.class);
        Collection<NotifyFilter> noticeFilters = Lists.newArrayList(filters.values());
        noticeFilters.add(new NoticeBaseFilter());
        noticeFilters = noticeFilters.stream()
                .filter(x -> x.supports(NotifyTypeEnum.COMMON))
                .sorted(Comparator.comparing(Filter::getOrder))
                .collect(Collectors.toList());
        return InvokerChainFactory.buildInvokerChain(new NoticeInvoker(), noticeFilters.toArray(new NotifyFilter[0]));
    }
}
