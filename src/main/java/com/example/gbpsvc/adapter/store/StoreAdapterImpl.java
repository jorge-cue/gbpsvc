package com.example.gbpsvc.adapter.store;

import com.example.gbpsvc.adapter.AdapterException;
import com.example.gbpsvc.adapter.dto.StoreSkuPriceDTO;
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

import static org.springframework.http.HttpMethod.GET;

@Slf4j
@Service("storeAdapter")
public class StoreAdapterImpl implements StoreAdapter {

    private final RestTemplate restTemplate;

    private final StoreAdapterConfig config;

    @Autowired
    public StoreAdapterImpl(RestTemplate restTemplate, StoreAdapterConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    /*
     * This is a synchronous REST call to get a store/sku price.
     */
    public StoreSkuPriceDTO getPriceByStoreIdAndSku(String storeId, String sku) {
        final URI uri = UriComponentsBuilder.fromUriString(config.getEntryPoint())
                .pathSegment("v1", "store", "{store-id}", "sku", "{sku}", "price")
                .build(storeId, sku);

        HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.ACCEPT, Collections.singletonList(MediaType.APPLICATION_JSON_VALUE));

        final RequestEntity<Void> requestEntity = new RequestEntity<>(headers, GET, uri);

        ResponseEntity<StoreSkuPriceDTO> response;
        try {
            response = restTemplate.exchange(uri, GET, requestEntity, StoreSkuPriceDTO.class);
        } catch (HttpClientErrorException | ResourceAccessException ex) {
            log.error(ex.getMessage());
            throw new AdapterException(ex.getMessage(), ex);
        }

        if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
            final StoreSkuPriceDTO storeSkuPriceDTO = response.getBody();
            if (storeSkuPriceDTO != null) {
                log.info("Received SkuPrice: " + storeSkuPriceDTO.toString());
                return storeSkuPriceDTO;
            }
        }

        String message = String.format("Unable to get price for store %s sku %s, error %s (%s)",
                storeId, sku, response.getStatusCodeValue(), response.getStatusCode().getReasonPhrase());

        log.error(message);

        throw new AdapterException(message);
    }
}
