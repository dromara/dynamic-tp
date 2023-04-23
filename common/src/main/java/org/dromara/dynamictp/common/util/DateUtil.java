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

    private DateUtil() {
    }

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
