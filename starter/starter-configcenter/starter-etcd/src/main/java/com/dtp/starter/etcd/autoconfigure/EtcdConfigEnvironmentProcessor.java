package com.dtp.starter.etcd.autoconfigure;

import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.properties.DtpProperties;
import com.dtp.core.spring.PropertiesBinder;
import com.dtp.starter.etcd.util.EtcdUtil;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;

import javax.annotation.Resource;

/**
 * @author Redick01
 */
public class EtcdConfigEnvironmentProcessor implements EnvironmentPostProcessor, Ordered {

    public static final String ETCD_PROPERTY_SOURCE_NAME = "etcdPropertySource";

    @Resource
    private DtpProperties dtpProperties;
    
    @SneakyThrows
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
            SpringApplication application) {
        final PropertiesBinder propertiesBinder = ApplicationContextHolder.getBean(PropertiesBinder.class);
        dtpProperties = propertiesBinder.bindDtpProperties(environment, dtpProperties);
        DtpProperties.Etcd etcd = dtpProperties.getEtcd();
        val properties = EtcdUtil.getConfigMap(etcd, dtpProperties.getConfigType());
        if (!checkPropertyExist(environment)) {
            createPropertySource(environment, properties);
        }
    }

    /**
     * check environment property exist.
     * @param environment {@link ConfigurableEnvironment}
     * @return result
     */
    private boolean checkPropertyExist(ConfigurableEnvironment environment) {
        MutablePropertySources propertySources = environment.getPropertySources();
        return propertySources.stream().anyMatch(p -> ETCD_PROPERTY_SOURCE_NAME.equals(p.getName()));
    }

    /**
     * create environment property
     * @param environment {@link ConfigurableEnvironment}
     * @param properties config info
     */
    private void createPropertySource(ConfigurableEnvironment environment, Map<Object, Object> properties) {
        MutablePropertySources propertySources = environment.getPropertySources();
        OriginTrackedMapPropertySource source = new OriginTrackedMapPropertySource(ETCD_PROPERTY_SOURCE_NAME,
                properties);
        propertySources.addLast(source);
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
