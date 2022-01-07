package io.lyh.dtp.domain;

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
     * 通知平台
     */
    private String platform;

    /**
     * url后需要拼接的key
     */
    private String urlKey;

    /**
     * 密钥（可为空）
     */
    private String secret;

    /**
     * receivers, split by ,
     */
    private String receivers = "所有人";
}
