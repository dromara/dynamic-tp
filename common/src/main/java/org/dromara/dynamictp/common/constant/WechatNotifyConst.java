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
 * WechatNotifyConst related
 *
 * @author yanhom
 * @since 1.0.0
 **/
public final class WechatNotifyConst {

    private WechatNotifyConst() { }

    public static final String WECHAT_WEB_HOOK = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send";

    public static final String KEY_PARAM = "key";

    public static final String WARNING_COLOR = "warning";

    public static final String INFO_COLOR = "info";

    public static final String COMMENT_COLOR = "comment";

    /**
     * receivers only supports userid, view more, see <a href="https://developer.work.weixin.qq.com/document/path/91770">more</a>.
     */
    public static final String WECHAT_ALARM_TEMPLATE =
            "<font color='warning'>【报警】</font> 动态线程池告警 \n" +
            "> <font color='comment'>服务名称：%s</font> \n" +
            "> <font color='comment'>实例信息：%s</font> \n" +
            "> <font color='comment'>环境：%s</font> \n" +
            "> <font color='comment'>线程池名称：%s</font> \n" +
            "> <font color='alarmType'>报警项：%s</font> \n" +
            "> <font color='alarmValue'>报警阈值 / 当前值：%s</font> \n" +
            "> <font color='corePoolSize'>核心线程数：%s</font> \n" +
            "> <font color='maximumPoolSize'>最大线程数：%s</font> \n" +
            "> <font color='poolSize'>当前线程数：%s</font> \n" +
            "> <font color='activeCount'>活跃线程数：%s</font> \n" +
            "> <font color='comment'>历史最大线程数：%s</font> \n" +
            "> <font color='comment'>任务总数：%s</font> \n" +
            "> <font color='comment'>执行完成任务数：%s</font> \n" +
            "> <font color='comment'>等待执行任务数：%s</font> \n" +
            "> <font color='queueType'>队列类型：%s</font> \n" +
            "> <font color='queueCapacity'>队列容量：%s</font> \n" +
            "> <font color='queueSize'>队列任务数量：%s</font> \n" +
            "> <font color='queueRemaining'>队列剩余容量：%s</font> \n" +
            "> <font color='rejectType'>拒绝策略：%s</font> \n" +
            "> <font color='rejectCount'>总拒绝任务数量：%s</font> \n" +
            "> <font color='runTimeoutCount'>总执行超时任务数量：%s</font> \n" +
            "> <font color='queueTimeoutCount'>总等待超时任务数量：%s</font> \n" +
            "> <font color='comment'>上次报警时间：%s</font> \n" +
            "> <font color='comment'>报警时间：%s</font> \n" +
            "> <font color='comment'>接收人：%s</font> \n" +
            "> <font color='comment'>trace 信息：%s</font> \n" +
            "> <font color='info'>报警间隔：%ss</font> \n" +
            "> <font color='comment'>扩展信息：%s</font> \n";

    public static final String WECHAT_CHANGE_NOTICE_TEMPLATE =
            "<font color='info'>【通知】</font> 动态线程池参数变更 \n" +
            "> <font color='comment'>服务名称：%s</font> \n" +
            "> <font color='comment'>实例信息：%s</font> \n" +
            "> <font color='comment'>环境：%s</font> \n" +
            "> <font color='comment'>线程池名称：%s</font> \n" +
            "> <font color='corePoolSize'>核心线程数：%s => %s</font> \n" +
            "> <font color='maxPoolSize'>最大线程数：%s => %s</font> \n" +
            "> <font color='allowCoreThreadTimeOut'>允许核心线程超时：%s => %s</font> \n" +
            "> <font color='keepAliveTime'>线程存活时间：%ss => %ss</font> \n" +
            "> <font color='comment'>队列类型：%s</font> \n" +
            "> <font color='queueCapacity'>队列容量：%s => %s</font> \n" +
            "> <font color='rejectType'>拒绝策略：%s => %s</font> \n" +
            "> <font color='comment'>接收人：%s</font> \n" +
            "> <font color='comment'>通知时间：%s</font> \n";
}
