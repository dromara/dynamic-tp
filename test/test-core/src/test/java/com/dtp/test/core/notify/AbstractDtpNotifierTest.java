package com.dtp.test.core.notify;

import com.dtp.common.em.NotifyItemEnum;
import com.dtp.common.entity.NotifyItem;
import com.dtp.common.entity.NotifyPlatform;
import com.dtp.common.entity.ServiceInstance;
import com.dtp.common.entity.TpMainFields;
import com.dtp.common.util.CommonUtil;
import com.dtp.core.notifier.AbstractDtpNotifier;
import com.dtp.core.notifier.DtpDingNotifier;
import com.dtp.core.notifier.base.Notifier;
import com.dtp.core.notifier.capture.CapturedExecutor;
import com.dtp.core.notifier.context.AlarmCtx;
import com.dtp.core.notifier.context.DtpNotifyCtxHolder;
import com.dtp.core.notifier.context.NoticeCtx;
import com.dtp.core.spring.EnableDynamicTp;
import com.dtp.core.support.ExecutorAdapter;
import com.dtp.core.support.ExecutorWrapper;
import com.dtp.core.support.ThreadPoolCreator;
import com.dtp.core.thread.DtpExecutor;
import com.dtp.test.core.thread.DtpExecutorTest;
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

import static com.dtp.common.em.QueueTypeEnum.VARIABLE_LINKED_BLOCKING_QUEUE;
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
