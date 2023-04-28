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
    public NotifyRedisRateLimiterFilter notifyRedisRateLimiterFilter(RedisRateLimiter<List<Long>> redisScriptRateLimiter) {
        return new NotifyRedisRateLimiterFilter(redisScriptRateLimiter);
    }
}
