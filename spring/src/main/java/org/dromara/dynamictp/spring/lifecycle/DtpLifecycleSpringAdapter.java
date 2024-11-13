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

package org.dromara.dynamictp.spring.lifecycle;

import org.dromara.dynamictp.core.lifecycle.LifeCycleManagement;
import org.springframework.context.SmartLifecycle;

/**
 * Adapts LifeCycleManagement to Spring's SmartLifecycle interface.
 *
 * @author vzer200
 * @since 1.2.0
 */
public class DtpLifecycleSpringAdapter implements SmartLifecycle {

    private final LifeCycleManagement lifeCycleManagement;

    private boolean isRunning = false;

    public DtpLifecycleSpringAdapter(LifeCycleManagement lifeCycleManagement) {
        this.lifeCycleManagement = lifeCycleManagement;
    }

    @Override
    public void start() {
        lifeCycleManagement.start();
        isRunning = true;
    }

    @Override
    public void stop() {
        lifeCycleManagement.stop();
        isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void stop(Runnable callback) {
        lifeCycleManagement.stop(callback);
        isRunning = false;
    }

    /**
     * Compatible with lower versions of spring.
     *
     * @return isAutoStartup
     */
    @Override
    public boolean isAutoStartup() {
        return lifeCycleManagement.isAutoStartup();
    }

    /**
     * Compatible with lower versions of spring.
     *
     * @return phase
     */
    @Override
    public int getPhase() {
        return lifeCycleManagement.getPhase();
    }
}
