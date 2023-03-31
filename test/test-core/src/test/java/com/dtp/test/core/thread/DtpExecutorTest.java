package com.dtp.test.core.thread;

import com.dtp.common.em.NotifyItemEnum;
import com.dtp.core.DtpRegistry;
import com.dtp.core.notify.manager.AlarmManager;
import com.dtp.core.spring.EnableDynamicTp;
import com.dtp.core.spring.YamlPropertySourceFactory;
import com.dtp.core.support.ExecutorWrapper;
import com.dtp.core.thread.DtpExecutor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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
        DtpExecutor dtpExecutor = DtpRegistry.getDtpExecutor("testRunTimeoutDtpExecutor");
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
        DtpExecutor dtpExecutor = DtpRegistry.getDtpExecutor("testQueueTimeoutDtpExecutor");
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
        DtpExecutor dtpExecutor = DtpRegistry.getDtpExecutor("testRejectedQueueTimeoutCancelDtpExecutor");
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
