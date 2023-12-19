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
 * The type JVMTI util.
 * This file is copied from <a href="https://github.com/alibaba/arthas">here</a>
 *
 * @author dragon-zhang
 * @since 1.1.4
 */
public class JVMTIUtil {

    private static String libName;

    static {
        if (OSUtils.isMac()) {
            libName = "libJniLibrary.dylib";
        }
        if (OSUtils.isLinux()) {
            if (OSUtils.isArm32()) {
                libName = "libJniLibrary-arm.so";
            } else if (OSUtils.isArm64()) {
                libName = "libJniLibrary-aarch64.so";
            } else if (OSUtils.isX8664()) {
                libName = "libJniLibrary-x64.so";
            } else {
                libName = "libJniLibrary-" + OSUtils.arch() + ".so";
            }
        }
        if (OSUtils.isWindows()) {
            libName = "libJniLibrary-x64.dll";
            if (OSUtils.isX86()) {
                libName = "libJniLibrary-x86.dll";
            }
        }
    }

    private JVMTIUtil() {
    }

    /**
     * detect native library name.
     *
     * @return the native library name
     */
    public static String detectLibName() {
        return libName;
    }
}
