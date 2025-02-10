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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Adapts LifeCycleManagement to Spring's SmartLifecycle interface.
 *
 * @author vzer200
 * @since 1.2.0
 */
public class DtpLifecycleSpringAdapter implements SmartLifecycle {

    private final LifeCycleManagement lifeCycleManagement;

    private final AtomicBoolean running = new AtomicBoolean(false);

    public DtpLifecycleSpringAdapter(LifeCycleManagement lifeCycleManagement) {
        this.lifeCycleManagement = lifeCycleManagement;
    }

    @Override
    public void start() {
        if (this.running.compareAndSet(false, true)) {
            lifeCycleManagement.start();
        }
    }

    @Override
    public void stop() {
        if (this.running.compareAndSet(true, false)) {
            lifeCycleManagement.stop();
        }
    }

    @Override
    public boolean isRunning() {
        return this.running.get();
    }

    @Override
    public void stop(Runnable callback) {
        if (this.running.compareAndSet(true, false)) {
            lifeCycleManagement.stop(callback);
        }
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
