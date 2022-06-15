package com.dtp.example;

import com.dtp.core.spring.EnableDynamicTp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;

/**
 * @author Redick01
 */
@EnableDynamicTp
@SpringBootApplication
@EnableHystrix
public class NacosCloudExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(NacosCloudExampleApplication.class, args);
    }
}
