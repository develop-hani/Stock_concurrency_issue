package com.practice.stock.service;

import com.practice.stock.domain.Stock;
import com.practice.stock.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OptimisticLockStockService {

    private final StockRepository stockRepository;

    public OptimisticLockStockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Transactional
    // Optimistic lock은 실패했을 때 재시도를 해야하므로 facade를 만들어 그곳에서 service layer의 함수를 호출한다.
    public void decrease(Long id, Long quantity) {
        Stock stock = stockRepository.findByIdWithOptimisticLock(id);

        stock.decrease(quantity);
        stockRepository.save(stock);
    }
}
