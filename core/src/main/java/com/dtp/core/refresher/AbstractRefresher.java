package com.dtp.core.refresher;

import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.em.ConfigFileTypeEnum;
import com.dtp.common.event.RefreshEvent;
import com.dtp.common.properties.DtpProperties;
import com.dtp.core.DtpRegistry;
import com.dtp.core.handler.ConfigHandler;
import com.dtp.core.spring.PropertiesBinder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * AbstractRefresher related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public abstract class AbstractRefresher implements Refresher {

    @Resource
    protected DtpProperties dtpProperties;
    
    @Resource
    private PropertiesBinder propertiesBinder;

    @Override
    public void refresh(String content, ConfigFileTypeEnum fileType) {

        if (StringUtils.isBlank(content) || Objects.isNull(fileType)) {
            log.warn("DynamicTp refresh, empty content or null fileType.");
            return;
        }

        try {
            val configHandler = ConfigHandler.getInstance();
            val properties = configHandler.parseConfig(content, fileType);
            doRefresh(properties);
        } catch (IOException e) {
            log.error("DynamicTp refresh error, content: {}, fileType: {}", content, fileType, e);
        }
    }

    protected void doRefresh(Map<Object, Object> properties) {
        if (MapUtils.isEmpty(properties)) {
            log.warn("DynamicTp refresh, empty properties.");
            return;
        }
        dtpProperties = propertiesBinder.bindDtpProperties(properties, dtpProperties);
        doRefresh(dtpProperties);
    }

    protected void doRefresh(DtpProperties dtpProperties) {
        DtpRegistry.refresh(dtpProperties);
        publishEvent(dtpProperties);
    }

    private void publishEvent(DtpProperties dtpProperties) {
        RefreshEvent event = new RefreshEvent(this, dtpProperties);
        ApplicationContextHolder.publishEvent(event);
    }
}
