package com.dtp.common.ex;

/**
 * RateLimitException related
 *
 * @author: yanhom
 * @since 1.0.8
 **/
public class RateLimitException extends RuntimeException {

    public RateLimitException(String message) {
        super(message);
    }
}
