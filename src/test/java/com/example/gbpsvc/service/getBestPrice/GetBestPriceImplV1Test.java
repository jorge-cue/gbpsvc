package com.example.gbpsvc.service.getBestPrice;

import com.example.gbpsvc.adapter.dto.StoreSkuPriceDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

@Slf4j
@RunWith(Parameterized.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
public class GetBestPriceImplV1Test extends AbstractGetBestPriceImplTest {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    // Subject Under Test
    @Autowired
    @Qualifier("getBestPriceV1")
    private GetBestPrice getBestPrice;

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> data() {
        return Arrays.asList(
                new Object[] {"warm up!", 1},
                new Object[] {"Running with 1 store", 1},
                new Object[] {"Running with 5 stores", 5},
                new Object[] {"Running with 10 stores", 10},
                new Object[] {"Running with 50 stores", 50},
                new Object[] {"Running with 100 stores", 100}
//                new Object[] {"Running with 500 stores", 500}
        );
    }

    @Parameterized.Parameter(0)
    public String testName;

    @Parameterized.Parameter(1)
    public Integer numberOfStores;

    @Test
    public void runTestBestPrice() {
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
