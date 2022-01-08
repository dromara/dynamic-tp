package io.lyh.dtp.notify;

import lombok.Data;

/**
 * NotifyPlatform related
 *
 * @author: yanhom1314@gmail.com
 * @date: 2021-12-22 10:20
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
