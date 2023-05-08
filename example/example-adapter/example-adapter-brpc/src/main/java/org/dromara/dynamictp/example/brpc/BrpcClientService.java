/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
