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

package org.dromara.dynamictp.starter.extension.limiter.redis.autoconfigure;

import org.dromara.dynamictp.extension.limiter.redis.ratelimiter.NotifyRedisRateLimiterFilter;
import org.dromara.dynamictp.extension.limiter.redis.ratelimiter.RedisRateLimiter;
import org.dromara.dynamictp.extension.limiter.redis.ratelimiter.SlidingWindowRateLimiter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

/**
 * RedisLimiterAutoConfiguration related
 *
 * @author yanhom
 * @since 1.0.8
 **/
@Configuration
@ConditionalOnClass(StringRedisTemplate.class)
public class RedisLimiterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RedisRateLimiter<List<Long>> redisScriptRateLimiter(StringRedisTemplate stringRedisTemplate) {
        return new SlidingWindowRateLimiter(stringRedisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public NotifyRedisRateLimiterFilter notifyRedisRateLimiterFilter() {
        return new NotifyRedisRateLimiterFilter();
    }
}
