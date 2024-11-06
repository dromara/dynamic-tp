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

package org.dromara.dynamictp.core.support.binder;

import org.dromara.dynamictp.common.properties.DtpProperties;

import java.util.Map;

/**
 * PropertiesBinder related
 *
 * @author yanhom
 * @since 1.0.3
 **/
public interface PropertiesBinder {

    /**
     * bind dtp properties
     *
     * @param properties   properties
     * @param dtpProperties dtp properties
     */
    void bindDtpProperties(Map<?, Object> properties, DtpProperties dtpProperties);

    /**
     * bind dtp properties
     *
     * @param environment  environment
     * @param dtpProperties dtp properties
     */
    void bindDtpProperties(Object environment, DtpProperties dtpProperties);

    /**
     * before bind
     *
     * @param source source
     * @param dtpProperties dtp properties
     */
    default void beforeBind(Object source, DtpProperties dtpProperties) {

    }

    /**
     * after bind
     *
     * @param source source
     * @param dtpProperties dtp properties
     */
    default void afterBind(Object source, DtpProperties dtpProperties) {

    }
}
