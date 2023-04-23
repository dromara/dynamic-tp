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
