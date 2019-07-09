package com.example.gbpsvc.adapter.store;

import java.util.concurrent.CompletableFuture;

public interface StoreAdapter {

    SkuPrice getPriceByStoreIdAndSku(String storeId, String sku);


    CompletableFuture<SkuPrice> getAsyncPriceByStoreIdAndSku(String storeId, String sku);
}
