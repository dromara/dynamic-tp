package com.dtp.common.dto;

import lombok.Data;

/**
 * NotifyPlatform related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Data
public class NotifyPlatform {

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
    private String receivers = "所有人";
}
