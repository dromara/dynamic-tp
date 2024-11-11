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

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AbstractRedisRateLimiter related
 *
 * @author yanhom
 * @since 1.0.8
 **/
@SuppressWarnings("all")
public abstract class AbstractRedisRateLimiter implements RedisRateLimiter<List<Long>> {

    private static final String SCRIPT_PATH = "/scripts/";

    protected static final String PREFIX = "dtp";

    private final RedisScript<List<Long>> script;

    protected final StringRedisTemplate stringRedisTemplate;

    protected static final AtomicInteger COUNTER = new AtomicInteger(0);

    public AbstractRedisRateLimiter(String scriptName, StringRedisTemplate stringRedisTemplate) {
        DefaultRedisScript redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(SCRIPT_PATH + scriptName)));
        redisScript.setResultType(List.class);
        this.script = redisScript;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public RedisScript<List<Long>> getScript() {
        return script;
    }

    public List<Object> isAllowed(String key, long windowSize, int limit) {
        RedisScript<?> script = this.getScript();
        List<String> keys = this.getKeys(key);
        String[] values = this.getArgs(key, windowSize, limit);
        return Collections.unmodifiableList((List) Objects.requireNonNull(stringRedisTemplate.execute(script, keys,
                values)));
    }

}
