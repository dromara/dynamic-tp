package com.dtp.common.entity;

import lombok.Data;

import java.util.UUID;

/**
 * NotifyPlatform related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Data
public class NotifyPlatform {

    /**
     * Notify platform id.
     */
    private String platformId = UUID.randomUUID().toString();

    /**
     * Notify platform name.
     */
    private String platform;

    /**
     * Token of url.
     */
    private String urlKey;

    /**
     * Secret, may be null.
     */
    private String secret;

    /**
     * Receivers, split by ,
     */
    private String receivers = "all";
}
