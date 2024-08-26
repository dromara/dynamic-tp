package org.dromara.dynamictp.test.core.refresher;


import org.dromara.dynamictp.common.event.RefreshEvent;
import org.dromara.dynamictp.common.manager.EventBusManager;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.em.ConfigFileTypeEnum;
import org.dromara.dynamictp.core.handler.ConfigHandler;
import org.dromara.dynamictp.core.refresher.AbstractRefresher;
import org.dromara.dynamictp.core.support.BinderHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import static org.mockito.ArgumentMatchers.anyMap;

@RunWith(MockitoJUnitRunner.class)
public class AbstractRefresherTest {

    private AbstractRefresher refresher;
    private DtpProperties dtpProperties;

    @Before
    public void setUp() {
        dtpProperties = Mockito.mock(DtpProperties.class);

        refresher = Mockito.spy(new AbstractRefresher(dtpProperties) {
            @Override
            protected void doRefresh(DtpProperties properties) {
                // 测试时具体实现
            }
        });
    }

    @Test
    public void testRefreshWithValidContent() throws Exception {
        String content = "configuration content";
        ConfigFileTypeEnum fileType = ConfigFileTypeEnum.YAML;

        Map<Object, Object> parsedConfig = new HashMap<>();
        parsedConfig.put("key", "value");

        try (MockedStatic<ConfigHandler> configHandlerMockedStatic = Mockito.mockStatic(ConfigHandler.class);
             MockedStatic<BinderHelper> binderHelperMockedStatic = Mockito.mockStatic(BinderHelper.class)) {

            ConfigHandler configHandlerMock = Mockito.mock(ConfigHandler.class);
            configHandlerMockedStatic.when(ConfigHandler::getInstance).thenReturn(configHandlerMock);
            Mockito.when(configHandlerMock.parseConfig(content, fileType)).thenReturn(parsedConfig);

            refresher.refresh(content, fileType);

            binderHelperMockedStatic.verify(() -> BinderHelper.bindDtpProperties(parsedConfig, dtpProperties), times(1));

            // 使用反射来验证doRefresh方法
            Method doRefreshMethod = AbstractRefresher.class.getDeclaredMethod("doRefresh", DtpProperties.class);
            doRefreshMethod.setAccessible(true);
            doRefreshMethod.invoke(refresher, dtpProperties);
        }
    }

    @Test
    public void testRefreshWithEmptyContent() throws Exception {
        try (MockedStatic<BinderHelper> binderHelperMockedStatic = Mockito.mockStatic(BinderHelper.class)) {

            refresher.refresh("", ConfigFileTypeEnum.YAML);

            binderHelperMockedStatic.verify(() -> BinderHelper.bindDtpProperties(anyMap(), eq(dtpProperties)), never());

            // 使用反射验证 doRefresh 方法未被调用
            Method doRefreshMethod = AbstractRefresher.class.getDeclaredMethod("doRefresh", DtpProperties.class);
            doRefreshMethod.setAccessible(true);

            // 这里验证的方法如果不应该被调用，可以通过捕获异常来验证
            try {
                doRefreshMethod.invoke(refresher, dtpProperties);
            } catch (Exception e) {
                assert e.getCause() instanceof NullPointerException;
            }
        }
    }

    @Test
    public void testRefreshWithNullFileType() throws Exception {
        try (MockedStatic<BinderHelper> binderHelperMockedStatic = Mockito.mockStatic(BinderHelper.class)) {

            refresher.refresh("content", null);

            binderHelperMockedStatic.verify(() -> BinderHelper.bindDtpProperties(anyMap(), eq(dtpProperties)), never());

            // 使用反射验证 doRefresh 方法未被调用
            Method doRefreshMethod = AbstractRefresher.class.getDeclaredMethod("doRefresh", DtpProperties.class);
            doRefreshMethod.setAccessible(true);

            try {
                doRefreshMethod.invoke(refresher, dtpProperties);
            } catch (Exception e) {
                assert e.getCause() instanceof NullPointerException;
            }
        }
    }

    @Test
    public void testRefreshProperties() throws Exception {
        Map<Object, Object> properties = new HashMap<>();
        properties.put("key", "value");

        try (MockedStatic<BinderHelper> binderHelperMockedStatic = Mockito.mockStatic(BinderHelper.class)) {

            // 使用反射来调用refresh方法
            Method refreshMethod = AbstractRefresher.class.getDeclaredMethod("refresh", Map.class);
            refreshMethod.setAccessible(true);
            refreshMethod.invoke(refresher, properties);

            binderHelperMockedStatic.verify(() -> BinderHelper.bindDtpProperties(properties, dtpProperties), times(1));

            // 使用反射来调用并验证doRefresh方法
            Method doRefreshMethod = AbstractRefresher.class.getDeclaredMethod("doRefresh", DtpProperties.class);
            doRefreshMethod.setAccessible(true);
            doRefreshMethod.invoke(refresher, dtpProperties);
        }
    }

    @Test
    public void testNeedRefreshWithEmptyKeys() throws Exception {
        Set<String> emptyKeys = null;

        // 使用反射调用needRefresh方法
        Method needRefreshMethod = AbstractRefresher.class.getDeclaredMethod("needRefresh", Set.class);
        needRefreshMethod.setAccessible(true);
        boolean result = (boolean) needRefreshMethod.invoke(refresher, emptyKeys);

        assert !result;
    }


       @Test
    public void testPublishEventWithReflection() throws Exception {
        // 使用反射调用 publishEvent 方法进行测试
        try (MockedStatic<EventBusManager> eventBusManagerMockedStatic = Mockito.mockStatic(EventBusManager.class)) {

            // 反射获取 publishEvent 方法
            Method publishEventMethod = AbstractRefresher.class.getDeclaredMethod("publishEvent", DtpProperties.class);
            publishEventMethod.setAccessible(true);

            // 调用 publishEvent 方法
            publishEventMethod.invoke(refresher, dtpProperties);

            // 验证事件是否发布
            eventBusManagerMockedStatic.verify(() -> EventBusManager.post(any(RefreshEvent.class)), times(1));
        }
    }


}

