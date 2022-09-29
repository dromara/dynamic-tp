package com.dtp.example;

import com.dtp.core.spring.EnableDynamicTp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author ljinfeng
 */
@EnableDynamicTp
@SpringBootApplication
public class NacosNotifyEmailExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(NacosNotifyEmailExampleApplication.class, args);
    }
}
