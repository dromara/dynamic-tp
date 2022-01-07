package io.lyh.dtp.handler.reject;

import io.lyh.dtp.handler.RejectedCountable;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * RejectedCountableAbortPolicy related
 *
 * @author: yanhom1314@gmail.com
 * @date: 2021-12-30 16:08
 * @since 1.0.0
 **/
public class RejectedCountableAbortPolicy extends ThreadPoolExecutor.AbortPolicy implements RejectedCountable {

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        beforeReject(e);
        super.rejectedExecution(r, e);
    }
}
