package com.example.gbpsvc.adapter.store;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuPrice {
    private String storeId;
    private String sku;
    private BigDecimal price;
}
