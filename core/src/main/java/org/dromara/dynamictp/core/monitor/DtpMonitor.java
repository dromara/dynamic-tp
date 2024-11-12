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

package org.dromara.dynamictp.core.monitor;

import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.entity.ThreadPoolStats;
import org.dromara.dynamictp.common.event.AlarmCheckEvent;
import org.dromara.dynamictp.common.event.CollectEvent;
import org.dromara.dynamictp.common.event.CustomContextRefreshedEvent;
import org.dromara.dynamictp.common.manager.EventBusManager;

import org.dromara.dynamictp.common.properties.DtpProperties;

import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.converter.ExecutorConverter;
import org.dromara.dynamictp.core.handler.CollectorHandler;
import org.dromara.dynamictp.core.notifier.manager.AlarmManager;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.ThreadPoolCreator;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.SCHEDULE_NOTIFY_ITEMS;

/**
 * DtpMonitor related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public class DtpMonitor {

    private static final ScheduledExecutorService MONITOR_EXECUTOR = ThreadPoolCreator.newScheduledThreadPool("dtp-monitor", 1);

    private final DtpProperties dtpProperties;

    private ScheduledFuture<?> monitorFuture;

    private int monitorInterval;

    public DtpMonitor(DtpProperties dtpProperties) {
        this.dtpProperties = dtpProperties;
        EventBusManager.register(this);
    }

    @Subscribe
    public synchronized void onContextRefreshedEvent(CustomContextRefreshedEvent event) {
        // if monitorInterval is same as before, do nothing.
        if (monitorInterval == dtpProperties.getMonitorInterval()) {
            return;
        }
        // cancel old monitor task.
        if (monitorFuture != null) {
            monitorFuture.cancel(true);
        }
        monitorInterval = dtpProperties.getMonitorInterval();
        monitorFuture = MONITOR_EXECUTOR.scheduleWithFixedDelay(this::run,
                0, dtpProperties.getMonitorInterval(), TimeUnit.SECONDS);
    }

    private void run() {
        Set<String> executorNames = DtpRegistry.getAllExecutorNames();
        checkAlarm(executorNames);
        collectMetrics(executorNames);
    }

    private void checkAlarm(Set<String> executorNames) {
        executorNames.forEach(name -> {
            ExecutorWrapper wrapper = DtpRegistry.getExecutorWrapper(name);
            AlarmManager.tryAlarmAsync(wrapper, SCHEDULE_NOTIFY_ITEMS);
        });
        publishAlarmCheckEvent();
    }

    private void collectMetrics(Set<String> executorNames) {
        if (!dtpProperties.isEnabledCollect()) {
            return;
        }
        executorNames.forEach(x -> {
            ExecutorWrapper wrapper = DtpRegistry.getExecutorWrapper(x);
            doCollect(ExecutorConverter.toMetrics(wrapper));
        });
        publishCollectEvent();
    }

    private void doCollect(ThreadPoolStats threadPoolStats) {
        try {
            CollectorHandler.getInstance().collect(threadPoolStats, dtpProperties.getCollectorTypes());
        } catch (Exception e) {
            log.error("DynamicTp monitor, metrics collect error.", e);
        }
    }

    private void publishCollectEvent() {
        CollectEvent event = new CollectEvent(this, dtpProperties);
        EventBusManager.post(event);
    }

    private void publishAlarmCheckEvent() {
        AlarmCheckEvent event = new AlarmCheckEvent(this, dtpProperties);
        EventBusManager.post(event);
    }

    public static void destroy() {
        MONITOR_EXECUTOR.shutdownNow();
    }
}
