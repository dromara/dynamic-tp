package org.dromara.dynamictp.core.notifier.base;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.dromara.dynamictp.common.entity.NotifyPlatform;
import org.dromara.dynamictp.core.notifier.context.BaseNotifyCtx;
import org.dromara.dynamictp.core.notifier.context.DtpNotifyCtxHolder;

import java.util.Optional;

/**
 * AbstractNotifier related
 *
 * @author kyao
 * @since 1.1.3
 */
@Slf4j
public abstract class AbstractNotifier implements Notifier {

    @Override
    public final void send(NotifyPlatform platform, String content) {
        try {
            sendMode(platform, content);
        } catch (Exception e) {
            log.error(StrUtil.format("DynamicTp notify, {} send failed...", platform()), e);
        }
    }

    /**
     * Message sending mode
     * @author kyao
     * @param platform
     * @param content
     */
    protected abstract void sendMode(NotifyPlatform platform, String content);

    /**
     * Get the notifyItem.receivers
     * @param platform platform
     * @return Receivers
     */
    protected String[] getNotifyItemReceivers(NotifyPlatform platform) {
        BaseNotifyCtx context = DtpNotifyCtxHolder.get();
        String receivers = Optional.ofNullable(context)
                .map(BaseNotifyCtx::getNotifyItem)
                .map(NotifyItem::getReceivers)
                .orElse(null);
        receivers = StrUtil.isBlank(receivers) ? platform.getReceivers() : receivers;
        return receivers.split(",");
    }

}
