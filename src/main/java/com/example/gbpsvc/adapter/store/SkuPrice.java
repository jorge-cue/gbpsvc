package com.example.gbpsvc.adapter.store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkuPrice {
    private String storeId;
    private String sku;
    private BigDecimal price;
    private String error;
}
