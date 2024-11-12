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

package org.dromara.dynamictp.spring.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;


import static org.springframework.util.ObjectUtils.nullSafeEquals;

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

    /**
     * The subclass overrides this method to handle {@link ContextRefreshedEvent}
     *
     * @param event {@link ContextRefreshedEvent}
     */
    protected void onContextRefreshedEvent(ContextRefreshedEvent event) {
    }

    /**
     * The subclass overrides this method to handle {@link ContextStartedEvent}
     *
     * @param event {@link ContextStartedEvent}
     */
    protected void onContextStartedEvent(ContextStartedEvent event) {
    }

    /**
     * The subclass overrides this method to handle {@link ContextStoppedEvent}
     *
     * @param event {@link ContextStoppedEvent}
     */
    protected void onContextStoppedEvent(ContextStoppedEvent event) {
    }

    /**
     * The subclass overrides this method to handle {@link ContextClosedEvent}
     *
     * @param event {@link ContextClosedEvent}
     */
    protected void onContextClosedEvent(ContextClosedEvent event) {
    }

    /**
     * Is original {@link ApplicationContext} as the event source
     * @param event {@link ApplicationEvent}
     * @return if original, return <code>true</code>, or <code>false</code>
     */
    private boolean isOriginalEventSource(ApplicationEvent event) {
        return nullSafeEquals(this.applicationContext, event.getSource());
    }

    @Override
    public final void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}

