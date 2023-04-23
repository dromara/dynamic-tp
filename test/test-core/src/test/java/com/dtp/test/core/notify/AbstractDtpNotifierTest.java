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
import com.dtp.core.notifier.context.AlarmCtx;
import com.dtp.core.notifier.context.DtpNotifyCtxHolder;
import com.dtp.core.notifier.context.NoticeCtx;
import com.dtp.core.support.ExecutorWrapper;
import com.dtp.core.support.ThreadPoolCreator;
import com.dtp.core.thread.DtpExecutor;
import com.google.common.collect.Lists;
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
@RunWith(PowerMockRunner.class)
@PrepareForTest(CommonUtil.class)
@SuppressStaticInitializationFor("com.dtp.common.util.CommonUtil")
public class AbstractDtpNotifierTest {

    private final Notifier notifier = Mockito.mock(Notifier.class);

    private final DtpExecutor dtpExecutor = ThreadPoolCreator.createDynamicFast("test");

    @Before
    public void setUp() {
        PowerMockito.mockStatic(CommonUtil.class);
        PowerMockito.when(CommonUtil.getInstance()).thenAnswer((Answer<ServiceInstance>) invocation ->
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
