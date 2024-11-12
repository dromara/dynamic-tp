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

package org.dromara.dynamictp.test.core.spring;

import org.dromara.dynamictp.core.support.DynamicTp;
import org.dromara.dynamictp.spring.annotation.EnableDynamicTp;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author fei biao team
 * @version $
 * Date: 2023/4/22
 * Time: 14:27
 */
@EnableDynamicTp
@Configuration
public class Config {
    /**
     * 由于Aop依赖于DemoService。
     * AOP优先级高于BeanPostProcessor导致依赖服务DemoService被提前加载.
     * DemoService又依赖于asyncExecutor.所以导致asyncExecutor提前初始化完成
     * 解决办法：
     * 使用@Lazy或者将DtpPostProcessor的优先级提到PriorityOrdered
     *
     * @param asyncExecutor 线程池
     * @return DemoService
     * @see org.springframework.core.PriorityOrdered
     */
    @Bean
    public DemoService demoService(@Qualifier("commonExecutor") Executor asyncExecutor) {
        return new DemoService(asyncExecutor);
    }

    @DynamicTp("commonExecutor")
    @Bean
    public ThreadPoolExecutor commonExecutor() {
        return (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    }

    @DynamicTp("taskExecutor")
    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        return new ThreadPoolTaskExecutor();
    }
}
