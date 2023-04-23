package org.dromara.dynamictp.example;

import org.dromara.dynamictp.core.spring.EnableDynamicTp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author fabian4
 */
@EnableDynamicTp
@SpringBootApplication
public class RabbitMqExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(RabbitMqExampleApplication.class, args);
    }
}
