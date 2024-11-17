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

package org.dromara.dynamictp.spring.initializer;

import org.apache.commons.lang3.StringUtils;
import org.dromara.dynamictp.core.support.init.DtpInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.APP_ENV_KEY;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.APP_NAME_KEY;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.APP_PORT_KEY;

/**
 * SpringDtpInitializer related
 *
 * @author yanhom
 * @since 1.1.0
 */
public class SpringDtpInitializer implements DtpInitializer {

    private static final String SPRING_APP_NAME_KEY = "spring.application.name";

    private static final String SERVER_PORT = "server.port";

    private static final String ACTIVE_PROFILES = "spring.profiles.active";

    @Override
    public String getName() {
        return "SpringDtpInitializer";
    }

    @Override
    public void init(Object... args) {
        ConfigurableApplicationContext c = (ConfigurableApplicationContext) args[0];
        String appName = c.getEnvironment().getProperty(SPRING_APP_NAME_KEY, "application");
        String appPort = c.getEnvironment().getProperty(SERVER_PORT, "0");
        String appEnv = c.getEnvironment().getProperty(ACTIVE_PROFILES);
        if (StringUtils.isBlank(appEnv)) {
            // fix #I8SSGQ
            String[] profiles = c.getEnvironment().getActiveProfiles();
            if (profiles.length < 1) {
                profiles = c.getEnvironment().getDefaultProfiles();
            }
            if (profiles.length >= 1) {
                appEnv = profiles[0];
            }
        }
        if (StringUtils.isBlank(appEnv)) {
            appEnv = "unknown";
        }
        System.setProperty(APP_NAME_KEY, appName);
        System.setProperty(APP_PORT_KEY, appPort);
        System.setProperty(APP_ENV_KEY, appEnv);
    }
}
