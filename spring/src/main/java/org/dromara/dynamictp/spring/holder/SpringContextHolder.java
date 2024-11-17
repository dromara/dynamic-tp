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

package org.dromara.dynamictp.spring.holder;

import org.dromara.dynamictp.common.manager.ContextManager;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;

import java.util.Map;
import java.util.Objects;

/**
 * Manages the Spring ApplicationContext and provides access to beans and environment properties.
 *
 * @author vzer200
 * @since 1.2.0
 */
public class SpringContextHolder implements ContextManager, ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    @Override
    public <T> T getBean(Class<T> clazz) {
        return getInstance().getBean(clazz);
    }

    @Override
    public <T> T getBean(String name, Class<T> clazz) {
        return getInstance().getBean(name, clazz);
    }

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return getInstance().getBeansOfType(clazz);
    }

    public static ApplicationContext getInstance() {
        if (Objects.isNull(context)) {
            throw new NullPointerException("ApplicationContext is null, please check if the spring container is started.");
        }
        return context;
    }

    @Override
    public Environment getEnvironment() {
        return getInstance().getEnvironment();
    }

    @Override
    public String getEnvironmentProperty(String key) {
        return getInstance().getEnvironment().getProperty(key);
    }

    @Override
    public String getEnvironmentProperty(String key, Object environment) {
        if (environment instanceof Environment) {
            Environment env = (Environment) environment;
            return env.getProperty(key);
        }
        return null;
    }

    @Override
    public String getEnvironmentProperty(String key, String defaultValue) {
        return getInstance().getEnvironment().getProperty(key, defaultValue);
    }
}
