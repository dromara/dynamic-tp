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

package org.dromara.dynamictp.extension.limiter.redis.ratelimiter;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.pattern.filter.Invoker;
import org.dromara.dynamictp.core.notifier.chain.filter.NotifyFilter;
import org.dromara.dynamictp.core.notifier.context.BaseNotifyCtx;

import javax.annotation.Resource;
import java.util.List;

/**
 * NotifyRedisRateLimiterFilter related
 *
 * @author yanhom
 * @since 1.0.8
 **/
@Slf4j
public class NotifyRedisRateLimiterFilter implements NotifyFilter {

    @Resource
    private RedisRateLimiter<List<Long>> redisScriptRateLimiter;

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public void doFilter(BaseNotifyCtx context, Invoker<BaseNotifyCtx> nextFilter) {
        String notifyName = context.getExecutorWrapper().getThreadPoolName() + ":" + context.getNotifyItemEnum().getValue();
        int interval = context.getNotifyItem().getInterval();
        int limit = context.getNotifyItem().getClusterLimit();
        boolean checkResult = redisScriptRateLimiter.check(notifyName, interval, limit);
        if (checkResult) {
            nextFilter.invoke(context);
        }
    }
}
