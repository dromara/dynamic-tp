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

package org.dromara.dynamictp.extension.notify.yunzhijia;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.dromara.dynamictp.common.manager.ContextManagerHelper;
import org.dromara.dynamictp.core.notifier.AbstractDtpNotifier;

/**
 * DtpYunZhiJiaNotifier related
 *
 * @author husky12138
 * @since 1.1.4
 **/
@Slf4j
public class DtpYunZhiJiaNotifier extends AbstractDtpNotifier {

    public DtpYunZhiJiaNotifier() {
        super(ContextManagerHelper.getBean(YunZhiJiaNotifier.class));
    }

    @Override
    public String platform() {
        return YunZhiJiaNotifyConst.PLATFORM_NAME.toLowerCase();
    }

    @Override
    protected String getNoticeTemplate() {
        return YunZhiJiaNotifyConst.CHANGE_NOTICE_TEMPLATE;
    }

    @Override
    protected String getAlarmTemplate() {
        return YunZhiJiaNotifyConst.ALARM_TEMPLATE;
    }

    @Override
    protected Pair<String, String> getColors() {
        return new ImmutablePair<>(YunZhiJiaNotifyConst.WARNING_COLOR, YunZhiJiaNotifyConst.COMMENT_COLOR);
    }
}
