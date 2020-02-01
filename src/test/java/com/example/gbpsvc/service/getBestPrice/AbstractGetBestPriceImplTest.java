package com.example.gbpsvc.service.getBestPrice;

import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.springframework.http.MediaType;

import java.util.Random;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@Slf4j
public class AbstractGetBestPriceImplTest {

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
                        .withFixedDelay(500)
                        .withBody("{\"storeId\":\"{{request.path.[2]}}\", \"sku\":\"{{request.path.[4]}}\", \"price\":{{generate-price}} }")));
    }
}
