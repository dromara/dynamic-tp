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

package org.dromara.dynamictp.common.manager;

import java.util.Map;

/**
 * NullContextManager related
 *
 * @author yanhom
 * @since 1.2.0
 */
public class NullContextManager implements ContextManager {

    @Override
    public <T> T getBean(Class<T> clazz) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getBean(String name, Class<T> clazz) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getEnvironment() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getEnvironmentProperty(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getEnvironmentProperty(String key, Object environment) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getEnvironmentProperty(String key, String defaultValue) {
        throw new UnsupportedOperationException();
    }
}
