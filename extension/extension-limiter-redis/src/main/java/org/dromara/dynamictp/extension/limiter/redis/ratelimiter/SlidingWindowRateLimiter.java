package org.dromara.dynamictp.extension.limiter.redis.ratelimiter;

import org.dromara.dynamictp.common.util.CommonUtil;
import org.dromara.dynamictp.extension.limiter.redis.em.RateLimitEnum;

import java.util.Arrays;
import java.util.List;

/**
 * SlidingWindowRateLimiter related
 *
 * @author yanhom
 * @since 1.0.8
 **/
public class SlidingWindowRateLimiter extends AbstractRedistRateLimiter {

    public SlidingWindowRateLimiter() {
        super(RateLimitEnum.SLIDING_WINDOW.getScriptName());
    }

    @Override
    public List<String> getKeys(final String key) {
        String cacheKey = CommonUtil.getInstance().getServiceName() + ":" + PREFIX + ":" + key;
        String memberKey = CommonUtil.getInstance().getIp() + ":" + COUNTER.incrementAndGet();
        return Arrays.asList(cacheKey, memberKey);
    }
}
