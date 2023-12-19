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

package org.dromara.dynamictp.jvmti;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Use JVMTI technology to implement some things that Java code can't do.
 * Note: Only 64-bit CPU architecture is supported now !
 *
 * @author dragon-zhang
 * @since 1.1.4
 */
@Slf4j
public class JVMTI {

	private static final AtomicBoolean AVAILABLE = new AtomicBoolean(false);

	static {
		try {
			NativeUtil.loadLibraryFromJar(JVMTIUtil.detectLibName());
			AVAILABLE.set(true);
		} catch (Throwable t) {
			log.error("JVMTI initialization failed!", t);
		}
	}

	private JVMTI() {
	}

	/**
	 * Get current surviving instance of a class in the jvm.
	 *
	 * @param klass class type
	 * @param <T>   class type
	 * @return current surviving instance
	 * @throws RuntimeException if find many instances
	 */
	public static <T> T getInstance(final Class<T> klass) {
		final List<T> instances = getInstances(klass, 1);
		if (CollectionUtils.isEmpty(instances)) {
			return null;
		}
		if (instances.size() > 1) {
			throw new RuntimeException("expect only one instance, actually find many instances !");
		}
		return instances.get(0);
	}

	/**
	 * Get all current surviving instances of a class in the jvm.
	 *
	 * <p>Note: be careful to use this method !
	 *
	 * @param klass class type
	 * @param <T>   class type
	 * @return current surviving instances
	 */
	public static <T> List<T> getInstances(final Class<T> klass) {
		return getInstances(klass, -1);
	}

	/**
	 * Get all current surviving instances of a class in the jvm.
	 *
	 * <p>Note: be careful to use this method !
	 *
	 * @param klass class type
	 * @param <T>   class type
	 * @param limit instance limit, less than 0 means no limit.
	 *              It is recommended to pass in a small {@code limit} value which is larger than 0.
	 * @return current surviving instances
	 */
	public static <T> List<T> getInstances(final Class<T> klass, final int limit) {
		if (!AVAILABLE.get()) {
			return Collections.emptyList();
		}
		return Arrays.asList(getInstances0(klass, limit));
	}

	/**
	 * Get all current surviving instances of a class in the jvm.
	 *
	 * <p>Note: Only 64-bit CPU architecture is supported now !
	 *
	 * @param klass class type
	 * @param <T>   class type
	 * @param limit instance limit, less than 0 means no limit
	 * @return current surviving instances
	 */
	private static synchronized native <T> T[] getInstances0(Class<T> klass, int limit);

	/**
	 * Force GC, even if JVM parameters are configured.
	 */
	public static synchronized native void forceGc();
}
