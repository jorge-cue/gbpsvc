package com.example.gbpsvc.service.getBestPrice;

import com.example.gbpsvc.adapter.dto.StoreSkuPriceDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
public class GetBestPriceImplV2Test extends AbstractGetBestPriceImplTest {
    // Subject Under Test
    @Autowired
    @Qualifier("getBestPriceV2")
    private GetBestPrice getBestPrice;

    @Test
    public void getBestPriceV1_1_Stores() {
        runTestBestPrice(1);
    }

    @Test
    public void getBestPriceV1_10_Stores() {
        runTestBestPrice(10);
    }

    @Test
    public void getBestPriceV1_20_Stores() {
        runTestBestPrice(20);
    }

    @Test
    public void getBestPriceV1_30_Stores() {
        runTestBestPrice(30);
    }

    @Test
    public void getBestPriceV1_40_Stores() {
        runTestBestPrice(40);
    }

    @Test
    public void getBestPriceV1_50_Stores() {
        runTestBestPrice(50);
    }

    @Test
    public void getBestPriceV1_100_Stores() {
        runTestBestPrice(100);
    }

    @Test
    public void getBestPriceV1_200_Stores() {
        runTestBestPrice(200);
    }

    @Test
    public void getBestPriceV1_300_Stores() {
        runTestBestPrice(300);
    }

    @Test
    public void getBestPriceV1_400_Stores() {
        runTestBestPrice(400);
    }

    @Test
    public void getBestPriceV1_500_Stores() {
        runTestBestPrice(500);
    }

    @Test
    public void getBestPriceV1_1000_Stores() {
        runTestBestPrice(1000);
    }

    private void runTestBestPrice(int numberOfStores) {
        List<String> stores = IntStream.rangeClosed(1, numberOfStores).mapToObj(i -> String.format("%04d", i)).collect(Collectors.toList());

        Optional<StoreSkuPriceDTO> response = getBestPrice.getBestPrice("750123456789", stores);

        assertTrue(response.isPresent());
        StoreSkuPriceDTO storeSkuPriceDTO = response.get();
        log.info("Minimum SkuPrice: {}", storeSkuPriceDTO);
        assertThat(storeSkuPriceDTO.getStoreId()).isNotEmpty();
        assertThat(storeSkuPriceDTO.getSku()).isEqualTo("750123456789");
        assertThat(storeSkuPriceDTO.getPrice()).isNotNull();
    }
}
