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

package org.dromara.dynamictp.core.plugin;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

/**
 * DtpInterceptorRegistry related
 *
 * @author windsearcher.lq
 * @since 1.1.4
 **/
@Slf4j
public class DtpInterceptorRegistry {

    /**
     * Maintain all automatically registered and manually registered INTERCEPTORS
     */
    private static final List<DtpInterceptor> INTERCEPTORS = new ArrayList<>();

    static  {
        ServiceLoader<DtpInterceptor> loader = ServiceLoader.load(DtpInterceptor.class);
        for (DtpInterceptor interceptor : loader) {
            INTERCEPTORS.add(interceptor);
        }
    }

    public static void register(DtpInterceptor dtpInterceptor) {
        log.info("DynamicTp register DtpInterceptor: {}", dtpInterceptor);
        INTERCEPTORS.add(dtpInterceptor);
    }

    public static List<DtpInterceptor> getInterceptors() {
        return Collections.unmodifiableList(INTERCEPTORS);
    }

    public static Object pluginAll(Object target) {
        for (DtpInterceptor dtpInterceptor : INTERCEPTORS) {
            target = dtpInterceptor.plugin(target);
        }
        return target;
    }

    public static Object pluginAll(Object target, Class<?>[] argumentTypes, Object[] arguments) {
        for (DtpInterceptor dtpInterceptor : INTERCEPTORS) {
            target = dtpInterceptor.plugin(target, argumentTypes, arguments);
        }
        return target;
    }
}
