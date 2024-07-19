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

package org.dromara.dynamictp.core.support;

import org.dromara.dynamictp.common.constant.DynamicTpConst;
import org.dromara.dynamictp.common.manager.ContextManagerHelper;
import org.dromara.dynamictp.common.util.VersionUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * DtpBannerPrinter related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public class DtpBannerPrinter {

    private static final String NAME = " :: Dynamic Thread Pool :: ";

    private static final String SITE = " :: https://dynamictp.cn ::";

    private static final String BANNER = "\n" +
            "|  __ \\                            (_) |__   __|   \n" +
            "| |  | |_   _ _ __   __ _ _ __ ___  _  ___| |_ __  \n" +
            "| |  | | | | | '_ \\ / _` | '_ ` _ \\| |/ __| | '_ \\ \n" +
            "| |__| | |_| | | | | (_| | | | | | | | (__| | |_) |\n" +
            "|_____/ \\__, |_| |_|\\__,_|_| |_| |_|_|\\___|_| .__/ \n" +
            "         __/ |                              | |    \n" +
            "        |___/                               |_|    ";

    public static void printBanner() {
        boolean enable = Boolean.parseBoolean(ContextManagerHelper.getEnvironmentProperty(DynamicTpConst.BANNER_ENABLED_PROP, "true"));
        if (enable) {
            log.info(BANNER + "\n" + NAME + "\n :: " + VersionUtil.getVersion() + " :: \n" + SITE + "\n");
        }
    }
}
