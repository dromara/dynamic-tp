/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.dynamictp.benchmark;

import com.google.common.collect.Sets;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.core.support.ThreadPoolBuilder;
import org.dromara.dynamictp.core.support.task.wrapper.TaskWrappers;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yanhom
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5)
public class ExecutorBenchmark {

    @Param({"100", "2000", "4000", "6000", "8000", "10000"})
    private int max;

    @Param({"MEDIUM"})
    private TaskType taskType;

    private ThreadPoolExecutor standardExecutor;
    private DtpExecutor dtpExecutor;

    public enum TaskType {
        // 轻量级任务，执行时间很短
        LIGHT,
        // 中等任务，有一定计算量
        MEDIUM,
        // 重量级任务，执行时间较长或有IO操作
        HEAVY
    }

    @Setup
    public void setup() {
        standardExecutor = new ThreadPoolExecutor(
                4,
                8,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1024),
                new ThreadFactory() {
                    private final AtomicInteger counter = new AtomicInteger(1);
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "standard-pool-" + counter.getAndIncrement());
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        dtpExecutor = ThreadPoolBuilder.newBuilder()
                .corePoolSize(4)
                .maximumPoolSize(8)
                .keepAliveTime(60)
                .threadFactory("dtp-test-pool")
                .runTimeout(100)
                .queueCapacity(100)
                .queueCapacity(1024)
                .taskWrappers(TaskWrappers.getInstance().getByNames(Sets.newHashSet("ttl", "mdc")))
                .rejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy())
                .buildDynamic();
    }

    @TearDown
    public void tearDown() {
        standardExecutor.shutdown();
        dtpExecutor.shutdown();
        try {
            if (!standardExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                standardExecutor.shutdownNow();
            }
            if (!dtpExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                dtpExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Benchmark
    @Threads(1)
    public void testStandardSubmit(Blackhole bh) {
        executeTasksAndWait(standardExecutor, bh);
    }

    @Benchmark
    @Threads(1)
    public void testDtpSubmit(Blackhole bh) {
        executeTasksAndWait(dtpExecutor, bh);
    }

    @Benchmark
    @Threads(8)
    public void test8ThreadsStandardSubmit(Blackhole bh) {
        executeTasksAndWait(standardExecutor, bh);
    }

    @Benchmark
    @Threads(8)
    public void test8ThreadsSingleEntry(Blackhole bh) {
        executeTasksAndWait(dtpExecutor, bh);
    }

    private void executeTasksAndWait(ThreadPoolExecutor executor, Blackhole bh) {
        executor.submit(() -> {
            try {
                switch (taskType) {
                    case LIGHT:
                        bh.consume(max * max);
                        break;
                    case MEDIUM:
                        bh.consume(calculatePrimes(max));
                        break;
                    case HEAVY:
                        bh.consume(calculatePrimes(max));
                        Thread.sleep(2);
                        break;
                }
                return max;
            } catch (Exception e) {
                return -1;
            }
        });
    }

    private int calculatePrimes(int max) {
        int count = 0;
        for (int i = 2; i <= max; i++) {
            boolean isPrime = true;
            for (int j = 2; j <= Math.sqrt(i); j++) {
                if (i % j == 0) {
                    isPrime = false;
                    break;
                }
            }
            if (isPrime) {
                count++;
            }
        }
        return count;
    }
}
