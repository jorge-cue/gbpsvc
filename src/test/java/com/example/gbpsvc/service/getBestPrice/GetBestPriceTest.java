package com.example.gbpsvc.service.getBestPrice;

import com.example.gbpsvc.adapter.store.SkuPrice;
import com.example.gbpsvc.adapter.store.StoreAdapter;
import com.example.gbpsvc.adapter.store.StoreAdapterConfig;
import com.example.gbpsvc.adapter.store.StoreAdapterImpl;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import lombok.extern.java.Log;
import org.apache.http.HttpHeaders;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

@Log
@ActiveProfiles("test")
public class GetBestPriceTest {

    private Helper<Double> generatePrice = new Helper<Double>() {

        private Random random = new Random();

        public Object apply(Double context, Options options) throws IOException {
            return random.nextDouble() * context;
        }
    };

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options()
            .port(8085)
            .extensions(new ResponseTemplateTransformer(true, "generate-price", generatePrice))
    );

    private RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder()
            .setConnectTimeout(Duration.ofMillis(20L))
            .setReadTimeout(Duration.ofMinutes(1L));

    private StoreAdapterConfig storeAdapterConfig = new StoreAdapterConfig();

    private Executor executor = Executors.newCachedThreadPool();

    private StoreAdapter storeAdapter = new StoreAdapterImpl(restTemplateBuilder, storeAdapterConfig, executor);

    // Subject Under Test
    private GetBestPrice getBestPrice = new GetBestPriceImpl(storeAdapter);

    @Before
    public void setUp() throws Exception {
        wireMockRule.stubFor(get(urlPathMatching("/v1/getPrice/store/\\d+/sku/\\d+"))
                .withHeader(HttpHeaders.ACCEPT, new RegexPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(ok()
                        .withLogNormalRandomDelay(250.0, 25.0)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"price\":{{generate-price 1000.00}}, \"storeId\":\"{{request.path.[3]}}\", \"sku\":\"{{request.path.[5]}}\"}")));
    }

    @Test
    public void getBestPrice() {
        List<String> stores = IntStream.rangeClosed(1, 1).mapToObj(i -> String.format("%04d", i)).collect(Collectors.toList());

        Optional<SkuPrice> response = getBestPrice.getBestPrice("750123456789", stores);

        assertTrue(response.isPresent());
        SkuPrice skuPrice = response.get();
        log.info("SkuPrice: " + skuPrice.toString());
        assertThat(skuPrice.getStoreId()).isNotEmpty();
        assertThat(skuPrice.getSku()).isEqualTo("750123456789");
        assertThat(skuPrice.getPrice()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }
}