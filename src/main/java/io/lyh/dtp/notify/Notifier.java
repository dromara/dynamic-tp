package io.lyh.dtp.notify;

import io.lyh.dtp.common.em.NotifyTypeEnum;
import io.lyh.dtp.domain.DtpMainPropWrapper;

import java.util.List;

/**
 * Notifier related
 *
 * @author: yanhom1314@gmail.com
 * @date: 2021-12-22 10:20
 * @since 1.0.0
 **/
public interface Notifier {

    /**
     * Get send platform
     *
     * @return platform
     */
    String platform();

    /**
     * Send notify message.
     *
     * @param oldPropWrapper old properties
     * @param diffs the changed keys
     */
    void sendChangeMsg(DtpMainPropWrapper oldPropWrapper, List<String> diffs);

    /**
     * Send alarm message
     * @param typeEnum notify type
     */
    void sendAlarmMsg(NotifyTypeEnum typeEnum);
}
