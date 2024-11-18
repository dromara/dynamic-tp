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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.dynamictp.common.entity.ServiceInstance;
import org.dromara.dynamictp.common.manager.ContextManagerHelper;
import org.dromara.dynamictp.common.properties.DtpProperties;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.APP_ENV_KEY;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.APP_NAME_KEY;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.APP_PORT_KEY;

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
        String address = null;
        try {
            address = getLocalHostExactAddress().getHostAddress();
        } catch (UnknownHostException | SocketException e) {
            log.error("get localhost address error.", e);
        }

        String env = DtpProperties.getInstance().getEnv();
        if (StringUtils.isBlank(env)) {
            env = ContextManagerHelper.getEnvironmentProperty(APP_ENV_KEY);
        }
        String appName = ContextManagerHelper.getEnvironmentProperty(APP_NAME_KEY);
        String portStr = ContextManagerHelper.getEnvironmentProperty(APP_PORT_KEY);
        int port = StringUtils.isNotBlank(portStr) ? Integer.parseInt(portStr) : 0;
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
