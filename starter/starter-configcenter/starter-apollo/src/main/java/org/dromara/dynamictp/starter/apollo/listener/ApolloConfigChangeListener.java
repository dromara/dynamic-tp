package org.dromara.dynamictp.starter.apollo.listener;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.property.PlaceholderHelper;
import com.ctrip.framework.apollo.spring.property.SpringValue;
import com.ctrip.framework.apollo.spring.property.SpringValueRegistry;
import com.ctrip.framework.apollo.spring.util.SpringInjector;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Set;

/**
 * ApolloConfigChangeListener
 * <p>Listen for configuration file changes</p>
 *
 * @author BanTanger
 * @since 1.0.0
 **/
@Slf4j
public class ApolloConfigChangeListener implements ConfigChangeListener {

    private final boolean typeConverterHasConvertIfNecessaryWithFieldParameter;
    private final Environment environment;
    private final ConfigurableBeanFactory beanFactory;
    private final TypeConverter typeConverter;
    private final PlaceholderHelper placeholderHelper;
    private final SpringValueRegistry springValueRegistry;
    private final Gson gson;

    public ApolloConfigChangeListener(Environment environment, ConfigurableListableBeanFactory beanFactory){
        this.typeConverterHasConvertIfNecessaryWithFieldParameter = testTypeConverterHasConvertIfNecessaryWithFieldParameter();
        this.beanFactory = beanFactory;
        this.typeConverter = this.beanFactory.getTypeConverter();
        this.environment = environment;
        this.placeholderHelper = SpringInjector.getInstance(PlaceholderHelper.class);
        this.springValueRegistry = SpringInjector.getInstance(SpringValueRegistry.class);
        this.gson = new Gson();
    }

    @Override
    public void onChange(ConfigChangeEvent changeEvent) {
        Set<String> keys = changeEvent.changedKeys();
        if (CollectionUtils.isEmpty(keys)) {
            return ;
        }
        for (String key : keys) {
            // 1. 检查配置文件更新项是否相关
            Collection<SpringValue> targetValues = springValueRegistry.get(beanFactory, key);
            if (targetValues == null || targetValues.isEmpty()) {
                continue;
            }
            // 2. 更新数据
            for (SpringValue targetValue : targetValues) {
                updateSpringValue(targetValue);
            }
        }

    }

    private void updateSpringValue(SpringValue targetValue) {
        try {
            Object value = resolvePropertyValue(targetValue);
            targetValue.update(value);

            log.info("Auto update apollo changed value successfully, new value: {}, {}", value,
                    targetValue);
        } catch (Throwable ex) {
            log.error("Auto update apollo changed value failed, {}", targetValue.toString(), ex);
        }
    }

    /**
     * Logic transplanted from DefaultListableBeanFactory
     * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#doResolveDependency(org.springframework.beans.factory.config.DependencyDescriptor, java.lang.String, java.util.Set, org.springframework.beans.TypeConverter)
     */
    private Object resolvePropertyValue(SpringValue springValue) {
        // value will never be null, as @Value and @ApolloJsonValue will not allow that
        Object value = placeholderHelper
                .resolvePropertyValue(beanFactory, springValue.getBeanName(), springValue.getPlaceholder());

        if (springValue.isJson()) {
            value = parseJsonValue((String)value, springValue.getGenericType());
        } else {
            if (springValue.isField()) {
                // org.springframework.beans.TypeConverter#convertIfNecessary(java.lang.Object, java.lang.Class, java.lang.reflect.Field) is available from Spring 3.2.0+
                if (typeConverterHasConvertIfNecessaryWithFieldParameter) {
                    value = this.typeConverter
                            .convertIfNecessary(value, springValue.getTargetType(), springValue.getField());
                } else {
                    value = this.typeConverter.convertIfNecessary(value, springValue.getTargetType());
                }
            } else {
                value = this.typeConverter.convertIfNecessary(value, springValue.getTargetType(),
                        springValue.getMethodParameter());
            }
        }

        return value;
    }

    private Object parseJsonValue(String json, Type targetType) {
        try {
            return gson.fromJson(json, targetType);
        } catch (Throwable ex) {
            log.error("Parsing json '{}' to type {} failed!", json, targetType, ex);
            throw ex;
        }
    }

    private boolean testTypeConverterHasConvertIfNecessaryWithFieldParameter() {
        try {
            TypeConverter.class.getMethod("convertIfNecessary", Object.class, Class.class, Field.class);
        } catch (Throwable ex) {
            return false;
        }

        return true;
    }

}
