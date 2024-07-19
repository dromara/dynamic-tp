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

package org.dromara.dynamictp.common.util;

import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.entity.ServiceInstance;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.dynamictp.common.manager.ContextManagerHelper;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * CommonUtil related
 *
 * @author yanhom
 */
@Slf4j
public final class CommonUtil {

    private CommonUtil() {
    }

    private static final ServiceInstance SERVICE_INSTANCE;

    static {
        String appName = ContextManagerHelper.getEnvironmentProperty("spring.application.name", "application");

        String portStr = ContextManagerHelper.getEnvironmentProperty("server.port", "0");
        int port = StringUtils.isNotBlank(portStr) ? Integer.parseInt(portStr) : 0;

        String address = null;
        try {
            address = getLocalHostExactAddress().getHostAddress();
        } catch (UnknownHostException | SocketException e) {
            log.error("get localhost address error.", e);
        }

        String env = DtpProperties.getInstance().getEnv();
        if (StringUtils.isBlank(env)) {
            // fix #I8SSGQ
            env = ContextManagerHelper.getEnvironmentProperty("spring.profiles.active");
        }
        if (StringUtils.isBlank(env)) {
            String[] profiles = ContextManagerHelper.getActiveProfiles();
            if (profiles.length < 1) {
                profiles = ContextManagerHelper.getDefaultProfiles();
            }
            if (profiles.length >= 1) {
                env = profiles[0];
            }
        }

        SERVICE_INSTANCE = new ServiceInstance(address, port, appName, env);
    }

    public static ServiceInstance getInstance() {
        return SERVICE_INSTANCE;
    }

    private static InetAddress getLocalHostExactAddress() throws SocketException, UnknownHostException {
        InetAddress candidateAddress = null;
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            for (Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses(); inetAddresses.hasMoreElements(); ) {
                InetAddress inetAddress = inetAddresses.nextElement();
                if (!inetAddress.isLoopbackAddress() && inetAddress.isSiteLocalAddress()) {
                    if (!networkInterface.isPointToPoint()) {
                        return inetAddress;
                    } else {
                        candidateAddress = inetAddress;
                    }
                }
            }
        }
        return candidateAddress == null ? InetAddress.getLocalHost() : candidateAddress;
    }
}