package org.dromara.dynamictp.example;

import org.dromara.dynamictp.core.spring.EnableDynamicTp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author windsearcher
 */
@EnableDynamicTp
@SpringBootApplication
public class HuaweiCloudExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(HuaweiCloudExampleApplication.class, args);
    }
}
