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

package org.dromara.dynamictp.common.util;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * Provides millisecond-level time of OS.
 *
 * @author reference sentinel-core
 * @version v1.0
 * @since 2023/2/3 15:49
 */
public final class TimeUtil {

    private static final long CONVERT_SECONDS = ChronoUnit.SECONDS.getDuration().toMillis();

    private static volatile long currentTimeMillis;

    static {
        currentTimeMillis = System.currentTimeMillis();
        Thread daemon = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    currentTimeMillis = System.currentTimeMillis();
                    try {
                        TimeUnit.MILLISECONDS.sleep(1);
                    } catch (Throwable ignore) {
                    }
                }
            }
        });
        daemon.setDaemon(true);
        daemon.setName("dtp-time-tick-thread");
        daemon.start();
    }

    public static long currentTimeMillis() {
        return currentTimeMillis;
    }

    public static long currentTimeSeconds() {
        return currentTimeMillis / CONVERT_SECONDS;
    }
}
