package org.dromara.dynamictp.spring.ex;



import org.dromara.dynamictp.common.manager.ContextManager;
import org.dromara.dynamictp.common.manager.EventBusManager;
import org.dromara.dynamictp.common.manager.RefreshedEvent;
import org.dromara.dynamictp.core.support.DtpBannerPrinter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.*;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

public class SpringContextHolder implements ContextManager, ApplicationContextAware, ApplicationListener<ApplicationEvent> {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
        DtpBannerPrinter.printBanner();  // 打印 banner
    }

    @Override
    public <T> T getBean(Class<T> clazz) {
        return getInstance().getBean(clazz);
    }

    @Override
    public <T> T getBean(String name, Class<T> clazz) {
        return getInstance().getBean(name, clazz);
    }

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return getInstance().getBeansOfType(clazz);
    }

    public static ApplicationContext getInstance() {
        if (Objects.isNull(context)) {
            throw new NullPointerException("ApplicationContext is null, please check if the spring container is started.");
        }
        return context;
    }

    @Override
    public Environment getEnvironment() {
        return getInstance().getEnvironment();
    }


    public void publishEvent(Object event) {
        if (event instanceof ApplicationEvent) {
            getInstance().publishEvent((ApplicationEvent) event);
        }
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (isOriginalEventSource(event) && event instanceof ApplicationContextEvent) {
            if (event instanceof ContextRefreshedEvent) {
                onContextRefreshedEvent((ContextRefreshedEvent) event);
            } else if (event instanceof ContextStartedEvent) {
                onContextStartedEvent((ContextStartedEvent) event);
            } else if (event instanceof ContextStoppedEvent) {
                onContextStoppedEvent((ContextStoppedEvent) event);
            } else if (event instanceof ContextClosedEvent) {
                onContextClosedEvent((ContextClosedEvent) event);
            }
        }
    }

    protected void onContextRefreshedEvent(ContextRefreshedEvent event) {
        RefreshedEvent refreshedEvent = new RefreshedEvent(this);
        EventBusManager.post(refreshedEvent);
    }

    protected void onContextStartedEvent(ContextStartedEvent event) {
        // Override to handle ContextStartedEvent
    }

    protected void onContextStoppedEvent(ContextStoppedEvent event) {
        // Override to handle ContextStoppedEvent
    }

    protected void onContextClosedEvent(ContextClosedEvent event) {
        // Override to handle ContextClosedEvent
    }

    private boolean isOriginalEventSource(ApplicationEvent event) {
        return Objects.equals(context, event.getSource());
    }

    @Override
    public void onEvent(Object event) {
        if (event instanceof ApplicationEvent) {
            onApplicationEvent((ApplicationEvent) event);
        }
    }

    @Override
    public String getEnvironmentProperty(String key) {
        return getInstance().getEnvironment().getProperty(key);
    }

    @Override
    public String getEnvironmentProperty(String key, String defaultValue) {
        return getInstance().getEnvironment().getProperty(key, defaultValue);
    }

    @Override
    public String[] getActiveProfiles() {
        return getInstance().getEnvironment().getActiveProfiles();
    }

    @Override
    public String[] getDefaultProfiles() {
        return getInstance().getEnvironment().getDefaultProfiles();
    }

    @Override
    public void setContext(Object context) {
        if (context instanceof ApplicationContext) {
            setApplicationContext((ApplicationContext) context);
        }
    }
}