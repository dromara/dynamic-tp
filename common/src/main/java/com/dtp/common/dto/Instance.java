package com.dtp.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Instance related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Data
@AllArgsConstructor
public class Instance {

    private String ip;

    private int port;

    private String serviceName;

    private String env;
}
