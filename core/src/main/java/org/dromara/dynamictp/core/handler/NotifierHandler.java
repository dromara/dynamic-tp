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

package org.dromara.dynamictp.core.handler;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.common.entity.TpMainFields;
import org.dromara.dynamictp.common.util.ExtensionServiceLoader;
import org.dromara.dynamictp.core.notifier.DtpDingNotifier;
import org.dromara.dynamictp.core.notifier.DtpLarkNotifier;
import org.dromara.dynamictp.core.notifier.DtpNotifier;
import org.dromara.dynamictp.core.notifier.DtpWechatNotifier;
import org.dromara.dynamictp.common.notifier.DingNotifier;
import org.dromara.dynamictp.common.notifier.LarkNotifier;
import org.dromara.dynamictp.common.notifier.WechatNotifier;
import org.dromara.dynamictp.core.notifier.context.DtpNotifyCtxHolder;
import org.dromara.dynamictp.core.notifier.manager.NotifyHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NotifierHandler related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public final class NotifierHandler {

    private static final Map<String, DtpNotifier> NOTIFIERS = new HashMap<>();

    private NotifierHandler() {
        List<DtpNotifier> loadedNotifiers = ExtensionServiceLoader.get(DtpNotifier.class);
        loadedNotifiers.forEach(notifier -> NOTIFIERS.put(notifier.platform().toLowerCase(), notifier));

        DtpNotifier dingNotifier = new DtpDingNotifier(new DingNotifier());
        DtpNotifier wechatNotifier = new DtpWechatNotifier(new WechatNotifier());
        DtpNotifier larkNotifier = new DtpLarkNotifier(new LarkNotifier());
        NOTIFIERS.put(dingNotifier.platform(), dingNotifier);
        NOTIFIERS.put(wechatNotifier.platform(), wechatNotifier);
        NOTIFIERS.put(larkNotifier.platform(), larkNotifier);
    }

    public void sendNotice(TpMainFields oldFields, List<String> diffs) {
        NotifyItem notifyItem = DtpNotifyCtxHolder.get().getNotifyItem();
        for (String platformId : notifyItem.getPlatformIds()) {
            NotifyHelper.getPlatform(platformId).ifPresent(p -> {
                DtpNotifier notifier = NOTIFIERS.get(p.getPlatform().toLowerCase());
                if (notifier != null) {
                    notifier.sendChangeMsg(p, oldFields, diffs);
                }
            });
        }
    }

    public void sendAlarm(NotifyItemEnum notifyItemEnum) {
        NotifyItem notifyItem = DtpNotifyCtxHolder.get().getNotifyItem();
        for (String platformId : notifyItem.getPlatformIds()) {
            NotifyHelper.getPlatform(platformId).ifPresent(p -> {
                DtpNotifier notifier = NOTIFIERS.get(p.getPlatform().toLowerCase());
                if (notifier != null) {
                    notifier.sendAlarmMsg(p, notifyItemEnum);
                }
            });
        }
    }

    public static NotifierHandler getInstance() {
        return NotifierHandlerHolder.INSTANCE;
    }

    private static class NotifierHandlerHolder {
        private static final NotifierHandler INSTANCE = new NotifierHandler();
    }
}
