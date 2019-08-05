package com.example.gbpsvc.service.getBestPrice;

import com.example.gbpsvc.adapter.store.SkuPrice;
import com.example.gbpsvc.adapter.store.StoreAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.StreamSupport;

@Slf4j
@Service("getBestPrice")
public class GetBestPriceImpl implements GetBestPrice {

    private final StoreAdapter storeAdapter;

    public GetBestPriceImpl(StoreAdapter storeAdapter) {
        this.storeAdapter = storeAdapter;
    }

    public Optional<SkuPrice> getBestPrice(String sku, Iterable<String> stores) {
        // Launch Asynchronous requests to stores, collect al CompletableFutures to claim responses after.
        @SuppressWarnings("unchecked")
        CompletableFuture<SkuPrice>[] futures = StreamSupport.stream(stores.spliterator(), true)
                .map(storeId -> storeAdapter.getAsyncPriceByStoreIdAndSku(storeId, sku))
                .toArray(size -> new CompletableFuture[size]);

        CompletableFuture.allOf(futures).join(); // Wait for all futures to complete

        // Find smallest price.
        return Arrays.stream(futures)
                .map(CompletableFuture::join)
                .filter(p -> p.getError() == null)
                .min(Comparator.comparing(SkuPrice::getPrice));
    }
}
