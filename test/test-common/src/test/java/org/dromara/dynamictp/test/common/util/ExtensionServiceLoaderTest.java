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

package org.dromara.dynamictp.test.common.util;

import org.dromara.dynamictp.common.util.ExtensionServiceLoader;
import org.dromara.dynamictp.common.plugin.DtpInterceptor;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.util.List;

/**
 * ExtensionServiceLoader test case
 */
public class ExtensionServiceLoaderTest {
    
    @Test
    public void test(){
        List<DtpInterceptor> loadedInterceptors = ExtensionServiceLoader.get(DtpInterceptor.class);
        Assertions.assertTrue(loadedInterceptors.stream().anyMatch(it-> it instanceof TestInterceptorLoader));
    }

}
