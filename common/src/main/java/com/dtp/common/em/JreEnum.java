package com.dtp.common.em;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;


/**
 * JRE version
 */
@Slf4j
public enum JreEnum {

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

    JAVA_19,

    OTHER;

    private static final JreEnum VERSION = getJre();

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
        if (!isBlank && version.startsWith("1.8")) {
            return JAVA_8;
        }
        try {
            // JDK 9+以上版本使用Runtime.version()获取JRE版本
            Object javaRunTimeVersion = MethodUtils.invokeMethod(Runtime.getRuntime(), "version");
            int majorVersion = (int) MethodUtils.invokeMethod(javaRunTimeVersion, "major");
            switch (majorVersion) {
                case 9:
                    return JAVA_9;
                case 10:
                    return JAVA_10;
                case 11:
                    return JAVA_11;
                case 12:
                    return JAVA_12;
                case 13:
                    return JAVA_13;
                case 14:
                    return JAVA_14;
                case 15:
                    return JAVA_15;
                case 16:
                    return JAVA_16;
                case 17:
                    return JAVA_17;
                case 18:
                    return JAVA_18;
                case 19:
                    return JAVA_19;
                default:
                    return OTHER;
            }
        } catch (Exception e) {
            log.debug("can't determine current JRE version", e);
        }
        return JAVA_8;
    }

}
