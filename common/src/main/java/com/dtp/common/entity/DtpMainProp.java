package com.dtp.common.entity;

import lombok.Data;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * DtpMainProp related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Data
public class DtpMainProp {

    private static final List<Field> FIELD_NAMES;

    static {
        FIELD_NAMES = Arrays.asList(DtpMainProp.class.getDeclaredFields());
    }

    private String threadPoolName;

    private int corePoolSize;

    private int maxPoolSize;

    private long keepAliveTime;

    private String queueType;

    private int queueCapacity;

    private String rejectType;

    private boolean allowCoreThreadTimeOut;

    public static List<Field> getMainProps() {
        return FIELD_NAMES;
    }
}
