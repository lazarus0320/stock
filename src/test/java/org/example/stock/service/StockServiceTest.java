package org.example.stock.service;

import org.example.stock.domain.Stock;
import org.example.stock.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StockServiceTest {

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
        stockService.decrese(1L, 1L);
        Stock stock = stockRepository.findById(1L).orElseThrow();

        assertEquals(99L, stock.getQuantity());
    }

    @Test
    public void 동시에_100개의_요청() throws InterruptedException {
        // when
        // 100개의 쓰레드 사용(멀티스레드)
        int threadCount = 100;

        // ExecutorService: 비동기로 실행하는 작업을 간단하게 실행할 수 있도록 도와주는 클래스
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        // CountDownLatch: 작업을 진행중인 다른 스레드가 작업을 완료할 때까지 대기할 수 있도록 도와주는 클래스
        CountDownLatch latch = new CountDownLatch(threadCount);
        // 100개의 작업 요청
        for (int i = 0; i< threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decrese(1L, 1L);
                } finally {
                    // CountDownLatch 1 감소
                    latch.countDown();
                }
            });
        }
        // CountDownLatch이 0이 될때까지 스레드 대기
        latch.await();

        // then
        Stock stock = stockRepository.findById(1L).orElseThrow();
        assertEquals(0L, stock.getQuantity());
    }
}