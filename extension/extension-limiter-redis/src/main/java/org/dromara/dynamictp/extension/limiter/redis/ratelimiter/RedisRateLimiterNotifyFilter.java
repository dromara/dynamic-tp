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
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.common.pattern.filter.Invoker;
import org.dromara.dynamictp.core.notifier.chain.filter.NotifyFilter;
import org.dromara.dynamictp.core.notifier.context.BaseNotifyCtx;

import java.util.List;

/**
 * RedisRateLimiterNotifyFilter related
 *
 * @author yanhom
 * @since 1.0.8
 **/
@Slf4j
public class RedisRateLimiterNotifyFilter implements NotifyFilter {

    private final RedisRateLimiter<List<Long>> redisScriptRateLimiter;

    public RedisRateLimiterNotifyFilter(RedisRateLimiter<List<Long>> redisScriptRateLimiter) {
        this.redisScriptRateLimiter = redisScriptRateLimiter;
    }

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public void doFilter(BaseNotifyCtx context, Invoker<BaseNotifyCtx> nextInvoker) {
        if (tryPass(context)) {
            nextInvoker.invoke(context);
        }
    }

    private boolean tryPass(BaseNotifyCtx context) {
        // silence period <= 0 indicates that no rate limit check is required.
        NotifyItem notifyItem = context.getNotifyItem();
        if (notifyItem.getSilencePeriod() <= 0) {
            return true;
        }
        String notifyName = context.getExecutorWrapper().getThreadPoolName() + "#" + context.getNotifyItemEnum().getValue();
        int silencePeriod = notifyItem.getSilencePeriod();
        int clusterLimit = notifyItem.getClusterLimit();
        return redisScriptRateLimiter.tryPass(notifyName, silencePeriod, clusterLimit);
    }
}
