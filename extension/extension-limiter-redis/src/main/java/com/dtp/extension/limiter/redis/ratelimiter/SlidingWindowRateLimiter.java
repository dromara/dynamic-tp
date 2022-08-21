package com.dtp.extension.limiter.redis.ratelimiter;

import com.dtp.common.util.CommonUtil;
import com.dtp.extension.limiter.redis.em.RateLimitEnum;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Arrays;
import java.util.List;

/**
 * SlidingWindowRateLimiter related
 *
 * @author: yanhom
 * @since 1.0.8
 **/
public class SlidingWindowRateLimiter extends AbstractRedistRateLimiter {

    public SlidingWindowRateLimiter() {
        super(RateLimitEnum.SLIDING_WINDOW.getScriptName());
    }

    @Override
    protected String getKeyPrefix() {
        return RateLimitEnum.SLIDING_WINDOW.getKeyName();
    }

    @Override
    public List<String> getKeys(final String key) {
        String cacheKey = getKeyPrefix() + ":" + key;
        String memberKey = CommonUtil.getInstance().getIp() + ":" + COUNTER.incrementAndGet();
        return Arrays.asList(cacheKey, memberKey);
    }
}
