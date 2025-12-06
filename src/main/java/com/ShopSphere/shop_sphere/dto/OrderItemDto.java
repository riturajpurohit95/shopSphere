package com.ShopSphere.shop_sphere.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class OrderItemDto {

    public OrderItemDto() {}

    public OrderItemDto(
            Integer orderItemsid,

            @NotNull(message = "Order Id is required")
            @Min(value = 1, message = "orderId must be >= 1")
            Integer orderId,

            @NotNull(message = "Product Id is required")
            @Min(value = 1, message = "productId must be >= 1")
            Integer productId,
            
            @NotNull(message = "Seller Id is required")
            @Min(value = 1, message = "sellerId must be >= 1")
            Integer sellerId,

            @NotBlank(message = "Product name is required")
            String productName,

            @NotNull(message = "Quantity is required")
            @Min(value = 1, message = "Quantity must be >= 1")
            Integer quantity,

            @NotNull(message = "UnitPrice is required")
            @DecimalMin(value = "0.0", message = "UnitPrice must be >= 0.0")
            @Digits(integer = 10, fraction = 2, message = "UnitPrice must have max 2 decimal places")
            BigDecimal unitPrice,

            BigDecimal totalItemPrice
    ) {
        this.orderItemsid = orderItemsid;
        this.orderId = orderId;
        this.productId = productId;
        this.sellerId = sellerId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalItemPrice = totalItemPrice;
    }

    private Integer orderItemsid;

    @NotNull(message = "Order Id is required")
    @Min(value = 1, message = "orderId must be >= 1")
    private Integer orderId;

    @NotNull(message = "Product Id is required")
    @Min(value = 1, message = "productId must be >= 1")
    private Integer productId;
    
    @NotNull(message = "Seller Id is required")
    @Min(value = 1, message = "sellerId must be >= 1")
    private Integer sellerId;

    @NotBlank(message = "Product name is required")
    private String productName;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be >= 1")
    private Integer quantity;

    @NotNull(message = "UnitPrice is required")
    @DecimalMin(value = "0.0", message = "UnitPrice must be >= 0.0")
    @Digits(integer = 10, fraction = 2, message = "UnitPrice must have max 2 decimal places")
    private BigDecimal unitPrice;

    // Optional - computed value
    private BigDecimal totalItemPrice;

    public Integer getOrderItemsid() {
        return orderItemsid;
    }

    public void setOrderItemsid(Integer orderItemsid) {
        this.orderItemsid = orderItemsid;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getProductId() {
        return productId;
    }
    
    public Integer getSellerId() {
        return sellerId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }
    
    public void setSellerId(Integer productId) {
        this.sellerId = sellerId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalItemPrice() {
        return totalItemPrice;
    }

    public void setTotalItemPrice(BigDecimal totalItemPrice) {
        this.totalItemPrice = totalItemPrice;
    }
}







