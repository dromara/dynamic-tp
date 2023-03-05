package com.dtp.core.notify;

import com.dtp.common.entity.DtpMainProp;
import com.dtp.common.em.NotifyItemEnum;

import java.util.List;

/**
 * DtpNotifier related
 *
 * @author yanhom
 * @since 1.0.0
 **/
public interface DtpNotifier {

    /**
     * Get the platform name.
     *
     * @return platform
     */
    String platform();

    /**
     * Send change notify message.
     *
     * @param threadPoolName thread pool name
     * @param oldProp        old properties
     * @param diffs          the changed keys
     */
    void sendChangeMsg(String threadPoolName, DtpMainProp oldProp, List<String> diffs);

    /**
     * Send alarm message.
     *
     * @param threadPoolName thread pool name
     * @param notifyItemEnum notify item enum
     */
    void sendAlarmMsg(String threadPoolName, NotifyItemEnum notifyItemEnum);
}
