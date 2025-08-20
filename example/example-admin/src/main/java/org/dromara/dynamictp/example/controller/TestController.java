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

import com.alipay.remoting.exception.RemotingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.example.service.TestService;
import org.dromara.dynamictp.common.em.AdminRequestTypeEnum;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Redick01
 */
@Slf4j
@RestController
@AllArgsConstructor
public class TestController {

    private final TestService testService;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @GetMapping("/dtp-nacos-example/testAdminClient")
    public String testAdminClient() {
        return toJson(testService.testAdminClient());
    }

    @GetMapping("/dtp-nacos-example/testAdminClient/{type}")
    public String testAdminClientByType(@PathVariable("type") String type) {
        AdminRequestTypeEnum requestType = AdminRequestTypeEnum.of(type);
        if (requestType == null) {
            return "unknown type: " + type;
        }
        return toJson(testService.testAdminClient(requestType));
    }

    @GetMapping("/dtp-nacos-example/testAdminClientAll")
    public String testAdminClientAll() {
        return toJson(testService.testAdminClientAll());
    }

    private String toJson(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
        }
    }

}
