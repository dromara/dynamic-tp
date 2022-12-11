package com.dtp.example.adapter;

import com.dtp.core.spring.EnableDynamicTp;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.ImportResource;

@EnableDynamicTp
@EnableHystrix
@EnableDubbo
@ImportResource(locations = {"classpath:motan_server.xml"})
@SpringBootApplication
public class ExampleAdapterApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleAdapterApplication.class, args);
    }

}
