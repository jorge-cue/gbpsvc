package com.example.gbpsvc.adapter.store;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

@Component
@PropertySource("application")
@ConfigurationProperties(prefix = "store")
@Data
public class StoreAdapterConfig {

    @NotNull
    private String entryPoint = "http://localhost:8085";
}
