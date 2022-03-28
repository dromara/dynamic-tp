package com.dtp.common.dto;

import com.dtp.common.em.NotifyTypeEnum;
import lombok.Builder;
import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * AlarmInfo related
 *
 * @author: yanhom
 * @since 1.0.4
 **/
@Data
@Builder
public class AlarmInfo {

    private NotifyTypeEnum type;

    private String lastAlarmTime;

    private final AtomicInteger counter = new AtomicInteger(0);

    public void incCounter() {
        counter.incrementAndGet();
    }

    public void reset() {
        counter.set(0);
    }

    public int getCount() {
        return counter.get();
    }
}
