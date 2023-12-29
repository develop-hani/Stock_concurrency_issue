package com.practice.stock.domain;

import jakarta.persistence.*;

@Entity
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    private Long quantity;

    @Version
    // optimistic lock을 사용하기 위한 version
    // jakarta.persistence의 annotation 사용
    private Long version;

    public Stock() {
    }

    public Stock(Long productId, Long quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getQuantity() {
        return quantity;
    }

    // 재고 감소
    public void decrease(Long quantity) {
        if(this.quantity - quantity < 0) {
            throw new RuntimeException("재고가 부족합니다.");
        }
        this.quantity -= quantity;
    }
}
