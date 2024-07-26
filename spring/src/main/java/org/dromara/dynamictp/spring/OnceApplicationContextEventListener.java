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

package org.dromara.dynamictp.spring;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.manager.EventBusManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;

import java.util.EventObject;
import java.util.Objects;

/**
 * The abstract class {@link ApplicationListener} for {@link ApplicationEvent} guarantees just one-time execution
 * and prevents the event propagation in the hierarchical {@link ApplicationContext ApplicationContexts}
 * @author yanhom
 * @since 1.1.4
 */
@Slf4j
public abstract class OnceApplicationContextEventListener implements ApplicationContextAware, ApplicationListener<ApplicationEvent> {

    private ApplicationContext applicationContext;

    protected OnceApplicationContextEventListener() { }

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
        return Objects.equals(SpringContextHolder.getInstance(), event.getSource());
    }

    @Override
    public final void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
