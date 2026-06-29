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

import org.apache.commons.lang3.tuple.Pair;
import org.dromara.dynamictp.common.constant.DingNotifyConst;
import org.dromara.dynamictp.common.notifier.Notifier;
import org.dromara.dynamictp.core.notifier.DtpDingNotifier;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author codex
 */
class DtpDingNotifierTest {

    private final TestableDtpDingNotifier notifier = new TestableDtpDingNotifier(Mockito.mock(Notifier.class));

    @Test
    void testPlatform() {
        assertEquals("ding", notifier.platform());
    }

    @Test
    void testTemplatesAndColors() {
        assertEquals(DingNotifyConst.DING_CHANGE_NOTICE_TEMPLATE, notifier.noticeTemplate());
        assertEquals(DingNotifyConst.DING_ALARM_TEMPLATE, notifier.alarmTemplate());

        Pair<String, String> colors = notifier.colors();
        assertEquals(DingNotifyConst.WARNING_COLOR, colors.getLeft());
        assertEquals(DingNotifyConst.CONTENT_COLOR, colors.getRight());
    }

    private static class TestableDtpDingNotifier extends DtpDingNotifier {

        TestableDtpDingNotifier(Notifier notifier) {
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
    }
}
