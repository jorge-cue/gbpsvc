package com.example.gbpsvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableConfigurationProperties
public class GbpsvcApplication {

    public static void main(String[] args) {
        SpringApplication.run(GbpsvcApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder().build();
    }
}
