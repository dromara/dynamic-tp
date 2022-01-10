package io.lyh.dtp.handler.reject;

import io.lyh.dtp.handler.RejectedCountable;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * RejectedCountableDiscardPolicy related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
public class RejectedCountableDiscardPolicy extends ThreadPoolExecutor.DiscardPolicy implements RejectedCountable {

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        beforeReject(e);
        super.rejectedExecution(r, e);
    }
}
