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

package org.dromara.dynamictp.core.notifier.base;

import cn.hutool.core.util.StrUtil;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.common.entity.NotifyPlatform;
import org.dromara.dynamictp.core.notifier.context.AlarmCtx;
import org.dromara.dynamictp.core.notifier.context.DtpNotifyCtxHolder;

import java.util.Optional;

/**
 * Notifier related
 *
 * @author yanhom
 * @since 1.0.8
 */
public interface Notifier {

    /**
     * Get the platform name.
     *
     * @return platform
     */
    String platform();

    /**
     * Send message.
     *
     * @param platform platform
     * @param content  content
     */
    void send(NotifyPlatform platform, String content);

    /**
     * Get the notifyItem.receivers
     * @param platform platform
     * @return Receivers
     */
    default String getNotifyItemReceivers(NotifyPlatform platform) {
        AlarmCtx context = (AlarmCtx) DtpNotifyCtxHolder.get();
        String receivers = Optional.ofNullable(context)
                .map(AlarmCtx::getNotifyItem)
                .map(NotifyItem::getReceivers)
                .orElse(null);
        return StrUtil.isBlank(receivers) ? platform.getReceivers() : receivers;
    }
}
