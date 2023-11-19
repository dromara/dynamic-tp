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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Version related
 *
 * @author kamtohung
 */
@Slf4j
public final class VersionUtil {

    private static String version;

    static {
        try {
            version = version();
        } catch (Exception e) {
            log.warn("no version number found");
        }
    }

    private VersionUtil() { }

    public static String version() {
        // find version info from MANIFEST.MF first
        Package pkg = VersionUtil.class.getPackage();
        String version;
        if (pkg != null) {
            version = pkg.getImplementationVersion();
            if (StringUtils.isNotEmpty(version)) {
                return version;
            }
            version = pkg.getSpecificationVersion();
            if (StringUtils.isNotEmpty(version)) {
                return version;
            }
        }
        return "unknown";
    }

    public static String getVersion() {
        return version;
    }

}
