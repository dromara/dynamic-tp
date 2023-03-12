package com.dtp.core.context;

import com.dtp.common.entity.TpMainFields;
import com.dtp.core.support.ExecutorWrapper;
import com.dtp.common.entity.NotifyItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * NoticeCtx related
 *
 * @author yanhom
 * @since 1.0.8
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NoticeCtx extends BaseNotifyCtx {

    private TpMainFields oldFields;

    private List<String> diffs;

    public NoticeCtx(ExecutorWrapper wrapper, NotifyItem notifyItem, TpMainFields oldFields, List<String> diffs) {
        super(wrapper, notifyItem);
        this.oldFields = oldFields;
        this.diffs = diffs;
    }
}
