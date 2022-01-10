package io.lyh.dtp.refresh.nacos;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import io.lyh.dtp.common.em.ConfigFileTypeEnum;
import io.lyh.dtp.config.DtpProperties;
import io.lyh.dtp.refresh.AbstractRefresher;
import io.lyh.dtp.support.DtpCreator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * NacosRefresher related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Slf4j
public class NacosRefresher extends AbstractRefresher implements InitializingBean, Listener {

    private static final ThreadPoolExecutor EXECUTOR = DtpCreator.createCommonFast("nacos-listener");

    private ConfigFileTypeEnum configFileType;

    @Resource
    private NacosConfigManager nacosConfigManager;

    @Resource
    private DtpProperties dtpProperties;

    @Resource
    private NacosConfigProperties nacosConfigProperties;

    @Resource
    private Environment environment;

    @Override
    public void afterPropertiesSet() {

        // get config file type
        if (StringUtils.isNotBlank(dtpProperties.getConfigType())) {
            configFileType = ConfigFileTypeEnum.of(dtpProperties.getConfigType());
        } else {
            configFileType = ConfigFileTypeEnum.of(nacosConfigProperties.getFileExtension());
        }

        // get dataId and group
        String group;
        String dataId;
        DtpProperties.Nacos nacos = dtpProperties.getNacos();
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

        if (nacos != null && StringUtils.isNotBlank(nacos.getGroup())) {
            group = nacos.getGroup();
        } else {
            group = nacosConfigProperties.getGroup();
        }

        try {
            nacosConfigManager.getConfigService().addListener(dataId, group, this);
        } catch (NacosException e) {
            log.error("DynamicTp, {} addListener error", getClass().getSimpleName());
        }
    }

    @Override
    public Executor getExecutor() {
        return EXECUTOR;
    }

    @Override
    public void receiveConfigInfo(String content) {
        refresh(content, configFileType);
    }

}
