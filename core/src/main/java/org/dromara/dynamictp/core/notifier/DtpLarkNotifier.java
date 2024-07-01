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

package org.dromara.dynamictp.core.notifier;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.dromara.dynamictp.common.constant.LarkNotifyConst;
import org.dromara.dynamictp.common.em.NotifyPlatformEnum;
import org.dromara.dynamictp.common.notifier.Notifier;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.dromara.dynamictp.common.constant.LarkNotifyConst.LARK_AT_FORMAT_OPENID;
import static org.dromara.dynamictp.common.constant.LarkNotifyConst.LARK_AT_FORMAT_USERNAME;
import static org.dromara.dynamictp.common.constant.LarkNotifyConst.LARK_OPENID_PREFIX;

/**
 * DtpLarkNotifier
 *
 * @author fxbin
 * @version v1.0
 * @since 2022/4/28 23:25
 */
@Slf4j
public class DtpLarkNotifier extends AbstractDtpNotifier {

    public DtpLarkNotifier(Notifier notifier) {
        super(notifier);
    }

    @Override
    public String platform() {
        return NotifyPlatformEnum.LARK.name().toLowerCase();
    }

    @Override
    protected String getNoticeTemplate() {
        return LarkNotifyConst.LARK_CHANGE_NOTICE_JSON_STR;
    }

    @Override
    protected String getAlarmTemplate() {
        return LarkNotifyConst.LARK_ALARM_JSON_STR;
    }

    @Override
    protected Pair<String, String> getColors() {
        return new ImmutablePair<>(LarkNotifyConst.WARNING_COLOR, LarkNotifyConst.COMMENT_COLOR);
    }

    @Override
    protected String formatReceivers(String receives) {
        return Arrays.stream(receives.split(","))
                .map(r -> StringUtils.startsWith(r, LARK_OPENID_PREFIX) ?
                        String.format(LARK_AT_FORMAT_OPENID, r) : String.format(LARK_AT_FORMAT_USERNAME, r, r))
                .collect(Collectors.joining(" "));
    }
}
