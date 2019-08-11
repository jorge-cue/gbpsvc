package com.example.gbpsvc.service.getBestPrice;

import com.example.gbpsvc.adapter.dto.StoreSkuPriceDTO;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.junit.AfterClass;
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
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
public class GetBestPriceImplTests {

    // Subject Under Test
    @Autowired
    private GetBestPrice getBestPrice;

    private static final Random RANDOM = new Random();

    @ClassRule
    public static WireMockClassRule wireMockRule = new WireMockClassRule(options()
            .asynchronousResponseEnabled(true)
            .disableRequestJournal()
            .port(8085)
            .extensions(new ResponseTemplateTransformer(
                    true,
                    "generate-price",
                    (context, options) -> String.format("%8.2f", RANDOM.nextDouble() * 1_000.00)))
    );

    @BeforeClass
    public static void startUp() {
        wireMockRule.stubFor(get(urlPathMatching("/v1/store/\\d+/sku/\\d+/price"))
                .withHeader(HttpHeaders.ACCEPT, new RegexPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(ok()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withLogNormalRandomDelay(500.0, 1.0)
                        .withBody("{\"storeId\":\"{{request.path.[2]}}\", \"sku\":\"{{request.path.[4]}}\", \"price\":{{generate-price}} }")));
    }

    @AfterClass
    public static void shutDown() {

    }

    @Test(timeout = 20_000L)
    public void getBestPrice() {
        List<String> stores = IntStream.rangeClosed(1, 100).mapToObj(i -> String.format("%04d", i)).collect(Collectors.toList());

        Optional<StoreSkuPriceDTO> response = getBestPrice.getBestPrice("750123456789", stores);

        assertTrue(response.isPresent());
        StoreSkuPriceDTO storeSkuPriceDTO = response.get();
        log.info("Minimum SkuPrice: " + storeSkuPriceDTO.toString());
        assertThat(storeSkuPriceDTO.getStoreId()).isNotEmpty();
        assertThat(storeSkuPriceDTO.getSku()).isEqualTo("750123456789");
        assertThat(storeSkuPriceDTO.getPrice()).isNotNull();
    }
}
