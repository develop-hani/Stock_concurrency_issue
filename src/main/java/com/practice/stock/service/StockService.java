package com.practice.stock.service;

import com.practice.stock.domain.Stock;
import com.practice.stock.repository.StockRepository;
import org.springframework.stereotype.Service;

@Service
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    // 재고 감소
    public void decreaseStock(Long id, Long quantity) {
        // Stock 조회
        Stock stock = stockRepository.findById(id).orElseThrow();
        stock.decrease(quantity);
        stockRepository.saveAndFlush(stock);
    }
}
