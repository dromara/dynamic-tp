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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日期工具类
 *
 * @author <a href = "kamtohung@gmail.com">hongjintao</a>
 */
public class DateUtil {

    private DateUtil() { }

    public static String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
    }

    /**
     * Date to LocalDate
     *
     * @param date 时间
     * @return LocalDate
     */
    public static LocalDate convertToLocalDate(Date date) {
        return new java.sql.Date(date.getTime()).toLocalDate();
    }

    /**
     * Date to LocalDate
     *
     * @param date 时间
     * @return Date
     */
    public static Date convertToDate(LocalDate date) {
        return java.sql.Date.valueOf(date);
    }

    /**
     * Date to LocalDate
     *
     * @param date 时间
     * @return LocalDateTime
     */
    public static LocalDateTime convertToLocalDateTime(Date date) {
        return new java.sql.Timestamp(date.getTime()).toLocalDateTime();
    }

    /**
     * LocalDate to Date
     *
     * @param date 时间
     * @return Date
     */
    public static Date convertToDate(LocalDateTime date) {
        return java.sql.Timestamp.valueOf(date);
    }

}
