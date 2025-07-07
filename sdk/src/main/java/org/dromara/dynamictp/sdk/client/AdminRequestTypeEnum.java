package org.dromara.dynamictp.sdk.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AdminRequestTypeEnum {

    EXECUTOR_MONITOR("executor_monitor"),

    EXECUTOR_REFRESH("executor_refresh"),

    ALARM_MANAGE("alarm_manage"),

    LOG_MANAGE("log_manage");

    private final String value;

    public static AdminRequestTypeEnum of(String value) {
        for (AdminRequestTypeEnum adminRequestType : AdminRequestTypeEnum.values()) {
            if (adminRequestType.value.equals(value)) {
                return adminRequestType;
            }
        }
        return null;
    }
}
