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

package org.dromara.dynamictp.extension.skywalking.init;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.support.init.DtpInitializer;
import org.dromara.dynamictp.jvmti.JVMTI;

import java.util.Objects;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.DTP_EXECUTE_ENHANCED;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.FALSE_STR;

/**
 * SwInitializer related
 *
 * @author yanhom
 * @since 1.1.6
 */
@Slf4j
public class SwInitializer implements DtpInitializer {

    private static final String SW_AGENT_CLASS_LOADER = "org.apache.skywalking.apm.agent.core.plugin.loader.AgentClassLoader";

    private static final String SW_RUNNABLE_WRAPPER = "org.apache.skywalking.apm.plugin.wrapper.SwRunnableWrapper";

    private static final String SW_CALLABLE_WRAPPER = "org.apache.skywalking.apm.plugin.wrapper.SwCallableWrapper";

    @Override
    public String getName() {
        return "SwInitializer";
    }

    @Override
    public void init() {
        try {
            ClassLoader[] classLoaders = JVMTI.getInstances(ClassLoader.class);
            if (Objects.isNull(classLoaders)) {
                return;
            }
            for (ClassLoader cl : classLoaders) {
                if (!SW_AGENT_CLASS_LOADER.equals(cl.getClass().getName())) {
                    continue;
                }
                if (conditionOnClass(SW_RUNNABLE_WRAPPER, cl) || conditionOnClass(SW_CALLABLE_WRAPPER, cl)) {
                    System.setProperty(DTP_EXECUTE_ENHANCED, FALSE_STR);
                    log.warn("DynamicTp init, disable enhancement for the execute method " +
                            "in the presence of the skywalking threadpool plugin.");
                    return;
                }
            }
        } catch (Throwable e) {
            log.error("DynamicTp {} init error", getName(), e);
        }
    }

    private boolean conditionOnClass(String className, ClassLoader classLoader) {
        try {
            Class.forName(className, false, classLoader);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
