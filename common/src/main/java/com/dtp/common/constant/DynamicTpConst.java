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
}
