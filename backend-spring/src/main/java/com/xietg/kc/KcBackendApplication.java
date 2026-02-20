package com.xietg.kc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class KcBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(KcBackendApplication.class, args);
    }
}
