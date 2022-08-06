package com.dtp.example;

import com.dtp.core.spring.EnableDynamicTp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Redick01
 */
@EnableDynamicTp
@SpringBootApplication
public class ReactiveNacosExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReactiveNacosExampleApplication.class, args);
    }
}
