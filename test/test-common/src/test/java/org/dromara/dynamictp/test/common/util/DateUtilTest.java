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

import org.dromara.dynamictp.common.util.DateUtil;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * DateUtilTest related
 *
 * @author <a href = "kamtohung@gmail.com">KamTo Hung</a>
 */
public class DateUtilTest {

    @Test
    void now() {
        String now = DateUtil.now();
        assertNotNull(now);
    }

    @Test
    void convertToLocalDate() {
        Calendar myCalendar = new GregorianCalendar(2023, Calendar.APRIL, 10);
        Date date = myCalendar.getTime();
        LocalDate expected = LocalDate.of(2023, 4, 10);
        LocalDate actual = DateUtil.convertToLocalDate(date);
        assertEquals(expected, actual);
    }

    @Test
    void convertToDate() {
        Calendar myCalendar = new GregorianCalendar(2023, Calendar.APRIL, 10);
        Date expected = myCalendar.getTime();
        Date actual = DateUtil.convertToDate(LocalDate.of(2023, 4, 10));
        assertEquals(expected, actual);
    }

    @Test
    void convertToLocalDateTime() {
        Calendar myCalendar = new GregorianCalendar(2023, Calendar.APRIL, 10);
        Date date = myCalendar.getTime();
        LocalDateTime expected = LocalDateTime.of(2023, 4, 10, 0, 0, 0);
        LocalDateTime actual = DateUtil.convertToLocalDateTime(date);
        assertEquals(expected, actual);
    }

    @Test
    void localDateTimeConvertToDate() {
        Calendar myCalendar = new GregorianCalendar(2023, Calendar.APRIL, 10);
        Date date = myCalendar.getTime();
        LocalDateTime localDateTime = LocalDateTime.of(2023, 4, 10, 0, 0, 0);
        Date actual = DateUtil.convertToDate(localDateTime);
        assertEquals(date, actual);
    }


}
