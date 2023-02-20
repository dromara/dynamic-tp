package com.dtp.example;

import com.dtp.core.spring.EnableDynamicTp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author fabian
 */
@EnableDynamicTp
@SpringBootApplication
public class Okhttp3ExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(Okhttp3ExampleApplication.class, args);
    }
}