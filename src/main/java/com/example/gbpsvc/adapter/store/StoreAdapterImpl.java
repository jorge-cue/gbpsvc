package com.example.gbpsvc.adapter.store;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

@Service("storeAdapter")
public class StoreAdapterImpl implements StoreAdapter {

    private final RestTemplate restTemplate;

    private final StoreAdapterConfig config;

    public StoreAdapterImpl(RestTemplate restTemplate, StoreAdapterConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    public SkuPrice getPriceByStoreIdAndSku(String storeId, String sku) {
        final URI url;
        try {
            url = new URI(new StringBuilder()
                    .append(config.getUrl())
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

        RequestEntity requestEntity = new RequestEntity(headers, HttpMethod.GET, url);

        ResponseEntity<SkuPrice> response = restTemplate.exchange(requestEntity, SkuPrice.class);

        if (response.getStatusCode().is2xxSuccessful())
            return response.getBody();

        String message = String.format("Unable to get price for store %s sku %s, error %s (%s)",
                storeId, sku, response.getStatusCodeValue(), response.getStatusCode().getReasonPhrase());

        throw new StoreAdapterException(message);
    }
}
