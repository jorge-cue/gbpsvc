package com.example.gbpsvc.adapter.store;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

@Data
@ConfigurationProperties(prefix = "store")
@Component
public class StoreAdapterConfig {

    @NotNull
    private String entryPoint = "http://localhost:8085";
}
