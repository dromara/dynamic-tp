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

package org.dromara.dynamictp.example.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.example.domain.UserInfo;
import org.dromara.dynamictp.example.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * UserController related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
@RestController
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/dtp-consul-example/getUserInfo")
    public UserInfo getUserInfo(@RequestParam("userId") Long userId) {
        return userService.getUserInfo(userId);
    }

    @GetMapping("/dtp-consul-example/saveUserInfo")
    public void saveUserInfo(@RequestParam("user_name") String userName, @RequestParam("password") String password) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserName(userName);
        userInfo.setPassword(password);
        userService.insert(userInfo);
    }
}
