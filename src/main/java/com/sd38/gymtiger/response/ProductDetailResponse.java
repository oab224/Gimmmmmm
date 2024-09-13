package com.sd38.gymtiger.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDetailResponse {

    private Integer productId;

    private String productName;

    private BigDecimal price;

    private Integer quantity;

    public ProductDetailResponse(int quantity, BigDecimal price) {
        this.quantity = quantity;
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
