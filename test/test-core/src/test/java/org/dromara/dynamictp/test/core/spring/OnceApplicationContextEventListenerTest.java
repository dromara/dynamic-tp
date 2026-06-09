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

package org.dromara.dynamictp.test.core.spring;

import org.dromara.dynamictp.spring.listener.OnceApplicationContextEventListener;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.support.GenericApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OnceApplicationContextEventListenerTest {

    @Test
    void shouldDispatchSupportedEventsFromOriginalContextOnly() {
        GenericApplicationContext context = new GenericApplicationContext();
        GenericApplicationContext otherContext = new GenericApplicationContext();
        CountingListener listener = new CountingListener();
        listener.setApplicationContext(context);

        listener.onApplicationEvent(new ContextRefreshedEvent(context));
        listener.onApplicationEvent(new ContextStartedEvent(context));
        listener.onApplicationEvent(new ContextStoppedEvent(context));
        listener.onApplicationEvent(new ContextClosedEvent(context));
        listener.onApplicationEvent(new ContextRefreshedEvent(otherContext));
        listener.onApplicationEvent(new ApplicationEvent(context) { });

        assertEquals(1, listener.refreshed);
        assertEquals(1, listener.started);
        assertEquals(1, listener.stopped);
        assertEquals(1, listener.closed);
    }

    private static class CountingListener extends OnceApplicationContextEventListener {

        private int refreshed;
        private int started;
        private int stopped;
        private int closed;

        @Override
        protected void onContextRefreshedEvent(ContextRefreshedEvent event) {
            refreshed++;
        }

        @Override
        protected void onContextStartedEvent(ContextStartedEvent event) {
            started++;
        }

        @Override
        protected void onContextStoppedEvent(ContextStoppedEvent event) {
            stopped++;
        }

        @Override
        protected void onContextClosedEvent(ContextClosedEvent event) {
            closed++;
        }
    }
}
