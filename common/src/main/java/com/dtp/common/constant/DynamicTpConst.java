package com.dtp.common.constant;

/**
 * DynamicTpConst related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
public class DynamicTpConst {

    private DynamicTpConst() {}

    public static final String MAIN_PROPERTIES_PREFIX = "spring.dynamic.tp";

    public static final String DTP_ENABLED_PROP = MAIN_PROPERTIES_PREFIX + ".enabled";

    public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    public static final String PROPERTIES_CHANGE_SHOW_STYLE = "%s => %s";

    public static final String UNKNOWN = "---";

    public static final String VALUE = "value";

    /**
     * Dtp executor properties const.
     */
    public static final String THREAD_POOL_NAME = "threadPoolName";

    public static final String ALLOW_CORE_THREAD_TIMEOUT = "allowCoreThreadTimeOut";

    public static final String NOTIFY_ITEMS = "notifyItems";

    public static final String WAIT_FOR_TASKS_TO_COMPLETE_ON_SHUTDOWN = "waitForTasksToCompleteOnShutdown";

    public static final String AWAIT_TERMINATION_SECONDS = "awaitTerminationSeconds";

    public static final String PRE_START_ALL_CORE_THREADS = "preStartAllCoreThreads";

    public static final String RUN_TIMEOUT = "runTimeout";

    public static final String QUEUE_TIMEOUT = "queueTimeout";

    public static final String TASK_WRAPPERS = "taskWrappers";

    /**
     * symbol
     */
    public static final String DOT = ".";

    public static final String ARR_LEFT_BRACKET = "[";

    public static final String ARR_RIGHT_BRACKET = "]";
}
