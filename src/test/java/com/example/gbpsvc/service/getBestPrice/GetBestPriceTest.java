package com.example.gbpsvc.service.getBestPrice;

import com.example.gbpsvc.adapter.store.SkuPrice;
import com.example.gbpsvc.adapter.store.StoreAdapter;
import com.example.gbpsvc.adapter.store.StoreAdapterConfig;
import com.example.gbpsvc.adapter.store.StoreAdapterImpl;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import lombok.extern.java.Log;
import org.apache.http.HttpHeaders;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

@Log
public class GetBestPriceTest {

    private static final Random RANDOM = new Random();

    @ClassRule
    public static WireMockClassRule wireMockRule = new WireMockClassRule(options()
            .port(8085)
            .extensions(new ResponseTemplateTransformer(
                    true,
                    "generate-price",
                    (context, options) -> String.format("%8.2f", RANDOM.nextDouble() * 1_000.00)))
    );

    private RestTemplate restTemplate = new RestTemplateBuilder()
            .setConnectTimeout(Duration.ofMillis(250L))
            .setReadTimeout(Duration.ofMinutes(1L))
            .build();

    private StoreAdapterConfig storeAdapterConfig = new StoreAdapterConfig();

    private Executor executor = Executors.newCachedThreadPool();

    private StoreAdapter storeAdapter = new StoreAdapterImpl(restTemplate, storeAdapterConfig, executor);

    // Subject Under Test
    private GetBestPrice getBestPrice = new GetBestPriceImpl(storeAdapter);

    @BeforeClass
    public static void startUp() {
        wireMockRule.resetAll();
        wireMockRule.stubFor(any(anyUrl())
                .willReturn(notFound()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"status\":404,\"message\":\"Not found.\",\"path\":\"{{request.path}}\"}")));
        wireMockRule.stubFor(get(urlPathMatching("/v1/store/\\d+/sku/\\d+/price"))
                .withHeader(HttpHeaders.ACCEPT, new RegexPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(ok()
                        .withFixedDelay(1_000)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"price\":{{generate-price}}, \"storeId\":\"{{request.path.[2]}}\", \"sku\":\"{{request.path.[4]}}\"}")));
    }

    @Before
    public void setUp() {
        storeAdapterConfig.setEntryPoint("http://localhost:8085");
    }

    @Test(timeout = 30_000L)
    public void getBestPrice() {
        List<String> stores = IntStream.rangeClosed(1, 100).mapToObj(i -> String.format("%04d", i)).collect(Collectors.toList());

        Optional<SkuPrice> response = getBestPrice.getBestPrice("750123456789", stores);

        assertTrue(response.isPresent());
        SkuPrice skuPrice = response.get();
        log.info("Minimum SkuPrice: " + skuPrice.toString());
        assertThat(skuPrice.getStoreId()).isNotEmpty();
        assertThat(skuPrice.getSku()).isEqualTo("750123456789");
        assertThat(skuPrice.getPrice()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }
}