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

/**
 * Use JVMTI technology to implement some things that Java code can't do.
 *
 * <p>Note: Do not delete this file!
 *
 * <p>Note: Only 64-bit CPU architecture is supported now !
 *
 * @author dragon-zhang
 * @since 1.1.4
 */
public class JVMTI {

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
