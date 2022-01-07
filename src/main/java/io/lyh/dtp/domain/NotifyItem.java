package io.lyh.dtp.domain;

import io.lyh.dtp.common.em.NotifyPlatformEnum;
import io.lyh.dtp.common.em.NotifyTypeEnum;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * NotifyItemConf related
 *
 * @author: yanhom1314@gmail.com
 * @date: 2021-12-28 16:18
 * @since 1.0.0
 **/
@Data
public class NotifyItem {

    /**
     * 通知平台
     * @see NotifyPlatformEnum
     */
    private List<String> platforms;

    /**
     * 是否开启通知
     */
    private boolean enabled = true;

    /**
     * 通知类型
     * @see NotifyTypeEnum
     */
    private String type;

    /**
     * 报警项阈值
     */
    private int threshold;

    /**
     * 报警间隔（s）
     */
    private Integer interval = 120;

    /**
     * 默认支持通知配置（线程池变动通知、活性报警、容量报警、拒绝报警）
     */
    private static final List<NotifyItem> DEFAULT_NOTIFY_ITEMS;

    static {
        NotifyItem changeNotify = new NotifyItem();
        changeNotify.setType(NotifyTypeEnum.CHANGE.getValue());
        changeNotify.setInterval(null);

        NotifyItem livenessNotify = new NotifyItem();
        livenessNotify.setType(NotifyTypeEnum.LIVENESS.getValue());
        // 队列元素数量达到容量的${threshold}%后报警
        livenessNotify.setThreshold(80);
        livenessNotify.setInterval(300);

        NotifyItem capacityNotify = new NotifyItem();
        capacityNotify.setType(NotifyTypeEnum.CAPACITY.getValue());
        // 队列元素数量达到容量的${threshold}%后报警
        capacityNotify.setThreshold(80);
        capacityNotify.setInterval(300);

        NotifyItem rejectNotify = new NotifyItem();
        rejectNotify.setType(NotifyTypeEnum.REJECT.getValue());
        // 拒绝任务数量达到${threshold}个后报警
        rejectNotify.setThreshold(1);
        rejectNotify.setInterval(300);

        DEFAULT_NOTIFY_ITEMS = Lists.newArrayList();
        DEFAULT_NOTIFY_ITEMS.add(livenessNotify);
        DEFAULT_NOTIFY_ITEMS.add(changeNotify);
        DEFAULT_NOTIFY_ITEMS.add(capacityNotify);
        DEFAULT_NOTIFY_ITEMS.add(rejectNotify);
    }

    public static List<NotifyItem> getDefaultNotifyItems() {
        return DEFAULT_NOTIFY_ITEMS;
    }
}
