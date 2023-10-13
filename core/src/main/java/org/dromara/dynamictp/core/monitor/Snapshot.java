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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

/**
 * Copy from com.codahale.metrics.Snapshot
 *
 * @author kyao
 * @since 1.1.5
 */
public class Snapshot {

    private final long[] values;

    /**
     * Create a new {@link Snapshot} with the given values.
     *
     * @param values    an unordered set of values in the reservoir
     */
    public Snapshot(Collection<Long> values) {
        final Object[] copy = values.toArray();
        this.values = new long[copy.length];
        for (int i = 0; i < copy.length; i++) {
            this.values[i] = (Long) copy[i];
        }
        Arrays.sort(this.values);
    }

    /**
     * Create a new {@link Snapshot} with the given values.
     *
     * @param values    an unordered set of values in the reservoir
     */
    public Snapshot(long[] values) {
        this.values = Arrays.copyOf(values, values.length);
        Arrays.sort(this.values);
    }

    /**
     * Returns the value at the given quantile.
     *
     * @param quantile    a given quantile, in {@code [0..1]}
     * @return the value in the distribution at {@code quantile}
     */
    public double getValue(double quantile) {
        if (quantile < 0.0 || quantile > 1.0) {
            throw new IllegalArgumentException(quantile + " is not in [0..1]");
        }

        if (values.length == 0) {
            return 0.0;
        }

        final double pos = quantile * (values.length + 1);

        if (pos < 1) {
            return values[0];
        }

        if (pos >= values.length) {
            return values[values.length - 1];
        }

        final double lower = values[(int) pos - 1];
        final double upper = values[(int) pos];
        return lower + (pos - Math.floor(pos)) * (upper - lower);
    }

    /**
     * Returns the number of values in the snapshot.
     *
     * @return the number of values
     */
    public int size() {
        return values.length;
    }

    /**
     * Returns the median value in the distribution.
     *
     * @return the median value
     */
    public double getMedian() {
        return getValue(0.5);
    }

    /**
     * Returns the value at the 75th percentile in the distribution.
     *
     * @return the value at the 75th percentile
     */
    public double get75thPercentile() {
        return getValue(0.75);
    }

    /**
     * Returns the value at the 95th percentile in the distribution.
     *
     * @return the value at the 95th percentile
     */
    public double get95thPercentile() {
        return getValue(0.95);
    }

    /**
     * Returns the value at the 98th percentile in the distribution.
     *
     * @return the value at the 98th percentile
     */
    public double get98thPercentile() {
        return getValue(0.98);
    }

    /**
     * Returns the value at the 99th percentile in the distribution.
     *
     * @return the value at the 99th percentile
     */
    public double get99thPercentile() {
        return getValue(0.99);
    }

    /**
     * Returns the value at the 99.9th percentile in the distribution.
     *
     * @return the value at the 99.9th percentile
     */
    public double get999thPercentile() {
        return getValue(0.999);
    }

    /**
     * Returns the entire set of values in the snapshot.
     *
     * @return the entire set of values
     */
    public long[] getValues() {
        return Arrays.copyOf(values, values.length);
    }

    /**
     * Returns the highest value in the snapshot.
     *
     * @return the highest value
     */
    public long getMax() {
        if (values.length == 0) {
            return 0;
        }
        return values[values.length - 1];
    }

    /**
     * Returns the lowest value in the snapshot.
     *
     * @return the lowest value
     */
    public long getMin() {
        if (values.length == 0) {
            return 0;
        }
        return values[0];
    }

    /**
     * Returns the arithmetic mean of the values in the snapshot.
     *
     * @return the arithmetic mean
     */
    public double getMean() {
        if (values.length == 0) {
            return 0;
        }

        double sum = 0;
        for (long value : values) {
            sum += value;
        }
        return sum / values.length;
    }

    /**
     * Returns the standard deviation of the values in the snapshot.
     *
     * @return the standard value
     */
    public double getStdDev() {
        // two-pass algorithm for variance, avoids numeric overflow
        if (values.length <= 1) {
            return 0;
        }

        final double mean = getMean();
        double sum = 0;

        for (long value : values) {
            final double diff = value - mean;
            sum += diff * diff;
        }

        final double variance = sum / (values.length - 1);
        return Math.sqrt(variance);
    }

    /**
     * Writes the values of the snapshot to the given stream.
     *
     * @param output an output stream
     */
    public void dump(OutputStream output) {
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8))) {
            for (long value : values) {
                out.printf("%d%n", value);
            }
        }
    }
}
