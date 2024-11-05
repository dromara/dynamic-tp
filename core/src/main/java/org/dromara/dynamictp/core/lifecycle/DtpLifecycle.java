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

package org.dromara.dynamictp.core.lifecycle;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.manager.EventBusManager;
import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.monitor.DtpMonitor;
import org.dromara.dynamictp.core.notifier.manager.AlarmManager;
import org.dromara.dynamictp.core.notifier.manager.NoticeManager;
import org.dromara.dynamictp.core.support.DtpLifecycleSupport;
import org.dromara.dynamictp.core.system.SystemMetricManager;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * DtpLifecycle related
 *
 * @author yanhom
 * @since 1.1.3
 **/
@Slf4j
public class DtpLifecycle implements LifeCycleManagement {

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public void start() {
        if (this.running.compareAndSet(false, true)) {
            DtpRegistry.getAllExecutors().forEach((k, v) -> DtpLifecycleSupport.initialize(v));
        }
    }

    @Override
    public void stop() {
        if (this.running.compareAndSet(true, false)) {
            shutdownInternal();
            DtpRegistry.getAllExecutors().forEach((k, v) -> DtpLifecycleSupport.destroy(v));
        }
    }

    @Override
    public boolean isRunning() {
        return this.running.get();
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void shutdownInternal() {
        DtpMonitor.destroy();
        AlarmManager.destroy();
        NoticeManager.destroy();
        SystemMetricManager.stop();
        EventBusManager.destroy();
    }
}
