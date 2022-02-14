package com.dtp.starter.cloud.nacos.refresh;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.em.ConfigFileTypeEnum;
import com.dtp.common.util.NacosUtil;
import com.dtp.core.refresh.AbstractRefresher;
import com.dtp.core.support.ThreadPoolCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * SpringCloudNacosRefresher related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Slf4j
public class CloudNacosRefresher extends AbstractRefresher implements InitializingBean, Listener {

    private static final ThreadPoolExecutor EXECUTOR = ThreadPoolCreator.createCommonFast("nacos-listener");

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

        DtpProperties.Nacos nacos = dtpProperties.getNacos();
        configFileType = NacosUtil.getConfigType(dtpProperties,
                ConfigFileTypeEnum.of(nacosConfigProperties.getFileExtension()));
        String dataId = NacosUtil.deduceDataId(nacos, environment, configFileType);
        String group = NacosUtil.getGroup(nacos, nacosConfigProperties.getGroup());

        try {
            nacosConfigManager.getConfigService().addListener(dataId, group, this);
            log.info("DynamicTp refresher, add listener success, dataId: {}, group: {}", dataId, group);
        } catch (NacosException e) {
            log.error("DynamicTp refresher, add listener error, dataId: {}, group: {}", dataId, group, e);
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
