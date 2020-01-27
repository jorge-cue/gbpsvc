package com.example.gbpsvc.service.getBestPrice;

import com.example.gbpsvc.adapter.dto.StoreSkuPriceDTO;
import com.example.gbpsvc.adapter.store.StoreAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Slf4j
@Service("getBestPriceV1")
public class GetBestPriceImplV1 implements GetBestPrice {

    private final StoreAdapter storeAdapter;

    public GetBestPriceImplV1(StoreAdapter storeAdapter) {
        this.storeAdapter = storeAdapter;
    }

    public Optional<StoreSkuPriceDTO> getBestPrice(String sku, Iterable<String> stores) {
        return StreamSupport.stream(stores.spliterator(), false).map(storeId -> {
            try {
                return storeAdapter.getPriceByStoreIdAndSku(storeId, sku);
            } catch (Throwable t) {
                return StoreSkuPriceDTO.builder().storeId(storeId).sku(sku).error(t.getMessage()).build();
            }
        })
                .peek(storeSkuPriceDTO -> log.info("Received Store/Sku: " + storeSkuPriceDTO.toString()))
                .filter(p -> p.getError() == null)
                .min(Comparator.comparing(StoreSkuPriceDTO::getPrice));
    }
}

