package com.dtp.core.refresh;

import cn.hutool.core.map.MapUtil;
import com.dtp.common.config.DtpProperties;
import com.dtp.common.em.ConfigFileTypeEnum;
import com.dtp.common.event.RefreshEvent;
import com.dtp.core.DtpRegistry;
import com.dtp.core.handler.ConfigHandler;
import com.dtp.core.support.PropertiesBinder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.ApplicationEventMulticaster;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * AbstractRefresher related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Slf4j
public abstract class AbstractRefresher implements Refresher {

    @Resource
    private DtpProperties dtpProperties;

    @Resource
    private ApplicationEventMulticaster applicationEventMulticaster;

    @Override
    public void refresh(String content, ConfigFileTypeEnum fileTypeEnum) {

        if (StringUtils.isBlank(content) || Objects.isNull(fileTypeEnum)) {
            log.warn("DynamicTp refresh, empty content or null fileType.");
            return;
        }

        try {
            val configHandler = ConfigHandler.getInstance();
            val properties = configHandler.parseConfig(content, fileTypeEnum);
            PropertiesBinder.bindDtpProperties(properties, dtpProperties);
            doRefresh(dtpProperties);
        } catch (IOException e) {
            log.error("DynamicTp refresh error, content: {}, fileType: {}", content, fileTypeEnum, e);
        }
    }

    protected void doRefresh(Map<Object, Object> properties) {
        if (MapUtil.isEmpty(properties)) {
            log.warn("DynamicTp refresh, empty properties.");
            return;
        }
        PropertiesBinder.bindDtpProperties(properties, dtpProperties);
        doRefresh(dtpProperties);
    }

    protected void doRefresh(DtpProperties dtpProperties) {
        DtpRegistry.refresh(dtpProperties);
        publishEvent(dtpProperties);
    }

    private void publishEvent(DtpProperties dtpProperties) {
        RefreshEvent event = new RefreshEvent(this, dtpProperties);
        applicationEventMulticaster.multicastEvent(event);
    }
}
