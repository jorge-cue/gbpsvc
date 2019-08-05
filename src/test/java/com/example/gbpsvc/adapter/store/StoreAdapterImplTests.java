package com.example.gbpsvc.adapter.store;

import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
public class StoreAdapterImplTests {

    private static final Random RANDOM = new Random();
    @ClassRule
    public static WireMockClassRule wireMockRule = new WireMockClassRule(options()
            .port(8085)
            .extensions(new ResponseTemplateTransformer(
                    true,
                    "generate-price",
                    (context, options) -> String.format("%8.2f", RANDOM.nextDouble() * 1_000.00)))
    );
    @Autowired
    public StoreAdapter storeAdapter;

    @BeforeClass
    public static void startUp() throws Exception {
        wireMockRule.stubFor(any(anyUrl())
                .willReturn(notFound()));
        wireMockRule.stubFor(get(urlPathMatching("/v1/store/0001/sku/\\d+/price"))
                .withHeader(HttpHeaders.ACCEPT, new RegexPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(ok()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"price\":{{generate-price}}, \"storeId\":\"{{request.path.[2]}}\", \"sku\":\"{{request.path.[4]}}\"}")));
        wireMockRule.stubFor(get(urlPathMatching("/v1/store/5000/sku/\\d+/price"))
                .withHeader(HttpHeaders.ACCEPT, new RegexPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(ok()
                        .withFixedDelay(1_000) // To cause RestTemplate timeout
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"price\":{{generate-price}}, \"storeId\":\"{{request.path.[2]}}\", \"sku\":\"{{request.path.[4]}}\"}")));
    }

    @Test
    public void getPriceByStoreIdAndSku_Success() {
        SkuPrice skuPrice = storeAdapter.getPriceByStoreIdAndSku("0001", "1234567890");
        assertThat(skuPrice).isNotNull();
    }

    @Test(expected = StoreAdapterException.class)
    public void getPriceByStoreIdAndSku_Timeout() {
        storeAdapter.getPriceByStoreIdAndSku("5000", "1234567890");
    }

    @Test
    public void getAsyncPriceByStoreIdAndSku_Success() {
        CompletableFuture<SkuPrice> future = storeAdapter.getAsyncPriceByStoreIdAndSku("0001", "1234567890");
        SkuPrice skuPrice = future.join();
        assertThat(skuPrice).isNotNull();
        assertThat(skuPrice.getPrice()).isGreaterThanOrEqualTo(BigDecimal.ZERO).isLessThanOrEqualTo(BigDecimal.valueOf(1_000.0));
        assertThat(skuPrice.getStoreId()).isEqualTo("0001");
        assertThat(skuPrice.getSku()).isEqualTo("1234567890");
    }

    @Test
    public void getAsyncPriceByStoreIdAndSku_Timeout() {
        CompletableFuture<SkuPrice> future = storeAdapter.getAsyncPriceByStoreIdAndSku("5000", "1234567890");
        SkuPrice skuPrice = future.join();
        assertThat(skuPrice).isNotNull();
        assertThat(skuPrice.getPrice()).isGreaterThanOrEqualTo(BigDecimal.valueOf(Long.MAX_VALUE, 2));
        assertThat(skuPrice.getStoreId()).isEqualTo("5000");
        assertThat(skuPrice.getSku()).isEqualTo("1234567890");
    }
}