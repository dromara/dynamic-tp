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

package org.dromara.dynamictp.adapter.common;

import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.MapUtils;
import org.dromara.dynamictp.common.event.AlarmCheckEvent;
import org.dromara.dynamictp.common.event.CollectEvent;
import org.dromara.dynamictp.common.event.RefreshEvent;
import org.dromara.dynamictp.common.manager.ContextManagerHelper;
import org.dromara.dynamictp.common.manager.EventBusManager;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.core.handler.CollectorHandler;
import org.dromara.dynamictp.core.notifier.manager.AlarmManager;

import java.util.EventObject;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.SCHEDULE_NOTIFY_ITEMS;

/**
 * DtpAdapterListener related
 *
 * @author yanhom
 * @since 1.0.6
 */
@Slf4j
public class DtpAdapterListener {

    public DtpAdapterListener() {
        EventBusManager.register(this);
    }

    @Subscribe
    public void handleDtpEvent(EventObject event) {
        try {
            if (event instanceof RefreshEvent) {
                RefreshEvent refreshEvent = (RefreshEvent) event;
                doRefresh(refreshEvent.getDtpProperties());
            } else if (event instanceof CollectEvent) {
                CollectEvent collectEvent = (CollectEvent) event;
                doCollect(collectEvent.getDtpProperties());
            } else if (event instanceof AlarmCheckEvent) {
                AlarmCheckEvent alarmCheckEvent = (AlarmCheckEvent) event;
                doAlarmCheck(alarmCheckEvent.getDtpProperties());
            }
        } catch (Exception e) {
            log.error("DynamicTp adapter, event handle failed.", e);
        }
    }

    /**
     * Do collect thread pool stats.
     * @param dtpProperties dtpProperties
     */
    protected void doCollect(DtpProperties dtpProperties) {
        val handlerMap = ContextManagerHelper.getBeansOfType(DtpAdapter.class);
        if (MapUtils.isEmpty(handlerMap)) {
            return;
        }
        handlerMap.forEach((k, v) -> v.getMultiPoolStats().forEach(ps ->
                CollectorHandler.getInstance().collect(ps, dtpProperties.getCollectorTypes())));
    }

    /**
     * Do refresh.
     * @param dtpProperties dtpProperties
     */
    protected void doRefresh(DtpProperties dtpProperties) {
        val handlerMap = ContextManagerHelper.getBeansOfType(DtpAdapter.class);
        if (MapUtils.isEmpty(handlerMap)) {
            return;
        }
        handlerMap.forEach((k, v) -> v.refresh(dtpProperties));
    }

    /**
     * Do alarm check.
     * @param dtpProperties dtpProperties
     */
    protected void doAlarmCheck(DtpProperties dtpProperties) {
        val handlerMap = ContextManagerHelper.getBeansOfType(DtpAdapter.class);
        if (MapUtils.isEmpty(handlerMap)) {
            return;
        }
        handlerMap.forEach((k, v) -> {
            val executorWrapper = v.getExecutorWrappers();
            executorWrapper.forEach((kk, vv) -> AlarmManager.tryAlarmAsync(vv, SCHEDULE_NOTIFY_ITEMS));
        });
    }
}
