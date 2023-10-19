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

package org.dromara.dynamictp.core.metric;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MMACounter related
 *
 * @author yanhom
 * @since 1.1.5
 */
@SuppressWarnings("all")
public class MMACounter implements Summary {

    private final AtomicLong total = new AtomicLong();

    private final AtomicLong count = new AtomicLong();

    private final AtomicLong min = new AtomicLong(Long.MAX_VALUE);

    private final AtomicLong max = new AtomicLong(Long.MIN_VALUE);

    @Override
    public void add(long value) {
        total.addAndGet(value);
        count.incrementAndGet();
        setMin(value);
        setMax(value);
    }

    @Override
    public void reset() {
        total.set(0);
        count.set(0);
        min.set(Long.MAX_VALUE);
        max.set(Long.MIN_VALUE);
    }

    public long getTotal() {
        return total.get();
    }

    public long getCount() {
        return count.get();
    }

    public long getMin() {
        long current = min.get();
        return (current == Long.MAX_VALUE) ? 0 : current;
    }

    public long getMax() {
        long current = max.get();
        return (current == Long.MIN_VALUE) ? 0 : current;
    }

    public double getAvg() {
        long currentCount = count.get();
        long currentTotal = total.get();
        if (currentCount > 0) {
            double avgLatency = currentTotal / (double) currentCount;
            BigDecimal bg = new BigDecimal(avgLatency);
            return bg.setScale(4, RoundingMode.HALF_UP).doubleValue();
        }
        return 0;
    }

    private void setMax(long value) {
        long current;
        while (value > (current = max.get()) && !max.compareAndSet(current, value)) {
            // no op
        }
    }

    private void setMin(long value) {
        long current;
        while (value < (current = min.get()) && !min.compareAndSet(current, value)) {
            // no op
        }
    }
}
