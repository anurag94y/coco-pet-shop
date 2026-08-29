package org.example.backend.sale;

import org.example.backend.sale.dto.CreateSaleItemRequest;
import org.example.backend.sale.dto.CreateSaleRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SaleConcurrencyTest {

    @Autowired
    private SaleService saleService;

    @Test
    void concurrentSalesShouldNotOversellInventory()
            throws Exception {

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        CountDownLatch ready =
                new CountDownLatch(2);

        CountDownLatch start =
                new CountDownLatch(1);

        Callable<Boolean> task = () -> {

            ready.countDown();

            start.await();

            try {

                CreateSaleRequest request =
                        createSaleRequest();

                saleService.createSale(request);

                return true;

            } catch (IllegalArgumentException ex) {

                return false;
            }
        };

        Future<Boolean> first =
                executor.submit(task);

        Future<Boolean> second =
                executor.submit(task);

        ready.await();

        start.countDown();

        boolean firstResult = first.get();
        boolean secondResult = second.get();

        executor.shutdown();

        int successCount = 0;

        if (firstResult) {
            successCount++;
        }

        if (secondResult) {
            successCount++;
        }

        assertEquals(
                1,
                successCount
        );
    }

    private CreateSaleRequest createSaleRequest() {

        return new CreateSaleRequest(
                1L,
                1L,
                1L,
                UUID.randomUUID().toString(),
                List.of(
                        new CreateSaleItemRequest(
                                1L,
                                4
                        )
                ),
                "CASH",
                null
        );
    }
}