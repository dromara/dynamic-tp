package com.dtp.common.entity;

import com.dtp.common.em.NotifyItemEnum;
import com.dtp.common.util.DateUtil;
import lombok.Builder;
import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * AlarmInfo related
 *
 * @author yanhom
 * @since 1.0.4
 **/
@Data
@Builder
public class AlarmInfo {

    private NotifyItemEnum notifyItem;

    private String lastAlarmTime;

    private final AtomicInteger counter = new AtomicInteger(0);

    public void incCounter() {
        counter.incrementAndGet();
    }

    public void reset() {
        lastAlarmTime = DateUtil.now();
        counter.set(0);
    }

    public int getCount() {
        return counter.get();
    }
}
