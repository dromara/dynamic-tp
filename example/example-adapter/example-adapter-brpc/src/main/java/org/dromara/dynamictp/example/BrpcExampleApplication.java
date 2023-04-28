package org.dromara.dynamictp.example;

import com.baidu.cloud.starlight.springcloud.server.annotation.StarlightScan;
import org.dromara.dynamictp.core.spring.EnableDynamicTp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author fabian4
 */
@StarlightScan
@EnableDynamicTp
@SpringBootApplication
public class BrpcExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(BrpcExampleApplication.class, args);
    }
}
