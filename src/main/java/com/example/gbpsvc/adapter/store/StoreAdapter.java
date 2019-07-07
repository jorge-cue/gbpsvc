package com.example.gbpsvc.adapter.store;

public interface StoreAdapter {

    SkuPrice getPriceByStoreIdAndSku(String storeId, String sku);
}
