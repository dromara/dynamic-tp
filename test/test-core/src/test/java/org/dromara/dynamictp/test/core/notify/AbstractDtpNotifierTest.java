package org.dromara.dynamictp.test.core.notify;

import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.common.entity.NotifyPlatform;
import org.dromara.dynamictp.common.entity.ServiceInstance;
import org.dromara.dynamictp.common.entity.TpMainFields;
import org.dromara.dynamictp.common.util.CommonUtil;
import org.dromara.dynamictp.core.notifier.AbstractDtpNotifier;
import org.dromara.dynamictp.core.notifier.DtpDingNotifier;
import org.dromara.dynamictp.core.notifier.base.Notifier;
import org.dromara.dynamictp.core.notifier.capture.CapturedExecutor;
import org.dromara.dynamictp.core.notifier.context.AlarmCtx;
import org.dromara.dynamictp.core.notifier.context.DtpNotifyCtxHolder;
import org.dromara.dynamictp.core.notifier.context.NoticeCtx;
import org.dromara.dynamictp.core.spring.EnableDynamicTp;
import org.dromara.dynamictp.core.support.ExecutorAdapter;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.ThreadPoolCreator;
import org.dromara.dynamictp.core.thread.DtpExecutor;
import org.dromara.dynamictp.test.core.thread.DtpExecutorTest;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.reflect.Method;
import java.util.List;

import static org.dromara.dynamictp.common.em.QueueTypeEnum.VARIABLE_LINKED_BLOCKING_QUEUE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * AbstractDtpNotifierTest related
 *
 * @author ruoan
 * @since 1.1.3
 */
@Slf4j
@EnableDynamicTp
public class AbstractDtpNotifierTest {

    private final Notifier notifier = Mockito.mock(Notifier.class);

    private final DtpExecutor dtpExecutor = ThreadPoolCreator.createDynamicFast("test");

    private ConfigurableApplicationContext context;

    private MockedStatic<CommonUtil> mockedStatic;

    @Before
    public void setUp() {
        context = SpringApplication.run(DtpExecutorTest.class);
        mockedStatic = Mockito.mockStatic(CommonUtil.class);
        mockedStatic.when(CommonUtil::getInstance).thenAnswer((Answer<ServiceInstance>) invocation ->
                new ServiceInstance("localhost", 8080, "test", "dev"));
        Assert.assertEquals("localhost", CommonUtil.getInstance().getIp());
        Assert.assertEquals(8080, CommonUtil.getInstance().getPort());
        Assert.assertEquals("test", CommonUtil.getInstance().getServiceName());
        Assert.assertEquals("dev", CommonUtil.getInstance().getEnv());
    }

    @After
    public void after() {
        mockedStatic.close();
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
    public void testGetQueueName1() throws Exception {
        Method getQueueName = AbstractDtpNotifier.class.getDeclaredMethod("getQueueName", ExecutorAdapter.class);
        getQueueName.setAccessible(true);

        AbstractDtpNotifier abstractDtpNotifier = Mockito.spy(AbstractDtpNotifier.class);
        CapturedExecutor capturedExecutor = new CapturedExecutor(dtpExecutor);
        String res = (String) getQueueName.invoke(abstractDtpNotifier, capturedExecutor);

        Assert.assertEquals(res, VARIABLE_LINKED_BLOCKING_QUEUE.getName());
        String simpleName = capturedExecutor.getQueue().getClass().getSimpleName();
        Assert.assertEquals(simpleName, "CapturedBlockingQueue");
    }

    @Test
    public void testGetQueueName2() throws Exception {
        Method getQueueName = AbstractDtpNotifier.class.getDeclaredMethod("getQueueName", ExecutorAdapter.class);
        getQueueName.setAccessible(true);

        AbstractDtpNotifier abstractDtpNotifier = Mockito.spy(AbstractDtpNotifier.class);
        String res = (String) getQueueName.invoke(abstractDtpNotifier, dtpExecutor);

        Assert.assertEquals(res, VARIABLE_LINKED_BLOCKING_QUEUE.getName());
    }
}
