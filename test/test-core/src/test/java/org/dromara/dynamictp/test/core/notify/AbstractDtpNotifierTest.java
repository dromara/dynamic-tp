package org.dromara.dynamictp.test.core.notify;

import com.google.common.collect.Lists;
import org.dromara.dynamictp.common.ApplicationContextHolder;
import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.common.entity.NotifyPlatform;
import org.dromara.dynamictp.common.entity.ServiceInstance;
import org.dromara.dynamictp.common.entity.TpMainFields;
import org.dromara.dynamictp.common.util.CommonUtil;
import org.dromara.dynamictp.core.notifier.AbstractDtpNotifier;
import org.dromara.dynamictp.core.notifier.DtpDingNotifier;
import org.dromara.dynamictp.core.notifier.base.Notifier;
import org.dromara.dynamictp.core.notifier.context.AlarmCtx;
import org.dromara.dynamictp.core.notifier.context.DtpNotifyCtxHolder;
import org.dromara.dynamictp.core.notifier.context.NoticeCtx;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.ThreadPoolCreator;
import org.dromara.dynamictp.core.thread.DtpExecutor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.util.List;

import static org.dromara.dynamictp.common.em.QueueTypeEnum.VARIABLE_LINKED_BLOCKING_QUEUE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * AbstractDtpNotifierTest related
 *
 * @author ruoan
 * @since 1.1.3
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ApplicationContextHolder.class, CommonUtil.class})
@SuppressStaticInitializationFor("com.dtp.common.util.CommonUtil")
public class AbstractDtpNotifierTest {

    private final Notifier notifier = Mockito.mock(Notifier.class);

    private final DtpExecutor dtpExecutor = ThreadPoolCreator.createDynamicFast("test");

    @Before
    public void setUp() {
        ApplicationContext contextMock = mock(ApplicationContext.class);
        PowerMockito.mockStatic(ApplicationContextHolder.class);
        when(ApplicationContextHolder.getInstance()).thenAnswer((Answer<ApplicationContext>) c -> contextMock);

        Environment envMock = mock(Environment.class);
        when(ApplicationContextHolder.getEnvironment()).thenAnswer((Answer<Environment>) c -> envMock);
        when(envMock.getProperty("spring.application.name")).thenReturn("test");
        when(envMock.getProperty("server.port")).thenReturn("8080");
        when(envMock.getActiveProfiles()).thenReturn(new String[]{"dev"});

        PowerMockito.mockStatic(CommonUtil.class);
        when(CommonUtil.getInstance()).thenAnswer((Answer<ServiceInstance>) invocation ->
                new ServiceInstance("localhost", 8080, "test", "dev"));
        Assert.assertEquals("localhost", CommonUtil.getInstance().getIp());
        Assert.assertEquals(8080, CommonUtil.getInstance().getPort());
        Assert.assertEquals("test", CommonUtil.getInstance().getServiceName());
        Assert.assertEquals("dev", CommonUtil.getInstance().getEnv());
    }

    @Test
    public void testSendChangeMsg() {
        AbstractDtpNotifier notifier = new DtpDingNotifier(this.notifier);
        NotifyPlatform notifyPlatform = new NotifyPlatform();
        TpMainFields oldFields = new TpMainFields();
        List<String> diffs = Lists.newArrayList("corePoolSize");
        DtpNotifyCtxHolder.set(new NoticeCtx(ExecutorWrapper.of(dtpExecutor), new NotifyItem(), oldFields, diffs));
        notifier.sendChangeMsg(notifyPlatform, oldFields, diffs);

        Mockito.verify(this.notifier, Mockito.times(1)).send(any(), anyString());
    }

    @Test
    public void testSendAlarmMsg() {
        AbstractDtpNotifier notifier = new DtpDingNotifier(this.notifier);
        NotifyPlatform notifyPlatform = new NotifyPlatform();
        NotifyItemEnum notifyItemEnum = NotifyItemEnum.LIVENESS;
        DtpNotifyCtxHolder.set(new AlarmCtx(ExecutorWrapper.of(dtpExecutor), new NotifyItem()));
        notifier.sendAlarmMsg(notifyPlatform, notifyItemEnum);

        Mockito.verify(this.notifier, Mockito.times(1)).send(any(), anyString());
    }

    @Test
    public void testGetQueueName2() {
        Assert.assertEquals(dtpExecutor.getQueueType(), VARIABLE_LINKED_BLOCKING_QUEUE.getName());
    }
}
