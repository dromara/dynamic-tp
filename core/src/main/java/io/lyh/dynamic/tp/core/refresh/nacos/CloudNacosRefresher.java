package io.lyh.dynamic.tp.core.refresh.nacos;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import io.lyh.dynamic.tp.common.config.DtpProperties;
import io.lyh.dynamic.tp.common.em.ConfigFileTypeEnum;
import io.lyh.dynamic.tp.common.util.NacosUtil;
import io.lyh.dynamic.tp.core.refresh.AbstractRefresher;
import io.lyh.dynamic.tp.core.support.DtpCreator;
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

        DtpProperties.Nacos nacos = dtpProperties.getNacos();
        configFileType = NacosUtil.getConfigType(dtpProperties,
                ConfigFileTypeEnum.of(nacosConfigProperties.getFileExtension()));
        String dataId = NacosUtil.deduceDataId(nacos, environment, configFileType);
        String group = NacosUtil.getGroup(nacos, nacosConfigProperties.getGroup());

        try {
            nacosConfigManager.getConfigService().addListener(dataId, group, this);
        } catch (NacosException e) {
            log.error("DynamicTp refresher, {} addListener error", getClass().getSimpleName());
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
