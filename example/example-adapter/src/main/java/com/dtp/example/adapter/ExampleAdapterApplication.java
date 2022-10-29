package com.dtp.example.adapter;

import com.dtp.core.spring.EnableDynamicTp;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;

@EnableDynamicTp
@EnableHystrix
@EnableDubbo
@SpringBootApplication
public class ExampleAdapterApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleAdapterApplication.class, args);
    }

}
