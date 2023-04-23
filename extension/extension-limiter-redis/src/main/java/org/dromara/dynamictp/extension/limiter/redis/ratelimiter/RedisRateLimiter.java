package org.dromara.dynamictp.extension.limiter.redis.ratelimiter;

import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

/**
 * RedisRateLimiter related.
 *
 * @param <T>
 * @author yanhom
 * @since 1.0.8
 **/
public interface RedisRateLimiter<T> {

    /**
     * Get script.
     *
     * @return the script
     */
    RedisScript<T> getScript();

    /**
     * Get keys.
     *
     * @param key the key
     * @return the keys
     */
    List<String> getKeys(String key);

    /**
     * If allowed.
     *
     * @param key the key
     * @param windowSize the window size
     * @param limit the limit
     * @return the result
     */
    T isAllowed(String key, long windowSize, int limit);
}
