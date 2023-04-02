package com.dtp.test.core.thread;

import com.dtp.common.ApplicationContextHolder;
import com.dtp.common.em.NotifyItemEnum;
import com.dtp.common.timer.HashedWheelTimer;
import com.dtp.core.DtpRegistry;
import com.dtp.core.notify.manager.AlarmManager;
import com.dtp.core.support.ExecutorWrapper;
import com.dtp.core.thread.DtpExecutor;
import org.mockito.MockedStatic;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

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
@Fork(1)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class DtpExecutorBenchmarkTest {

    private ConfigurableApplicationContext context;

    DtpExecutor dtpExecutor1;

    HashedWheelTimer hashedWheelTimer;

    @TearDown(Level.Trial)
    public void close() {
        context.close();
        dtpExecutor1.shutdown();
        hashedWheelTimer.stop();
    }

    @Setup(Level.Trial)
    public void setup() {
        context = SpringApplication.run(DtpExecutorTest.class);
        dtpExecutor1 = DtpRegistry.getDtpExecutor("testRunTimeoutDtpExecutor");
        hashedWheelTimer = ApplicationContextHolder.getBean(HashedWheelTimer.class);
        // mock alarm
        try (MockedStatic<AlarmManager> utilities = mockStatic(AlarmManager.class)) {
            utilities.when(() -> AlarmManager.doAlarmAsync(any(), any(), any())).then(invocation -> null);
            utilities.when(() -> AlarmManager.doAlarmAsync(any(DtpExecutor.class), any(NotifyItemEnum.class))).then(invocation -> null);
            utilities.when(() -> AlarmManager.doAlarmAsync(any(ExecutorWrapper.class), anyList())).then(invocation -> null);
            utilities.when(() -> AlarmManager.doAlarmAsync(any(DtpExecutor.class), anyList())).then(invocation -> null);
        }
    }

    @Benchmark
    public void test() {
        dtpExecutor1.execute(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(250);
            } catch (InterruptedException e) {
                // ignore
            }
            System.out.println("dtpExecutor1 execute");
        });
    }

    @Benchmark
    public void normal() {
        dtpExecutor1.execute(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(150);
            } catch (InterruptedException e) {
                // ignore
            }
            System.out.println("dtpExecutor1 execute");
        });
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(DtpExecutorBenchmarkTest.class.getSimpleName())
                .result("result.json")
                .resultFormat(ResultFormatType.JSON)
                .build();
        new Runner(opt).run();
    }

}
