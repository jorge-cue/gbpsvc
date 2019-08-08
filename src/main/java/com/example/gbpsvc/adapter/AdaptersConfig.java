package com.example.gbpsvc.adapter;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class AdaptersConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofMillis(10L))
                .setReadTimeout(Duration.ofMillis(3_000L))
                .build();
    }

    @Bean
    public Executor executor() {
        return Executors.newCachedThreadPool();
    }
}
