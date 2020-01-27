package com.example.gbpsvc.adapter.store;

import com.example.gbpsvc.adapter.AdapterException;
import com.example.gbpsvc.adapter.dto.StoreSkuPriceDTO;
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

import java.util.List;
import java.util.Random;
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

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
public class StoreAdapterImplTests {

    private static final Random RANDOM = new Random();
    public static final String SKU = "1234567890";

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
        wireMockRule.stubFor(get(urlPathMatching("/v1/store/\\d+/sku/\\d+/price"))
                .withHeader(HttpHeaders.ACCEPT, new RegexPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(ok()
                        .withLogNormalRandomDelay(500.0, 1.0)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"price\":{{generate-price}}, \"storeId\":\"{{request.path.[2]}}\", \"sku\":\"{{request.path.[4]}}\"}")));
        wireMockRule.stubFor(get(urlPathMatching("/v1/store/1000/sku/\\d+/price"))
                .withHeader(HttpHeaders.ACCEPT, new RegexPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(ok()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"price\":{{generate-price}}, \"storeId\":\"{{request.path.[2]}}\", \"sku\":\"{{request.path.[4]}}\"}")));
        wireMockRule.stubFor(get(urlPathMatching("/v1/store/2000/sku/\\d+/price"))
                .withHeader(HttpHeaders.ACCEPT, new RegexPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(notFound()));
        wireMockRule.stubFor(get(urlPathMatching("/v1/store/3000/sku/\\d+/price"))
                .withHeader(HttpHeaders.ACCEPT, new RegexPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(ok()
                        .withFixedDelay(5_000) // To cause RestTemplate timeout
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"price\":{{generate-price}}, \"storeId\":\"{{request.path.[2]}}\", \"sku\":\"{{request.path.[4]}}\"}")));
    }

    @Test
    public void getPriceByStoreIdAndSku_Success() {
        StoreSkuPriceDTO storeSkuPriceDTO = storeAdapter.getPriceByStoreIdAndSku("1000", SKU);
        assertThat(storeSkuPriceDTO).isNotNull();
    }

    @Test(expected = AdapterException.class)
    public void getPriceByStoreIdAndSku_NotFound() {
        storeAdapter.getPriceByStoreIdAndSku("2000", SKU);
    }

    @Test(expected = AdapterException.class)
    public void getPriceByStoreIdAndSku_Timeout() {
        storeAdapter.getPriceByStoreIdAndSku("3000", SKU);
    }

    @Test
    public void getPriceByStoreIdAndSku_100Stores() {
        List<String> stores = IntStream.rangeClosed(1, 100).mapToObj(i -> String.format("%04d", i)).collect(Collectors.toList());
        List<StoreSkuPriceDTO> storeSkuPriceDTOList = stores.stream().map(storeId -> {
            try {
                return storeAdapter.getPriceByStoreIdAndSku(storeId, SKU);
            } catch (Exception e) {
                return StoreSkuPriceDTO.builder().storeId(storeId).sku(SKU).error(e.getMessage()).build();
            }
        })
                .collect(Collectors.toList());
    }
}