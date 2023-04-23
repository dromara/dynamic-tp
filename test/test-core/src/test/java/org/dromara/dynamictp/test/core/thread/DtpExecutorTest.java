package org.dromara.dynamictp.test.core.thread;

import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.notifier.manager.AlarmManager;
import org.dromara.dynamictp.core.spring.EnableDynamicTp;
import org.dromara.dynamictp.core.spring.YamlPropertySourceFactory;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.thread.DtpExecutor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mockStatic;

/**
 * DtpExecutorTest related
 *
 * @author yanhom
 * @author kamtohung
 * @since 1.1.0
 */
@Slf4j
@EnableDynamicTp
@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = DtpExecutorTest.class)
@PropertySource(value = "classpath:/dynamic-tp-demo.yml", factory = YamlPropertySourceFactory.class)
public class DtpExecutorTest {

    public void mock(MockedStatic<AlarmManager> mockAlarmManager) {
        mockAlarmManager.when(() -> AlarmManager.doAlarmAsync(any(), any(), any())).then(invocation -> null);
        mockAlarmManager.when(() -> AlarmManager.doAlarmAsync(any(DtpExecutor.class), any(NotifyItemEnum.class))).then(invocation -> null);
        mockAlarmManager.when(() -> AlarmManager.doAlarmAsync(any(ExecutorWrapper.class), anyList())).then(invocation -> null);
        mockAlarmManager.when(() -> AlarmManager.doAlarmAsync(any(DtpExecutor.class), anyList())).then(invocation -> null);
    }

    @RepeatedTest(100)
    public void testRunTimeout() {
        Executor dtpExecutor = DtpRegistry.getExecutor("testRunTimeoutDtpExecutor");
        dtpExecutor.execute(() -> {
            try (MockedStatic<AlarmManager> mockAlarmManager = mockStatic(AlarmManager.class)) {
                mock(mockAlarmManager);
                TimeUnit.MILLISECONDS.sleep(300);
            } catch (InterruptedException e) {
                // ignore
            }
        });
    }

    @RepeatedTest(100)
    public void testQueueTimeout() {
        Executor dtpExecutor = DtpRegistry.getExecutor("testQueueTimeoutDtpExecutor");
        dtpExecutor.execute(() -> {
            try (MockedStatic<AlarmManager> mockAlarmManager = mockStatic(AlarmManager.class)) {
                mock(mockAlarmManager);
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                // ignore
            }
        });
    }

    @RepeatedTest(100)
    public void testRejectedQueueTimeoutCancel() {
        Executor dtpExecutor = DtpRegistry.getExecutor("testRejectedQueueTimeoutCancelDtpExecutor");
        dtpExecutor.execute(() -> {
            try (MockedStatic<AlarmManager> mockAlarmManager = mockStatic(AlarmManager.class)) {
                mock(mockAlarmManager);
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                // ignore
            }
        });
    }

    @AfterAll
    public static void afterAll() throws InterruptedException {
//        TimeUnit.SECONDS.sleep(100);
    }


}
