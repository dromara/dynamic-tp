package com.dtp.common.util;

import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;

/**
 * StringUtil related
 *
 * @author yanhom
 * @since 1.0.4
 **/
public final class StringUtil {

    private StringUtil() { }

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
        if (isEmpty(str) || CollectionUtils.isEmpty(testStrList)) {
            return null;
        }
        CharSequence[] array = testStrList.toArray(new CharSequence[0]);
        int size = testStrList.size();
        for (int i = 0; i < size; ++i) {
            CharSequence testStr = array[i];
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
