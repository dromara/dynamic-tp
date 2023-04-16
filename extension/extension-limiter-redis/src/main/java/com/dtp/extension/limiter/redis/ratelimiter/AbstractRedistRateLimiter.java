package com.dtp.extension.limiter.redis.ratelimiter;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AbstractRedistRateLimiter related
 *
 * @author yanhom
 * @since 1.0.8
 **/
@SuppressWarnings("all")
public abstract class AbstractRedistRateLimiter implements RedisRateLimiter<List<Long>> {

    private static final String SCRIPT_PATH = "/scripts/";

    protected static final String PREFIX = "dtp";

    private final RedisScript<List<Long>> script;

    protected final StringRedisTemplate stringRedisTemplate;

    protected static final AtomicInteger COUNTER = new AtomicInteger(0);

    public AbstractRedistRateLimiter(String scriptName, StringRedisTemplate stringRedisTemplate) {
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

    @Override
    public List<String> getKeys(final String key) {
        String cacheKey = PREFIX + ":" + key;
        return Collections.singletonList(cacheKey);
    }

    @Override
    public List<Long> isAllowed(String key, long windowSize, int limit) {
        RedisScript<?> script = this.getScript();
        List<String> keys = this.getKeys(key);

        return Collections.unmodifiableList((List) Objects.requireNonNull(stringRedisTemplate.execute(script, keys,
                doubleToString(windowSize), doubleToString(limit), doubleToString(Instant.now().getEpochSecond()))));
    }

    private String doubleToString(final double param) {
        return String.valueOf(param);
    }
}
