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

package org.dromara.dynamictp.common.constant;

import com.google.common.collect.ImmutableList;
import org.dromara.dynamictp.common.em.NotifyItemEnum;

import java.util.List;

/**
 * DynamicTpConst related
 *
 * @author yanhom
 * @since 1.0.0
 **/
public final class DynamicTpConst {

    private DynamicTpConst() { }

    public static final String MAIN_PROPERTIES_PREFIX = "dynamictp";

    public static final String DTP_ENABLED_PROP = MAIN_PROPERTIES_PREFIX + ".enabled";

    public static final String BANNER_ENABLED_PROP = MAIN_PROPERTIES_PREFIX + ".enabledBanner";

    public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    public static final String PROPERTIES_CHANGE_SHOW_STYLE = "%s => %s";

    public static final String UNKNOWN = "---";

    public static final String TRACE_ID = "traceId";

    public static final String GLOBAL_CONFIG_PREFIX = MAIN_PROPERTIES_PREFIX + ".globalExecutorProps.";

    public static final String EXECUTORS_CONFIG_PREFIX = MAIN_PROPERTIES_PREFIX + ".executors[";

    public static final String APP_NAME_KEY = "APP.NAME";

    public static final String APP_PORT_KEY = "APP.PORT";

    public static final String APP_ENV_KEY = "APP.ENV";

    /**
     * Dtp executor properties const.
     */
    public static final String THREAD_POOL_NAME = "threadPoolName";

    public static final String THREAD_POOL_ALIAS_NAME = "threadPoolAliasName";

    public static final String ALLOW_CORE_THREAD_TIMEOUT = "allowCoreThreadTimeOut";

    public static final String NOTIFY_ITEMS = "notifyItems";

    public static final String PLATFORM_IDS = "platformIds";

    public static final String NOTIFY_ENABLED = "notifyEnabled";

    public static final String WAIT_FOR_TASKS_TO_COMPLETE_ON_SHUTDOWN = "waitForTasksToCompleteOnShutdown";

    public static final String AWAIT_TERMINATION_SECONDS = "awaitTerminationSeconds";

    public static final String PRE_START_ALL_CORE_THREADS = "preStartAllCoreThreads";

    public static final String REJECT_ENHANCED = "rejectEnhanced";

    public static final String REJECT_HANDLER_TYPE = "rejectHandlerType";

    public static final String RUN_TIMEOUT = "runTimeout";

    public static final String TRY_INTERRUPT_WHEN_TIMEOUT = "tryInterrupt";

    public static final String QUEUE_TIMEOUT = "queueTimeout";

    public static final String TASK_WRAPPERS = "taskWrappers";

    public static final String PLUGIN_NAMES = "pluginNames";

    public static final String AWARE_NAMES = "awareNames";

    /**
     * symbol
     */
    public static final String DOT = ".";

    public static final String ARR_LEFT_BRACKET = "[";

    public static final String ARR_RIGHT_BRACKET = "]";

    public static final List<NotifyItemEnum> SCHEDULE_NOTIFY_ITEMS = ImmutableList.of(NotifyItemEnum.LIVENESS,
            NotifyItemEnum.CAPACITY);

    /**
     * unit
     */
    public static final Integer M_1 = 1024 * 1024;

    /**
     * OS
     */
    public static final String OS_NAME_KEY = "os.name";

    public static final String OS_LINUX_PREFIX = "linux";

    public static final String OS_WIN_PREFIX = "win";

    /**
     * switch
     */
    public static final String DTP_EXECUTE_ENHANCED = "dtp.execute.enhanced";

    public static final String TRUE_STR = "true";

    public static final String FALSE_STR = "false";
}
