package io.lyh.dtp.support;

import io.lyh.dtp.core.DtpExecutor;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * DtpMainPropWrapper related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Data
public class DtpMainPropWrapper {

    private static final List<Field> FIELD_NAMES;

    static {
        FIELD_NAMES = Arrays.asList(DtpMainPropWrapper.class.getDeclaredFields());
    }

    private String dtpName;

    private int corePoolSize;

    private int maxPoolSize;

    private long keepAliveTime;

    private String queueType;

    private int queueCapacity;

    private String rejectType;

    private boolean allowCoreThreadTimeOut;

    public static DtpMainPropWrapper of(DtpExecutor dtpExecutor) {
        DtpMainPropWrapper wrapper = new DtpMainPropWrapper();
        wrapper.setDtpName(dtpExecutor.getThreadPoolName());
        wrapper.setCorePoolSize(dtpExecutor.getCorePoolSize());
        wrapper.setMaxPoolSize(dtpExecutor.getMaximumPoolSize());
        wrapper.setKeepAliveTime(dtpExecutor.getKeepAliveTime(TimeUnit.SECONDS));
        wrapper.setQueueType(dtpExecutor.getQueueName());
        wrapper.setQueueCapacity(dtpExecutor.getQueueCapacity());
        wrapper.setRejectType(dtpExecutor.getRejectHandlerName());
        wrapper.setAllowCoreThreadTimeOut(dtpExecutor.allowsCoreThreadTimeOut());
        return wrapper;
    }

    public static List<Field> getMainProps() {
        return FIELD_NAMES;
    }

}
