package com.dtp.core.parser;

import com.dtp.common.em.ConfigFileTypeEnum;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ByteArrayResource;

import java.util.List;
import java.util.Map;

/**
 * YamlConfigParser related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
public class YamlConfigParser extends AbstractConfigParser {

    private static final List<ConfigFileTypeEnum> CONFIG_TYPE = Lists.newArrayList(
            ConfigFileTypeEnum.YML, ConfigFileTypeEnum.YAML);

    @Override
    public List<ConfigFileTypeEnum> type() {
        return CONFIG_TYPE;
    }

    @Override
    public Map<Object, Object> doParse(String content) {

        if (StringUtils.isEmpty(content)) {
            return Maps.newHashMapWithExpectedSize(0);
        }

        YamlPropertiesFactoryBean bean = new YamlPropertiesFactoryBean();
        bean.setResources(new ByteArrayResource(content.getBytes()));
        return bean.getObject();
    }
}
