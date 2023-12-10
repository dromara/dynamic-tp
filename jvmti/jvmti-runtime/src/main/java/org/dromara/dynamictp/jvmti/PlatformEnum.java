package org.dromara.dynamictp.jvmti;

/**
 * Enum of supported operating systems.
 * <p>
 * This file is copied from <a href="https://github.com/alibaba/arthas"/>
 *
 * @author dragon-zhang
 * @since 1.1.6
 */
public enum PlatformEnum {
    /**
     * Microsoft Windows
     */
    WINDOWS,
    /**
     * A flavor of Linux
     */
    LINUX,
    /**
     * macOS (OS X)
     */
    MACOSX,

    UNKNOWN
}