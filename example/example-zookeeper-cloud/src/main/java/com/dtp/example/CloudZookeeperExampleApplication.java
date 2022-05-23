package com.dtp.example;

import com.dtp.core.spring.EnableDynamicTp;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Redick01
 */
@EnableDynamicTp
@SpringBootApplication
@EnableDubbo
public class CloudZookeeperExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudZookeeperExampleApplication.class, args);
    }
}
