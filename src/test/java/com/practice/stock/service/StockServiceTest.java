package com.practice.stock.service;

import com.practice.stock.domain.Stock;
import com.practice.stock.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class StockServiceTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    public void before() {
        stockRepository.saveAndFlush(new Stock(1L, 100L));
    }

    @AfterEach
    public void after() {
        stockRepository.deleteAll();
    }

    @Test
    public void 재고감소() {
        stockService.decreaseStock(1L, 1L);

        Stock stock = stockRepository.findById(1L).orElseThrow();

        assertEquals(99L, stock.getQuantity()); // 100개 - 1개 = 99개
    }

    /*
        Thread를 이용하여 동시에 100개의 요청을 보낸다.
        ExecutorService는 비동기로 실행하는 작업을 단순화하여 사용할 수 있도록 도와주는 Java API이다.
        CountDownLatch는 다른 스레드가 수행하는 작업이 끝날 때까지 기다릴 수 있는 기능을 제공한다.

        이 경우  test에 실패한다. (expected: <0> but was: <94>)
        Race condition이 발생했기 때문이다.
        Race conditiond은 둘 이상의 Thread에 공유 자원 동시에 접근할 때 발생하는 문제이다.

        기대했던 순서는 Thread1 재고 확인 -> Thread1 재고 감소 -> Thread2 재고 확인 -> Thread2 재고 감소 -> ...
        실제 실행 순서는 Thread1 재고 확인 -> Thread2 재고 확인 -> Thread1 재고 감소 -> Thread2 재고 감소 -> ...
        이를 해결하기 위해 하나의 스레드 작업이 완료된 이후에 다른 스레드 작업을 하도록한다.
    */
    @Test
    public void 동시에_100개_요청() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; ++i) {
            executorService.submit( () -> {
                try {
                    stockService.decreaseStock(1L, 1L);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        Stock stock = stockRepository.findById(1L).orElseThrow();

        assertEquals(0L, stock.getQuantity());
    }

}
