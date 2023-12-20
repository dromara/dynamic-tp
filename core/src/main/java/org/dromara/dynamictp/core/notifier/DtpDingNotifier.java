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

import org.dromara.dynamictp.common.constant.DingNotifyConst;
import org.dromara.dynamictp.common.em.NotifyPlatformEnum;
import org.dromara.dynamictp.common.notifier.Notifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * DtpDingNotifier related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public class DtpDingNotifier extends AbstractDtpNotifier {

    public DtpDingNotifier(Notifier notifier) {
        super(notifier);
    }

    @Override
    public String platform() {
        return NotifyPlatformEnum.DING.name().toLowerCase();
    }

    @Override
    protected String getNoticeTemplate() {
        return DingNotifyConst.DING_CHANGE_NOTICE_TEMPLATE;
    }

    @Override
    protected String getAlarmTemplate() {
        return DingNotifyConst.DING_ALARM_TEMPLATE;
    }

    @Override
    protected Pair<String, String> getColors() {
        return new ImmutablePair<>(DingNotifyConst.WARNING_COLOR, DingNotifyConst.CONTENT_COLOR);
    }
}
