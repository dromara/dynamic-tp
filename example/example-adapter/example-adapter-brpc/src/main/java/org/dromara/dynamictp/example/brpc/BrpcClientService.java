package org.dromara.dynamictp.example.brpc;

import com.baidu.cloud.starlight.springcloud.client.annotation.RpcProxy;
import org.springframework.stereotype.Service;

/**
 * BrpcClientService related
 *
 * @author yanhom
 * @since 1.1.0
 */
@Service
public class BrpcClientService {

    /**
     * 使用注解引用服务，指定服务端IP Port，采用brpc协议调用
     */
    @RpcProxy(remoteUrl = "localhost:8777", protocol = "brpc")
    private UserService userService;

    /**
     * 使用注解引用服务，指定服务端IP Port，采用springrest(http)协议调用
     */
    @RpcProxy(remoteUrl = "localhost:8777", protocol = "springrest")
    private UserService restUserService;

    public String getUserName(Long userId) {
        return userService.getUserName(userId);
    }
}
