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
