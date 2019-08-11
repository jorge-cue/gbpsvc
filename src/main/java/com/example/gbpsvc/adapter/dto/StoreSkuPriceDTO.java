package com.example.gbpsvc.adapter.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class StoreSkuPriceDTO {
    private final String storeId;
    private final String sku;
    private final BigDecimal price;
    private final String error;
}
