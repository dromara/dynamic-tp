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

import lombok.val;
import org.dromara.dynamictp.common.em.RejectedTypeEnum;
import org.dromara.dynamictp.common.entity.TpMainFields;
import org.dromara.dynamictp.common.pattern.filter.InvokerChain;
import org.dromara.dynamictp.core.notifier.context.BaseNotifyCtx;
import org.dromara.dynamictp.core.notifier.context.NoticeCtx;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.ThreadPoolBuilder;

import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.dromara.dynamictp.common.em.NotifyItemEnum.CHANGE;
import static org.dromara.dynamictp.common.em.QueueTypeEnum.LINKED_BLOCKING_QUEUE;

/**
 * NoticeManager related
 *
 * @author yanhom
 * @since 1.0.8
 */
public class NoticeManager {

    private static final ExecutorService NOTICE_EXECUTOR = ThreadPoolBuilder.newBuilder()
            .threadFactory("dtp-notify")
            .corePoolSize(1)
            .maximumPoolSize(1)
            .workQueue(LINKED_BLOCKING_QUEUE.getName(), 100)
            .rejectedExecutionHandler(RejectedTypeEnum.DISCARD_OLDEST_POLICY.getName())
            .buildCommon();

    private NoticeManager() { }

    private static final InvokerChain<BaseNotifyCtx> NOTICE_INVOKER_CHAIN;

    static {
        NOTICE_INVOKER_CHAIN = NotifyFilterBuilder.getCommonInvokerChain();
    }

    public static void tryNoticeAsync(ExecutorWrapper executor, TpMainFields oldFields, List<String> diffKeys) {
        NOTICE_EXECUTOR.execute(() -> doTryNotice(executor, oldFields, diffKeys));
    }

    public static void doTryNotice(ExecutorWrapper executor, TpMainFields oldFields, List<String> diffKeys) {
        NotifyHelper.getNotifyItem(executor, CHANGE).ifPresent(notifyItem -> {
            val noticeCtx = new NoticeCtx(executor, notifyItem, oldFields, diffKeys);
            NOTICE_INVOKER_CHAIN.proceed(noticeCtx);
        });
    }

    public static void destroy() {
        NOTICE_EXECUTOR.shutdownNow();
    }
}
