package io.grpc.internal;
import io.grpc.ManagedChannel;
import org.dromara.dynamictp.common.util.ReflectionUtil;

import java.util.concurrent.Executor;

/**
 * @author Assassinxc
 * @title: ChannelExecutorFetcher
 * @projectName dynamic-tp
 * @description:
 * @date 2025/1/24 17:31
 */
public class ChannelExecutorFetcher {
    public static Executor getManagedChannelImplExecutor(ManagedChannel channel) {
        if(channel instanceof ManagedChannelImpl) {
            return (Executor) ReflectionUtil.getFieldValue(ManagedChannelImpl.class, "executor", channel);
        }else{
            return null;
        }
    }
}
