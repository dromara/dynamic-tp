package io.lyh.dtp.common.em;

import lombok.Getter;

/**
 * NotifyTypeEnum related
 *
 * @author: yanhom1314@gmail.com
 * @date: 2021-12-30 14:08
 * @since 1.0.0
 **/
@Getter
public enum NotifyTypeEnum {

    /**
     * 配置变动通知
     */
    CHANGE("change"),

    /**
     * 活性监控
     * 线程池活跃度 = activeCount/maximumPoolSize
     */
    LIVENESS("liveness"),

    /**
     * 容量达到阈值预警
     */
    CAPACITY("capacity"),

    /**
     * 触发拒绝策略告警
     */
    REJECT("reject"),;

    private final String value;

    NotifyTypeEnum(String value) {
        this.value = value;
    }

    public static NotifyTypeEnum of(String value) {
        for (NotifyTypeEnum typeEnum : NotifyTypeEnum.values()) {
            if (typeEnum.value.equals(value)) {
                return typeEnum;
            }
        }
        return null;
    }
}
