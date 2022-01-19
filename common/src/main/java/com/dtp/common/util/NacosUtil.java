package com.dtp.common.util;

import com.dtp.common.em.ConfigFileTypeEnum;
import com.dtp.common.config.DtpProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

/**
 * NacosUtil related
 *
 * @author yanhom
 * @since 1.0.0
 */
public class NacosUtil {

    private NacosUtil() {}

    public static String deduceDataId(DtpProperties.Nacos nacos,
                                      Environment environment,
                                      ConfigFileTypeEnum configFileType) {
        String dataId;
        if (nacos != null && StringUtils.isNotBlank(nacos.getDataId())) {
            dataId = nacos.getDataId();
        } else {
            String[] profiles = environment.getActiveProfiles();
            if (profiles.length < 1) {
                profiles = environment.getDefaultProfiles();
            }

            String appName = environment.getProperty("spring.application.name");
            appName = StringUtils.isNoneBlank(appName) ? appName : "application";

            // default dataId style, for example: demo-dev
            dataId = appName + "-" + profiles[0] + "." + configFileType.getValue();
        }

        return dataId;
    }

    public static String getGroup(DtpProperties.Nacos nacos, String defaultGroup) {
        String group = defaultGroup;
        if (nacos != null && StringUtils.isNotBlank(nacos.getGroup())) {
            group = nacos.getGroup();
        }
        return group;
    }

    public static ConfigFileTypeEnum getConfigType(DtpProperties dtpProperties,
                                                   ConfigFileTypeEnum defaultType) {
        ConfigFileTypeEnum configFileType = defaultType;
        if (StringUtils.isNotBlank(dtpProperties.getConfigType())) {
            configFileType = ConfigFileTypeEnum.of(dtpProperties.getConfigType());
        }
        return configFileType;
    }
}
