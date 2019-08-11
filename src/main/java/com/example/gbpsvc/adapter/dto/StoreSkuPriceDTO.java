package com.example.gbpsvc.adapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoreSkuPriceDTO {
    private String storeId;
    private String sku;
    private BigDecimal price;
    private String error;
}
