package org.dromara.dynamictp.example.extensiontest;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.plugin.DtpExtension;
import org.dromara.dynamictp.core.plugin.DtpExtensionPoint;
import org.dromara.dynamictp.core.plugin.DtpInvocation;
import org.dromara.dynamictp.core.plugin.DtpSignature;
import org.dromara.dynamictp.core.thread.DtpExecutor;
import org.dromara.dynamictp.core.thread.ScheduledDtpExecutor;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

@DtpExtensionPoint({@DtpSignature(clazz = DtpExecutor.class, method = "execute", args = {Runnable.class}), @DtpSignature(clazz = ScheduledDtpExecutor.class, method = "execute", args = {Runnable.class})})
@Slf4j
public class TestExtension implements DtpExtension {

    @Override
    public Object intercept(DtpInvocation invocation) throws InvocationTargetException, IllegalAccessException {

        System.out.println("--------------线程池开始执行任务-------------");
        DtpExecutor dtpExecutor = (DtpExecutor) invocation.getTarget();

        log.info("dtpExecutor corePoolSize: {}, maximum size: {}, {}, {}, {}", dtpExecutor.getCorePoolSize(),
                dtpExecutor.getMaximumPoolSize(), dtpExecutor.getThreadPoolName(), dtpExecutor.getQueueCapacity(),
                dtpExecutor.getKeepAliveTime(TimeUnit.MILLISECONDS));
//        log.info("dtpExecutor : {}", executorWrapper);

        Object result = invocation.proceed();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("--------------线程池结束执行任务-------------");

        return result;
    }
}
