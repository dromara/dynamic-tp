package com.dtp.test.common.util;

import com.dtp.common.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * <span>Form File</span>
 * <p>Description</p>
 *
 * @author topsuder
 * @version v1.0.0
 * @DATE 2023/4/11-15:00
 * @Description
 * @see com.dtp.test.common.util dynamic-tp
 */
@Slf4j
class JsonUtilTest {

    /**
     * 测试 toJson 构造一个测试用例内部类
     */
    static class TestObject {
        private String name;
        private int age;

        public TestObject(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        @Override
        public String toString() {
            return "TestObject{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
    }


    @Test
    void testToJson() {
        //构造测试用例
        TestObject testObject = new TestObject("test", 1);
        JsonUtil.toJson(testObject);
    }

    @Test
    void testFromJson() {
        //构造测试用例
        final TestObject testObject = JsonUtil.fromJson("{\"name\":\"test\",\"age\":1}", TestObject.class);
        log.info(testObject.toString());
    }
}
