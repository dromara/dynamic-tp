package com.dtp.example;

import com.dtp.core.spring.EnableDynamicTp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * @author fabian
 */
@EnableDynamicTp
@SpringBootApplication
@ImportResource(locations = {"classpath:motan_server.xml"})
public class MotanExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(MotanExampleApplication.class, args);
    }
}
