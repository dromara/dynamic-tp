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

package org.dromara.dynamictp.test.common.manager;

import com.google.common.eventbus.Subscribe;
import org.dromara.dynamictp.common.manager.EventBusManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * EventBusManagerTest related.
 */
class EventBusManagerTest {

    @AfterEach
    void tearDown() {
        EventBusManager.destroy();
    }

    @Test
    void testRegisterAndPostDispatchesEventToSubscriber() {
        TestSubscriber subscriber = new TestSubscriber();
        EventBusManager.register(subscriber);

        EventBusManager.post("refresh");

        Assertions.assertEquals(1, subscriber.count.get());
        Assertions.assertEquals("refresh", subscriber.lastEvent.get());
    }

    @Test
    void testRegisterSameSubscriberOnlyOnce() {
        TestSubscriber subscriber = new TestSubscriber();
        EventBusManager.register(subscriber);
        EventBusManager.register(subscriber);

        EventBusManager.post("alarm");

        Assertions.assertEquals(1, subscriber.count.get());
    }

    @Test
    void testUnregisterStopsDeliveryAndIsIdempotent() {
        TestSubscriber subscriber = new TestSubscriber();
        EventBusManager.register(subscriber);

        EventBusManager.unregister(subscriber);
        EventBusManager.unregister(subscriber);
        EventBusManager.post("notice");

        Assertions.assertEquals(0, subscriber.count.get());
    }

    @Test
    void testDestroyUnregistersAllSubscribers() {
        TestSubscriber firstSubscriber = new TestSubscriber();
        TestSubscriber secondSubscriber = new TestSubscriber();
        EventBusManager.register(firstSubscriber);
        EventBusManager.register(secondSubscriber);

        EventBusManager.destroy();
        EventBusManager.post("closed");

        Assertions.assertEquals(0, firstSubscriber.count.get());
        Assertions.assertEquals(0, secondSubscriber.count.get());
    }

    private static class TestSubscriber {

        private final AtomicInteger count = new AtomicInteger();

        private final AtomicReference<String> lastEvent = new AtomicReference<>();

        @Subscribe
        public void onEvent(String event) {
            count.incrementAndGet();
            lastEvent.set(event);
        }
    }
}
