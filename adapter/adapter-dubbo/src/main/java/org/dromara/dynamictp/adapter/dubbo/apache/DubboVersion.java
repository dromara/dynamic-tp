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

package org.dromara.dynamictp.adapter.dubbo.apache;

import org.apache.dubbo.common.Version;

/**
 * DubboVersion related
 *
 * @author yanhom
 * @since 1.0.6
 */
@SuppressWarnings("all")
public class DubboVersion {

    private DubboVersion() { }

    public static final String VERSION_2_7_5 = "2.7.5";

    public static final String VERSION_3_0_3 = "3.0.3";

    public static final String VERSION_3_0_9 = "3.0.9";

    public static final String VERSION_3_1_8 = "3.1.8";

    /**
     * Compare versions
     * @return the value {@code 0} if {@code version1 == version2};
     *         a value less than {@code 0} if {@code version1 < version2}; and
     *         a value greater than {@code 0} if {@code version1 > version2}
     */
    public static int compare(String v1, String v2) {
        return Integer.compare(Version.getIntVersion(v1), Version.getIntVersion(v2));
    }
}

