package com.dtp.test.common.util;

import com.dtp.common.util.StringUtil;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * StringUtilTest related
 *
 * @author yanhom
 * @date 2022-11-20 8:16 PM
 */
class StringUtilTest {

    @Test
    void testContainsIgnoreCase() {
        String str = "ttl";
        List<String> testStrList = Lists.newArrayList("ttl", "mdc");
        boolean r0 = StringUtil.containsIgnoreCase(str, testStrList);
        Assertions.assertTrue(r0);

        boolean r = StringUtil.containsIgnoreCase("str", testStrList);
        Assertions.assertFalse(r);
    }
}
