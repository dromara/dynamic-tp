package com.dtp.common.util;

import static com.dtp.common.constant.DynamicTpConst.AVAILABLE_PROCESSORS;

/**
 * Support class for thread pool
 *
 * @author: yanhom
 * @since 1.0.0
 */
public final class ThreadPoolUtil {

	private static final double DEFAULT_BLOCK_COEFFICIENT = 0.9;

	private ThreadPoolUtil() {}

	/**
     * A cpu-intensive task has a blocking coefficient of 0.
	 * @return thread pool size
	 */
	public static int getCpuIntensivePoolSize() {
		return getIoIntensivePoolSize(0);
	}

	public static int getIoIntensivePoolSize() {
		return getIoIntensivePoolSize(DEFAULT_BLOCK_COEFFICIENT);
	}

	/**
	 * Each task blocks 90% of the time, and works only 10% of its
	 * lifetime, that is I/O intensive pool, io-intensive task has a value close to 1
	 * @return thread pool size
	 */
	public static int getIoIntensivePoolSize(double blockingCoefficient) {
		return poolSize(blockingCoefficient);
	}

	/**
	 * Number of threads = Number of Available Cores / (1 - blockingCoefficient)
	 *
	 *  @param blockingCoefficient the coefficient
	 *  @return suggest thread pool size
	 */
	public static int poolSize(double blockingCoefficient) {
		return (int) (AVAILABLE_PROCESSORS / (1 - blockingCoefficient));
	}
}
