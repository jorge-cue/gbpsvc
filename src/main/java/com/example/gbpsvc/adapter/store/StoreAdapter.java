package com.example.gbpsvc.adapter.store;

public interface StoreAdapter {
    /**
     * Get price of <b>sku</b> from store <b>storeId</b>. Using REST API, therefore it  is a normal HTTP blocking until
     * the response is get back.
     *
     * @param storeId Store ID of store to query.
     * @param sku     SKU identifier of product whose price is required.
     * @return SkuPrice.
     */
    SkuPrice getPriceByStoreIdAndSku(String storeId, String sku);
}
