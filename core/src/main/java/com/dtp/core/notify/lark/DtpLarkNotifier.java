package com.dtp.core.notify.lark;

import com.dtp.common.constant.LarkNotifyConst;
import com.dtp.common.dto.DtpMainProp;
import com.dtp.common.dto.NotifyPlatform;
import com.dtp.common.em.NotifyPlatformEnum;
import com.dtp.common.em.NotifyTypeEnum;
import com.dtp.core.context.DtpContext;
import com.dtp.core.context.DtpContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import static com.dtp.common.constant.LarkNotifyConst.LARK_ALARM_JSON_STR;
import static com.dtp.common.constant.LarkNotifyConst.LARK_CHANGE_NOTICE_JSON_STR;

/**
 * DtpLarkNotifier
 *
 * @author fxbin
 * @version v1.0
 * @since 2022/4/28 23:25
 */
@Slf4j
public class DtpLarkNotifier extends AbstractLarkNotifier {

    @Override
    public String platform() {
        return NotifyPlatformEnum.LARK.name().toLowerCase();
    }

    @Override
    public void sendChangeMsg(DtpMainProp oldProp, List<String> diffs) {
        DtpContext contextWrapper = DtpContextHolder.get();
        NotifyPlatform platform = contextWrapper.getPlatform(NotifyPlatformEnum.LARK.name());
        String content = buildNoticeContent(platform, LARK_CHANGE_NOTICE_JSON_STR, oldProp, diffs);
        if (StringUtils.isBlank(content)) {
            return;
        }
        doSend(platform, content);
    }

    @Override
    public void sendAlarmMsg(NotifyTypeEnum typeEnum) {
        DtpContext contextWrapper = DtpContextHolder.get();
        NotifyPlatform platform = contextWrapper.getPlatform(NotifyPlatformEnum.LARK.name());
        String content = buildAlarmContent(platform, typeEnum, LARK_ALARM_JSON_STR);
        if (StringUtils.isBlank(content)) {
            return;
        }
        doSend(platform, content);
    }

    @Override
    protected Pair<String, String> getColors() {
        return new ImmutablePair<>(LarkNotifyConst.WARNING_COLOR, LarkNotifyConst.COMMENT_COLOR);
    }
}
