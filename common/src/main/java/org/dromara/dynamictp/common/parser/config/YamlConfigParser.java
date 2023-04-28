package org.dromara.dynamictp.common.parser.config;

import org.dromara.dynamictp.common.em.ConfigFileTypeEnum;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ByteArrayResource;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * YamlConfigParser related
 *
 * @author yanhom
 * @since 1.0.0
 **/
public class YamlConfigParser extends AbstractConfigParser {

    private static final List<ConfigFileTypeEnum> CONFIG_TYPES = Lists.newArrayList(
            ConfigFileTypeEnum.YML, ConfigFileTypeEnum.YAML);

    @Override
    public List<ConfigFileTypeEnum> types() {
        return CONFIG_TYPES;
    }

    @Override
    public Map<Object, Object> doParse(String content) {

        if (StringUtils.isEmpty(content)) {
            return Collections.emptyMap();
        }
        YamlPropertiesFactoryBean bean = new YamlPropertiesFactoryBean();
        bean.setResources(new ByteArrayResource(content.getBytes()));
        return bean.getObject();
    }
}
