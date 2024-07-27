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

package org.dromara.dynamictp.common.constant;

/**
 * LarkNotifyConst related
 *
 * @author fxbin
 * @version v1.0
 * @since 2022/4/28 23:27
 */
public class LarkNotifyConst {

    private LarkNotifyConst() { }

    /**
     * lark bot url
     */
    public static final String LARK_WEBHOOK = "https://open.feishu.cn/open-apis/bot/v2/hook";

    /**
     * lark at format. openid
     * 当配置openid时,机器人可以@人
     */
    public static final String LARK_AT_FORMAT_OPENID = "<at id='%s'></at>";

    /**
     * lark at format. username
     * 当配置username时,只能蓝色字体展示@username,被@人无@提醒
     */
    public static final String LARK_AT_FORMAT_USERNAME = "<at id='%s'>%s</at>";

    /**
     * lark openid prefix
     */
    public static final String LARK_OPENID_PREFIX = "ou_";

    public static final String WARNING_COLOR = "\uD83D\uDD34";

    public static final String INFO_COLOR = "";

    public static final String COMMENT_COLOR = "";

    public static final String SIGN_REPLACE = "\\{";

    public static final String SIGN_PARAM_PREFIX = "{\"timestamp\": \"%s\",\"sign\": \"%s\",";

    /**
     * lark alarm json str
     */
    public static final String LARK_ALARM_JSON_STR =
            "{\"msg_type\":\"interactive\",\"card\":{\"config\":{\"wide_screen_mode\":true},\"header\":{\"template\":\"red\",\"title\":{\"tag\":\"plain_text\",\"content\":\"【报警】 动态线程池告警\"}},\"elements\":[{\"tag\":\"div\",\"fields\":[{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"**服务名称：**\\n%s\"}},{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"**实例信息：**\\n%s\"}}]},{\"tag\":\"div\",\"fields\":[{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"**环境：**\\n%s\"}},{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"**线程池名称：**\\n%s\"}}]},{\"tag\":\"div\",\"fields\":[{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"alarmType **报警项：**\\n%s\"}},{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"alarmValue **报警阈值 / 当前值：**\\n%s\"}}]},{\"tag\":\"hr\"},{\"tag\":\"div\",\"fields\":[{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"corePoolSize **核心线程数：**\\n%s\"}},{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"maximumPoolSize **最大线程数：**\\n%s\"}}]},{\"tag\":\"div\",\"fields\":[{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"poolSize **当前线程数：**\\n%s\"}},{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"activeCount **活跃线程数：**\\n%s\"}}]},{\"tag\":\"div\",\"fields\":[{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"**历史最大线程数：**\\n%s\"}},{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"**任务总数：**\\n%s\"}}]},{\"tag\":\"div\",\"fields\":[{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"**执行完成任务数：**\\n%s\"}},{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"**等待执行任务数：**\\n%s\"}}]},{\"tag\":\"hr\"},{\"tag\":\"div\",\"fields\":[{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"queueType **队列类型：**\\n%s\"}},{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"queueCapacity **队列容量：**\\n%s\"}}]},{\"tag\":\"div\",\"fields\":[{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"queueSize **队列任务数量：**\\n%s\"}},{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"queueRemaining **队列剩余容量：**\\n%s\"}}]},{\"tag\":\"div\",\"fields\":[{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"rejectType **拒绝策略：**\\n%s\"}},{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"rejectCount **总拒绝任务数量：**\\n%s\"}}]},{\"tag\":\"div\",\"fields\":[{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"runTimeoutCount **总执行超时任务数量：**\\n%s\"}},{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"queueTimeoutCount **总等待超时任务数量：**\\n%s\"}}]},{\"tag\":\"hr\"},{\"tag\":\"div\",\"fields\":[{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"**上次报警时间：**\\n %s\"}},{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"**报警时间：**\\n %s\"}}]},{\"tag\":\"div\",\"fields\":[{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"**接收人：**\\n %s\"}},{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"**trace 信息：**\\n %s\"}}]},{\"tag\":\"div\",\"fields\":[{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"**报警间隔：**\\n %ss\"}},{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"**扩展信息：**\\n %s\"}}]}]}}";

    /**
     * lark notice json str
     */
    public static final String LARK_CHANGE_NOTICE_JSON_STR =
            "{\"msg_type\":\"interactive\",\"card\":{\"config\":{\"wide_screen_mode\":true},\"header\":{\"template\":\"green\",\"title\":{\"tag\":\"plain_text\",\"content\":\"【通知】动态线程池参数变更\"}},\"elements\":[{\"tag\":\"div\",\"fields\":[{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"**服务名称: ** \\n %s\"}},{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"**实例信息：**\\n %s\"}}]},{\"tag\":\"div\",\"fields\":[{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"**环境：**\\n %s\"}},{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"**线程池名称：**\\n %s\"}}]},{\"tag\":\"hr\"},{\"tag\":\"div\",\"fields\":[{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"corePoolSize **核心线程数：**\\n %s => %s\"}},{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"maxPoolSize **最大线程数：**\\n %s => %s\"}}]},{\"tag\":\"div\",\"fields\":[{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"allowCoreThreadTimeOut **允许核心线程超时：**\\n %s => %s\"}},{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"keepAliveTime **线程存活时间：**\\n %s => %s\"}}]},{\"tag\":\"hr\"},{\"tag\":\"div\",\"fields\":[{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"**队列类型：**\\n %s\"}},{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"queueCapacity **队列容量：**\\n %s => %s\"}}]},{\"tag\":\"div\",\"fields\":[{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"rejectType **拒绝策略：**\\n %s => %s\"}}]},{\"tag\":\"hr\"},{\"tag\":\"div\",\"fields\":[{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"**接收人：**\\n %s\"}},{\"is_short\":true,\"text\":{\"tag\":\"lark_md\",\"content\":\"**通知时间：**\\n %s\"}}]}]}}";

}
