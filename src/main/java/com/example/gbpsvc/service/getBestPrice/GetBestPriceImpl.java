package com.example.gbpsvc.service.getBestPrice;

import com.example.gbpsvc.adapter.store.SkuPrice;
import com.example.gbpsvc.adapter.store.StoreAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
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
        List<CompletableFuture<SkuPrice>> futures = StreamSupport.stream(stores.spliterator(), false)
                .map(storeId -> storeAdapter.getAsyncPriceByStoreIdAndSku(storeId, sku))
                .collect(Collectors.toList());

        // Collect request responses.
        List<SkuPrice> prices = futures.parallelStream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        // Stream collected prices selecting lower price at each step.
        return prices.stream().reduce((one, two) -> one.getPrice().compareTo(two.getPrice()) < 0 ? one : two);
    }
}
