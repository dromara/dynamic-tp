package com.dtp.common.em;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

/**
 * JRE version
 * @author kamtohung
 */
@Slf4j
public enum JreEnum {

    /**
     * JRE version
     */
    JAVA_8,

    JAVA_9,

    JAVA_10,

    JAVA_11,

    JAVA_12,

    JAVA_13,

    JAVA_14,

    JAVA_15,

    JAVA_16,

    JAVA_17,

    JAVA_18,

    JAVA_19;

    private static final JreEnum VERSION = getJre();

    public static final String DEFAULT_JAVA_VERSION = "1.8";

    /**
     * get current JRE version
     *
     * @return JRE version
     */
    public static JreEnum currentVersion() {
        return VERSION;
    }

    /**
     * is current version
     *
     * @return true if current version
     */
    public boolean isCurrentVersion() {
        return this == VERSION;
    }

    private static JreEnum getJre() {
        String version = System.getProperty("java.version");
        boolean isBlank = StringUtils.isBlank(version);
        if (isBlank) {
            log.debug("java.version is blank");
        }
        if (!isBlank && version.startsWith(DEFAULT_JAVA_VERSION)) {
            return JAVA_8;
        }
        try {
            // JDK 9+以上版本使用Runtime.version()获取JRE版本
            Object javaRunTimeVersion = MethodUtils.invokeMethod(Runtime.getRuntime(), "version");
            int majorVersion = (int) MethodUtils.invokeMethod(javaRunTimeVersion, "major");
            return JreEnum.valueOf("JAVA_" + majorVersion);
        } catch (Exception e) {
            log.debug("can't determine current JRE version", e);
        }
        return JAVA_8;
    }

}
