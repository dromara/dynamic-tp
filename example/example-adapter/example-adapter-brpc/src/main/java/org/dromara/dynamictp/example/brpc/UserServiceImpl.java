package org.dromara.dynamictp.example.brpc;

import com.baidu.cloud.starlight.springcloud.server.annotation.RpcService;

/**
 * UserServiceImpl related
 *
 * @author yanhom
 * @since 1.1.0
 */
@RpcService
public class UserServiceImpl implements UserService {

    @Override
    public String getUserName(Long userId) {
        return "dynamictp";
    }
}
