package com.dtp.common.constant;

/**
 * DingNotifyConst related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
public class DingNotifyConst {

    private DingNotifyConst() {}

    public static final String DING_WEBHOOK = "https://oapi.dingtalk.com/robot/send?access_token=";

    public static final String WARNING_COLOR = "#EA9F00";

    public static final String CONTENT_COLOR = "#664B4B";

    public static final String DING_ALARM_TITLE = "动态线程池告警";

    public static final String DING_NOTICE_TITLE = "动态线程池通知";

    public static final String DING_ALARM_TEMPLATE =
            "<font color='#EA9F00'>【报警】 </font> 动态线程池运行告警 \n\n" +
                    "<font color='#664B4B' size=2>服务名称：%s</font> \n\n " +
                    "<font color='#664B4B' size=2>实例信息：%s</font> \n\n " +
                    "<font color='#664B4B' size=2>环境：%s</font> \n\n " +
                    "<font color='#664B4B' size=2>线程池名称：%s</font> \n\n " +
                    "<font color='alarmType' size=2>报警类型：%s</font> \n\n " +
                    "<font color='threshold' size=2>报警阈值：%s</font> \n\n " +
                    "<font color='corePoolSize' size=2>核心线程数：%d</font> \n\n " +
                    "<font color='maximumPoolSize' size=2>最大线程数：%d</font> \n\n " +
                    "<font color='poolSize' size=2>当前线程数：%d</font> \n\n " +
                    "<font color='activeCount' size=2>活跃线程数：%d</font> \n\n " +
                    "<font color='#664B4B' size=2>历史最大线程数：%d</font> \n\n " +
                    "<font color='#664B4B' size=2>任务总数：%d</font> \n\n " +
                    "<font color='#664B4B' size=2>执行完成任务数：%d</font> \n\n " +
                    "<font color='#664B4B' size=2>等待执行任务数：%d</font> \n\n " +
                    "<font color='queueType' size=2>队列类型：%s</font> \n\n " +
                    "<font color='queueCapacity' size=2>队列容量：%d</font> \n\n " +
                    "<font color='queueSize' size=2>队列任务数量：%d</font> \n\n " +
                    "<font color='queueRemaining' size=2>队列剩余容量：%d</font> \n\n " +
                    "<font color='rejectType' size=2>拒绝策略：%s</font> \n\n" +
                    "<font color='rejectCount' size=2>拒绝任务数量：%d</font> \n\n " +
                    "<font color='#664B4B' size=2>接收人：@%s</font> \n\n" +
                    "<font color='#664B4B' size=2>通知时间：%s</font> \n\n" +
                    "<font color='#22B838' size=2>报警间隔：%ss</font> \n\n";

    public static final String DING_CHANGE_NOTICE_TEMPLATE =
            "#### <font color='#5AB030'>【通知】</font> 动态线程池参数变更 \n\n " +
                    "<font color='#664B4B' size=2>服务名称：%s</font> \n\n " +
                    "<font color='#664B4B' size=2>实例信息：%s</font> \n\n " +
                    "<font color='#664B4B' size=2>环境：%s</font> \n\n " +
                    "<font color='#664B4B' size=2>线程池名称：%s</font> \n\n " +
                    "<font color='corePoolSize' size=2>核心线程数：%s => %s</font> \n\n " +
                    "<font color='maxPoolSize' size=2>最大线程数：%s => %s</font> \n\n " +
                    "<font color='allowCoreThreadTimeOut' size=2>允许核心线程超时：%s => %s</font> \n\n " +
                    "<font color='keepAliveTime' size=2>线程存活时间：%ss => %ss</font> \n\n " +
                    "<font color='#664B4B' size=2>队列类型：%s</font> \n\n " +
                    "<font color='queueCapacity' size=2>队列容量：%s => %s</font> \n\n " +
                    "<font color='rejectType' size=2>拒绝策略：%s => %s</font> \n\n " +
                    "<font color='#664B4B' size=2>接收人：@%s</font> \n\n" +
                    "<font color='#664B4B' size=2>通知时间：%s</font> \n\n";
}
