package io.lyh.dtp.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Instance related
 *
 * @author: yanhom1314@gmail.com
 * @date: 2021-12-31 17:18
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
