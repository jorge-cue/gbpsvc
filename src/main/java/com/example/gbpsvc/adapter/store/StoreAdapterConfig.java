package com.example.gbpsvc.adapter.store;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("application")
@ConfigurationProperties(prefix = "store")
@Data
public class StoreAdapterConfig {

    private String url;
}
