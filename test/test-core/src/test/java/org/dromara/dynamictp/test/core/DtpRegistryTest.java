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

package org.dromara.dynamictp.test.core;

import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.ThreadPoolBuilder;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * DtpRegistryTest related
 *
 * @author yanhom
 * @since 1.1.0
 */
class DtpRegistryTest {

    @Test
    void testRegisterDtp() {
        DtpExecutor dtpExecutor = ThreadPoolBuilder.newBuilder()
                .threadPoolName("test_dtp")
                .buildDynamic();
        DtpRegistry.registerExecutor(ExecutorWrapper.of(dtpExecutor), "test");
        Assertions.assertEquals("test_dtp", ((DtpExecutor)DtpRegistry.getExecutor("test_dtp")).getThreadPoolName());
    }

}
