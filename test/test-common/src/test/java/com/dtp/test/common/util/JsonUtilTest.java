package com.dtp.test.common.util;

import com.dtp.common.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Date;

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

        private Date date;

        public TestObject(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public TestObject() {
        }

        public TestObject(String name, int age, Date date) {
            this.name = name;
            this.age = age;
            this.date = date;
        }
        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
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
                    ", date=" + date +
                    '}';
        }
    }


    @Test
    void testToJson() {
        TestObject testObject = new TestObject("test", 1);
        log.info(JsonUtil.toJson(testObject));
    }

    @Test
    void testFromJson() {
        String json = "{\"name\":\"test\",\"age\":1,\"date\":\"2021-04-12 11:56:00\"}";
        TestObject testObject = JsonUtil.fromJson(json, TestObject.class);
        log.info("testObject:{}", testObject);
    }


}
