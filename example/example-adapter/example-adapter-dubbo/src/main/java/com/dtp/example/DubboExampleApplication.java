package com.dtp.example;

import com.dtp.core.spring.EnableDynamicTp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author fabian
 */
@EnableDynamicTp
@SpringBootApplication
public class DubboExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(DubboExampleApplication.class, args);
    }
}
