package com.dtp.extension.limiter.redis.ratelimiter;

import cn.hutool.core.collection.CollUtil;
import com.dtp.common.ex.RateLimitException;
import com.dtp.common.pattern.filter.Invoker;
import com.dtp.core.context.BaseNotifyCtx;
import com.dtp.core.notify.filter.NotifyFilter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.annotation.Resource;
import java.util.List;

/**
 * NotifyRedisRateLimiterFilter related
 *
 * @author: yanhom
 * @since 1.0.8
 **/
@Slf4j
public class NotifyRedisRateLimiterFilter implements NotifyFilter {

    public static final int LUA_RES_REMAIN_INDEX = 2;

    @Resource
    private RedisRateLimiter<List<Long>> redisScriptRateLimiter;

    @Override
    public int order() {
        return 10;
    }

    @Override
    public void doFilter(BaseNotifyCtx context, Invoker<BaseNotifyCtx> nextFilter) {
        String notifyName = context.getExecutorWrapper().getThreadPoolName() + ":" + context.getNotifyType().getValue();
        check(notifyName, context.getNotifyItem().getClusterLimit(), context.getNotifyItem().getInterval());
        nextFilter.invoke(context);
    }

    private void check(String notifyName, int limit, long interval) {
        val res = redisScriptRateLimiter.isAllowed(notifyName, interval, limit);
        if (CollUtil.isEmpty(res)) {
            return;
        }
        if (res.get(LUA_RES_REMAIN_INDEX) <= 0) {
            log.debug("DynamicTp notify trigger rate limit, limitKey:{}", res.get(0));
            throw new RateLimitException("DynamicTp notify trigger rate limit");
        }
    }
}
