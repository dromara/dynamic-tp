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

package org.dromara.dynamictp.test.core.notify;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.dromara.dynamictp.common.constant.LarkNotifyConst;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.common.entity.NotifyPlatform;
import org.dromara.dynamictp.common.notifier.Notifier;
import org.dromara.dynamictp.core.notifier.DtpLarkNotifier;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author codex
 */
class DtpLarkNotifierTest {

    private final TestableDtpLarkNotifier notifier = new TestableDtpLarkNotifier(Mockito.mock(Notifier.class));

    @Test
    void testPlatform() {
        assertEquals("lark", notifier.platform());
    }

    @Test
    void testTemplatesAndColors() {
        assertEquals(LarkNotifyConst.LARK_CHANGE_NOTICE_JSON_STR, notifier.noticeTemplate());
        assertEquals(LarkNotifyConst.LARK_ALARM_JSON_STR, notifier.alarmTemplate());

        Pair<String, String> colors = notifier.colors();
        assertEquals(LarkNotifyConst.WARNING_COLOR, colors.getLeft());
        assertEquals(LarkNotifyConst.COMMENT_COLOR, colors.getRight());
    }

    @Test
    void testFormatReceiversWithOpenIdAndUserName() {
        String result = notifier.receivers("ou_123456,tester");

        assertEquals("<at id='ou_123456'></at> <at id='tester'>tester</at>", result);
    }

    @Test
    void testGetReceivesUsesNotifyItemFirst() {
        NotifyItem notifyItem = new NotifyItem();
        notifyItem.setReceivers("ou_notice,alice");
        NotifyPlatform platform = new NotifyPlatform();
        platform.setReceivers("fallback");

        assertEquals("<at id='ou_notice'></at> <at id='alice'>alice</at>",
                notifier.getReceivesForTest(notifyItem, platform));
    }

    @Test
    void testGetReceivesFallsBackToPlatform() {
        NotifyItem notifyItem = new NotifyItem();
        NotifyPlatform platform = new NotifyPlatform();
        platform.setReceivers("ou_platform,bob");

        assertEquals("<at id='ou_platform'></at> <at id='bob'>bob</at>",
                notifier.getReceivesForTest(notifyItem, platform));
    }

    @Test
    void testGetReceivesReturnsEmptyWhenBothBlank() {
        NotifyItem notifyItem = new NotifyItem();
        NotifyPlatform platform = new NotifyPlatform();
        platform.setReceivers(StringUtils.EMPTY);

        assertEquals(StringUtils.EMPTY, notifier.getReceivesForTest(notifyItem, platform));
    }

    private static class TestableDtpLarkNotifier extends DtpLarkNotifier {

        TestableDtpLarkNotifier(Notifier notifier) {
            super(notifier);
        }

        private String noticeTemplate() {
            return getNoticeTemplate();
        }

        private String alarmTemplate() {
            return getAlarmTemplate();
        }

        private Pair<String, String> colors() {
            return getColors();
        }

        private String receivers(String receives) {
            return formatReceivers(receives);
        }

        private String getReceivesForTest(NotifyItem notifyItem, NotifyPlatform platform) {
            return getReceives(notifyItem, platform);
        }
    }
}
