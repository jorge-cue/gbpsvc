package com.example.gbpsvc.adapter.store;

import java.util.concurrent.CompletableFuture;

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

    /**
     * Does the same think as above call buti using a non blocking operation, returns a CompletableFuture than can be
     * queried later to retrieve the result.
     *
     * @param storeId Store ID of store to query.
     * @param sku SKU identified of product whose price is required.
     * @return <code>CompletableFuture<SkuPrice></code> it a ticket that can be used to retrieve actual SkuPrice later,
     *         since the operation is nonblocking this program may continue with productive work while the remote call
     *         (HTTP CALL) is completed.
     */
    CompletableFuture<SkuPrice> getAsyncPriceByStoreIdAndSku(String storeId, String sku);
}
