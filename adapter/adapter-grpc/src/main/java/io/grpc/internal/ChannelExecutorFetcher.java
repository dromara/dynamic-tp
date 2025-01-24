package io.grpc.internal;
import io.grpc.ManagedChannel;
import org.dromara.dynamictp.common.util.ReflectionUtil;

import java.util.concurrent.Executor;

/**
 * ChannelExecutorFetcher
 *
 * @author Assassinxc
 * @since 1.1.9.1
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
