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

package org.dromara.dynamictp.core.monitor;

import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingStream;
import lombok.Getter;
import lombok.val;
import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.handler.NotifierHandler;
import org.dromara.dynamictp.core.metric.MMAPCounter;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.proxy.VirtualThreadExecutorProxy;

import java.io.Closeable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.MAX_PINNED_TIME;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.PINNED_EVENT;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.TOTAL_PINNED_TIME;
import static org.dromara.dynamictp.common.util.StringUtil.formatJfrStackTrace;


/**
 * PerformanceProvider related
 *
 * @author kyao
 * @since 1.1.5
 */
public class PerformanceProvider implements Closeable {

    /**
     * last refresh timestamp
     */
    private final AtomicLong lastRefreshMillis = new AtomicLong(System.currentTimeMillis());

    private final MMAPCounter mmapCounter = new MMAPCounter();

    private static final Map<String, Map<String, Double>> VTE_STATS_CACHE = new ConcurrentHashMap<>();

    private static final int DEFAULT_STACK_TRACE_MAX_DEPTH = 15;

    private static final Timer TIMER =  new SimpleMeterRegistry().timer(PINNED_EVENT);
    private static final RecordingStream RECORDING_STREAM = createRecordingStream();

    public void completeTask(long rt) {
        mmapCounter.add(rt);
    }

    public PerformanceSnapshot getSnapshotAndReset() {
        long currentMillis = System.currentTimeMillis();
        int intervalTs = (int) (currentMillis - lastRefreshMillis.get()) / 1000;
        val performanceSnapshot = new PerformanceSnapshot(mmapCounter, intervalTs);
        reset(currentMillis);
        return performanceSnapshot;
    }

    private void reset(long currentMillis) {
        mmapCounter.reset();
        lastRefreshMillis.compareAndSet(lastRefreshMillis.get(), currentMillis);
    }

    public static RecordingStream createRecordingStream() {
        RecordingStream recordingStream = new RecordingStream();
        recordingStream.enable(PINNED_EVENT).withStackTrace();
        recordingStream.setMaxAge(Duration.ofSeconds(5));
        recordingStream.startAsync();
        recordingStream.onEvent(PINNED_EVENT, PerformanceProvider::handlePinnedEvent);
        return recordingStream;
    }

    @Override
    public void close() {
        RECORDING_STREAM.close();
    }

    /**
     * When an event is pinned, the data is saved to the cache
     */
    static void handlePinnedEvent(RecordedEvent event) {
        String executorName = event.getThread() != null ? event.getThread().getJavaName() : "";
        if (executorName.isEmpty()) {
            return;
        }
        Duration duration = event.getDuration();
        String stackTrace = formatJfrStackTrace(event.getStackTrace(), DEFAULT_STACK_TRACE_MAX_DEPTH);
        TIMER.record(duration);

        ConcurrentHashMap<String, Double> vtExecutorStat = new ConcurrentHashMap<>(3);
        double maxPinnedTime = TIMER.max(TimeUnit.MILLISECONDS);
        double totalPinnedTime = TIMER.totalTime(TimeUnit.MILLISECONDS);
        long durationPinnedTime = duration.toMillis();
        vtExecutorStat.put(MAX_PINNED_TIME, maxPinnedTime);
        vtExecutorStat.put(TOTAL_PINNED_TIME, totalPinnedTime);

        String[] pinContent = populatePinContent(maxPinnedTime, totalPinnedTime, durationPinnedTime, stackTrace);
        ExecutorWrapper executorWrapper = DtpRegistry.getExecutorWrapper(executorName);
        ((VirtualThreadExecutorProxy) executorWrapper.getExecutor().getOriginal()).setCurPinDuration(duration.toSeconds());
        List<NotifyItem> pinnedNotifyItems = executorWrapper.getNotifyItems().stream().filter(notifyItem -> notifyItem.getType().equals(NotifyItemEnum.PIN_TIMEOUT.getValue())).toList();
        if (!pinnedNotifyItems.isEmpty()) {
            NotifyItem pinnedNotifyItem = pinnedNotifyItems.getFirst();
            if (pinnedNotifyItem.isEnabled() && duration.toSeconds() > pinnedNotifyItem.getThreshold()) {
                NotifierHandler.getInstance().sendCommonAlarm(executorWrapper, pinnedNotifyItem, true, pinContent);
            }
        }
        VTE_STATS_CACHE.put(executorName, vtExecutorStat);
    }

    private static String[] populatePinContent(double maxPinnedTime, double totalPinnedTime, long durationPinnedTime, String stackTrace) {
        return new String[] {
                "maxPinnedTime: " + maxPinnedTime + "ms",
                "totalPinnedTime: " + totalPinnedTime + "ms",
                "durationPinnedTime: " + durationPinnedTime + "ms",
                "stackTrace: \n" + stackTrace
        };
    }

    public static Map<String, Double> getVteStat(String executorName) {
        VTE_STATS_CACHE.putIfAbsent(executorName, new ConcurrentHashMap<>(3));
        return VTE_STATS_CACHE.get(executorName);
    }

    @Getter
    public static class PerformanceSnapshot {

        private final double tps;

        private final long maxRt;

        private final long minRt;

        private final double avg;

        private final double tp50;

        private final double tp75;

        private final double tp90;

        private final double tp95;

        private final double tp99;

        private final double tp999;

        public PerformanceSnapshot(MMAPCounter mmapCounter, int monitorInterval) {
            tps = BigDecimal.valueOf(mmapCounter.getMmaCounter().getCount())
                    .divide(BigDecimal.valueOf(Math.max(monitorInterval, 1)), 1, RoundingMode.HALF_UP)
                    .doubleValue();

            maxRt = mmapCounter.getMmaCounter().getMax();
            minRt = mmapCounter.getMmaCounter().getMin();
            avg = mmapCounter.getMmaCounter().getAvg();

            tp50 = mmapCounter.getSnapshot().getMedian();
            tp75 = mmapCounter.getSnapshot().get75thPercentile();
            tp90 = mmapCounter.getSnapshot().getValue(0.9);
            tp95 = mmapCounter.getSnapshot().get95thPercentile();
            tp99 = mmapCounter.getSnapshot().get99thPercentile();
            tp999 = mmapCounter.getSnapshot().get999thPercentile();
        }
    }
}
