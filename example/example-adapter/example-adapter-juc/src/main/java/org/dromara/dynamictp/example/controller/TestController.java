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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.dromara.dynamictp.core.DtpRegistry;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.support.ThreadPoolExecutorProxy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dragon-zhang
 */
@Slf4j
@RestController
public class TestController {
    
    private final ThreadPoolExecutorProxy executor = DtpRegistry.wrap(
            "namedThreadPoolExecutor", new ThreadPoolExecutor(10,
                    10, 0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>()),"classField");
    
    @GetMapping("/dtp-example-adapter/testJUC")
    public String testJUC() {
        executor.execute(() -> System.out.println("ok"));
        // see the yml config
        assert executor.getCorePoolSize() == 1;
        assert executor.getMaximumPoolSize() == 1;
        return "ok";
    }
}
