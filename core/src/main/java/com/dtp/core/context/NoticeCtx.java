package com.dtp.core.context;

import com.dtp.common.dto.DtpMainProp;
import com.dtp.common.dto.ExecutorWrapper;
import com.dtp.common.dto.NotifyItem;
import com.dtp.common.dto.NotifyPlatform;
import com.dtp.common.em.NotifyTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * NoticeCtx related
 *
 * @author: yanhom
 * @since 1.0.8
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NoticeCtx extends BaseNotifyCtx {

    private DtpMainProp prop;

    private List<String> diffs;

    public NoticeCtx(ExecutorWrapper wrapper, NotifyItem notifyItem, NotifyTypeEnum notifyType,
                     List<NotifyPlatform> platforms, DtpMainProp prop, List<String> diffs) {
        super(wrapper, notifyItem, notifyType);
        setPlatforms(platforms);
        this.prop = prop;
        this.diffs = diffs;
    }
}
