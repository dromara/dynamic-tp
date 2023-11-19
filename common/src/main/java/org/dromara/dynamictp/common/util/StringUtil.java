/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.dynamictp.common.util;

import org.apache.commons.collections4.CollectionUtils;

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
