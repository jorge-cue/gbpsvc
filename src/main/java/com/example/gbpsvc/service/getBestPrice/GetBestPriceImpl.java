package com.example.gbpsvc.service.getBestPrice;

import com.example.gbpsvc.adapter.store.SkuPrice;
import com.example.gbpsvc.adapter.store.StoreAdapter;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service("getBestPrice")
public class GetBestPriceImpl implements GetBestPrice {

    private final StoreAdapter storeAdapter;

    public GetBestPriceImpl(StoreAdapter storeAdapter) {
        this.storeAdapter = storeAdapter;
    }

    public Optional<SkuPrice> getBestPrice(String sku, Iterable<String> stores) {
        List<CompletableFuture<SkuPrice>> futures = StreamSupport.stream(stores.spliterator(), false)
                .map(storeId -> storeAdapter.getAsyncPriceByStoreIdAndSku(storeId, sku))
                .collect(Collectors.toList());
        return futures.stream()
                .map(f -> {
                    try {
                        return f.get(30, TimeUnit.SECONDS);
                    } catch(Exception x) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .min(Comparator.comparing(SkuPrice::getPrice));
    }
}
