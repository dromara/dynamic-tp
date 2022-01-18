package io.lyh.dynamic.tp.common.util;

import io.lyh.dynamic.tp.common.config.DtpProperties;
import io.lyh.dynamic.tp.common.em.ConfigFileTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

/**
 * NacosUtil related
 *
 * @author yanhom
 */
public class NacosUtil {

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
        if (StringUtils.isNotBlank(nacos.getGroup())) {
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
