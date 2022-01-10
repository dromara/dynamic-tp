package io.lyh.dtp.notify.ding;

import cn.hutool.core.util.StrUtil;
import io.lyh.dtp.core.DtpContextHolder;
import io.lyh.dtp.core.DtpContext;
import io.lyh.dtp.common.em.NotifyTypeEnum;
import io.lyh.dtp.support.DtpMainPropWrapper;
import io.lyh.dtp.notify.NotifyPlatform;
import io.lyh.dtp.common.constant.DynamicTpConst;
import io.lyh.dtp.common.em.NotifyPlatformEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * DtpDingNotifier related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Slf4j
public class DtpDingNotifier extends DingAbsNotifier {

    @Override
    public String platform() {
        return NotifyPlatformEnum.DING.name().toLowerCase();
    }

    @Override
    public void sendChangeMsg(DtpMainPropWrapper oldPropWrapper, List<String> diffs) {
        DtpContext contextWrapper = DtpContextHolder.get();
        NotifyPlatform platform = contextWrapper.getPlatform(NotifyPlatformEnum.DING.name());
        String content = buildNoticeContent(platform, DingNotifyConst.DING_CHANGE_NOTICE_TEMPLATE, oldPropWrapper, diffs);
        if (StringUtils.isBlank(content)) {
            return;
        }

        List<String> receivesList = StrUtil.split(platform.getReceivers(), ',');
        doSend(platform, DingNotifyConst.DING_NOTICE_TITLE, content, receivesList);
    }

    @Override
    public void sendAlarmMsg(NotifyTypeEnum typeEnum) {
        DtpContext contextWrapper = DtpContextHolder.get();
        NotifyPlatform platform = contextWrapper.getPlatform(NotifyPlatformEnum.DING.name());
        String content = buildAlarmContent(platform, typeEnum, DingNotifyConst.DING_ALARM_TEMPLATE);
        if (StringUtils.isBlank(content)) {
            return;
        }

        List<String> receivesList = StrUtil.split(platform.getReceivers(), ',');
        doSend(platform, DingNotifyConst.DING_ALARM_TITLE, content, receivesList);
    }

    @Override
    protected Pair<String, String> getColors() {
        return new ImmutablePair<>(DynamicTpConst.WARNING_COLOR, DynamicTpConst.CONTENT_COLOR);
    }
}
