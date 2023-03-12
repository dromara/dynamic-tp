package com.dtp.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * ServiceInstance related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Data
@AllArgsConstructor
public class ServiceInstance {

    private String ip;

    private int port;

    private String serviceName;

    private String env;
}
