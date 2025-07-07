package org.dromara.dynamictp.sdk;

import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.LifeCycleException;
import com.alipay.remoting.RemotingContext;
import com.alipay.remoting.rpc.protocol.UserProcessor;

import java.util.concurrent.Executor;

public class AdminClientUserProcessor implements UserProcessor<AdminRequestBody> {

    @Override
    public BizContext preHandleRequest(RemotingContext remotingCtx, AdminRequestBody request) {
        return null;
    }

    @Override
    public void handleRequest(BizContext bizCtx, AsyncContext asyncCtx, AdminRequestBody request) {

    }

    @Override
    public Object handleRequest(BizContext bizCtx, AdminRequestBody request) throws Exception {
        return null;
    }

    @Override
    public String interest() {
        return "";
    }

    @Override
    public Executor getExecutor() {
        return null;
    }

    @Override
    public ClassLoader getBizClassLoader() {
        return null;
    }

    @Override
    public boolean processInIOThread() {
        return false;
    }

    @Override
    public boolean timeoutDiscard() {
        return false;
    }

    @Override
    public void setExecutorSelector(ExecutorSelector executorSelector) {

    }

    @Override
    public ExecutorSelector getExecutorSelector() {
        return null;
    }

    @Override
    public void startup() throws LifeCycleException {

    }

    @Override
    public void shutdown() throws LifeCycleException {

    }

    @Override
    public boolean isStarted() {
        return false;
    }
}
