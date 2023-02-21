package com.dtp.example;

import com.dtp.core.spring.EnableDynamicTp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author fabian4
 */
@EnableDynamicTp
@SpringBootApplication
public class GrpcExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(GrpcExampleApplication.class, args);
    }
}
