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

package org.dromara.dynamictp.test.core.thread;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.plugin.DtpInterceptorRegistry;
import org.junit.Test;

/**
 * @author hanli
 * @date 2023年07月19日 3:33 PM
 */
@Slf4j
public class InterceptTest {

    public static class TestA {

        public void execute() {
            beforeExecute();
            log.info("execute");
            afterExecute();
        }

        public void beforeExecute() {
            log.info("beforeExecute");
        }

        public void afterExecute() {
            log.info("afterExecute");
        }

    }

    @Test
    public void test() {
        AInterceptorTest AInterceptorTest = new AInterceptorTest();
        DtpInterceptorRegistry.register("TestAInterceptor", AInterceptorTest);
        TestA testA = new TestA();
        TestA testA1 = (TestA) DtpInterceptorRegistry.plugin(testA, CollectionUtil.newHashSet("TestAInterceptor"));
        testA1.execute();
    }
}
