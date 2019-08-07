package com.example.gbpsvc.adapter.store;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.springframework.http.HttpMethod.GET;

@Slf4j
@Service("storeAdapter")
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

    /*
     * This is a synchronous REST call to get a store/sku price.
     */
    public SkuPrice getPriceByStoreIdAndSku(String storeId, String sku) {
        final URI uri = UriComponentsBuilder.fromUriString(config.getEntryPoint())
                .pathSegment("v1", "store", "{store-id}", "sku", "{sku}", "price")
                .build(storeId, sku);

        HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.ACCEPT, Collections.singletonList(MediaType.APPLICATION_JSON_VALUE));

        final RequestEntity<Void> requestEntity = new RequestEntity<>(headers, GET, uri);

        ResponseEntity<SkuPrice> response;
        try {
            response = restTemplate.exchange(uri, GET, requestEntity, SkuPrice.class);
        } catch (HttpClientErrorException | ResourceAccessException ex) {
            log.error(ex.getMessage());
            throw new StoreAdapterException(ex.getMessage(), ex);
        }

        if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
            final SkuPrice skuPrice = response.getBody();
            if (skuPrice != null) {
                log.info("Received SkuPrice: " + skuPrice.toString());
                return skuPrice;
            }
        }

        String message = String.format("Unable to get price for store %s sku %s, error %s (%s)",
                storeId, sku, response.getStatusCodeValue(), response.getStatusCode().getReasonPhrase());

        log.error(message);

        throw new StoreAdapterException(message);
    }

    /*
     * The synchronous call above is launched in another thread so this thread may continue non blocked.
     * CompletableFuture is used to retrieve the response.
     * Adds an error handler, reporting the error to log and returning an SkuPrice without price and an error message.
     */
    public CompletableFuture<SkuPrice> getAsyncPriceByStoreIdAndSku(String storeId, String sku) {
        return CompletableFuture
                .supplyAsync(() -> getPriceByStoreIdAndSku(storeId, sku), executor)
                .handle((skuPrice, throwable) -> { // This is to handle the completion of the CompltableFuture
                    if (throwable != null) { // Was it completed with Error?
                        log.error(throwable.getMessage()); // Report error
                        return SkuPrice.builder() // Return special SkuPrice without price and with error message.
                                .storeId(storeId)
                                .sku(sku)
                                .price(null) // No price available
                                .error(throwable.getMessage()) // Report the error instead.
                                .build();
                    }
                    return skuPrice; // No error, return skuPrice as received.
                });
    }
}
