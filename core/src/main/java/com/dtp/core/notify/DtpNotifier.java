package com.dtp.core.notify;

import com.dtp.common.dto.DtpMainProp;
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
     * @param oldProp old properties
     * @param diffs the changed keys
     */
    void sendChangeMsg(DtpMainProp oldProp, List<String> diffs);

    /**
     * Send alarm message.
     *
     * @param notifyItemEnum notify item enum
     */
    void sendAlarmMsg(NotifyItemEnum notifyItemEnum);
}
