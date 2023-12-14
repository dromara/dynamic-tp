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
import org.dromara.dynamictp.example.service.TestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Redick01
 */
@Slf4j
@RestController
@AllArgsConstructor
public class TestController {

    private final TestService testService;

    @GetMapping("/dtp-etcd-example/testJucTp")
    public String testJuc() {
        testService.testJucTp();
        return "testJucTp success";
    }

    @GetMapping("/dtp-etcd-example/testSpringTp")
    public String testSpring() {
        testService.testSpringTp();
        return "testSpringTp success";
    }

    @GetMapping("/dtp-etcd-example/testCommonDtp")
    public String testCommon() {
        testService.testCommonDtp();
        return "testCommonDtp success";
    }

    @GetMapping("/dtp-etcd-example/testEagerDtp")
    public String testEager() {
        testService.testEagerDtp();
        return "testEagerDtp success";
    }

    @GetMapping("/dtp-etcd-example/testScheduledDtp")
    public String testScheduled() {
        testService.testScheduledDtp();
        return "testScheduledDtp success";
    }

    @GetMapping("/dtp-etcd-example/testOrderedDtp")
    public String testOrdered() {
        testService.testOrderedDtp();
        return "testOrderedDtp success";
    }
}
