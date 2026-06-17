package org.dromara.dynamictp.test.extension.limiter.redis;

import org.dromara.dynamictp.extension.limiter.redis.ratelimiter.SlidingWindowRateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlidingWindowRateLimiterTest {

    @Test
    void testTryPassFailsOpenWhenRedisUnavailable() {
        SlidingWindowRateLimiter rateLimiter = new TestSlidingWindowRateLimiter(
                null, new RedisConnectionFailureException("Unable to connect to Redis"));

        assertTrue(rateLimiter.tryPass("rate-limiter", 120, 5));
    }

    @Test
    void testTryPassAllowsWhenRedisReturnsEmptyResult() {
        SlidingWindowRateLimiter rateLimiter = new TestSlidingWindowRateLimiter(Collections.emptyList(), null);

        assertTrue(rateLimiter.tryPass("rate-limiter", 120, 5));
    }

    @Test
    void testTryPassBlocksWhenRemainingIsZero() {
        SlidingWindowRateLimiter rateLimiter = new TestSlidingWindowRateLimiter(Arrays.asList(1L, 5L, 0L), null);

        assertFalse(rateLimiter.tryPass("rate-limiter", 120, 5));
    }

    @Test
    void testTryPassAllowsWhenRemainingIsPositive() {
        SlidingWindowRateLimiter rateLimiter = new TestSlidingWindowRateLimiter(Arrays.asList(1L, 5L, 1L), null);

        assertTrue(rateLimiter.tryPass("rate-limiter", 120, 5));
    }

    private static class TestSlidingWindowRateLimiter extends SlidingWindowRateLimiter {

        private final List<Object> result;

        private final RuntimeException exception;

        TestSlidingWindowRateLimiter(List<Object> result, RuntimeException exception) {
            super(null);
            this.result = result;
            this.exception = exception;
        }

        @Override
        public List<Object> isAllowed(String key, long windowSize, int limit) {
            if (exception != null) {
                throw exception;
            }
            return result;
        }
    }
}
