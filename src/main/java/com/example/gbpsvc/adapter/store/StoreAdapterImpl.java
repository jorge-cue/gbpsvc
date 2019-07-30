package com.example.gbpsvc.adapter.store;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.springframework.http.HttpMethod.GET;

@Component("storeAdapter")
@Log
public class StoreAdapterImpl implements StoreAdapter {

    private final RestTemplate restTemplate;

    private final StoreAdapterConfig config;

    private final Executor executor;

    @Autowired
    public StoreAdapterImpl(
            RestTemplate restTemplate,
            StoreAdapterConfig config,
            Executor executor) {
        this.restTemplate = restTemplate;
        this.config = config;
        this.executor = executor;
    }

    public SkuPrice getPriceByStoreIdAndSku(String storeId, String sku) {
        final URI uri = UriComponentsBuilder.fromUriString(config.getEntryPoint())
                .pathSegment("v1", "store", "{store-id}", "sku", "{sku}", "price")
                .build(storeId, sku);

        HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.ACCEPT, Collections.singletonList(MediaType.APPLICATION_JSON_VALUE));

        final RequestEntity<Void> requestEntity = new RequestEntity<>(headers, GET, uri);

        ResponseEntity<SkuPrice> response = restTemplate.exchange(uri, GET, requestEntity, SkuPrice.class);

        if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
            SkuPrice skuPrice = response.getBody();
            if (skuPrice != null) {
                log.info("Received SkuPrice: " + skuPrice.toString());
                return skuPrice;
            }
        }

        String message = String.format("Unable to get price for store %s sku %s, error %s (%s)",
                storeId, sku, response.getStatusCodeValue(), response.getStatusCode().getReasonPhrase());

        throw new StoreAdapterException(message);
    }

    @Override
    public CompletableFuture<SkuPrice> getAsyncPriceByStoreIdAndSku(String storeId, String sku) {
        return CompletableFuture.supplyAsync(() -> getPriceByStoreIdAndSku(storeId, sku), executor);
    }
}
