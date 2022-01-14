package io.lyh.dtp.common.constant;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * DynamicTpConst related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
public class DynamicTpConst {

    public static final String MAIN_PROPERTIES_PREFIX = "spring.dynamic.tp";

    public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    public static final String PROPERTIES_CHANGE_SHOW_STYLE = "%s => %s";

    public static final String WARNING_COLOR = "#EA9F00";

    public static final String CONTENT_COLOR = "#664B4B";

    public static final List<String> LIVENESS_ALARM_KEYS = Lists.newArrayList(
            "alarmType", "threshold", "corePoolSize",
            "maximumPoolSize", "poolSize", "activeCount"
    );

    public static final List<String> CAPACITY_ALARM_KEYS = Lists.newArrayList(
            "alarmType", "threshold", "queueType",
            "queueCapacity", "queueSize", "queueRemaining"
    );

    public static final List<String> REJECT_ALARM_KEYS = Lists.newArrayList(
            "alarmType", "threshold", "rejectType", "rejectCount"
    );

    public static final List<String> ALL_ALARM_KEYS =
            Stream.of(CAPACITY_ALARM_KEYS, REJECT_ALARM_KEYS, LIVENESS_ALARM_KEYS)
            .flatMap(Collection::stream).collect(Collectors.toList());
}
