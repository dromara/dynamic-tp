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
 * DingNotifyConst related
 *
 * @author yanhom
 * @since 1.0.0
 **/
public final class DingNotifyConst {

    private DingNotifyConst() { }

    public static final String DING_WEBHOOK = "https://oapi.dingtalk.com/robot/send";

    public static final String ACCESS_TOKEN_PARAM = "access_token";

    public static final String TIMESTAMP_PARAM = "timestamp";

    public static final String SIGN_PARAM = "sign";

    public static final String WARNING_COLOR = "#EA9F00";

    public static final String CONTENT_COLOR = "#664B4B";

    public static final String DING_NOTICE_TITLE = "动态线程池通知";

    public static final String DING_ALARM_TEMPLATE =
            "<font color=#EA9F00>【报警】 </font> 动态线程池运行告警 \n\n" +
            "<font color=#664B4B size=2>服务名称：%s</font> \n\n " +
            "<font color=#664B4B size=2>实例信息：%s</font> \n\n " +
            "<font color=#664B4B size=2>环境：%s</font> \n\n " +
            "<font color=#664B4B size=2>线程池名称：%s</font> \n\n " +
            "<font color=alarmType size=2>报警项：%s</font> \n\n " +
            "<font color=alarmValue size=2>报警阈值 / 当前值：%s</font> \n\n " +
            "<font color=corePoolSize size=2>核心线程数：%d</font> \n\n " +
            "<font color=maximumPoolSize size=2>最大线程数：%d</font> \n\n " +
            "<font color=poolSize size=2>当前线程数：%d</font> \n\n " +
            "<font color=activeCount size=2>活跃线程数：%d</font> \n\n " +
            "<font color=#664B4B size=2>历史最大线程数：%d</font> \n\n " +
            "<font color=#664B4B size=2>任务总数：%d</font> \n\n " +
            "<font color=#664B4B size=2>执行完成任务数：%d</font> \n\n " +
            "<font color=#664B4B size=2>等待执行任务数：%d</font> \n\n " +
            "<font color=queueType size=2>队列类型：%s</font> \n\n " +
            "<font color=queueCapacity size=2>队列容量：%d</font> \n\n " +
            "<font color=queueSize size=2>队列任务数量：%d</font> \n\n " +
            "<font color=queueRemaining size=2>队列剩余容量：%d</font> \n\n " +
            "<font color=rejectType size=2>拒绝策略：%s</font> \n\n" +
            "<font color=rejectCount size=2>总拒绝任务数量：%s</font> \n\n " +
            "<font color=runTimeoutCount size=2>总执行超时任务数量：%s</font> \n\n " +
            "<font color=queueTimeoutCount size=2>总等待超时任务数量：%s</font> \n\n " +
            "<font color=#664B4B size=2>上次报警时间：%s</font> \n\n" +
            "<font color=#664B4B size=2>报警时间：%s</font> \n\n" +
            "<font color=#664B4B size=2>接收人：@%s</font> \n\n" +
            "<font color=#664B4B size=2>trace 信息：%s</font> \n\n" +
            "<font color=#22B838 size=2>报警间隔：%ss</font> \n\n" +
            "<font color=#664B4B size=2>扩展信息：%s</font> \n\n";

    public static final String DING_CHANGE_NOTICE_TEMPLATE =
            "<font color=#5AB030>【通知】</font> 动态线程池参数变更 \n\n " +
            "<font color=#664B4B size=2>服务名称：%s</font> \n\n " +
            "<font color=#664B4B size=2>实例信息：%s</font> \n\n " +
            "<font color=#664B4B size=2>环境：%s</font> \n\n " +
            "<font color=#664B4B size=2>线程池名称：%s</font> \n\n " +
            "<font color=corePoolSize size=2>核心线程数：%s => %s</font> \n\n " +
            "<font color=maxPoolSize size=2>最大线程数：%s => %s</font> \n\n " +
            "<font color=allowCoreThreadTimeOut size=2>允许核心线程超时：%s => %s</font> \n\n " +
            "<font color=keepAliveTime size=2>线程存活时间：%ss => %ss</font> \n\n " +
            "<font color=#664B4B size=2>队列类型：%s</font> \n\n " +
            "<font color=queueCapacity size=2>队列容量：%s => %s</font> \n\n " +
            "<font color=rejectType size=2>拒绝策略：%s => %s</font> \n\n " +
            "<font color=#664B4B size=2>接收人：@%s</font> \n\n" +
            "<font color=#664B4B size=2>通知时间：%s</font> \n\n";
}
