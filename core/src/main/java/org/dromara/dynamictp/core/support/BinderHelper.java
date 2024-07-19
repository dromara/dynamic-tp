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
import org.dromara.dynamictp.common.util.ExtensionServiceLoader;

import java.util.Map;
import java.util.Objects;

/**
 * BinderHelper related
 *
 * @author dragon-zhang
 * @since 1.1.4
 */
@Slf4j
public class BinderHelper {

    private BinderHelper() { }

    private static PropertiesBinder getBinder() {
        PropertiesBinder binder = Singleton.INST.get(PropertiesBinder.class);
        if (Objects.nonNull(binder)) {
            return binder;
        }
        final PropertiesBinder loadedFirstBinder = ExtensionServiceLoader.getFirst(PropertiesBinder.class);
        if (Objects.isNull(loadedFirstBinder)) {
            log.error("DynamicTp refresh, no SPI for org.dromara.dynamictp.spring.ex.PropertiesBinder.");
            return null;
        }
        Singleton.INST.single(PropertiesBinder.class, loadedFirstBinder);
        return loadedFirstBinder;
    }

    public static void bindDtpProperties(Map<?, Object> properties, DtpProperties dtpProperties) {
        final PropertiesBinder binder = getBinder();
        if (Objects.isNull(binder)) {
            return;
        }
        binder.bindDtpProperties(properties, dtpProperties);
    }

    public static void bindDtpProperties(Object environment, DtpProperties dtpProperties) {
        final PropertiesBinder binder = getBinder();
        if (Objects.isNull(binder)) {
            return;
        }
        binder.bindDtpProperties(environment, dtpProperties);
    }
}
