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

/**
 * YunZhiJiaNotifyConst related
 *
 * @author husky12138
 * @since 1.1.4
 **/
public final class YunZhiJiaNotifyConst {

    private YunZhiJiaNotifyConst() {
    }

    public static final String WEB_HOOK = "https://www.yunzhijia.com/gateway/robot/webhook/send";

    public static final String YZJ_TYPE_PARAM = "yzjtype";

    public static final String YZJ_TOKEN_PARAM = "yzjtoken";

    public static final String WARNING_COLOR = "warning";

    public static final String INFO_COLOR = "info";

    public static final String COMMENT_COLOR = "comment";

    public static final String PLATFORM_NAME = "YUNZHIJIA";

    public static final String ALARM_TEMPLATE =
            "【报警】 动态线程池告警 \n" +
                    "服务名称：%s \n" +
                    "实例信息：%s \n" +
                    "环境：%s \n" +
                    "线程池名称：%s \n" +
                    "报警类型：%s \n" +
                    "报警阈值：%s \n" +
                    "核心线程数：%s \n" +
                    "最大线程数：%s \n" +
                    "当前线程数：%s \n" +
                    "活跃线程数：%s \n" +
                    "历史最大线程数：%s \n" +
                    "任务总数：%s \n" +
                    "执行完成任务数：%s \n" +
                    "等待执行任务数：%s \n" +
                    "队列类型：%s \n" +
                    "队列容量：%s \n" +
                    "队列任务数量：%s \n" +
                    "队列剩余容量：%s \n" +
                    "拒绝策略：%s \n" +
                    "拒绝任务数量：%s \n" +
                    "执行超时任务数量：%s \n" +
                    "等待超时任务数量：%s \n" +
                    "上次报警时间：%s \n" +
                    "报警时间：%s \n" +
                    "接收人：@%s \n" +
                    "trace 信息：%s \n" +
                    "报警间隔：%ss \n" +
                    "扩展信息：%s \n";

    public static final String CHANGE_NOTICE_TEMPLATE =
            "【通知】 动态线程池参数变更 \n" +
                    "服务名称：%s \n" +
                    "实例信息：%s \n" +
                    "环境：%s \n" +
                    "线程池名称：%s \n" +
                    "核心线程数：%s => %s \n" +
                    "最大线程数：%s => %s \n" +
                    "允许核心线程超时：%s => %s \n" +
                    "线程存活时间：%ss => %ss \n" +
                    "队列类型：%s \n" +
                    "队列容量：%s => %s \n" +
                    "拒绝策略：%s => %s \n" +
                    "接收人：@%s \n" +
                    "通知时间：%s \n";
}
