package org.dromara.dynamictp.core.notifier;

import org.dromara.dynamictp.common.entity.TpMainFields;
import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.entity.NotifyPlatform;

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
     * @param notifyPlatform notify platform
     * @param oldFields      old properties
     * @param diffs          the changed keys
     */
    void sendChangeMsg(NotifyPlatform notifyPlatform, TpMainFields oldFields, List<String> diffs);

    /**
     * Send alarm message.
     *
     * @param notifyPlatform notify platform
     * @param notifyItemEnum notify item enum
     */
    void sendAlarmMsg(NotifyPlatform notifyPlatform, NotifyItemEnum notifyItemEnum);
}
