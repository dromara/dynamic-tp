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

package org.dromara.dynamictp.core.reject;

import cn.hutool.core.util.StrUtil;
import org.dromara.dynamictp.core.aware.AwareManager;
import org.dromara.dynamictp.core.aware.TaskTimeoutAware;
import org.dromara.dynamictp.core.notifier.alarm.ThreadPoolAlarmHelper;
import org.dromara.dynamictp.core.notifier.manager.AlarmManager;
import org.dromara.dynamictp.core.support.ExecutorAdapter;
import org.slf4j.Logger;
import org.slf4j.MDC;
import java.util.Collections;
import java.util.concurrent.Executor;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.TRACE_ID;
import static org.dromara.dynamictp.common.em.NotifyItemEnum.REJECT;

/**
 * RejectedAware related
 *
 * @author kyao
 * @since 1.1.4
 **/
public interface RejectedAware {

    /**
     * Do sth before reject.
     *
     * @param runnable the runnable
     * @param executor ThreadPoolExecutor instance
     * @param log      logger
     */
    default void beforeReject(Runnable runnable, Executor executor, Logger log) {
        AwareManager.beforeReject(runnable, executor, log);
    }
}
