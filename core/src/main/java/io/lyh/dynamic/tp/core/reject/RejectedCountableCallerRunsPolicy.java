package io.lyh.dynamic.tp.core.reject;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * RejectedCountableCallerRunsPolicy related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
public class RejectedCountableCallerRunsPolicy extends ThreadPoolExecutor.CallerRunsPolicy implements RejectedCountable {

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        beforeReject(e);
        super.rejectedExecution(r, e);
    }
}
