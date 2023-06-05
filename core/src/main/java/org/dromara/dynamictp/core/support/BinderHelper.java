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

package org.dromara.dynamictp.core.support;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.pattern.singleton.Singleton;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.core.spring.PropertiesBinder;
import org.springframework.core.env.Environment;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * BinderHelper related
 *
 * @author dragon-zhang
 * @since 1.1.4
 */
@Slf4j
public class BinderHelper {
    
    private static PropertiesBinder getBinder() {
        PropertiesBinder binder = Singleton.INST.get(PropertiesBinder.class);
        if (Objects.nonNull(binder)) {
            return binder;
        }
        final Iterator<PropertiesBinder> iterator = ServiceLoader.load(PropertiesBinder.class).iterator();
        if (!iterator.hasNext()) {
            log.error("DynamicTp refresh, no SPI for org.dromara.dynamictp.core.spring.PropertiesBinder.");
            return null;
        }
        binder = iterator.next();
        Singleton.INST.single(PropertiesBinder.class, binder);
        return binder;
    }
    
    public static void bindDtpProperties(Map<?, Object> properties, DtpProperties dtpProperties) {
        final PropertiesBinder binder = getBinder();
        if (Objects.isNull(binder)) {
            return;
        }
        binder.bindDtpProperties(properties, dtpProperties);
    }
    
    public static void bindDtpProperties(Environment environment, DtpProperties dtpProperties) {
        final PropertiesBinder binder = getBinder();
        if (Objects.isNull(binder)) {
            return;
        }
        binder.bindDtpProperties(environment, dtpProperties);
    }
}
