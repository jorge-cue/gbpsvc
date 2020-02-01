package com.example.gbpsvc.service.getBestPrice;

import com.example.gbpsvc.adapter.dto.StoreSkuPriceDTO;
import com.example.gbpsvc.adapter.store.StoreAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Slf4j
@Service("getBestPriceV2")
public class GetBestPriceImplV2 implements GetBestPrice {

    private final StoreAdapter storeAdapter;

    public GetBestPriceImplV2(StoreAdapter storeAdapter) {
        this.storeAdapter = storeAdapter;
    }

    /**
     * Gets best price for an SKU from a list of stores.
     * Ths second version just queries all stores in a parallel stream then computes the minimum price from them if
     * @param sku
     * @param stores
     * @return
     */
    public Optional<StoreSkuPriceDTO> getBestPrice(String sku, Iterable<String> stores) {
        return StreamSupport.stream(stores.spliterator(), true)// <- Parallel stream
                .map(storeId -> getPriceByStoreIdAndSku(storeId, sku))
                .peek(storeSkuPriceDTO -> log.info("Received Store/Sku: " + storeSkuPriceDTO.toString()))
                .filter(p -> p.getError() == null)
                .min(Comparator.comparing(StoreSkuPriceDTO::getPrice));
    }

    private StoreSkuPriceDTO getPriceByStoreIdAndSku(String storeId, String sku) {
        try {
            return storeAdapter.getPriceByStoreIdAndSku(storeId, sku); // <- Synchronous call
        } catch (Throwable t) {
            return StoreSkuPriceDTO.builder().storeId(storeId).sku(sku).error(t.getMessage()).build();
        }
    }
}

