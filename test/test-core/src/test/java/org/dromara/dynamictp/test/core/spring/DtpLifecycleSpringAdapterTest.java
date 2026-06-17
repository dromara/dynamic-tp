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

import org.dromara.dynamictp.core.lifecycle.LifeCycleManagement;
import org.dromara.dynamictp.spring.lifecycle.DtpLifecycleSpringAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DtpLifecycleSpringAdapterTest related
 *
 * @author vzer200
 * @since 1.1.8
 */
class DtpLifecycleSpringAdapterTest {

    private DtpLifecycleSpringAdapter dtpLifecycleSpringAdapter;

    private LifeCycleManagement lifeCycleManagement;

    @BeforeEach
    void setUp() {
        lifeCycleManagement = Mockito.mock(LifeCycleManagement.class);
        dtpLifecycleSpringAdapter = new DtpLifecycleSpringAdapter(lifeCycleManagement);
    }

    @Test
    void testLifecycleManagementIntegration() {
        assertNotNull(dtpLifecycleSpringAdapter);
        assertNotNull(lifeCycleManagement);

        dtpLifecycleSpringAdapter.start();
        assertTrue(dtpLifecycleSpringAdapter.isRunning());
        Mockito.verify(lifeCycleManagement).start();
        Mockito.when(lifeCycleManagement.isRunning()).thenReturn(true);
        assertTrue(lifeCycleManagement.isRunning());

        Mockito.reset(lifeCycleManagement);

        dtpLifecycleSpringAdapter.stop();
        assertFalse(dtpLifecycleSpringAdapter.isRunning());
        Mockito.verify(lifeCycleManagement).stop();
        Mockito.when(lifeCycleManagement.isRunning()).thenReturn(false);
        assertFalse(lifeCycleManagement.isRunning());
    }

    @Test
    void testAutoStartupAndPhase() {
        Mockito.when(lifeCycleManagement.isAutoStartup()).thenReturn(true);
        Mockito.when(lifeCycleManagement.getPhase()).thenReturn(0);
        assertEquals(lifeCycleManagement.isAutoStartup(), dtpLifecycleSpringAdapter.isAutoStartup());
        assertEquals(lifeCycleManagement.getPhase(), dtpLifecycleSpringAdapter.getPhase());
    }
}
