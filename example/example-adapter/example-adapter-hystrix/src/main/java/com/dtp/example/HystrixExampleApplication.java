package com.dtp.example;

import com.dtp.core.spring.EnableDynamicTp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;

/**
 * @author fabian
 */
@EnableHystrix
@EnableDynamicTp
@SpringBootApplication
public class HystrixExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(HystrixExampleApplication.class, args);
    }
}
