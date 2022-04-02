package com.dtp.common.util;

import cn.hutool.core.util.ArrayUtil;

import java.util.Collection;

/**
 * StringUtil related
 *
 * @author: yanhom
 * @since 1.0.4
 **/
public class StringUtil {

    private StringUtil() {}

    public static boolean isEmpty(CharSequence str) {
        return null == str || str.length() == 0;
    }

    public static boolean isNotEmpty(CharSequence str) {
        return !isEmpty(str);
    }

    public static boolean containsIgnoreCase(CharSequence str, Collection<? extends CharSequence> testStrList) {
        return null != getContainsStrIgnoreCase(str, testStrList);
    }

    public static String getContainsStrIgnoreCase(CharSequence str, Collection<? extends CharSequence> testStrList) {
        if (isEmpty(str) || ArrayUtil.isEmpty(testStrList)) {
            return null;
        }
        CharSequence[] var2 = testStrList.toArray(new CharSequence[0]);
        int var3 = testStrList.size();
        for(int var4 = 0; var4 < var3; ++var4) {
            CharSequence testStr = var2[var4];
            if (containsIgnoreCase(str, testStr)) {
                return testStr.toString();
            }
        }
        return null;
    }

    public static boolean containsIgnoreCase(CharSequence str, CharSequence testStr) {
        if (null == str) {
            return null == testStr;
        }
        return str.toString().toLowerCase().contains(testStr.toString().toLowerCase());
    }
}
