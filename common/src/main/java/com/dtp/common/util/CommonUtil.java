package com.dtp.common.util;

import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.entity.Instance;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * CommonUtil related
 *
 * @author yanhom
 */
@Slf4j
public final class CommonUtil {

    private CommonUtil() { }

    private static final Instance INSTANCE;

    static {
        Environment environment = ApplicationContextHolder.getEnvironment();

        String appName = environment.getProperty("spring.application.name");
        appName = StringUtils.isNoneBlank(appName) ? appName : "application";

        String portStr = environment.getProperty("server.port");
        int port = StringUtils.isNotBlank(portStr) ? Integer.parseInt(portStr) : 0;

        String address = null;
        try {
            address = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.error("get localhost address error.", e);
        }

        String[] profiles = environment.getActiveProfiles();
        if (profiles.length < 1) {
            profiles = environment.getDefaultProfiles();
        }
        INSTANCE = new Instance(address, port, appName, profiles[0]);

    }

    public static Instance getInstance() {
        return INSTANCE;
    }
}
