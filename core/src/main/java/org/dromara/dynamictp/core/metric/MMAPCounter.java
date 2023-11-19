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

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Snapshot;

/**
 * MMAPCounter related
 *
 * @author yanhom
 * @since 1.1.5
 */
@SuppressWarnings("all")
public class MMAPCounter implements Summary {

    private final MMACounter mmaCounter;

    private final LimitedUniformReservoir reservoir;

    private final Histogram histogram;

    public MMAPCounter() {
        this.mmaCounter = new MMACounter();
        reservoir = new LimitedUniformReservoir();
        histogram = new Histogram(reservoir);
    }

    @Override
    public void add(long value) {
        mmaCounter.add(value);
        histogram.update(value);
    }

    @Override
    public void reset() {
        mmaCounter.reset();
        reservoir.reset();
    }

    public Snapshot getSnapshot() {
        return histogram.getSnapshot();
    }

    public MMACounter getMmaCounter() {
        return mmaCounter;
    }
}
