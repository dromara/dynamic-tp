package com.dtp.adapter.dubbo.apache;

import org.apache.dubbo.common.Version;

/**
 * DubboVersion related
 *
 * @author yanhom
 * @since 1.0.6
 */
@SuppressWarnings("all")
public class DubboVersion {

    private DubboVersion() {}

    public static final String VERSION_2_7_5 = "2.7.5";

    public static final String VERSION_3_0_3 = "3.0.3";

    /**
     * Compare versions
     * @return the value {@code 0} if {@code version1 == version2};
     *         a value less than {@code 0} if {@code version1 < version2}; and
     *         a value greater than {@code 0} if {@code version1 > version2}
     */
    public static int compare(String v1, String v2) {
        return Integer.compare(Version.getIntVersion(v1), Version.getIntVersion(v2));
    }
}
