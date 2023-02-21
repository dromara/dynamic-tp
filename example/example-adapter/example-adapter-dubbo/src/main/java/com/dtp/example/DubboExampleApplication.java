package com.dtp.example;

import com.dtp.core.spring.EnableDynamicTp;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author fabian4
 */
@EnableDubbo
@EnableDynamicTp
@SpringBootApplication
public class DubboExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(DubboExampleApplication.class, args);
    }
}
