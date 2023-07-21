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

package org.dromara.dynamictp.core.timer;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.timer.Timeout;
import org.dromara.dynamictp.common.timer.TimerTask;
import org.dromara.dynamictp.core.ThirdPartTpAlarm;
import org.dromara.dynamictp.core.notifier.manager.AlarmManager;
import org.dromara.dynamictp.core.support.ExecutorWrapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A timer task used to handle run timeout.
 *
 * @author kamtohung
 **/
@Slf4j
public class ThirdPartQueueTimeoutTimerTask implements TimerTask {

    private List<NotifyItemEnum> notifyItemEnumList = Collections.singletonList(NotifyItemEnum.QUEUE_TIMEOUT);

    private final ExecutorWrapper executorWrapper;

    public ThirdPartQueueTimeoutTimerTask(ExecutorWrapper executorWrapper) {
        this.executorWrapper = executorWrapper;
    }

    @Override
    public void run(Timeout timeout) {
        Optional.ofNullable(executorWrapper.getThirdPartTpAlarm()).ifPresent(s ->
            s.getThirdPartTpAlarmHelper().incQueueTimeoutCount(1)
        );
        AlarmManager.doAlarmAsync(this.executorWrapper, notifyItemEnumList);
        log.warn("DynamicTp execute, queue timeout, tpName: {}", this.executorWrapper.getThreadPoolName());
    }

}
