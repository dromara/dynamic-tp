package org.dromara.dynamictp.starter.apollo.refresh;

import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.ConfigFileChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.model.ConfigFileChangeEvent;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.em.ConfigFileTypeEnum;
import org.dromara.dynamictp.core.refresher.AbstractRefresher;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

/**
 * ApolloRefresher related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public class ApolloRefresher extends AbstractRefresher implements ConfigFileChangeListener, InitializingBean {

    @Value("${apollo.bootstrap.namespaces:application}")
    private String namespace;

    private ConfigFileTypeEnum configFileType;

    @Override
    public void afterPropertiesSet() {

        String[] apolloNamespaces = this.namespace.split(",");
        String realNamespace = apolloNamespaces[0];
        DtpProperties.Apollo apollo = dtpProperties.getApollo();
        if (apollo != null && StringUtils.isNotBlank(apollo.getNamespace())) {
            realNamespace = apollo.getNamespace();
        }

        configFileType = deduceFileType(realNamespace);
        namespace = realNamespace.replaceAll("." + configFileType.getValue(), "");
        ConfigFileFormat configFileFormat = ConfigFileFormat.fromString(configFileType.getValue());
        ConfigFile configFile = ConfigService.getConfigFile(namespace, configFileFormat);
        try {
            configFile.addChangeListener(this);
            log.info("DynamicTp refresher, add listener success, namespace: {}", realNamespace);
        } catch (Exception e) {
            log.error("DynamicTp refresher, add listener error, namespace: {}", realNamespace, e);
        }
    }

    @Override
    public void onChange(ConfigFileChangeEvent changeEvent) {
        String content = changeEvent.getNewValue();
        refresh(content, configFileType);
    }

    private ConfigFileTypeEnum deduceFileType(String namespace) {
        ConfigFileTypeEnum configFileFormat = ConfigFileTypeEnum.PROPERTIES;
        if (namespace.contains(ConfigFileTypeEnum.YAML.getValue())) {
            configFileFormat = ConfigFileTypeEnum.YAML;
        } else if (namespace.contains(ConfigFileTypeEnum.YML.getValue())) {
            configFileFormat = ConfigFileTypeEnum.YML;
        } else if (namespace.contains(ConfigFileTypeEnum.JSON.getValue())) {
            configFileFormat = ConfigFileTypeEnum.JSON;
        } else if (namespace.contains(ConfigFileTypeEnum.XML.getValue())) {
            configFileFormat = ConfigFileTypeEnum.XML;
        } else if (namespace.contains(ConfigFileTypeEnum.TXT.getValue())) {
            configFileFormat = ConfigFileTypeEnum.TXT;
        }

        return configFileFormat;
    }
}
