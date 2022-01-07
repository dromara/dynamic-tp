package io.lyh.dtp.refresh;

import io.lyh.dtp.common.em.ConfigFileTypeEnum;
import io.lyh.dtp.config.DtpProperties;
import io.lyh.dtp.core.DtpKeeper;
import io.lyh.dtp.handler.ConfigChangeHandler;
import io.lyh.dtp.common.constant.DynamicTpConst;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * AbstractRefresher related
 *
 * @author: yanhom1314@gmail.com
 * @date: 2021-12-29 18:20
 * @since 1.0.0
 **/
@Slf4j
public abstract class AbstractRefresher implements Refresher {

    @Resource
    private DtpProperties dtpProperties;

    @Override
    public void refresh(String content, ConfigFileTypeEnum fileTypeEnum) {

        if (StringUtils.isBlank(content) || Objects.isNull(fileTypeEnum)) {
            return;
        }

        try {
            val prop = ConfigChangeHandler.getInstance().parseConfig(content, fileTypeEnum);
            doRefresh(prop);
        } catch (IOException e) {
            log.error("DynamicTp refresh error, content: {}, fileType: {}", content, fileTypeEnum, e);
        }
    }

    private void doRefresh(Map<Object, Object> properties) {
        ConfigurationPropertySource sources = new MapConfigurationPropertySource(properties);
        Binder binder = new Binder(sources);
        DtpProperties bindDtpProperties = binder.bind(DynamicTpConst.MAIN_PROPERTIES_PREFIX, Bindable.ofInstance(dtpProperties)).get();
        DtpKeeper.refresh(bindDtpProperties);
    }

}
