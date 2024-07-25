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
// Copyright (c) 2007-2020 VMware, Inc. or its affiliates.  All rights reserved.
//
// This software, the RabbitMQ Java client library, is triple-licensed under the
// Mozilla Public License 2.0 ("MPL"), the GNU General Public License version 2
// ("GPL") and the Apache License version 2 ("ASL"). For the MPL, please see
// LICENSE-MPL-RabbitMQ. For the GPL, please see LICENSE-GPL2.  For the ASL,
// please see LICENSE-APACHE2.
//
// This software is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND,
// either express or implied. See the LICENSE file for specific language governing
// rights and limitations of this software.
//
// If you have any questions regarding licensing, please contact us at
// info@rabbitmq.com.

/*
 * Modifications Copyright 2015-2020 VMware, Inc. or its affiliates. and licenced as per
 * the rest of the RabbitMQ Java client.
 */

/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * https://creativecommons.org/licenses/publicdomain
 */

package org.dromara.dynamictp.spring;



import org.dromara.dynamictp.common.manager.ContextManager;
import org.dromara.dynamictp.common.manager.EventBusManager;
import org.dromara.dynamictp.core.support.DtpBannerPrinter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.core.env.Environment;

import java.util.EventObject;
import java.util.Map;
import java.util.Objects;

public class SpringContextHolder implements ContextManager, ApplicationContextAware, ApplicationListener<ApplicationEvent> {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
        DtpBannerPrinter.printBanner();  // 打印 banner
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


    public void publishEvent(Object event) {
        if (event instanceof ApplicationEvent) {
            getInstance().publishEvent((ApplicationEvent) event);
        }
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (isOriginalEventSource(event) && event instanceof ApplicationContextEvent) {
            if (event instanceof ContextRefreshedEvent) {
                onContextRefreshedEvent((ContextRefreshedEvent) event);
            } else if (event instanceof ContextStartedEvent) {
                onContextStartedEvent((ContextStartedEvent) event);
            } else if (event instanceof ContextStoppedEvent) {
                onContextStoppedEvent((ContextStoppedEvent) event);
            } else if (event instanceof ContextClosedEvent) {
                onContextClosedEvent((ContextClosedEvent) event);
            }
        }
    }

    protected void onContextRefreshedEvent(ContextRefreshedEvent event) {
        EventObject refreshedEvent = new EventObject(this);
        EventBusManager.post(refreshedEvent);
    }

    protected void onContextStartedEvent(ContextStartedEvent event) {
        // Override to handle ContextStartedEvent
    }

    protected void onContextStoppedEvent(ContextStoppedEvent event) {
        // Override to handle ContextStoppedEvent
    }

    protected void onContextClosedEvent(ContextClosedEvent event) {
        // Override to handle ContextClosedEvent
    }

    private boolean isOriginalEventSource(ApplicationEvent event) {
        return Objects.equals(context, event.getSource());
    }

    @Override
    public void onEvent(Object event) {
        if (event instanceof ApplicationEvent) {
            onApplicationEvent((ApplicationEvent) event);
        }
    }

    @Override
    public String getEnvironmentProperty(String key) {
        return getInstance().getEnvironment().getProperty(key);
    }

    @Override
    public String getEnvironmentProperty(String key, String defaultValue) {
        return getInstance().getEnvironment().getProperty(key, defaultValue);
    }

    @Override
    public String[] getActiveProfiles() {
        return getInstance().getEnvironment().getActiveProfiles();
    }

    @Override
    public String[] getDefaultProfiles() {
        return getInstance().getEnvironment().getDefaultProfiles();
    }

    @Override
    public void setContext(Object context) {
        if (context instanceof ApplicationContext) {
            setApplicationContext((ApplicationContext) context);
        }
    }
}
