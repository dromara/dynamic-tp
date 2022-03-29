package com.dtp.example;

import com.dtp.core.spring.EnableDynamicTp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Redick01
 */
@EnableDynamicTp
@SpringBootApplication
public class CloudZookeeperExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudZookeeperExampleApplication.class, args);
    }
}
