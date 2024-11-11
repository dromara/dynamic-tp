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
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.dromara.dynamictp.common.util.CommonUtil;
import org.dromara.dynamictp.extension.limiter.redis.em.RateLimitEnum;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * SlidingWindowRateLimiter related
 *
 * @author yanhom
 * @since 1.0.8
 **/
@Slf4j
public class SlidingWindowRateLimiter extends AbstractRedisRateLimiter {

    public static final int LUA_RES_REMAIN_INDEX = 2;

    public SlidingWindowRateLimiter(StringRedisTemplate stringRedisTemplate) {
        super(RateLimitEnum.SLIDING_WINDOW.getScriptName(), stringRedisTemplate);
    }

    @Override
    public List<String> getKeys(final String key) {
        String cacheKey = CommonUtil.getInstance().getServiceName() + ":" + PREFIX + ":" + key;
        return Collections.singletonList(cacheKey);
    }

    @Override
    public String[] getArgs(String key, long windowSize, int limit) {
        String memberKey = CommonUtil.getInstance().getIp() + ":" + COUNTER.incrementAndGet();
        return new String[]{
                doubleToString(windowSize),
                doubleToString(limit),
                doubleToString(Instant.now().getEpochSecond()),
                memberKey
        };
    }

    @Override
    public boolean check(String name, long interval, int limit) {
        try {
            val res = isAllowed(name, interval, limit);
            if (CollectionUtils.isEmpty(res)) {
                return true;
            }
            if (Objects.isNull(res.get(LUA_RES_REMAIN_INDEX)) || (long) res.get(LUA_RES_REMAIN_INDEX) <= 0) {
                log.debug("DynamicTp notify, trigger redis rate limit, limitKey:{}", res.get(0));
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("DynamicTp notify, redis rate limit check failed, limitKey:{}", name, e);
            return true;
        }
    }

    private String doubleToString(final double param) {
        return String.valueOf(param);
    }
}
