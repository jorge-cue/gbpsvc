package com.example.gbpsvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class GbpsvcApplication {

    public static void main(String[] args) {
        SpringApplication.run(GbpsvcApplication.class, args);
    }
}
