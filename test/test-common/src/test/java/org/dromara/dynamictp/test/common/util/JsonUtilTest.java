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

package org.dromara.dynamictp.test.common.util;

import org.dromara.dynamictp.common.util.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * <span>Form File</span>
 * <p>Description</p>
 *
 * @author topsuder
 * @author KamToHung
 * @see org.dromara.dynamictp.test.common.util dynamic-tp
 */
class JsonUtilTest {

    /**
     * 测试 toJson 构造一个测试用例内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class TestObject {

        private int age;

        private Date date;

        private String name;

        private LocalDateTime updateDate;

    }


    @Test
    void testToJson() {
        //language=JSON5
        String expected = "{\"age\":1,\"date\":\"2023-04-12 11:56:00\",\"name\":\"test\",\"updateDate\":\"2023-04-12 11:56:00\"}";
        Calendar myCalendar = new GregorianCalendar(2023, Calendar.APRIL, 12, 11, 56, 0);
        Date date = myCalendar.getTime();
        LocalDateTime updateDate = LocalDateTime.of(2023, 4, 12, 11, 56, 0);
        TestObject testObject = new TestObject(1, date, "test", updateDate);
        assertEquals(expected, JsonUtil.toJson(testObject));
    }

    @Test
    void testFromJson() {
        Calendar myCalendar = new GregorianCalendar(2023, Calendar.APRIL, 12, 11, 56, 0);
        Date expected = myCalendar.getTime();
        LocalDateTime updateDate = LocalDateTime.of(2023, 4, 12, 11, 56, 0);
        //language=JSON5
        String json = "{\"age\":1,\"date\":\"2023-04-12 11:56:00\",\"name\":\"test\",\"updateDate\":\"2023-04-12 11:56:00\"}";
        TestObject testObject = JsonUtil.fromJson(json, TestObject.class);
        assertNotNull(testObject);
        assertEquals("test", testObject.getName());
        assertEquals(1, testObject.getAge());
        assertEquals(expected, testObject.getDate());
        assertEquals(updateDate, testObject.getUpdateDate());
    }
}
