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

package org.dromara.dynamictp.test.extension.limiter.redis;

import lombok.val;
import org.dromara.dynamictp.extension.limiter.redis.ratelimiter.RedisRateLimiter;
import org.dromara.dynamictp.spring.annotation.EnableDynamicTp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest(classes = RedisRateLimiterTest.class)
@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration
@EnableDynamicTp
class RedisRateLimiterTest {

    @Resource
    private RedisRateLimiter<List<Long>> redisScriptRateLimiter;

    @Test
    void testRedisRateLimiterCheck() throws InterruptedException {
        for (int i = 0; i < 6; i++) {
            TimeUnit.SECONDS.sleep(1);
            val res = redisScriptRateLimiter.check("rate-limiter", 120, 5);
            System.out.println(res);
        }
    }
}



