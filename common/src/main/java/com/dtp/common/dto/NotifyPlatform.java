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
     * Notify platform.
     */
    private String platform;

    /**
     * Key of url.
     */
    private String urlKey;

    /**
     * Secret, can be null.
     */
    private String secret;

    /**
     * receivers, split by ,
     */
    private String receivers = "所有人";
}
