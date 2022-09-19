package com.dtp.extension.limiter.redis.em;

import lombok.Getter;

/**
 * RateLimitEnum related
 *
 * @author yanhom
 * @since 1.0.8
 **/
@Getter
public enum RateLimitEnum {

    /**
     * rate limit
     */
    SLIDING_WINDOW("sw_rt", "sliding_window_rate_limiter.lua"),
    ;

    private final String keyName;

    private final String scriptName;

    RateLimitEnum(final String keyName, final String scriptName) {
        this.keyName = keyName;
        this.scriptName = scriptName;
    }

    public String getKeyName() {
        return this.keyName;
    }

    public String getScriptName() {
        return this.scriptName;
    }
}
