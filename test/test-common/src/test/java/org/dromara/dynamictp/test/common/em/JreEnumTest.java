package org.dromara.dynamictp.test.common.em;

import org.dromara.dynamictp.common.em.JreEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;

/**
 * JreEnumTest related
 */
class JreEnumTest {

    @Test
    @EnabledOnJre(value = JRE.JAVA_8)
    void testJRE8() {
        Assertions.assertEquals(JreEnum.JAVA_8, JreEnum.currentVersion());
    }

    @Test
    @EnabledOnJre(value = JRE.JAVA_11)
    void testJRE11() {
        // 当前JRE版本为11，但是通过System.setProperty("java.version", "")设置为空情况
        System.setProperty("java.version", "");
        Assertions.assertEquals(JreEnum.JAVA_11, JreEnum.currentVersion());
    }

}
