package com.example.gbpsvc.service.getBestPrice;

import com.example.gbpsvc.adapter.store.SkuPrice;

import java.util.Optional;

public interface GetBestPrice {

    Optional<SkuPrice> getBestPrice(final String sku, final Iterable<String> stores);
}
