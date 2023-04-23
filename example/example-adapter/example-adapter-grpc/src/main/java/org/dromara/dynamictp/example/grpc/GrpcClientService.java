package org.dromara.dynamictp.example.grpc;

import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

/**
 * GrpcClientService related
 *
 * @author yanhom
 */
@Service
@Slf4j
public class GrpcClientService {

    @GrpcClient("cloud-grpc-server")
    private SimpleGrpc.SimpleBlockingStub simpleStub;

    public String sendMessage(final String name) {
        try {
            final HelloReply response = this.simpleStub.sayHello(HelloRequest.newBuilder().setName(name).build());
            return response.getMessage();
        } catch (final StatusRuntimeException e) {
            log.error("Request failed", e);
            return "FAILED with " + e.getStatus().getCode();
        }
    }

}
