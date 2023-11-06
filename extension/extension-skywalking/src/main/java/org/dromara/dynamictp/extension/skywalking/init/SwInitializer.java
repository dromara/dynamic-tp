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

import org.dromara.dynamictp.core.support.init.DtpInitializer;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.DTP_EXECUTE_ENHANCED;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.FALSE_STR;

/**
 * SwInitializer related
 *
 * @author yanhom
 * @since 1.1.6
 */
public class SwInitializer implements DtpInitializer {

    private static final String SW_RUNNABLE_WRAPPER = "org.apache.skywalking.apm.plugin.wrapper.SwRunnableWrapper";

    private static final String SW_CALLABLE_WRAPPER = "org.apache.skywalking.apm.plugin.wrapper.SwCallableWrapper";

    @Override
    public void init() {
        if (conditionOnClass(SW_RUNNABLE_WRAPPER) || conditionOnClass(SW_CALLABLE_WRAPPER)) {
            System.setProperty(DTP_EXECUTE_ENHANCED, FALSE_STR);
        }
    }

    private boolean conditionOnClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
