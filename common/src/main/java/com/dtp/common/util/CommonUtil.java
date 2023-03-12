package com.dtp.common.util;

import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.entity.ServiceInstance;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

    private CommonUtil() { }

    private static final ServiceInstance SERVICE_INSTANCE;

    static {
        Environment environment = ApplicationContextHolder.getEnvironment();

        String appName = environment.getProperty("spring.application.name");
        appName = StringUtils.isNoneBlank(appName) ? appName : "application";

        String portStr = environment.getProperty("server.port");
        int port = StringUtils.isNotBlank(portStr) ? Integer.parseInt(portStr) : 0;

        String address = null;
        try {
            address = getLocalHostExactAddress().getHostAddress();
        } catch (UnknownHostException | SocketException e) {
            log.error("get localhost address error.", e);
        }

        String[] profiles = environment.getActiveProfiles();
        if (profiles.length < 1) {
            profiles = environment.getDefaultProfiles();
        }
        SERVICE_INSTANCE = new ServiceInstance(address, port, appName, profiles[0]);

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
