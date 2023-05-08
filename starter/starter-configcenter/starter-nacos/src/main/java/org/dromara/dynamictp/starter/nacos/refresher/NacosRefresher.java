package org.dromara.dynamictp.starter.nacos.refresher;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.em.ConfigFileTypeEnum;
import org.dromara.dynamictp.common.util.NacosUtil;
import org.dromara.dynamictp.core.refresher.AbstractRefresher;
import org.dromara.dynamictp.core.support.ThreadPoolCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * NacosRefresher related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public class NacosRefresher extends AbstractRefresher implements InitializingBean, DisposableBean, Listener {

    private static final ThreadPoolExecutor EXECUTOR = ThreadPoolCreator.createCommonFast("nacos-listener");

    private ConfigFileTypeEnum configFileType;

    @NacosInjected
    private ConfigService configService;

    @Resource
    private Environment environment;

    @Override
    public void afterPropertiesSet() {

        DtpProperties.Nacos nacos = dtpProperties.getNacos();
        configFileType = NacosUtil.getConfigType(dtpProperties, ConfigFileTypeEnum.PROPERTIES);
        String dataId = NacosUtil.deduceDataId(nacos, environment, configFileType);
        String group = NacosUtil.getGroup(nacos, "DEFAULT_GROUP");

        try {
            configService.addListener(dataId, group, this);
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

    @Override
    public void destroy() {
        EXECUTOR.shutdown();
    }

}
