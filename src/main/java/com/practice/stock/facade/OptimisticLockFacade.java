package com.practice.stock.facade;

import com.practice.stock.service.OptimisticLockStockService;
import org.springframework.stereotype.Component;

@Component
public class OptimisticLockFacade {

    private final OptimisticLockStockService optimisticLockStockService;

    public OptimisticLockFacade(OptimisticLockStockService optimisticLockStockService) {
        this.optimisticLockStockService = optimisticLockStockService;
    }

    public void decrease(Long id, Long quantity) throws InterruptedException {
        while (true) {
            try {
                optimisticLockStockService.decrease(id, quantity);

                break;
            } catch (Exception e) {
                Thread.sleep(50);
            }
        }
    }
}
