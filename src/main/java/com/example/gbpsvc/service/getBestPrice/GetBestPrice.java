package com.example.gbpsvc.service.getBestPrice;

import com.example.gbpsvc.adapter.dto.StoreSkuPriceDTO;

import java.util.Optional;

public interface GetBestPrice {

    Optional<StoreSkuPriceDTO> getBestPrice(final String sku, final Iterable<String> stores);
}
