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

package org.dromara.dynamictp.core.system;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.util.MethodUtil;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * OperatingSystemBeanManager related.
 *
 * @author yanhom
 * @since 1.1.5
 */
@Slf4j
public class OperatingSystemBeanManager {

    /**
     * com.ibm for J9
     * com.sun for HotSpot
     */
    private static final List<String> OPERATING_SYSTEM_BEAN_CLASS_NAMES = Arrays.asList(
            "com.sun.management.OperatingSystemMXBean", "com.ibm.lang.management.OperatingSystemMXBean");

    private static final OperatingSystemMXBean OPERATING_SYSTEM_BEAN;

    private static final Class<?> OPERATING_SYSTEM_BEAN_CLASS;

    private static final Method SYSTEM_CPU_USAGE_METHOD;

    private static final Method PROCESS_CPU_TIME_METHOD;

    private static final Method FREE_PHYSICAL_MEM_METHOD;

    private static final Method TOTAL_PHYSICAL_MEM_METHOD;

    static {
        OPERATING_SYSTEM_BEAN = ManagementFactory.getOperatingSystemMXBean();
        OPERATING_SYSTEM_BEAN_CLASS = loadOne(OPERATING_SYSTEM_BEAN_CLASS_NAMES);
        SYSTEM_CPU_USAGE_METHOD = deduceMethod("getSystemCpuLoad");
        PROCESS_CPU_TIME_METHOD = deduceMethod("getProcessCpuTime");

        Method totalPhysicalMem = deduceMethod("getTotalPhysicalMemorySize");
        // getTotalPhysicalMemory for ibm jdk 7.
        TOTAL_PHYSICAL_MEM_METHOD = totalPhysicalMem != null ? totalPhysicalMem :
                deduceMethod("getTotalPhysicalMemory");

        FREE_PHYSICAL_MEM_METHOD = deduceMethod("getFreePhysicalMemorySize");
    }

    private OperatingSystemBeanManager() { }

    public static OperatingSystemMXBean getOperatingSystemBean() {
        return OPERATING_SYSTEM_BEAN;
    }

    public static double getSystemCpuUsage() {
        return MethodUtil.invokeAndReturnDouble(SYSTEM_CPU_USAGE_METHOD, OPERATING_SYSTEM_BEAN);
    }

    public static long getProcessCpuTime() {
        return MethodUtil.invokeAndReturnLong(PROCESS_CPU_TIME_METHOD, OPERATING_SYSTEM_BEAN);
    }

    public static long getTotalPhysicalMem() {
        return MethodUtil.invokeAndReturnLong(TOTAL_PHYSICAL_MEM_METHOD, OPERATING_SYSTEM_BEAN);
    }

    public static long getFreePhysicalMem() {
        return MethodUtil.invokeAndReturnLong(FREE_PHYSICAL_MEM_METHOD, OPERATING_SYSTEM_BEAN);
    }

    private static Class<?> loadOne(List<String> classNames) {
        for (String className : classNames) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                log.warn("Failed to load operating system bean class.", e);
            }
        }
        return null;
    }

    private static Method deduceMethod(String name) {
        if (Objects.isNull(OPERATING_SYSTEM_BEAN_CLASS)) {
            return null;
        }
        try {
            OPERATING_SYSTEM_BEAN_CLASS.cast(OPERATING_SYSTEM_BEAN);
            return OPERATING_SYSTEM_BEAN_CLASS.getDeclaredMethod(name);
        } catch (Exception e) {
            return null;
        }
    }
}

