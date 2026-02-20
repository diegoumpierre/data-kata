package com.demo.observability.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order domain model
 */
public class Order {
    private Long id;
    private Long userId;
    private String product;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createdAt;

    public Order() {}

    public Order(Long id, Long userId, String product, BigDecimal amount, String status, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.product = product;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", userId=" + userId +
                ", product='" + product + '\'' +
                ", amount=" + amount +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
