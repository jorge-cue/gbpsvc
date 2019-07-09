package com.example.gbpsvc.adapter.store;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Component("storeAdapter")
public class StoreAdapterImpl implements StoreAdapter {

    private final RestTemplateBuilder restTemplateBuilder;

    private final RestTemplate restTemplate;

    private final StoreAdapterConfig config;

    private final Executor executor;

    public StoreAdapterImpl(RestTemplateBuilder restTemplateBuilder, StoreAdapterConfig config, Executor executor) {
        this.restTemplateBuilder = restTemplateBuilder;
        this.restTemplate = restTemplateBuilder.build();
        this.config = config;
        this.executor = executor;
    }

    public SkuPrice getPriceByStoreIdAndSku(String storeId, String sku) {
        final URI url;
        try {
            url = new URI(new StringBuilder()
                    .append(config.getEntryPoint())
                    .append("/store/")
                    .append(storeId)
                    .append("/sku/")
                    .append(sku)
                    .toString());
        } catch (URISyntaxException e) {
            throw new StoreAdapterException("Unable to build URI", e);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.ACCEPT, Collections.singletonList(MediaType.APPLICATION_JSON_VALUE));

        final RequestEntity requestEntity = new RequestEntity(headers, HttpMethod.GET, url);

        ResponseEntity<SkuPrice> response = restTemplate.exchange(requestEntity, SkuPrice.class);

        if (response.getStatusCode().is2xxSuccessful())
            return response.getBody();

        String message = String.format("Unable to get price for store %s sku %s, error %s (%s)",
                storeId, sku, response.getStatusCodeValue(), response.getStatusCode().getReasonPhrase());

        throw new StoreAdapterException(message);
    }

    @Override
    public CompletableFuture<SkuPrice> getAsyncPriceByStoreIdAndSku(String storeId, String sku) {
        return CompletableFuture.supplyAsync( () -> getPriceByStoreIdAndSku(storeId, sku), executor);
    }
}
