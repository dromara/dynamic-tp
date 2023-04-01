package com.dtp.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;


/**
 * Version related
 *
 * @author kamtohung
 */
@Slf4j
public final class VersionUtil {

    private static String version;

    static {
        try {
            version = version();
        } catch (Throwable e) {
            log.warn("no version number found");
        }
    }

    private VersionUtil() { }

    public static String version() {
        // find version info from MANIFEST.MF first
        Package pkg = VersionUtil.class.getPackage();
        String version;
        if (pkg != null) {
            version = pkg.getImplementationVersion();
            if (StringUtils.isNotEmpty(version)) {
                return version;
            }
            version = pkg.getSpecificationVersion();
            if (StringUtils.isNotEmpty(version)) {
                return version;
            }
        }
        return "unknown";
    }

    public static String getVersion() {
        return version;
    }

}
