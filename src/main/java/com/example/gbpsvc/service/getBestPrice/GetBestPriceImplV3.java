package com.example.gbpsvc.service.getBestPrice;

import com.example.gbpsvc.adapter.dto.StoreSkuPriceDTO;
import com.example.gbpsvc.adapter.store.StoreAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service("getBestPriceV3")
public class GetBestPriceImplV3 implements GetBestPrice {

    private final StoreAdapter storeAdapter;

    public GetBestPriceImplV3(StoreAdapter storeAdapter, Executor executor) {
        this.storeAdapter = storeAdapter;
    }

    public Optional<StoreSkuPriceDTO> getBestPrice(String sku, Iterable<String> stores) {
        // Launch Asynchronous requests to stores, collect all CompletableFutures to claim responses after.
        @SuppressWarnings("unchecked")
        CompletableFuture<StoreSkuPriceDTO>[] futures = StreamSupport.stream(stores.spliterator(), true)
                .map(storeId -> CompletableFuture.supplyAsync(() -> storeAdapter.getPriceByStoreIdAndSku(storeId, sku))
                        /*
                         * Handles completion of CompletableFuture, when completed skuPrice has a value an throwable is null
                         * and vice versa when completedExceptionally skuPrice is null and throwable is the exception used to
                         * complete the future.
                         */
                        .handle((skuPrice, throwable) -> {
                                    if (throwable != null) {
                                        log.error(throwable.getMessage(), throwable);
                                        return StoreSkuPriceDTO.builder().storeId(storeId).sku(sku).error(throwable.getMessage()).build();
                                    }
                                    return skuPrice;
                                }
                        ))
                .toArray(CompletableFuture[]::new);
        List<StoreSkuPriceDTO> results = Arrays.stream(futures).map(CompletableFuture::join)
                .sorted(Comparator.comparing(StoreSkuPriceDTO::getStoreId))
                .peek(storeSkuPriceDTO -> log.info("Received Store/Sku: " + storeSkuPriceDTO.toString()))
                .collect(Collectors.toList());

        log.info("Number of successfully received prices: " + results.stream().filter(s -> s.getError() == null).count());
        log.info("Number of error on received prices: " + results.stream().filter(s -> s.getError() != null).count());

        return results.stream().parallel()
                .filter(p -> p.getError() == null)
                .min(Comparator.comparing(StoreSkuPriceDTO::getPrice));
    }
}
