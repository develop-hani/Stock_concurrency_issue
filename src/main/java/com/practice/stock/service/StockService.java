package com.practice.stock.service;

import com.practice.stock.domain.Stock;
import com.practice.stock.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    // 재고 감소
    @Transactional(propagation = Propagation.REQUIRES_NEW) // 부모(NamedLockStockFacade)의 transaction과 별도로 실행되어야 하므로 propagation 변경
    public void decreaseStock(Long id, Long quantity) {
        // Stock 조회
        Stock stock = stockRepository.findById(id).orElseThrow();
        stock.decrease(quantity);
        stockRepository.saveAndFlush(stock);
    }
}
