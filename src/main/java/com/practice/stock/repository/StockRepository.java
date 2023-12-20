package com.practice.stock.repository;

import com.practice.stock.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, Long> {

}
